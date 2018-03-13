package system.bookmarks;

/**
 * This class represents a Bookmark entity.
 */
public class Bookmark {
    public String title;
    public String url;

    @Override
    public String toString() {
        return "Bookmark{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
