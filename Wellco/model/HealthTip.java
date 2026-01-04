package model;

/**
 * HealthTip data class
 */
public class HealthTip {
    public int id;
    public String title;
    public String content;
    public String linkSource; // MANDATORY: Source link for health tip

    public HealthTip(int id, String title, String content, String linkSource) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.linkSource = linkSource;
    }

    public HealthTip(String title, String content, String linkSource) {
        this(0, title, content, linkSource);
    }
}
