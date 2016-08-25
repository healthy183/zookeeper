package com.kang.curator.lock.multiSharedLock;

import com.kang.curator.lock.FakeLimitedResource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Healthy on 2015/10/7.
 */
public class InterProcessMultiLockExample {

    private static final String PATH1 = "/examples/locks1";
    private static final String PATH2 = "/examples/locks2";


    public static void main(String[] args) {

        FakeLimitedResource resource = new FakeLimitedResource();

        try{
            TestingServer server = new TestingServer();
            ExponentialBackoffRetry retry = new ExponentialBackoffRetry(1000, 3);
            CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(),retry);
            client.start();

            InterProcessLock lock1 = new InterProcessMutex(client,PATH1);
            InterProcessLock lock2 = new InterProcessSemaphoreMutex(client,PATH2);

            InterProcessMultiLock lock =
                    new InterProcessMultiLock(Arrays.asList(lock1,lock2));

            if(!lock.acquire(10, TimeUnit.SECONDS)){

                throw new IllegalStateException("can not acquire the lock");
            }

            System.out.println("acquire the lock!");

            System.out.println("has the lock1 :"+ lock1.isAcquiredInThisProcess());
            System.out.println("has the lock2 :"+ lock2.isAcquiredInThisProcess());

            try {
                resource.use();
            }finally {
                System.out.println("releasing the lock");
                lock.release(); // always release the lock in a finally block
            }
            System.out.println("has the lock1: " + lock1.isAcquiredInThisProcess());
            System.out.println("has the lock2: " + lock2.isAcquiredInThisProcess());

        } catch (Exception e) {
            e.printStackTrace();
        } finally{



        }


    }

}
