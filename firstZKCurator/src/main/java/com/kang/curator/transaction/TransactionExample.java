package com.kang.curator.transaction;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;

import java.util.Collection;

/**
 * @Title
 * @Description
 * @Date 2016/8/31 17:19
 * @Author Healthy
 * @Version 2.0
 */
public class TransactionExample {


    public static void main(String[] args) {

    }


    public static Collection<CuratorTransactionResult> transaction(CuratorFramework client) throws Exception {
        Collection<CuratorTransactionResult> results =
                 client.inTransaction()
                .create().forPath("/a/path", "someData".getBytes())
                .and().setData().forPath("/another/path", "otherData".getBytes())
                .and().delete().forPath("/yet/another/path")
                .and().commit(); // IMPORTANT!

        for (CuratorTransactionResult result : results) {
            System.out.println(result.getForPath() + " - " + result.getType());
        }
        return results;
    }


    public static CuratorTransaction startTransaction(CuratorFramework client){
        // start the transaction builder
        return client.inTransaction();
    }


    public static CuratorTransactionFinal addCreateTotransaction(CuratorTransaction transaction) throws Exception {
        return transaction.create().forPath("/a/path","someData".getBytes()).and();
    }

    public static  CuratorTransactionFinal addDeleteTotransaction(CuratorTransaction transaction) throws Exception {
        // add a delete operation
        return transaction.delete().forPath("/another/path").and();
    }

    public static void commitTransaction(CuratorTransactionFinal transaction) throws Exception {
        // commit the transaction
        transaction.commit();
    }
}
