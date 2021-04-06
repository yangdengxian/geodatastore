package org.geodatastore.zk.service.impl;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Master implements Watcher {
    ZooKeeper zooKeeper;
    String host;

    Master(String host) {
        this.host = host;
    }

    void startZK() throws IOException {
        zooKeeper = new ZooKeeper(host,1500, this);
    }
    @Override
    public void process(WatchedEvent event) {
        System.out.println(event);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Master master = new Master(args[0]);
        master.startZK();
        Thread.sleep(6000);
    }
}
