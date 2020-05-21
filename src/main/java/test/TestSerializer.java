package test;

import base.MySerializer;
import bean.JavaBean;
import bean.NettyBean;
import bean.TestBean;
import serializer.JavaSerializer;
import serializer.NettySerializer;

import java.util.Arrays;

/**
 * @author linhao
 * @date 2020/5/18 23:21
 * @description:
 */
public class TestSerializer {
    public static void main(String[] args) throws Exception {
        System.out.println("------------------JavaSerializer------------------");
        MySerializer mySerializer = new JavaSerializer();
        JavaBean bean = new JavaBean();
        bean.setId(1);
        bean.setAge(100);
        bean.setName("test");
        bean.setIds(new int[]{1, 2, 3});
        bean.setBean(new TestBean(1));
        byte[] beanBytes = mySerializer.serialize(bean);
        System.out.println("序列化:" + beanBytes.length);
        System.out.println(bean);
        System.out.println(Arrays.toString(beanBytes));
        JavaBean res = mySerializer.deserialize(beanBytes, JavaBean.class);
        System.out.println("反序列化");
        System.out.println(res);
        System.out.println("----------------------Netty-----------------");
        mySerializer = new NettySerializer();
        NettyBean nettyBean = new NettyBean();
        nettyBean.setId(1);
        nettyBean.setAge(100);
        nettyBean.setName("test");
        nettyBean.setIds(new int[]{1, 2, 3});
        nettyBean.setMoney(50.55);
        nettyBean.setBean(new TestBean(1));
        beanBytes = mySerializer.serialize(nettyBean);
        System.out.println("序列化:" + beanBytes.length);
        System.out.println(nettyBean);
        System.out.println(Arrays.toString(beanBytes));
        NettyBean resBean = mySerializer.deserialize(beanBytes, NettyBean.class);
        System.out.println("反序列化");
        System.out.println(resBean);
    }
}
