package com.kang.curator.leaderElection.leaderLatch;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/9/16.
 * @Author Healthy
 * @Version
 */
@Slf4j
public class SimpleConnectionStateListener  implements ConnectionStateListener {
    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if(newState  == ConnectionState.LOST || newState  == ConnectionState.SUSPENDED ){
            log.info("client lose connect!");
        }
    }
}
