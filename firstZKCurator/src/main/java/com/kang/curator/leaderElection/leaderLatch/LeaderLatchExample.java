package com.kang.curator.leaderElection.leaderLatch;

import com.google.common.base.Throwables;
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
 * Created by Healthy on 2015/10/5.
 */

@Slf4j
public class LeaderLatchExample {

    private static final int CLIENT_QTY = 5;
    private static final String PATH = "/examples/leader";

    public static void main(String[] args) {
        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderLatch> examples = Lists.newArrayList();
        TestingServer server = null;
        try {
            server = new TestingServer();//模拟服务器
            for(int i = 0;i<CLIENT_QTY;i++){
                //重试次数和重试间距(间距动态)
                //时间间隔 = baseSleepTimeMs * Math.max(1, random.nextInt(1 << (retryCount + 1)))
                ExponentialBackoffRetry retry =  new ExponentialBackoffRetry(1000, 3);
                String url = server.getConnectString();
                //zookeeper客户端
                CuratorFramework client = CuratorFrameworkFactory.newClient(url,retry);
                client.getConnectionStateListenable().addListener(new SimpleConnectionStateListener());
                clients.add(client);

                String clientName = "client#"+i;
                LeaderLatch leaderLatch =  new LeaderLatch(client,PATH,clientName);
                MyLeaderLatchListener myLeaderLatchListener = new MyLeaderLatchListener(clientName);
                leaderLatch.addListener(myLeaderLatchListener);
                examples.add(leaderLatch);

                client.start();
                leaderLatch.start();
            }
                Thread.sleep(20000);
                LeaderLatch currentLeader = null;
            for(int i = (CLIENT_QTY-1);i>=0;i--){
                LeaderLatch example = examples.get(i);
                if(example.hasLeadership()){
                    currentLeader = example;
                   examples.remove(example);
                   break;
                }
            }
                log.info("current leader is [{}]", currentLeader.getId());
                currentLeader.close();
                examples.get(0).await(2, TimeUnit.SECONDS);//阻塞,尝试获取leadership，不一定成功
                log.info("client #0 maybe elected be the leader!");
                log.info("the new leader is [{}]", examples.get(0).getLeader().getId());
                System.out.println("pls enter /return to quit\n");
                new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            log.info("exception throw,cause:{}", Throwables.getStackTraceAsString(e));
        }finally {
            log.info("Shutting down...");
            for(LeaderLatch exampleClient : examples){
                try{
                    CloseableUtils.closeQuietly(exampleClient);
                }catch(IllegalStateException ex){
                    log.info("illegalStateException throw  when close exampleClient,cause:{}", Throwables.getStackTraceAsString(ex));
                }catch (Exception ex){
                    log.info("exception throw  when close exampleClient,cause:{}", Throwables.getStackTraceAsString(ex));
                }
            }
            for(CuratorFramework client : clients){
                try{
                    CloseableUtils.closeQuietly(client);
                }catch(IllegalStateException ex){
                    log.info("illegalStateException throw  when close CuratorFramework,cause:{}", Throwables.getStackTraceAsString(ex));
                }catch (Exception ex){
                    log.info("exception throw  when close exampleClient,cause:{}", Throwables.getStackTraceAsString(ex));
                }
            }

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
