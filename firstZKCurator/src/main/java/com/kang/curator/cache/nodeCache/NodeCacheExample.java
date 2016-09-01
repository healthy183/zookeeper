package com.kang.curator.cache.nodeCache;

import com.google.common.base.Throwables;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @Title
 * @Description
 * @Date 2016/8/30 16:31
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class NodeCacheExample {

    private static final String PATH = "/example/nodeCache";

    public static void main(String[] args) {
        TestingServer server = null;
        CuratorFramework client = null;
        NodeCache cache = null;
        try {
            server = new TestingServer();
            client = CuratorFrameworkFactory.newClient
                    (server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();
            cache = new NodeCache(client,PATH);
            cache.start();
            processCommands(client, cache);
        } catch (Exception e) {
            log.info("throw exception,cause:{}", Throwables.getStackTraceAsString(e));
        }finally {
            if(cache != null){
                CloseableUtils.closeQuietly(cache);
            }
            if(client != null){
                CloseableUtils.closeQuietly(client);
            }
            if(server != null){
                CloseableUtils.closeQuietly(server);
            }
        }
    }

    private static void addListener(final NodeCache cache){
        NodeCacheListener listener =  new NodeCacheListener(){
            @Override
            public void nodeChanged() throws Exception {
                if(cache.getCurrentData() != null){
                    log.info("node change [{}],value:[{}]",
                            cache.getCurrentData().getPath(),new String(cache.getCurrentData().getData()));
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

    private static void processCommands(CuratorFramework client,NodeCache cache) throws  Exception{
        printHelp();
        try{
            addListener(cache);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean done  = false;
            while(!done){
                log.info(">");
                String line = in.readLine();
                if(line == null){
                    break;
                }
                String command = line.trim();
                String[] parts = command.split("\\s");
                if(parts.length == 0){
                    continue;
                }
                String operation = parts[0];
                String args[] = Arrays.copyOfRange(parts,1,parts.length);
                if(operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("h")|| operation.equalsIgnoreCase("?")){
                    printHelp();
                }else if(operation.equalsIgnoreCase("q") || operation.equalsIgnoreCase("quit")){
                    done = true;
                }else if(operation.equals("set")){
                    setValue(client, command, args);
                }else if(operation.equals("remove")){
                    remove(client);
                } else if (operation.equals("show")) {
                    show(cache);
                }
                Thread.sleep(1000); // just to allow the console output to catch up
            }
        }catch(Exception ex){
            log.info("exception cause {}", Throwables.getStackTraceAsString(ex));
        }finally {

        }
    }

    private static void show(NodeCache cache) {
        if(cache.getCurrentData() != null){
            log.info(cache.getCurrentData().getPath() + " = " + new String(cache.getCurrentData().getData()));
        }else{
            log.info("cache don't set a value");
        }
    }

    private static void printHelp() {
        log.info("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:\n");
        log.info("set <value>: Adds or updates a node with the given name");
        log.info("remove: Deletes the node with the given name");
        log.info("show: Display the node's value in the cache");
        log.info("quit: Quit the example");
        log.info("");
    }


    private static void setValue(CuratorFramework client, String command, String[] args) throws Exception{
        if(args.length != 1){
            log.info("syntax error (expected set <value>) [{}] ", command);
            return;
        }
        byte[] bytes = args[0].getBytes();
        try{
            client.setData().forPath(PATH,bytes);
        }catch(KeeperException.NoNodeException e){
            log.info("Parents node no found,system will be created automatically ");
            client.create().creatingParentsIfNeeded().forPath(PATH, bytes);
        }
    }

    private static void remove(CuratorFramework client)  throws Exception{
        try {
            client.delete().forPath(PATH);
        } catch (KeeperException.NoNodeException e) {
            // ignore
        }
    }


}
