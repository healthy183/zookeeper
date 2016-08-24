package com.kang.zk.curator.leaderLatch;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/8/24.
 * @Author Healthy
 * @Version
 */
@Slf4j
public class LeaderLatchExample {

    private static  final int CLIENT_QTY = 10;
    private static final String PATH  ="/examples/leader";

    public static void main(String[] args) throws Exception {
        List<CuratorFramework> clients =  Lists.newArrayList();
        List<LeaderLatch> examples = Lists.newArrayList();
        TestingServer testingServer =  new TestingServer();//mock server
        BufferedReader bufferedReader = null;
        try {
            for(int i = 0; i< CLIENT_QTY;i++){
                CuratorFramework client =  CuratorFrameworkFactory.newClient
                        (testingServer.getConnectString(),new ExponentialBackoffRetry(1000, 3));
                clients.add(client);
                LeaderLatch example = new LeaderLatch(client,PATH,"Client #" + i);
                examples.add(example);
                client.start();
                example.start();
            }
            Thread.sleep(2000);
            LeaderLatch currentLeader = null;
            for(int i = 0;i<CLIENT_QTY;i++){
                if(examples.get(i).hasLeadership()){
                    currentLeader  = examples.get(i);
                    break;
                }
            }
            log.info("current leader is [{}]",currentLeader.getId());
            log.info("release leaderShip [{}]",currentLeader.getId());
            currentLeader.close();
            examples.get(0).await(2, TimeUnit.SECONDS);
            log.info("Client #0 maybe is elected as the leader or not although it want to be");
            log.info("current leader is [{}]",examples.get(0).getLeader().getId());
            System.out.println("Press enter/return to quit\n");
            bufferedReader =  new BufferedReader(new InputStreamReader(System.in));
            String str = null;
            while((str = bufferedReader.readLine()).length() != 0){
                System.out.println(str);
            }
        } catch (Exception e) {
           log.info("exception cause:{}",e);
        }finally {
            log.info("shutting down!");
            for(LeaderLatch exmpleCLient : examples){
                CloseableUtils.closeQuietly(exmpleCLient);
            }
            for(CuratorFramework curatorFramework : clients){
                CloseableUtils.closeQuietly(curatorFramework);
            }
            CloseableUtils.closeQuietly(testingServer);
            if(bufferedReader != null){
                bufferedReader.close();
            }
        }

    }
}
