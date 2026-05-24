package com.example.cashflowkujava.services;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalAuthService implements AuthService {

    private static final String PREF_NAME = "CashFlowKuAuth";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PIC = "user_pic";

    private final SharedPreferences prefs;

    public LocalAuthService(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    @Override
    public void login(String name, String email, String profilePicUrl) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PIC, profilePicUrl)
                .apply();
    }

    @Override
    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PIC)
                .apply();
    }

    @Override
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Tamu");
    }

    @Override
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "guest@cashflowku.com");
    }

    @Override
    public String getUserProfilePic() {
        return prefs.getString(KEY_USER_PIC, "");
    }
}
