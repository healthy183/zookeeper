package com.keeper.znode;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2015/9/20.
 */
@Slf4j
public class SuperDigestZnode {

    private final static String PATH = "/first_auths";
    private final static String PATH_DELETE = PATH+"/will_delete";

    private  final static String AUTH_TYPE ="digest";

    private final static String CORRECT_AUTH = "taokeeper:true";

    private final static String BAD_AUTH = "taokeeper:errorCode";

    //启动参数添加即可使用超级用户
    //加密方法:DigestAuthenticationProvider.generateDigest("super:yinshi.nc-1988")
    //"-Dzookeeper.DigestAuthenticationProvider.superDigest=super:/7ahZf2EjED/untmtb2NRkHhVlA="
    private final static String SUPER_AUTH = "super:yinshi.nc-1988";

    public void mainRun(){

        creatZnode();
        getDataByNoAuthentication(PATH);
        getDataByBadAuthentication(PATH);
        getDataByCorrectAuthentication(PATH);
        getDataByBadAuthentication(PATH);

    }

    public void creatZnode(){

        List<ACL> acls = new ArrayList<ACL>(1);

        for (ACL ids_acl : ZooDefs.Ids.CREATOR_ALL_ACL ) {
            acls.add(ids_acl);
        }

        CommonWatcher cw = new CommonWatcher();
        cw.connect(cw.host);
        ZooKeeper zk =  cw.getZk();

        zk.addAuthInfo(AUTH_TYPE,CORRECT_AUTH.getBytes());

        try {
            zk.create(PATH,"initContent".getBytes(),
                    acls, CreateMode.PERSISTENT);

            log.info("使用授权key[{}]创建持久znode[{}]!",CORRECT_AUTH ,PATH);

            zk.create(PATH_DELETE,"deleteContent".getBytes(),
                    acls, CreateMode.PERSISTENT);
            log.info("使用授权key[{}]创建持久znode[{}]!", CORRECT_AUTH, PATH_DELETE);

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cw.close();
    }

    /*
    * 获取节点数据:不使用授权信息 */
    public void  getDataByNoAuthentication(String path){

        String prefix  = "不使用授权信息";

        log.info("尝试[{}]获取[{}]数据!", prefix, path);

        CommonWatcher cw = new CommonWatcher();
        cw.connect(cw.host);
        ZooKeeper zk =  cw.getZk();

        try {
         log.info("节点[{}]数据为[{}]",path,new String(zk.getData(path,false,null)));
        } catch (KeeperException e) {
            e.printStackTrace();
            log.info("节点[{}]数据失败!",path,e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cw.close();
    }

    /*
    * 获取节点数据:使用错误授权信息 */
    public void getDataByBadAuthentication(String path){
        String prefix  = "使用错误授权信息";
        commonAuthentication(prefix,path,BAD_AUTH);
    }
    /*
     * 获取节点数据:使用正确授权信息 */
    public void getDataByCorrectAuthentication(String path){
        String prefix  = "使用正确授权信息";
        commonAuthentication(prefix,path,CORRECT_AUTH);
    }
    /*
     * 通用获取znode数据*/
    public void commonAuthentication(String prefix,String path,String authMsg){

        log.info("尝试[{}]获取[{}]数据!", prefix,path);

        CommonWatcher cw = new CommonWatcher();
        cw.connect(cw.host);
        ZooKeeper zk =  cw.getZk();
        zk.addAuthInfo(AUTH_TYPE,authMsg.getBytes());

        try {
            log.info("节点[{}]数据为[{}]",path,new String(zk.getData(path,false,null)));
        } catch (KeeperException e) {
            e.printStackTrace();
            log.info("节点[{}]数据失败!",path,e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cw.close();
    }

    public static void main(String[] args) {

        try {
            log.info("super:yinshi.nc-1988:" + DigestAuthenticationProvider.generateDigest("super:yinshi.nc-1988") );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        SuperDigestZnode szNode = new SuperDigestZnode();
        szNode.mainRun();

    }
}
