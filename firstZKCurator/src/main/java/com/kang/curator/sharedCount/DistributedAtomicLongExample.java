package com.kang.curator.sharedCount;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Title
 * @Description
 * @Date 2016/8/30 15:33
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class DistributedAtomicLongExample {

    private static final int QTY = 5;
    private static final String PATH = "/examples/counter";

    public static void main(String[] args) {
        TestingServer server = null;
        CuratorFramework client = null;
        try {
            server = new TestingServer();
            client = CuratorFrameworkFactory.newClient
                    (server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();

            List<DistributedAtomicLong> examples = Lists.newArrayList();
            ExecutorService service = Executors.newFixedThreadPool(QTY);
            for(int i = 0;i<QTY;i++){
                final DistributedAtomicLong count = new DistributedAtomicLong
                        (client, PATH, new RetryNTimes(10, 10));
                examples.add(count);
                Callable<Void> task = new Callable<Void>(){
                    @Override
                    public Void call() throws Exception {
                        AtomicValue<Long> value =  count.increment();
                        log.info("succeed {}",value.succeeded());
                        if(value.succeeded()){
                            log.info("Increment: from [{}] to [{}] ",value.preValue(),value.postValue());
                        }
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.info("exception cause {}", Throwables.getStackTraceAsString(e));
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
