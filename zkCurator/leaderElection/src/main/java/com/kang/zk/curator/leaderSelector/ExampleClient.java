package com.kang.zk.curator.leaderSelector;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/8/25.
 * @Author Healthy
 * @Version
 */
public class ExampleClient  extends LeaderSelectorListenerAdapter implements Closeable {
    @Override
    public void close() throws IOException {
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
    }
}
