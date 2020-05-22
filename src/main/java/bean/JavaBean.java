package bean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private double money;

    private TestBean bean;

    private List list;

    private Set set;

    private Map map;

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

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "JavaBean{" +
                "id=" + id +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", ids=" + Arrays.toString(ids) +
                ", money=" + money +
                ", bean=" + bean +
                ", list=" + list +
                ", set=" + set +
                ", map=" + map +
                '}';
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
//        out.defaultWriteObject();
        out.writeInt(id);
        out.writeLong(age);
        out.writeUTF(name);
        out.writeObject(ids);
        out.writeDouble(money);
        out.writeObject(bean);
        out.writeObject(list);
        out.writeObject(set);
        out.writeObject(map);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
        id = in.readInt();
        age = in.readLong();
        name = in.readUTF();
        ids = (int[]) in.readObject();
        money = in.readDouble();
        bean = (TestBean) in.readObject();
        list = (List) in.readObject();
        set = (Set) in.readObject();
        map = (Map) in.readObject();
    }
}
