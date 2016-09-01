package com.kang.curator.stateListener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;

/**
 * @Title
 * @Description
 * @Date 2016/8/31 18:25
 * @Author Healthy
 * @Version 2.0
 */
public class ConnectionStateListenerDemo {

    public static void createClient(){
        String path ="/tom/cat";
        TestingServer testingServer = null;
        try {
            testingServer = new TestingServer();
            CuratorFramework curator = CuratorFrameworkFactory.newClient
                    (testingServer.getConnectString(),5000,3000,new ExponentialBackoffRetry(1000, 3));
            curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path,"hello".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
