package com.project.me.authjavaservice.util;

public class AuthUtil {
    public static String hideEmail(String userEmail) {
        return userEmail.replaceAll("(?!^|.)[^@](?=[^@]*@)", "*");
    }
}
