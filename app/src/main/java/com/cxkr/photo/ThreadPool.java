package com.cxkr.photo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPool {

    private ExecutorService executorService;
    private static ThreadPool instance;

    private ThreadPool() {
        executorService = Executors.newFixedThreadPool(4);
    }

    public static ThreadPool getInstance() {
        if (instance == null) {
            synchronized (ThreadPool.class) {
                instance = new ThreadPool();
            }
        }
        return instance;
    }

    public void addThreadRunable(Runnable runnable) {
        executorService.execute(runnable);
    }

}
