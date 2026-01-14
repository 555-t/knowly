package com.example.knowly;
import java.util.List;

public class Post {
    private String content;
    private String author;
    private List<String> categories;

    public Post() {} // Required for Firebase

    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public List<String> getCategories() { return categories; }
}