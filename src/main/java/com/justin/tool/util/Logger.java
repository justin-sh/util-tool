package com.justin.tool.util;

import java.util.Date;


public final class Logger {

    public static void info(Class clz, Object msg) {
        String tmp;
        if (msg instanceof String) {
            tmp = (String) msg;
        } else {
            tmp = String.valueOf(msg);
        }
        System.out.println(String.format("%s %s %s", DateUitl.formatTime(new Date()), clz.getSimpleName(), tmp));
    }
}
