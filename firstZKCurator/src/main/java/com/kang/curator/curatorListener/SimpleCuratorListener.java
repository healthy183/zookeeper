package com.kang.curator.curatorListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/9/16.
 * @Author Healthy
 * @Version
 */
@Slf4j
public class SimpleCuratorListener implements CuratorListener {
    @Override
    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        if(event.getType()== CuratorEventType.CREATE){
            log.info("create path="+event.getPath()+",code="+event.getResultCode());
        }else if(event.getType()==CuratorEventType.GET_DATA){
            log.info("get path="+event.getPath()+",data="+new String(event.getData()));
        }else if(event.getType()==CuratorEventType.SET_DATA){
            log.info("set path="+event.getPath()+",data="+new String(client.getData().forPath(event.getPath()))/*+",data="+new String(event.getData())*/);
        }else if(event.getType()==CuratorEventType.DELETE){
            log.info("delete path="+event.getPath());
        }
    }
}
