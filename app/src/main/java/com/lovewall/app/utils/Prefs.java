package com.lovewall.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "lovewall";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sp;

    public Prefs(Context ctx) {
        sp = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(String token, String userId, String username, String nickname, String avatar, String role) {
        sp.edit().putString(KEY_TOKEN, token).putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username).putString(KEY_NICKNAME, nickname)
            .putString(KEY_AVATAR, avatar).putString(KEY_ROLE, role).apply();
    }

    public void clear() { sp.edit().clear().apply(); }
    public boolean isLoggedIn() { return sp.getString(KEY_TOKEN, null) != null; }
    public String getToken() { return sp.getString(KEY_TOKEN, ""); }
    public String getUserId() { return sp.getString(KEY_USER_ID, ""); }
    public String getUsername() { return sp.getString(KEY_USERNAME, ""); }
    public String getNickname() { return sp.getString(KEY_NICKNAME, ""); }
    public String getAvatar() { return sp.getString(KEY_AVATAR, ""); }
    public String getRole() { return sp.getString(KEY_ROLE, "user"); }
    public void setNickname(String v) { sp.edit().putString(KEY_NICKNAME, v).apply(); }
    public void setAvatar(String v) { sp.edit().putString(KEY_AVATAR, v).apply(); }
}
