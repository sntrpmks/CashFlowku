package com.example.cashflowkujava.services;

public interface AuthService {
    boolean isLoggedIn();
    void login(String name, String email, String profilePicUrl);
    void logout();
    String getUserName();
    String getUserEmail();
    String getUserProfilePic();
}
