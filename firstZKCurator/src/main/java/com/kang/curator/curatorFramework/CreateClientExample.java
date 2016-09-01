package com.kang.curator.curatorFramework;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * @Title
 * @Description
 * @Date 2016/8/31 16:14
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class CreateClientExample {

    private static final String PATH = "/example/basic";

    public static void main(String[] args) {
        TestingServer server = null;
        CuratorFramework client = null;
        CuratorFramework optionsClient = null;

        try {
            server  = new TestingServer();
            client = createSimple(server.getConnectString());
            client.start();
            client.create().creatingParentsIfNeeded().forPath(PATH,"test".getBytes());

            optionsClient = createWithOptions(server.getConnectString(), new ExponentialBackoffRetry(1000, 3), 1000, 1000);
            optionsClient.start();
            log.info("[{}]",new String(optionsClient.getData().forPath(PATH)));
        } catch (Exception e) {
          log.info("exception throws cause {}", Throwables.getStackTraceAsString(e));
        }finally {
            if(client != null){
                CloseableUtils.closeQuietly(client);
            }
            if(optionsClient != null){
                CloseableUtils.closeQuietly(optionsClient);
            }
        }


    }

    public static CuratorFramework createSimple(String connectString){
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000,3);
        return CuratorFrameworkFactory.newClient(connectString,retryPolicy);
    }

    public static CuratorFramework createWithOptions(String connectionString,RetryPolicy retryPolicy,
                                                     int connectionTimeoutMs,int sessionTimeoutMs){
        return  CuratorFrameworkFactory.builder().connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }



    public static void createPath(CuratorFramework client,String path,byte[] payload) throws Exception {
        client.create().forPath(path,payload);
    }

    public static void createaEphemeral(CuratorFramework client,String path,byte[] payload) throws Exception {
        //client.create().withProtection().withMode(CreateMode.EPHEMERAL,)
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path,payload);
    }

    public static String createEphemeralSequential(CuratorFramework client,String path,byte[] payload) throws Exception {
        return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path,payload);
    }

    public static void setData(CuratorFramework client,String path,byte[] payload) throws Exception {
        client.setData().forPath(path,payload);
    }

    public static void setDataAsync(CuratorFramework client,String path,byte[] payload) throws Exception {
        CuratorListener listener = new CuratorListener(){
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                // examine event for details
            }
        };
        client.getCuratorListenable().addListener(listener);
        client.setData().inBackground().forPath(path,payload);
    }

    public static void setDataAsyncWithCallback(CuratorFramework client, BackgroundCallback callback, String path, byte[] payload) throws Exception {
        // this is another method of getting notification of an async completion
        client.setData().inBackground(callback).forPath(path, payload);
    }

    public static void detele(CuratorFramework client,String path) throws Exception {
        client.delete().forPath(path);
    }

    public static void guaranteedDelete(CuratorFramework client, String path) throws Exception {
        // delete the given node and guarantee(保证) that it completes
        client.delete().guaranteed().forPath(path);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path) throws Exception {
        /**
         * Get children and set a watcher on the node. The watcher notification
         * will come through the CuratorListener (see setDataAsync() above).
         */
        return client.getChildren().watched().forPath(path);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher) throws Exception {
        /**
         * Get children and set the given watcher on the node.
         */
        return client.getChildren().usingWatcher(watcher).forPath(path);
    }
}
