package com.kang.zookeeper.znode;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2015/9/3.
 */
@Slf4j
public class FirstZnode {

    private static String ip = "192.168.202.130";
    private static String port = "2181";
    private static String ipPort = ip+":"+port;
    private static String testRootPath = "/testRootPath";
    private static String testRootData = "testRootData";
    private static String testRootPathChildren = "/testChildPathOne";
    private static String testChildPathOne = "testChildPathOne";
    private static String testRootPathChildrenTwo = "/testChildPathTwo";
    private static String testChildPathTwo = "testChildPathTwo";


    public static void  main(String[] args) {

       // System.out.println("wkx");
        //log.info("test!");
        firstTest();
    }




    private static void firstTest(){

        try {
            ZooKeeper zk = new ZooKeeper(ipPort,50000, new Watcher(){

                @Override // 监控所有被触发的事件
                public void process(WatchedEvent event) {
                        log.info("触发'"+event.getType()+"'事件");
                }
            });

            // 创建一个目录节点
            /**/
            zk.create(testRootPath,
                    testRootData.getBytes(),
                           ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);  //None事件

            // 创建一个目录子节点 //None事件
           /**/ zk.create(testRootPath+testRootPathChildren,
                    testChildPathOne.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);

            log.info(testRootPath+" data: "+new String(zk.getData(testRootPath,true,null)));

            // 取出子目录节点列表
            /**/List<String> childrenList =  zk.getChildren(testRootPath, true);
            log.info(testRootPath+"子目录节点list:"+childrenList.toString());

            //修改子节点数据
            zk.setData(testRootPath + testRootPathChildren,
                    "testChildPathOneModify".getBytes(), -1);//-1的意思是绕过zk的版本监测机制

          /* */ log.info(testRootPath + testRootPathChildren + " data: "
                    + new String(zk.getData(testRootPath + testRootPathChildren, true, null)));

            log.info("目录节点状态："+zk.exists(testRootPath, true));
            // 创建另一个目录子节点
            zk.create(testRootPath+testRootPathChildrenTwo,
                    testChildPathTwo.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);/**/  //NodeChildrenChanged事件

            log.info(testRootPath + testRootPathChildrenTwo + " data: "
                    + new String(zk.getData(testRootPath + testRootPathChildrenTwo, true, null)));
/**/

            //删除子节点 NodeDeleted事件
            zk.delete(testRootPath+testRootPathChildren,-1);
            zk.delete(testRootPath+testRootPathChildrenTwo,-1);

            //删除父节点 NodeDeleted事件
            zk.delete(testRootPath,-1);
            //log.info(testRootPath+" data: "+new String(zk.getData(testRootPath,false,null)));


            zk.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }


    }

}
