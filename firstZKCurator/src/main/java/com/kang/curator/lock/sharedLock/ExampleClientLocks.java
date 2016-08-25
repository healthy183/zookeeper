package com.kang.curator.lock.sharedLock;

import com.kang.curator.lock.FakeLimitedResource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

import java.util.concurrent.TimeUnit;

/**
 * Created by Healthy on 2015/10/6.
 */
public class ExampleClientLocks {


    private final InterProcessSemaphoreMutex lock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public ExampleClientLocks
            (CuratorFramework client, String lockPath,
             FakeLimitedResource resource,String clientName){

        this.lock = new InterProcessSemaphoreMutex(client,lockPath);
        this.resource = resource;
        this.clientName = clientName;
    }

    public void doWork(long time,TimeUnit timeUnit) throws Exception {

        if(!lock.acquire(time,timeUnit)){
            throw new IllegalStateException(clientName+" acquire lock fail!");
        }

        System.out.println(clientName+" had the lock!");

        if(!lock.acquire(time,timeUnit)){
            throw new IllegalStateException(clientName+" acquire lock fail!");
        }

        System.out.println(clientName+" had the lock again!");

        try{
            resource.use();//access resource exclusively
        }finally{
            System.out.println(clientName+" releasing the rock!");
            lock.release();
            lock.release();
        }

    }








}
