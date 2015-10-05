package com.kang.zookeeper.znode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2015/9/4.
 */
@Slf4j
@Data
public class PersistentZnode implements Watcher {

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
        if(event.getState() == Event.KeeperState.SyncConnected){
            connectedSignal.countDown();//结束一个等待CountDownLatch
            log.info("服务器已经连上！");
        }

        log.info("触发'"+event.getType()+"'事件!");

    }

    public void create(String groupName){

        String  path = "/"+groupName;
        try {
            //创建一个znode
            zk.create(path,
                    (groupName + "data").getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);

            log.info("groupName data is " + new String(zk.getData(path, true, null)));



        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delete(String groupName){

        try {
            zk.delete("/"+groupName,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public void close(){

        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        PersistentZnode secondZnode = new PersistentZnode();
        //连接服务
        secondZnode.connect(host);
        //创建znode
        secondZnode.create(SECOND_ZNODE);
        //删除znode
       //secondZnode.delete(SECOND_ZNODE);

        secondZnode.close();

    }
}
