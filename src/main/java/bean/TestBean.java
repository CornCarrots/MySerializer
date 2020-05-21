package bean;

import annotation.MySerializable;
import annotation.MySerialize;

import java.io.Serializable;

/**
 * @author linhao
 * @date 2020/5/18 23:22
 * @description: 测试Bean
 */
@MySerializable
public class TestBean implements Serializable {

    @MySerialize(order = 0)
    private int id;

    public TestBean() {
    }

    public TestBean(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "id=" + id +
                '}';
    }
}
