package com.kang.zk.curator.leaderSelector;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/8/25.
 * @Author Healthy
 * @Version
 */
@Slf4j
public class ExampleClient  extends LeaderSelectorListenerAdapter implements Closeable {

    private final String name;
    private final LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger();


    public  ExampleClient(CuratorFramework client,String path, String name){
        this.name = name;
        this.leaderSelector = new LeaderSelector(client,path,this);
        leaderSelector.autoRequeue();//guarantee client can takeLeadership again;
    }

    public void start() throws IOException{
        leaderSelector.start();
    }

    @Override
    public void close() throws IOException {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client)  throws Exception {
        final int waitSeconds = (int)(5*Math.random())+1;
        log.info("[{}] is the leader now,it had waited [{}]",name,waitSeconds);
        log.info("[{}] had been leader [{}] time(s)  before!",name,leaderCount.getAndIncrement());
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        } catch (InterruptedException e) {
            log.info("[{}] was interrupted. cause:[{}]",name, Throwables.getStackTraceAsString(e));
            Thread.currentThread().interrupt();
        }finally {
            log.info("[{}] relinquishing leadership.",name);
        }
    }
}
