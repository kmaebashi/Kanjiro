package com.kmaebashi.nctfwimpl;

import com.kmaebashi.nctfw.InvokerOption;

public class Util {
    Util() {}

    static boolean containsOption(InvokerOption[] options, InvokerOption target) {
        for (InvokerOption opt : options) {
            if (opt == target) {
                return true;
            }
        }
        return false;
    }

    public static String getSuffix(String fileName) {
        int pointIndex = fileName.lastIndexOf(".");
        if (pointIndex != -1) {
            return fileName.substring(pointIndex + 1);
        } else {
            return null;
        }
    }
}
