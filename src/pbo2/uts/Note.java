/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pbo2.uts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


/**
 *
 * @author 07rrk
 */
public class Note {
    private String id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Note(String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = this.createdAt;
    }

    public Note(String id, String title, String content, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    // Getter dan setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
        touch();
    }
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content; 
        touch();
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }

    private void touch() {
        this.modifiedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return title + " (" + createdAt.format(fmt) + ")";
    }
}