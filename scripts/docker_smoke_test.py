"""Exercise an isolated local Docker Compose deployment with disposable test accounts.

Run after `docker compose -p mini-twitter-security-check up --build -d`.
Only Python standard-library modules are required.
"""
import http.cookiejar
import os
import re
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

BASE = os.environ.get("TEST_BASE_URL", "http://127.0.0.1:8080")
PROJECT = os.environ.get("TEST_COMPOSE_PROJECT", "mini-twitter-security-check")
assert urllib.parse.urlsplit(BASE).hostname in {"127.0.0.1", "localhost"}, "Local test only"


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args):
        return None


def browser():
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()), NoRedirect())


def request(client, path, form=None, token=None, method=None):
    data = None if form is None else urllib.parse.urlencode(form).encode()
    headers = {"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"}
    if token is not None:
        headers["X-CSRF-Token"] = token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        response = client.open(req, timeout=30)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        return response.code, response.read().decode("utf-8")


def csrf(client):
    status, html = request(client, "/login.jsp")
    assert status == 200, status
    return re.search(r'name="_csrf" value="([^"]+)"', html).group(1)


def post(client, path, form):
    return request(client, path, {**form, "_csrf": csrf(client)})


def sql(query):
    return subprocess.check_output(["docker", "compose", "-p", PROJECT, "exec", "-T", "db",
                                    "psql", "-U", "postgres", "-d", "mini_twitter", "-tAc", query], text=True).strip()


def main():
    client = browser()
    for attempt in range(60):
        try:
            if request(client, "/login")[0] == 200:
                break
        except (OSError, urllib.error.URLError):
            pass
        time.sleep(1)
    else:
        raise AssertionError("Application did not become ready")
    username = "smoke_" + uuid.uuid4().hex[:12]
    password = uuid.uuid4().hex
    assert request(client, "/register", {"username": username, "password": password})[0] == 403
    assert post(client, "/register", {"fullName": "Docker Test", "username": username, "password": password})[0] == 302
    assert sql(f"SELECT password LIKE 'pbkdf2-sha256$600000$%' FROM users WHERE username='{username}'") == "t"
    old_token = csrf(client)
    assert post(client, "/login", {"username": username, "password": "wrong"})[0] == 200
    assert post(client, "/login", {"username": username, "password": password})[0] == 302
    assert old_token != csrf(client)
    assert request(client, "/add-post", {"title": "Blocked", "description": "Blocked"})[0] == 403
    assert request(client, "/add-post", {"title": "Blocked", "description": "Blocked"}, "x" * 43)[0] == 403
    assert post(client, "/add-post", {"title": "Docker <script>", "description": "O‘zbekcha PostgreSQL matni"})[0] == 302
    post_id = sql(f"SELECT p.id FROM posts p JOIN users u ON p.user_id=u.id WHERE u.username='{username}'")
    assert post_id.isdigit()
    status, body = request(client, "/home")
    assert status == 200 and "Docker &lt;script&gt;" in body and "O‘zbekcha PostgreSQL matni" in body
    assert post(client, "/edit-post?id=" + post_id, {"title": "Docker updated", "description": "Updated"})[0] == 302
    assert request(client, "/delete-post?id=" + post_id)[0] == 405
    assert request(client, "/logout")[0] == 405
    assert request(client, "/delete-post?id=" + post_id, method="HEAD")[0] == 405
    foreign = csrf(browser())
    assert request(client, "/delete-post?id=" + post_id, {}, foreign)[0] == 403
    assert sql(f"SELECT count(*) FROM posts WHERE id={post_id}") == "1"
    subprocess.run(["docker", "compose", "-p", PROJECT, "restart", "app"], check=True, stdout=subprocess.DEVNULL)
    # A fresh session after restarting confirms PostgreSQL data and password hashes survive restart.
    client = browser()
    for attempt in range(60):
        try:
            if request(client, "/login")[0] == 200:
                break
        except (OSError, urllib.error.URLError):
            pass
        time.sleep(1)
    else:
        raise AssertionError("Application did not restart")
    assert post(client, "/login", {"username": username, "password": password})[0] == 302
    assert request(client, "/my-posts")[0] == 200
    assert sql(f"SELECT title FROM posts WHERE id={post_id}") == "Docker updated"
    assert post(client, "/delete-post?id=" + post_id, {})[0] == 302
    assert sql(f"SELECT count(*) FROM posts WHERE id={post_id}") == "0"
    assert post(client, "/logout", {})[0] == 302
    assert request(client, "/my-posts")[0] == 302
    print("PASS: Docker/PostgreSQL registration, hashed credentials, login, CSRF rejection, UTF-8, create/edit/delete, restart persistence and logout")


if __name__ == "__main__":
    main()
