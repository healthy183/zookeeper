package com.kang.zookeeper.example;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;

/**
 * Created by Administrator on 2015/9/12.
 */
public class ActiveKeyValueStore extends  ConnectionWatcher {

    private static final Charset charset = Charset.forName("UTF-8");
    private static final Integer MAX_RETRIES = 100;

    public void write(String path,String value) throws KeeperException, InterruptedException {
        //幂等操作支持无限重试
        int retries = 0;
        while (true){

            try {
               // Stat stat =  zk.exists(path,false);
                Stat stat =  zk.exists(path,new ConfigWatcher());
                //zk.getDa
                //zk.getChild
                if(stat== null){

                    zk.create(path,value.getBytes(charset),
                            ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);

                }else{

                    zk.setData(path,value.getBytes(charset),-1);
                }
                return;

            } catch (KeeperException.SessionExpiredException e) {
                e.printStackTrace();
                throw e;
            } catch (KeeperException e) {
                //e.printStackTrace();
                if(retries++ == MAX_RETRIES){
                    throw e;
                }
               // Thread.Sleep(10000);
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }



    }


    public String read(String path,Watcher watcher){

        try {
            byte[]  data = zk.getData(path, watcher, null/*stat*/);
            return new String(data,charset);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}