package com.justin.tool.util;


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
            }
        };
        timer.schedule(task,0, TimeUnit.SECONDS.toMillis(5));

//        Thread.sleep(1000000);
        System.out.println("test----");
    }
}
