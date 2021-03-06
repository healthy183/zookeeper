package com.kang.curator.lock.sharedLock;

import com.google.common.base.Throwables;
import com.kang.curator.lock.FakeLimitedResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Healthy on 2015/10/6.
 */
@Slf4j
public class InterProcessSemaphoreMutexExample {

    private static final int QTY = 5;
    private static final int REPETITIONS = QTY * 10;
    private static final String PATH ="/examples/locks";

    public static void main(String[] args) throws Exception {
        final FakeLimitedResource resource = new FakeLimitedResource();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        final TestingServer server = new TestingServer();
    try{
        for(int i=0;i<QTY;i++){
            final int index = i;
            Callable<Void> task = new Callable<Void>(){
                @Override
                public Void call() throws Exception {
                    ExponentialBackoffRetry retry =  new ExponentialBackoffRetry(1000, 3);
                    CuratorFramework client =  CuratorFrameworkFactory
                             .newClient(server.getConnectString(), retry);
                    try {
                        client.start();
                        final ExampleClientLocks example = new
                                ExampleClientLocks(client,PATH,resource,"client#"+index);
                        for(int j = 0;j<REPETITIONS;j++){
                            example.doWork(10, TimeUnit.SECONDS);
                        }
                    }catch(Throwable e){
                        log.info("throw exception,cause {}", Throwables.getStackTraceAsString(e));
                    }finally {
                        CloseableUtils.closeQuietly(client);
                    }
                    return null;
                }
            };
            service.submit(task);
        }
            service.shutdown();
            service.awaitTermination(10,TimeUnit.MINUTES);
        }finally {
            CloseableUtils.closeQuietly(server);
        }
    }

}