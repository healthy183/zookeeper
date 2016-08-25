package com.kang.curator.lock.sharedReentrantReadWriteLock;

import ch.qos.logback.core.util.TimeUtil;
import com.kang.curator.lock.FakeLimitedResource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

import java.util.concurrent.TimeUnit;

/**
 * Created by Healthy on 2015/10/6.
 */
public class ExampleClientReadWriteLocks {

    private final InterProcessReadWriteLock lock;
    private final InterProcessMutex readLock;
    private final InterProcessMutex wirterLock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public ExampleClientReadWriteLocks
            (CuratorFramework client,
             String lockPath,
                    FakeLimitedResource resource,
                      String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        this.lock = new InterProcessReadWriteLock(client,lockPath);
        this.readLock = lock.readLock();
        this.wirterLock = lock.writeLock();
    }

    public void doWork(long time,TimeUnit unit) throws Exception {

        if (!wirterLock.acquire(time,unit)) {
            throw new IllegalStateException(clientName+" could not acquire wirter lock!");
        }
        System.out.println(clientName+" has writer lock!");

        if(!readLock.acquire(time,unit)){
            throw new IllegalStateException(clientName+" could not acquire read lock!");
        }
        System.out.println(clientName + " has the readLock too");

        try{
            resource.use();
        }finally {
            System.out.println(clientName + "release the lock!");
            readLock.release();
            wirterLock.release();
        }


    }



}
