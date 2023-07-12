package com.example.songsurf.config;

public class UrlProvider {
    private static final String NGROK_CODE = "38b1-79-112-35-48";
    public static String NGROK_URL = "https://" + NGROK_CODE + ".ngrok-free.app";
    public static String LOGIN_URL = NGROK_URL + "/login";
    public static String REGISTER_URL = NGROK_URL + "/register";
    public static String SONG_URL = NGROK_URL + "/song";
    public static String FILTER_URL = NGROK_URL + "/filter";
    public static String PLAYLIST_URL = NGROK_URL + "/playlist";
    public static String PLAYLISTS_URL = NGROK_URL + "/playlists";
    public static String GET_USERNAME_URL = NGROK_URL + "/get_username";
    public static String CHANGE_USERNAME_URL = NGROK_URL + "/change_username";
    public static String CHANGE_EMAIL_URL = NGROK_URL + "/change_email";
    public static String CHANGE_PASSWORD_URL = NGROK_URL + "/change_password";
    public static String LOGOUT_URL = NGROK_URL + "/logout";
    public static String DELETE_ACCOUNT_URL = NGROK_URL + "/delete_account";

}
