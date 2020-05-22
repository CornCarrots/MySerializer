package test;

import base.MySerializer;
import bean.JavaBean;
import bean.NettyBean;
import bean.TestBean;
import cn.hutool.core.collection.CollUtil;
import serializer.JavaSerializer;
import serializer.NettySerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linhao
 * @date 2020/5/18 23:21
 * @description:
 */
public class TestSerializer {
    public static void main(String[] args) throws Exception {
        System.out.println("------------------JavaSerializer------------------");
        long start = System.currentTimeMillis();
        MySerializer mySerializer = new JavaSerializer();
        JavaBean bean = new JavaBean();
        bean.setId(1);
        bean.setAge(100);
        bean.setName("test");
        bean.setIds(new int[]{1, 2, 3});
        bean.setMoney(50.88);
        bean.setBean(new TestBean(1));
        bean.setList(CollUtil.newArrayList(1,2,3));
        bean.setSet(CollUtil.newHashSet(1,2,3));
        Map<Integer,String> map = new HashMap<>();
        map.put(1, "10");
        map.put(2, "20");
        bean.setMap(map);
        byte[] beanBytes = mySerializer.serialize(bean);
        System.out.println("序列化:" + beanBytes.length);
        System.out.println(bean);
        System.out.println(Arrays.toString(beanBytes));
        JavaBean res = mySerializer.deserialize(beanBytes, JavaBean.class);
        System.out.println("反序列化");
        System.out.println(res);
        long end = System.currentTimeMillis();
        System.out.println("序列化时间：" + (end - start));
        System.out.println("----------------------Netty-----------------");
        start = System.currentTimeMillis();
        mySerializer = new NettySerializer();
        NettyBean nettyBean = new NettyBean();
        nettyBean.setId(1);
        nettyBean.setAge(100);
        nettyBean.setName("test");
        nettyBean.setIds(new int[]{1, 2, 3});
        nettyBean.setMoney(50.55);
        nettyBean.setBean(new TestBean(1));
        nettyBean.setList(CollUtil.newArrayList(1,2,3));
        nettyBean.setSet(CollUtil.newLinkedHashSet(7,9,8));
        nettyBean.setMap(map);
        beanBytes = mySerializer.serialize(nettyBean);
        System.out.println("序列化:" + beanBytes.length);
        System.out.println(nettyBean);
        System.out.println(Arrays.toString(beanBytes));
        NettyBean resBean = mySerializer.deserialize(beanBytes, NettyBean.class);
        System.out.println("反序列化");
        System.out.println(resBean);
        end = System.currentTimeMillis();
        System.out.println("序列化时间：" + (end - start));
    }
}
