package com.library.model;

/** Abstract catalogue item. Books today; magazines, journals etc. could extend this later. */
public abstract class LibraryItem {
    private int id;
    private String title;
    private String category;

    protected LibraryItem(int id, String title, String category) {
        this.id = id;
        this.title = title;
        this.category = category;
    }

    /** One-line description used when listing items. */
    public abstract String describe();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
