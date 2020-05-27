package test;

import base.MySerializer;
import bean.JavaBean;
import bean.NettyBean;
import bean.TestBean;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import serializer.JavaSerializer;
import serializer.NettySerializer;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author linhao
 * @date 2020/5/18 23:21
 * @description:
 */
public class TestSerializer {
    public static void main(String[] args) throws Exception {
//        System.out.println("------------------JavaSerializer------------------");
//        MySerializer mySerializer = new JavaSerializer();
//        JavaBean bean = new JavaBean();
//        bean.setId(1);
//        bean.setAge(100);
//        bean.setName("test");
//        bean.setIds(new int[]{1, 2, 3});
//        bean.setMoney(50.88);
//        bean.setBean(new TestBean(1));
//        bean.setList(CollUtil.newArrayList(1,2,3));
//        bean.setSet(CollUtil.newHashSet(1,2,3));
//        Map<Integer,String> map = new HashMap<>();
//        map.put(1, "10");
//        map.put(2, "20");
//        bean.setMap(map);
//        long start = System.currentTimeMillis();
//        byte[] beanBytes = mySerializer.serialize(bean);
//        long end = System.currentTimeMillis();
//        System.out.println("序列化时间：" + (end - start));
//        System.out.println("序列化:" + beanBytes.length);
//        System.out.println(bean);
//        System.out.println(Arrays.toString(beanBytes));
//        start = System.currentTimeMillis();
//        JavaBean res = mySerializer.deserialize(beanBytes, JavaBean.class);
//        end = System.currentTimeMillis();
//        System.out.println("反序列化时间：" + (end - start));
//        System.out.println("反序列化:");
//        System.out.println(res);
//        System.out.println("----------------------Netty-----------------");
//        mySerializer = new NettySerializer();
//        NettyBean nettyBean = new NettyBean();
//        nettyBean.setId(1);
//        nettyBean.setAge(100);
//        nettyBean.setName("test");
//        nettyBean.setIds(new int[]{1, 2, 3});
//        nettyBean.setMoney(50.55);
//
        TestBean testBean = new TestBean(1);
        NettyBean bean = new NettyBean();
        bean.setId(2);
        testBean.setNettyBeans(CollUtil.newArrayList(bean));
//
//        nettyBean.setBean(testBean);
//        nettyBean.setList(CollUtil.newArrayList(1,2,3));
////        nettyBean.setSet(CollUtil.newLinkedHashSet(7,9,8));
////        nettyBean.setMap(map);
//        start = System.currentTimeMillis();
//        beanBytes = mySerializer.serialize(nettyBean);
//        end = System.currentTimeMillis();
//        System.out.println("序列化时间：" + (end - start));
//        System.out.println("序列化:" + beanBytes.length);
//        System.out.println(nettyBean);
//        System.out.println(Arrays.toString(beanBytes));
//        start = System.currentTimeMillis();
//        NettyBean resBean = mySerializer.deserialize(beanBytes, NettyBean.class);
//        end = System.currentTimeMillis();
//        System.out.println("反序列化时间：" + (end - start));
//        System.out.println("反序列化:");
//        System.out.println(resBean);

        System.out.println("-------------------------多线程测试--------------------");
        Runnable runnable = ()->{
            try {
                NettySerializer serializer1 = new NettySerializer();
                NettyBean nettyBean1 = new NettyBean();
                nettyBean1.setId(1);
                nettyBean1.setAge(100);
                nettyBean1.setName("哈哈");
                nettyBean1.setIds(new int[]{1, 2, 3});
                nettyBean1.setMoney(50.55);
                nettyBean1.setBean(testBean);
                nettyBean1.setDate(new Date());
                nettyBean1.setTestEnum(NettyBean.TestEnum.test1);
//            nettyBean.setList(CollUtil.newArrayList(1,2,3));
//        nettyBean.setSet(CollUtil.newLinkedHashSet(7,9,8));
//        nettyBean.setMap(map);
                byte[] beanBytes1 = serializer1.serialize(nettyBean1,1, new int[]{1, 2, 3},CollUtil.newArrayList(1,2,3));
                System.out.println(Thread.currentThread().getName() +  " " + nettyBean1);
                System.out.println(Thread.currentThread().getName() + " " + Arrays.toString(beanBytes1));
//                NettyBean resBean1 = serializer1.deserialize(beanBytes1, NettyBean.class);
                List deserializeAll = serializer1.deserialize(beanBytes1);
//                NettyBean resBean1 = (NettyBean) deserializeAll.get(0);
                System.out.println(Thread.currentThread().getName() + " 反序列化:");
                for (int i = 0; i < deserializeAll.size(); i++) {
                    System.out.print(deserializeAll.get(i)+"  ");
                }
                System.out.println();
            }catch (Exception e){
                e.printStackTrace();
            }
        };
        ThreadPoolExecutor executor =  new ThreadPoolExecutor(2, 10, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2), ThreadFactoryBuilder.create().build());
        try {
            for (int i = 0; i < 1; i++) {
                executor.execute(runnable);
            }
        }finally {
            executor.shutdown();
        }
    }
}
