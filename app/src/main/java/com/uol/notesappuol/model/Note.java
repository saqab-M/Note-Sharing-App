package com.uol.notesappuol.model;

public class Note {

    //note variables
    private String title;
    private String content;
    private String createdBy;
    private boolean published;
    private Integer color;

    // constructors
    public Note(){}

    public Note(String title, String content, String createdBy, boolean published, Integer color){
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.published = published;
        this.color = color;
    }


    //getters and setters
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean getpublished(){
        return published;
    }

    public  void setPublished(boolean published){
        this.published = published;
    }

    public Integer getColor(){return color;}

    public void setColor(Integer color){ this.color = color;}
}
