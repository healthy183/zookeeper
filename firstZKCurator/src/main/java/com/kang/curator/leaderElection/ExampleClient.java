package com.kang.curator.leaderElection;

import ch.qos.logback.core.util.TimeUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Healthy on 2015/10/6.
 */
public class ExampleClient  extends LeaderSelectorListenerAdapter
            implements Closeable{

    private final String name;
    private final LeaderSelector  leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger();//用于记录领导次数

    public ExampleClient(CuratorFramework client,String path,String name) {
        this.name = name;
        leaderSelector = new LeaderSelector(client, path, this);
        leaderSelector.autoRequeue();//保留重新获取领导权资格
    }

    public void start() throws IOException {
        leaderSelector.start();
    }


    @Override
    public void close() throws IOException {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {

        final int waitSeconds =(int)(Math.random()*5)+1;

        System.out.println(name + " is  the leader now,wait " + waitSeconds + " seconds!");

        System.out.println(name + " had been leader for " + leaderCount.getAndIncrement() + " time(s) before");

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));//睡眠超过最大等待时间将失去leadership
        }catch (InterruptedException e){
            System.err.println(name+" was interrupted!");
            Thread.currentThread().interrupt();
        }finally{
            System.out.println(name+" relinquishing leadership.\n");
        }


    }
}
