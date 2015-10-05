package com.kang.zookeeper.znode;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.util.List;

/**
 * Created by Administrator on 2015/9/5.
 */
@Slf4j
public class ChildrenZnodeList extends  PersistentZnode {


    public void getChildList(String path){

        try {
              List<String> znodeList =  zk.getChildren(path,true,null);

             if(!znodeList.isEmpty()){

                for(String s: znodeList){
                        log.info(path+"其中一个节点是"+s);
                }

             }else{
                 log.info(path+"没有子节点!");
             }

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        ChildrenZnodeList list  = new ChildrenZnodeList();
        list.connect(host);
        list.getChildList("/"+SECOND_ZNODE);
        list.close();;
    }

}
