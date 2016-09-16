package com.kang.curator.leaderElection.leaderLatch;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

/**
 * @Title 类名
 * @Description 描述
 * @Date 2016/9/16.
 * @Author Healthy
 * @Version
 */
@Slf4j
@Getter
public class MyLeaderLatchListener implements LeaderLatchListener {
    private String clientName;
    public MyLeaderLatchListener(String clientName){
        this.clientName = clientName;
    }
    @Override
    public void isLeader() {
        log.info("[{}] is the leader now!",this.getClientName());
    }

    @Override
    public void notLeader() {
        log.info("[{}] lose the leaderShip now!",this.getClientName());
    }
}
