package com.kang.curator;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.List;

/**
 * Created by Healthy on 2015/10/5.
 */

public class LeaderLatchExample {

    private static final int CLIENT_QTY = 10;
    private static final String PATH = "/examples/leader";


    public static void main(String[] args) {

        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderLatch> examples = Lists.newArrayList();

        try {

            TestingServer server = new TestingServer();//模拟服务器

            for(int i = 0;i<CLIENT_QTY;i++){

                //重试次数和重试间距
                ExponentialBackoffRetry retry =  new ExponentialBackoffRetry(1000, 3);

                String url = server.getConnectString();

                //zookeeper客户端
                CuratorFramework client = CuratorFrameworkFactory.newClient(url,retry);
                clients.add(client);

                LeaderLatch leaderLatch =  new LeaderLatch(client,PATH,"client#"+i);
                examples.add(leaderLatch);

                client.start();
                leaderLatch.start();
            }

            Thread.sleep(2000);

            LeaderLatch currentLeader = null;

            for(int i = 0;i<CLIENT_QTY;i++){

                LeaderLatch example = examples.get(i);

                if(example.hasLeadership()){
                    currentLeader = example;
                   // break;
                }
            }









        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
