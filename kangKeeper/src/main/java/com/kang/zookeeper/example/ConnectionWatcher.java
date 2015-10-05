package com.kang.zookeeper.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2015/9/12.
 */
@Slf4j
public class ConnectionWatcher   implements Watcher {

    public  final static String SECOND_ZNODE = "secondZnode";
    private final static Integer SESSION_TIMEOUT = 5000;
    private final static String IP = "192.168.202.130";
    private final static String PORT = "2181";
    public static String host = IP +":"+ PORT;
    protected ZooKeeper zk;
    private CountDownLatch connectedSignal
            = new CountDownLatch(1); //默认一个等待CountDownLatch

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
        if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
            connectedSignal.countDown();//结束一个等待CountDownLatch
            log.info("服务器已经连上！");
        }

        log.info("触发'"+event.getType()+"'事件!");

    }

    public void close(){

        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
