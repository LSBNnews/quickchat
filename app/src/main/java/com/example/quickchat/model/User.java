package com.example.quickchat.model;

public class User {
    private String username, email, password, imageURL;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public User(String username, String email, String password, String imageURL) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.imageURL = imageURL;
    }
}
