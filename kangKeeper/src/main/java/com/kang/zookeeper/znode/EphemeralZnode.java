package com.kang.zookeeper.znode;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

/**
 * Created by Administrator on 2015/9/5.
 */
@Slf4j
public class EphemeralZnode extends PersistentZnode {

    public void joinZnode(String groupName,String joinName){

        java.util.Random rander = new java.util.Random();
        int  fileNo = rander.nextInt(999999);

        String  path = "/"+groupName + "/"+joinName+fileNo;

        try {
            //创建一个znode
            zk.create(path,
                    (path+"data").getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL); //zk执行close后就自动凋亡

            log.info("groupName data is " + new String(zk.getData(path, true, null)));

            //创建完就线程睡眠，模拟工作中
            Thread.sleep(Integer.MAX_VALUE);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        EphemeralZnode thirdZnode = new EphemeralZnode();

        thirdZnode.connect(host);

        thirdZnode.joinZnode(SECOND_ZNODE,"firstZnode");

        thirdZnode.close();
    }


}
