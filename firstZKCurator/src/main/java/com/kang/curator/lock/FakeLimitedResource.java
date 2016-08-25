package com.kang.curator.lock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Healthy on 2015/10/6.
 */
public class FakeLimitedResource {

    private final AtomicBoolean inUse = new AtomicBoolean(false);//构造参数:initialValue

    public void use(){
        //生产中我们这里访问/维护一个共享资源
        if(!inUse.compareAndSet(false,true)){//initialValue == arg0?arg1:initialValue
                                            //非首次初始化则抛异常
                throw new IllegalStateException("just support one client at the same time");
        }
        try {
            Thread.sleep((long)(3*Math.random()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            inUse.set(false);
        }
    }


}
