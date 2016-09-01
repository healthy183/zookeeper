package com.kang.curator.barrier.simpleBarrier;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Title
 * @Description
 * @Date 2016/8/29 20:09
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class DistributedBarrierExample {

    private static final int QTY = 5;
    private static final String PATH = "/examples/barrier";

    public static void main(String[] args) {

        try(TestingServer server = new TestingServer()){
            CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString()
                    ,new ExponentialBackoffRetry(1000, 3));
            client.start();
            ExecutorService service = Executors.newFixedThreadPool(QTY);
            DistributedBarrier controlBarrier = new DistributedBarrier(client,PATH);
            controlBarrier.setBarrier();//set Barrier
            for(int i = 0;i<QTY;i++){
                final DistributedBarrier barrier = new DistributedBarrier(client,PATH);
                final int index =  i;
                Callable<Void> task = new Callable<Void>(){
                    @Override
                    public Void call() throws Exception {
                        Thread.sleep((long)(3*Math.random()));
                        log.info("client#{} waits on Barrier",index);
                        barrier.waitOnBarrier();//all  thread blockde until removeBarrier()
                        log.info("client#{} begins",index);
                        return null;
                    }
                };
                service.submit(task);
            }
            Thread.sleep(10000);
            controlBarrier.removeBarrier();//remove,Barrier,and all thread will run
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        } catch (IOException e) {
            log.info("throw IOException,cases{}", Throwables.getStackTraceAsString(e));
        } catch (Exception e) {
            log.info("throw Exception,cases{}", Throwables.getStackTraceAsString(e));
        }


    }
}
