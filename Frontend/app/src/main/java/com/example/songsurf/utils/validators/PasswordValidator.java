package com.example.songsurf.utils.validators;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=.*[a-zA-Z])" +
                    "(?=.*[!@#$%^&*()_+{}|:<>?;',./])" +
                    "(?=\\S+$)" +
                    ".{8,}" +
                    "$");

    public static boolean validate(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

}
