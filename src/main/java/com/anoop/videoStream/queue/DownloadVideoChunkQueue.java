package com.anoop.videoStream.queue;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.DownloadVideoTask;

@Component
public class DownloadVideoChunkQueue {
    private BlockingQueue<DownloadVideoTask> tasks = new ArrayBlockingQueue<>(20);

    private final Set<String> queuedTasks = ConcurrentHashMap.newKeySet();

    public String setTaskToQueue(DownloadVideoTask downloadVideoTask) throws InterruptedException {
        tasks.put(downloadVideoTask);
        return "video chunk download  task added";

    }

    private String getTaskKey(DownloadVideoTask task) {
        return task.getVideoId() + ":" + task.getIndex();
    }

    public boolean addTask(DownloadVideoTask task) {

        String key = getTaskKey(task);

        // Already queued/running
        if (!queuedTasks.add(key)) {
            System.out.println("Duplicate task ignored: " + key);
            return false;
        }

        try {

            tasks.put(task);

            System.out.println("Task added: " + key);

            return true;

        } catch (InterruptedException e) {

            // Important: remove key if task couldn't be queued
            queuedTasks.remove(key);

            Thread.currentThread().interrupt();

            return false;
        }
    }

    public void removeSetQueue(DownloadVideoTask task){
        String key = task.getVideoId() + ":" + task.getIndex();
        queuedTasks.remove(key);
    }

    public DownloadVideoTask getTaskToQueue() throws InterruptedException {
        return  tasks.take();
    }
}
