package com.example.newsapp.model;

public class NewsModel {
    public String author;
    public String title;
    public String description;
    public String url;
    public String urltoImage;
    public String publishAt;
    public String content;

    public NewsModel(String author, String title, String description, String url, String urltoImage, String publishAt,
                     String content){
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urltoImage = urltoImage;
        this.publishAt = publishAt;
        this.content = content;
    }
}
