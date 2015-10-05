package com.kang.zookeeper.znode;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.util.List;

/**
 * Created by Administrator on 2015/9/5.
 */
@Slf4j
public class DeleteZnode extends  PersistentZnode {

    public void delete(String path){

        try {
            List<String> znodeList =  zk.getChildren(path,true,null);

            if(!znodeList.isEmpty()){

                for(String s: znodeList){
                    log.info(path+"其中一个节点是"+s);
                    //执行删除,-1代表忽略zk的版本检查机制
                    zk.delete(path+"/"+s,-1);
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

        DeleteZnode dz = new DeleteZnode();
        dz.connect(host);
        dz.delete("/"+SECOND_ZNODE);
        dz.close();
    }
}
