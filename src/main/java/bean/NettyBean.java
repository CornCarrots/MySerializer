package bean;

import annotation.MySerializable;
import annotation.MySerialize;

import java.util.Arrays;

/**
 * @author linhao
 * @date 2020/5/21 10:20
 * @description: 使用自定义序列化的Bean
 */
@MySerializable
public class NettyBean {

    @MySerialize(order = 0)
    private int id;

    @MySerialize(order = 1)
    private long age;

    @MySerialize(order = 2)
    private String name;

    @MySerialize(order = 3)
    private int[] ids;

    @MySerialize(order = 4)
    private double money;

    @MySerialize(order = 5)
    private TestBean bean;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getIds() {
        return ids;
    }

    public void setIds(int[] ids) {
        this.ids = ids;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public TestBean getBean() {
        return bean;
    }

    public void setBean(TestBean bean) {
        this.bean = bean;
    }

    @Override
    public String toString() {
        return "NettyBean{" +
                "id=" + id +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", ids=" + Arrays.toString(ids) +
                ", money=" + money +
                ", bean=" + bean +
                '}';
    }
}
