package com.kang.curator.cache.pathChildrenCache;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/9/16.
 * @Author Healthy
 * @Version
 */
@Slf4j
public class PathChildrenCacheDemo {

    public static void main(String[] args) {
        TestingServer server=null;
        CuratorFramework client=null;
        NodeCache nodeCache=null;
        String path="/francis/nodecache/b";
        try {
            server = new TestingServer();
            client= CuratorFrameworkFactory.newClient(server.getConnectString(),new ExponentialBackoffRetry(1000,3));
            client.start();
            //client.delete().deletingChildrenIfNeeded().forPath("/francis");
            //true meets get the  "CHILDREN_UPDATE"  notice when set the childnote path data;
            PathChildrenCache childrenCache =  new PathChildrenCache(client,path,true);
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            childrenCache.getListenable().addListener(new PathChildrenCacheListener(){
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    // TODO Auto-generated method stub
                    if(event.getType()== PathChildrenCacheEvent.Type.INITIALIZED){
                        System.out.println("create"+event.getData().getPath());
                    }else if(event.getType()==PathChildrenCacheEvent.Type.CHILD_ADDED){
                        System.out.println("create"+event.getData().getPath());
                    }else if(event.getType()==PathChildrenCacheEvent.Type.CHILD_REMOVED){
                        System.out.println("remove:"+event.getData().getPath());
                    }else if(event.getType()==PathChildrenCacheEvent.Type.CHILD_UPDATED){
                        //System.out.println("update:"+event.getData().getPath());
                        System.out.println("update:"+new String(event.getData().getData()));
                    }
                };
             });

            String values = client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(path,"init".getBytes());
            log.info("value is [{}]!",values);
            Thread.sleep(1000);

            String  childPathOne = ZKPaths.makePath(path,"a");
            client.create().withMode(CreateMode.PERSISTENT).forPath(childPathOne,"one".getBytes());
            Thread.sleep(1000);

            client.setData().forPath(childPathOne,"childPathOneValue".getBytes());
            Thread.sleep(1000);

            client.delete().forPath(childPathOne);
            client.delete().deletingChildrenIfNeeded().forPath("/francis");
            Thread.sleep(2000);

            }catch (Exception ex){
            log.info("unknow exception,cause:[{}]", Throwables.getStackTraceAsString(ex));
        }finally {
            if(nodeCache != null){
                CloseableUtils.closeQuietly(nodeCache);
            }
            if(client != null){
                CloseableUtils.closeQuietly(client);
            }
            if(server != null){
                CloseableUtils.closeQuietly(server);
            }
        }
    }
};
