package com.kang.curator.cache.treeNode;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

/**
 * @Title
 * @Description
 * @Date 2016/8/31 10:00
 * @Author Healthy
 * @Version 2.0
 */
@Slf4j
public class TreeCacheExample {

    private static final String PATH = "/example/treeCache";

    public static void main(String[] args) {

        TestingServer server = null;
        CuratorFramework client = null;
        TreeCache cache = null;
        try {
            server = new TestingServer();
            client = CuratorFrameworkFactory.newClient
                    (server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();
            cache = new TreeCache(client,PATH);
            cache.start();
            processCommands(client, cache);
        } catch (Exception e) {
           log.info("e cause {}", Throwables.getStackTraceAsString(e));
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

    private static void addListener(final TreeCache cache){
        TreeCacheListener listener = new TreeCacheListener(){
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                switch(event.getType()){
                    case NODE_ADDED:{
                        log.info("TreeNode added:{}, value:{}",ZKPaths.getNodeFromPath(event.getData().getPath()),
                                new String(event.getData().getData()));
                        break;
                    }
                    case NODE_UPDATED:{
                        log.info("TreeNode changed:{},value{}",ZKPaths.getNodeFromPath(event.getData().getPath()),
                                new String(event.getData().getData()));
                        break;
                    }
                    case NODE_REMOVED:{
                        log.info("TreeNode removed {}",ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }
                    default:
                        log.info("Other event:{}",event.getType().name());
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

    private static void processCommands(CuratorFramework client, TreeCache cache) {
        printHelp();
        try{
            addListener(cache);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean done = false;
            while (!done){
                log.info(">");
                String line = in.readLine();
                if(line == null){
                    break;
                }
                String command = line.trim();
                String[] parts = command.split("\\s");
                if (parts.length == 0) {
                    continue;
                }
                String operation = parts[0];
                String args[] = Arrays.copyOfRange(parts, 1, parts.length);
                if (operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("?")) {
                    printHelp();
                } else if (operation.equalsIgnoreCase("q") || operation.equalsIgnoreCase("quit")) {
                    done = true;
                } else if (operation.equals("set")) {
                    setValue(client, command, args);
                } else if (operation.equals("remove")) {
                    remove(client, command, args);
                } else if (operation.equals("show")) {
                    show(cache);
                }
                Thread.sleep(1000); // just to allow the console output to catch
            }
        }catch(Exception ex){
            log.info("unknow exception cause{}",Throwables.getStackTraceAsString(ex));
        }
    }

    private static void show(TreeCache cache){
        if(cache.getCurrentChildren(PATH)== null || cache.getCurrentChildren(PATH).size() == 0){
            log.info("[{}] didnot had any children path!",PATH);
        }else{
            for(Map.Entry<String,ChildData> entry : cache.getCurrentChildren(PATH).entrySet()){
                log.info("path [{}],value [{}]",entry.getKey(),new String(entry.getValue().getData()));
            }
        }
    }

    private static void remove(CuratorFramework client, String command, String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("syntax error (expected remove <path>): " + command);
            return;
        }
        String name = args[0];
        if (name.contains("/")) {
            System.err.println("Invalid node name" + name);
            return;
        }
        String path = ZKPaths.makePath(PATH, name);
        try {
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            log.info("path[{}] not found ",path);
        }
    }

    private static void setValue(CuratorFramework client, String command, String[] args) throws Exception {
        if(args.length != 2){
            log.info("syntax error (expected set <path> <value>): ",command);
            return;
        }
        String name = args[0];
        if(name.contains("/")){
            log.info("Invalid node name {}",name);
            return;
        }
        String path = ZKPaths.makePath(PATH,name);
        byte[] bytes = args[1].getBytes();

        try {
            client.setData().forPath(path,bytes);
        }catch (KeeperException.NoNodeException e) {
            log.info("parents path not found [{}]",path);
            client.create().creatingParentsIfNeeded().forPath(path, bytes);
        }
    }

    private static void printHelp() {
        log.info("An example of using PathChildrenCache. This example is driven by entering commands at the prompt:\n");
        log.info("set <name> <value>: Adds or updates a node with the given name");
        log.info("remove <name>: Deletes the node with the given name");
        log.info("show: List the nodes/values in the cache");
        log.info("quit: Quit the example");
        log.info("");
    }

}
