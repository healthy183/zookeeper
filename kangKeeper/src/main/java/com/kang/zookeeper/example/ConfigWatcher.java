package com.kang.zookeeper.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by Administrator on 2015/9/12.
 */
@Slf4j
public class ConfigWatcher implements Watcher {

    private ActiveKeyValueStore activeKeyValueStore;

    public ConfigWatcher(){
        activeKeyValueStore = new ActiveKeyValueStore();
        activeKeyValueStore.connect(activeKeyValueStore.host);
    }

    public ConfigWatcher(String host){

         activeKeyValueStore = new ActiveKeyValueStore();
         activeKeyValueStore.connect(host);
    }

    public void displayConfig(){

       String value =  activeKeyValueStore.read(ConfigUpdater.PATH,this);

        log.info("set [{}] to [{}]!",ConfigUpdater.PATH,value);

    }



    @Override
    public void process(WatchedEvent event) {

        if(event.getType() == Event.EventType.NodeDataChanged){
            displayConfig();
        }

    }

    public static void main(String[] args) {

        ConfigWatcher configWatcher  = new ConfigWatcher(ActiveKeyValueStore.host);
        configWatcher.displayConfig();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }





}
