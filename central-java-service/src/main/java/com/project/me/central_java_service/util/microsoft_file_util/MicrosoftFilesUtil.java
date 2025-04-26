package com.project.me.central_java_service.util.microsoft_file_util;

public class MicrosoftFilesUtil {
    public static boolean isDocOrDocx(String filename) {
        return filename != null && (filename.endsWith(".doc") || filename.endsWith(".docx"));
    }
}
