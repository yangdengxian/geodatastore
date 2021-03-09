package com.geodatastore.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ZKConnectionTest {
    private final String zkLocation = "192.168.1.5:2181";
    private  final int sessionTimeOut = 5000;
    /**
     * 迭代获取父节点的子节点
     * @param parentNodeName
     * @param zooKeeper
     * @return
     */
    public ArrayList<String> iterChildNodeList(String parentNodeName, ZooKeeper zooKeeper){
        if(parentNodeName != null && !parentNodeName.equals("")){
            try {
                ArrayList<String> childNodeList = (ArrayList<String>)zooKeeper.getChildren(parentNodeName, null);
                if(childNodeList.size() > 0){
                    System.out.println("父结点:" + parentNodeName);
                    for(String childNode : childNodeList){
                        String childNodePath = "";
                        if(!parentNodeName.equals("/")){
                            childNodePath = parentNodeName + "/" + childNode;
                        }else {
                            childNodePath = parentNodeName +  childNode;
                        }
                        System.out.println(parentNodeName + "的子节点：" + childNodePath);
                        iterChildNodeList(childNodePath, zooKeeper);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<String>();
    }

    @Test
    public void zkConnect() {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zooKeeper = new ZooKeeper(
                    zkLocation,
                    sessionTimeOut, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (Event.KeeperState.SyncConnected == event.getState()) {
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
            System.out.println(zooKeeper.getState());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }


    /**
     * 获取服务器节点列表
     */
    @Test
    public void listTest(){
        try {
            ZooKeeper zooKeeper = new ZooKeeper(zkLocation, sessionTimeOut, null);
            String root = "/";
            iterChildNodeList(root, zooKeeper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定节点的节点数据
     */
    @Test
    public void getDataTest(){
        try {
            ZooKeeper zooKeeper = new ZooKeeper(zkLocation, sessionTimeOut, null);
            String path = "/hbase";
            String data = new String(zooKeeper.getData(path, null, new Stat()));
            System.out.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 节点创建测试
     */
    @Test
    public void createTest(){
        try{
            ZooKeeper zooKeeper = new ZooKeeper(zkLocation, sessionTimeOut,null);
            String dataStr = "100";
            byte[] data = dataStr.getBytes();
            // CreateMode:
            //（1）PERSISTENT：持久；（2）PERSISTENT_SEQUENTIAL：持久顺序；（3）EPHEMERAL：临时；（4）EPHEMERAL_SEQUENTIAL：临时顺序。
            String res = zooKeeper.create("/zookeeper/testNode", data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            if(res != null && !res.equals("")){
                System.out.println("插入节点为：" + res);
                String newData = new String(zooKeeper.getData("/zookeeper/testNode", null, new Stat()));
                System.out.println("新插入数据为:" + newData);
            }else {
                System.out.println("创建失败！");
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * 节点数据更新测试
     */
    @Test
    public void setTest(){
        try{
            ZooKeeper zooKeeper = new ZooKeeper(zkLocation, sessionTimeOut,null);
            // 修改的节点
            String path = "/zookeeper/testNode";
            // 修改的新数据
            byte[] data = new String("man").getBytes();
            // 期望更新的版本号
            // 从未修改后的话版本号为0，修改成功后版本号递增1
            int version = 0;
            // 修改之前的节点数据
            String beforeData = new String(zooKeeper.getData(path, null, new Stat()));
            Stat stat = zooKeeper.setData(path, data, version);
            // 修改之后的版本号
            int newVersion = stat.getVersion();
            // 修改之后的节点数据
            String afterData = new String(zooKeeper.getData(path, null, new Stat()));
            System.out.println("更新之前数据为:" + beforeData);
            System.out.println("更新之后数据为:" + afterData);
            System.out.println("更新数据后新版本号为:" + newVersion);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * 节点删除测试
     */
    @Test
    public void deleteTest(){
        try{
            ZooKeeper zooKeeper = new ZooKeeper(zkLocation, sessionTimeOut,null);
            // 删除的节点
            String path = "/it1002/grade";
            Stat stat = zooKeeper.exists(path, null);
            // 删除的节点的版本
            int version = stat.getVersion();
            // 执行删除
            zooKeeper.delete(path, version);
            // 删除后列出最新的zk节点结构
            listTest();
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
