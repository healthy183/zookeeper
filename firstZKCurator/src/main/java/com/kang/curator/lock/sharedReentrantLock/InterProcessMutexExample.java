package com.kang.curator.lock.sharedReentrantLock;

import com.google.common.base.Throwables;
import com.kang.curator.lock.FakeLimitedResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.*;

/**
 * Created by Healthy on 2015/10/6.
 */
@Slf4j
public class InterProcessMutexExample {

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
                            final ExampleClientThatLocks example = new
                                    ExampleClientThatLocks(client,PATH,resource,"client#"+index);
                            for(int j = 0;j<REPETITIONS;j++){
                                example.doWork(10, TimeUnit.SECONDS);
                            }
                        }catch(Throwable e){
                            log.info("unknow exception,cause{}", Throwables.getStackTraceAsString(e));
                        }finally {
                            try{
                                CloseableUtils.closeQuietly(client);
                            }catch(Exception ex){
                                log.info("unknow exception,cause{}", Throwables.getStackTraceAsString(ex));
                            }
                        }
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10,TimeUnit.MINUTES);
        }catch(Exception ex){
            log.info("unknow exception,cause{}", Throwables.getStackTraceAsString(ex));
        }finally {
            try{
            CloseableUtils.closeQuietly(server);
            }catch(Exception ex){
                log.info("unknow exception,cause{}", Throwables.getStackTraceAsString(ex));
            }
        }
    }
}