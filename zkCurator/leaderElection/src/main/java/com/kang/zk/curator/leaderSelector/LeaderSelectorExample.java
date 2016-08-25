package com.kang.zk.curator.leaderSelector;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @Title
 * @Description
 * @Date 2016/8/25 11:30
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class LeaderSelectorExample {

    private static final int CLIENT_QTY = 10;

    private static final String PATH = "/examples/leader";


    public static void main(String[] args) {

        List<CuratorFramework> clients =  Lists.newArrayList();
        List<ExampleClient> examples =  Lists.newArrayList();
        TestingServer server = null;
        BufferedReader bufferedReader = null;
        try {
            server = new TestingServer();
            for(int i = 0; i<CLIENT_QTY;i++){
                CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(),new ExponentialBackoffRetry(100,3));
                clients.add(client);
                ExampleClient example = new ExampleClient(client,PATH,"client#"+i);
                examples.add(example);
                client.start();
                example.start();
            }
            System.out.println("Press enter/return to quit\n");
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            bufferedReader.readLine();
        } catch (Exception e) {
            log.info("unknow exception,cause:", Throwables.getStackTraceAsString(e));
        }finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("shutting down...");
        for(ExampleClient exampleClient : examples){
            CloseableUtils.closeQuietly(exampleClient);
        }
        for(CuratorFramework client : clients){
            CloseableUtils.closeQuietly(client);
        }
        CloseableUtils.closeQuietly(server);
    }
}
