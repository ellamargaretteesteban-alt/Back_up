package model;

/**
 * Pill (Medicine) data class
 */
public class Pill {
    public String name;
    public String description;
    public String recommendation;

    public Pill(String name, String description, String recommendation) {
        this.name = name;
        this.description = description;
        this.recommendation = recommendation;
    }
}
