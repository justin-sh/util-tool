package com.justin.tool.util;


import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AutoLogin {
    public static void main(String[] args) throws InterruptedException {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date());

                try {
                    Http.get("http://www.163.com", Collections.<String, String>emptyMap());
//                    System.out.println(Http.get("http://www.163.com", "GB2312"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("--------------------------------");
            }
        };
        timer.schedule(task, 0, TimeUnit.SECONDS.toMillis(1));

//        Thread.sleep(1000000);
        System.out.println("test----");
    }
}
