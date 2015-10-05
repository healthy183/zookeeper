package com.keeper.znode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.server.LogFormatter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2015/9/20.
 */

@Slf4j
@Data
public class PersistentZnode  implements Watcher {

    public  final static String SECOND_ZNODE = "firsttCluster";
    private final static Integer SESSION_TIMEOUT = 50000;
    private final static String IP = "192.168.202.130";
    private final static String FIRST_PORT = "2181";
    private final static String SECOND_PORT = "2182";
    private final static String THIRD_PORT = "2183";

    public static String host = IP +":"+ FIRST_PORT+","+
                                    IP +":"+ SECOND_PORT+","+
                                        IP +":"+ THIRD_PORT;
    protected ZooKeeper zk;
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


    public void createPstSeq(String groupName){

        String  path = groupName;
        try {
            //创建一个znode
            zk.create(path,
                    (groupName + "data").getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL);

           // log.info("groupName data is " + new String(zk.getData(path, true, null)));

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getChildPath(String childPath){

        // 取出子目录节点列表
        /**/
        List<String> childrenList = null;
        try {

            childrenList = zk.getChildren(childPath, true);
            log.info(childPath+"子目录节点list:"+childrenList.toString());

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }



    public void delete(String groupName){

        try {
            zk.delete("/" + groupName, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        PersistentZnode  pznode = new PersistentZnode();
        pznode.connect(host);

        String firstPath ="first";

        //pznode.create(firstPath);

       // String firstPathChild = "firstChild";

       // pznode.createPstSeq("/"+firstPath+"/"+firstPathChild);

      //  pznode.getChildPath("/" + firstPath);

        //pznode.delete("first");

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pznode.close();
    }



}
