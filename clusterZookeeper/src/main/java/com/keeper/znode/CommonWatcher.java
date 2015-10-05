package com.keeper.znode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2015/9/20.
 */
@Slf4j
@Data
public class CommonWatcher   implements Watcher {

    private final static Integer SESSION_TIMEOUT = 20000;
    private final static String IP = "192.168.202.130";
    private final static String FIRST_PORT = "2181";
    private final static String SECOND_PORT = "2182";
    private final static String THIRD_PORT = "2183";

    public static String host = IP +":"+ FIRST_PORT+","+
                                    IP +":"+ SECOND_PORT+","+
                                        IP +":"+ THIRD_PORT;


    protected ZooKeeper zk;
    //protected ZKClient zkClient;
    private CountDownLatch connectedSignal
            = new CountDownLatch(1);//默认一个等待CountDownLatch

    public void connect(String host){
        try {
            zk  = new ZooKeeper(host,SESSION_TIMEOUT,this);
            connectedSignal.await(); //开始等待
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        //已经连上
        if(event.getState() == Event.KeeperState.SyncConnected){
            connectedSignal.countDown();//结束一个等待CountDownLatch
            log.info("服务器已经连上！");
        }

        log.info("收到[{}]事件通知,触发[{}]事件!",event.getState(),event.getType());
    }


    public void close(){
        try {
            // if(!ObjectUtils.isNotBlank(zk)){}
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
