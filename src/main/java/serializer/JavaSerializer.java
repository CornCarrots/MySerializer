package serializer;

import base.MySerializer;

import java.io.*;

/**
 * @author linhao
 * @date 2020/5/18 23:00
 * @description: JDK原生序列化
 */
public class JavaSerializer implements MySerializer {
    @Override
    public <T> byte[] serialize(T obj) throws Exception{
        try {
            // 对象字节数组
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // 对象流
            ObjectOutputStream output = new ObjectOutputStream(stream);
            output.writeObject(obj);
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception{
        try {
            // 对象字节数组
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            // 对象流
            ObjectInputStream input = new ObjectInputStream(stream);
            return (T)input.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
