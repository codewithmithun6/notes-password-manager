package com.geeksforgeeks.notespasswordmanager.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteForm {
    private Long id;
    @NotBlank @Size(max = 160)
    private String title;
    @NotBlank @Size(max = 100000)
    private String content;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
