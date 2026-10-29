package uz.mini.twitter.servlet;

final class PostInput {
    static final String ERROR = "Sarlavha va matnni to'ldiring (har biri 255 belgigacha).";

    private PostInput() { }

    static String clean(String value) {
        return value == null ? "" : value.strip();
    }

    static boolean valid(String title, String description) {
        return !title.isBlank() && title.length() <= 255
                && !description.isBlank() && description.length() <= 255;
    }

    static Long id(String value) {
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
