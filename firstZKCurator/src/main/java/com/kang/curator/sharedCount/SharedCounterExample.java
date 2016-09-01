package com.kang.curator.sharedCount;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Title
 * @Description
 * @Date 2016/8/30 10:49
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class SharedCounterExample implements SharedCountListener {

    private static final int QTY = 5;
    private static final String PATH ="/examples/counter";


    public static void main(String[] args) {
        final Random rand = new Random();
        SharedCounterExample example = new SharedCounterExample();
        TestingServer server = null;
        CuratorFramework client = null;
        try {
             server = new TestingServer();
             client = CuratorFrameworkFactory.newClient
                    (server.getConnectString(),new ExponentialBackoffRetry(100,3));
            client.start();
            SharedCount baseCount = new SharedCount(client,PATH,0);
            baseCount.addListener(example);
            baseCount.start();

            List<SharedCount> examples = Lists.newArrayList();
            ExecutorService service = Executors.newFixedThreadPool(QTY);
            for(int i = 0;i<QTY;i++){
                final SharedCount count = new SharedCount(client,PATH,0);
                examples.add(count);
                Callable<Void> task = new Callable<Void>(){
                    @Override
                    public Void call() throws Exception {
                        count.start();
                        Thread.sleep(rand.nextInt(10000));
                                                      //setCount()  <==update coercively
                        log.info("Increment: {}",count.trySetCount(count.getVersionedValue(),count.getCount() + rand.nextInt(10)));
                        return null;
                    }
                };
                service.submit(task);
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
            for(int i = 0;i<QTY;i++){
                examples.get(i).close();
            }
            baseCount.close();
        } catch (Exception e) {
            log.info("exception cause,{}", Throwables.getStackTraceAsString(e));
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

    @Override
    public void countHasChanged(SharedCountReader sharedCount, int newCount) throws Exception {
        log.info("Counter's value is changed to " + newCount);
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
       log.info("State changed: " + newState.toString());
    }
}
