package com.kmaebashi.nctfwimpl;

import com.kmaebashi.nctfw.InvokerOption;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    public static String exceptionToString(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
