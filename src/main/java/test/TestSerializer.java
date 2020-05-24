package test;

import base.MySerializer;
import bean.JavaBean;
import bean.NettyBean;
import bean.TestBean;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import serializer.JavaSerializer;
import serializer.NettySerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
////        NettyBean nettyBean1 = new NettyBean();
////        nettyBean1.setId(2);
////        testBean.setNettyBeans(CollUtil.newArrayList(nettyBean1));
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
                nettyBean1.setName("test");
                nettyBean1.setIds(new int[]{1, 2, 3});
                nettyBean1.setMoney(50.55);
                nettyBean1.setBean(testBean);
//            nettyBean.setList(CollUtil.newArrayList(1,2,3));
//        nettyBean.setSet(CollUtil.newLinkedHashSet(7,9,8));
//        nettyBean.setMap(map);
                byte[] beanBytes1 = serializer1.serialize(nettyBean1);
                System.out.println(Thread.currentThread().getName() + " 序列化:" + beanBytes1.length);
                System.out.println(Thread.currentThread().getName() +  " " + nettyBean1);
                System.out.println(Thread.currentThread().getName() + " " + Arrays.toString(beanBytes1));
                NettyBean resBean1 = serializer1.deserialize(beanBytes1, NettyBean.class);
                System.out.println(Thread.currentThread().getName() + " 反序列化:");
                System.out.println(Thread.currentThread().getName() + " " + resBean1);

            }catch (Exception e){
                e.printStackTrace();
            }
        };
        ThreadPoolExecutor executor =  new ThreadPoolExecutor(2, 10, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2), ThreadFactoryBuilder.create().build());
        try {
            for (int i = 0; i < 5; i++) {
                executor.execute(runnable);
                executor.execute(runnable);

            }
        }finally {
            executor.shutdown();
        }
    }
}
