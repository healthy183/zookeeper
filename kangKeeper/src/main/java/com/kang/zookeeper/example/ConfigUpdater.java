package com.kang.zookeeper.example;

import ch.qos.logback.core.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.util.Random;

/**
 * Created by Administrator on 2015/9/12.
 */
@Slf4j
public class ConfigUpdater {

    public static final String PATH = "/config";

    private ActiveKeyValueStore activeKeyValueStore;

    private Random random = new Random();

    public ConfigUpdater() {

        activeKeyValueStore = new ActiveKeyValueStore();
        activeKeyValueStore.connect(activeKeyValueStore.host);

    }

    public void run() {

        while (true) {

            String value = random.nextInt() + "";
            try {
                activeKeyValueStore.write(PATH, value);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("set [{}] to [{}]!",PATH,value);

            try {
                Thread.sleep(random.nextInt(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        ConfigUpdater configUpdater = new ConfigUpdater();

        configUpdater.run();

    }


}


