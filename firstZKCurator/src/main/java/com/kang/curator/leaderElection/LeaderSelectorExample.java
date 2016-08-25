package com.kang.curator.leaderElection;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Healthy on 2015/10/6.
 */
public class LeaderSelectorExample {

    private static final int CLIENT_QTY = 10;
    private static final String PATH ="/examples/leader";

    public static void main(String[] args){

        List<CuratorFramework> clients =  Lists.newArrayList();
        List<ExampleClient> exampleClients = Lists.newArrayList();

        TestingServer server = null;

        try {

            server = new TestingServer();
            String url = server.getConnectString();

            for(int i=0;i<CLIENT_QTY;i++){
                ExponentialBackoffRetry retry =  new ExponentialBackoffRetry(1000,3);
                CuratorFramework client = CuratorFrameworkFactory.newClient(url,retry);
                clients.add(client);
                ExampleClient example = new ExampleClient(client,PATH,"CLIENT#"+i);
                exampleClients.add(example);
                client.start();
                example.start();
            }

            System.out.println("Press enter/return to quit\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println("shutting down...");
            for(ExampleClient exampleClient : exampleClients){
                CloseableUtils.closeQuietly(exampleClient);
            }
            for(CuratorFramework client : clients){
                CloseableUtils.closeQuietly(client);
            }

            CloseableUtils.closeQuietly(server);
        }


    }
}
