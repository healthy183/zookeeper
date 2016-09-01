package com.kang.curator.cache.ephemeralNode;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.KillSession;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Title
 * @Description
 * @Date 2016/8/31 15:11
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class PersistentEphemeralNodeExample {

    private static final String ephemeraPath = "/example/ephemeralNode";
    private static final String PersistentPath = "/example/node";

    public static void main(String[] args) {
        TestingServer server = null;
        CuratorFramework client = null;
        PersistentEphemeralNode node = null;
        try {
            server = new TestingServer();
            client = CuratorFrameworkFactory.newClient
                    (server.getConnectString(),new ExponentialBackoffRetry(1000, 3));
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    log.info("client state: [{}]",newState.name());
                }
            });
            client.start();
            node = new PersistentEphemeralNode(client, PersistentEphemeralNode.Mode.EPHEMERAL, ephemeraPath,"test".getBytes());
            node.start();
            node.waitForInitialCreate(3, TimeUnit.SECONDS);
            String actualPath = node.getActualPath();
            log.info("node path [{}] value [{}]",actualPath,new String(client.getData().forPath(actualPath)));

            client.create().forPath(PersistentPath,"persistentNode".getBytes());
            log.info("node path2 [{}] value [{}]", PersistentPath,new String(client.getData().forPath(PersistentPath)));

            KillSession.kill(client.getZookeeperClient().getZooKeeper(), server.getConnectString());
            log.info("node [{}] doesn't exist:[{}] ",actualPath,(client.checkExists().forPath(actualPath) == null));
            log.info("node [{}] value: [{}]", PersistentPath, new String(client.getData().forPath(PersistentPath)));
        } catch (Exception e) {
          log.info("throw exception cause{}", Throwables.getStackTraceAsString(e));
        }finally {
            if(node != null){
                CloseableUtils.closeQuietly(node);
            }
            if(client != null){
                CloseableUtils.closeQuietly(client);
            }
            if(server != null){
                CloseableUtils.closeQuietly(server);
            }
        }
    }
}
