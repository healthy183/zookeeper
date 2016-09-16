package com.kang.curator.stateListener;

import com.google.common.base.Throwables;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;

/**
 * @Title
 * @Description
 * @Date 2016/8/31 19:36
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
@AllArgsConstructor
public class MyConnectionStateListener  implements ConnectionStateListener {

    private String path;
    private String regContent;

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
        if(newState  == ConnectionState.LOST || newState  == ConnectionState.SUSPENDED ){
            while(true){
                try{
                    if(curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                                .forPath(path, regContent.getBytes("UTF-8"));
                        break;
                    }
                }catch(Exception ex){
                    log.info("throw ex ", Throwables.getStackTraceAsString(ex));
                }
            }
        }
    }
}
