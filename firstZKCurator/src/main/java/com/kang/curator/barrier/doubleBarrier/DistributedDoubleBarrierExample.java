package com.kang.curator.barrier.doubleBarrier;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Title
 * @Description
 * @Date 2016/8/29 21:13
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class DistributedDoubleBarrierExample {

    private static final int  QTY = 5;
    private static final String PATH = "/examples/barrier";

    public static void main(String[] args) {
        TestingServer server = null;
        CuratorFramework client = null;
        try{
            server  = new TestingServer();
            client = CuratorFrameworkFactory.newClient
                    (server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();
            ExecutorService service = Executors.newFixedThreadPool(QTY);
            for(int i =0;i<QTY;i++){
                final DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, PATH, QTY);
                final int index = i;
                Callable<Void> task = new Callable<Void>(){
                    @Override
                    public Void call() throws Exception {
                        Thread.sleep((long)(3*Math.random()));
                        log.info("Client#{} enters!",index);
                        barrier.enter();//warit for all barriers enter,thread will run;
                        log.info("Client#{} begins!",index);
                        Thread.sleep((long) (3000 * Math.random()));
                        barrier.leave();//warit for all barriers left,thread will end;
                        log.info("Client#{} left!",index);
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        }catch(Exception ex){
            log.info("throw Exception case{},", Throwables.getStackTraceAsString(ex));
        }finally {
            if(client != null){
                try{
                    CloseableUtils.closeQuietly(client);
                }catch(IllegalStateException ex){
                    log.info("illegalStateException throw  when close CuratorFramework,cause:{}", Throwables.getStackTraceAsString(ex));
                }catch (Exception ex){
                    log.info("exception throw  when close exampleClient,cause:{}", Throwables.getStackTraceAsString(ex));
                }
            }
            if(server != null){
                try{
                    CloseableUtils.closeQuietly(server);
                }catch(IllegalStateException ex){
                    log.info("illegalStateException throw  when close TestingServer,cause:{}", Throwables.getStackTraceAsString(ex));
                }catch (Exception ex){
                    log.info("exception throw  when close exampleClient,cause:{}", Throwables.getStackTraceAsString(ex));
                }
            }
        }
    }

}
