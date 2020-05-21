package bean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author linhao
 * @date 2020/5/19 10:26
 * @description: 使用JDK原生序列化的Bean
 */
public class JavaBean implements Serializable {

    private int id;

    private long age;

    private String name;

    private int[] ids;

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

    public TestBean getBean() {
        return bean;
    }

    public void setBean(TestBean bean) {
        this.bean = bean;
    }

    @Override
    public String toString() {
        return "JavaBean{" +
                "id=" + id +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", ids=" + Arrays.toString(ids) +
                ", bean=" + bean +
                '}';
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
//        out.defaultWriteObject();
        out.writeInt(id);
        out.writeLong(age);
        out.writeUTF(name);
        out.writeObject(ids);
        out.writeObject(bean);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
        id = in.readInt();
        age = in.readLong();
        name = in.readUTF();
        ids = (int[]) in.readObject();
        bean = (TestBean) in.readObject();
    }
}
