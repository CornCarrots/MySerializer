package serializer;

import annotation.MySerializable;
import annotation.MySerialize;
import base.MySerializer;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linhao
 * @date 2020/5/19 11:25
 * @description: 自定义序列化协议，底层使用Netty的通道
 */
public class NettySerializer implements MySerializer {

    /**
     * 缓存序列化的类和属性
     */
    private static HashMap<Class, List<Field>> classInfoCache;

    private static ByteBuf writeByteBuf;

    private static ByteBuf readByteBuf;

    static {
        writeByteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        classInfoCache = new HashMap<>();
    }

    /**
     * 序列化
     *
     * @param obj
     * @param <T>
     * @return
     */
    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        try {
            // 对象为空
            if (ObjectUtil.isNull(obj)) {
                writeNull();
            } else {
                List<Field> fields = initClass(obj.getClass());
                // 对象没有可序列化的属性
                if (fields.size() == 0) {
                    writeNull();
                } else {
                    writeNotNull();
                    Class[] classes = new Class[fields.size()];
                    Object[] values = new Object[fields.size()];
                    initFields(obj, fields, classes, null, values);
                    // 进行序列化
                    writeAll(obj, classes, values);
                }
            }
//            return writeByteBuf.array();
            int len = writeByteBuf.readableBytes();
            byte[] arr = new byte[len];
            writeByteBuf.getBytes(0, arr);
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writeByteBuf.clear();
        }
        return new byte[0];
    }

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        try {
            // 序列化异常
            if (data.length == 0) {
                return null;
            }
            readByteBuf = Unpooled.copiedBuffer(data);
            byte head = readByte();
            // 序列化的对象为空
            if (head == HEAD_NULL) {
                return null;
            } else if (head == HEAD_NOT_NULL) {
                List<Field> fields = initClass(clazz);
                Class[] classes = new Class[fields.size()];
                String[] keys = new String[fields.size()];
                // 反序列化
                initFields(null, fields, classes, keys, null);
                return readAll(classes, keys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readByteBuf.clear();
        }
        return null;
    }

    /**
     * 初始化 序列化类的所有可序列化属性
     *
     * @param tClass
     * @return
     */
    private List<Field> initClass(Class tClass) {
        try {
            // 可序列化对象
            if (!tClass.isAnnotationPresent(MySerializable.class)) {
                throw new RuntimeException("对象不可序列化");
            }
            // 如果之前有序列化过，直接从缓存拿
            if (classInfoCache.containsKey(tClass)) {
                return classInfoCache.get(tClass);
            }
            // 获取所有字段
            Field[] fields = tClass.getDeclaredFields();
            List<Field> list = Arrays.stream(fields).filter(field -> field.isAnnotationPresent(MySerialize.class)).collect(Collectors.toList());
            classInfoCache.put(tClass, list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CollUtil.newArrayList();
    }

    /**
     * 初始化 可序列化的属性信息
     *
     * @param fields
     * @param classes
     * @param keys
     * @param values
     */
    private <T> void initFields(T obj, List<Field> fields, Class[] classes, String[] keys, Object[] values) {
        try {
            for (Field field : fields) {
                // 获取字段的属性名和属性值
                field.setAccessible(true);
                Class type = field.getType();
                // 序列化的顺序
                MySerialize mySerialize = field.getAnnotation(MySerialize.class);
                int order = mySerialize.order();
                classes[order] = type;
                if (ObjectUtil.isNotNull(keys)) {
                    String name = field.getName();
                    keys[order] = name;
                }
                if (ObjectUtil.isNotNull(values)) {
                    Object value = field.get(obj);
                    values[order] = value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 序列化对象
     *
     * @param obj
     * @param <T>
     */
    private <T> void writeAll(T obj, Class[] classes, Object[] values) {
        try {
            // 写入类信息
            String className = obj.getClass().getName();
            writeStr(className);
            // 写入序列化的属性
            int len = classes.length;
            for (int i = 0; i < len; i++) {
                if (ObjectUtil.isNotNull(classes[i])) {
                    write(classes[i], values[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 序列化属性
     *
     * @param clazz
     * @param value
     */
    private void write(Class clazz, Object value) throws Exception {
        if (clazz == byte.class || clazz == Byte.class) {
            writeByte((Byte) value);
        } else if (clazz == short.class || clazz == Short.class) {
            writeShort((Short) value);
        } else if (clazz == char.class || clazz == Character.class) {
            writeChar((Character) value);
        } else if (clazz == int.class || clazz == Integer.class) {
            writeInt((Integer) value);
        } else if (clazz == long.class || clazz == Long.class) {
            writeLong((Long) value);
        } else if (clazz == float.class || clazz == Float.class) {
            writeFloat((Float) value);
        } else if (clazz == double.class || clazz == Double.class) {
            writeDouble((Double) value);
        } else if (clazz == String.class) {
            writeStr((String) value);
        } else if (clazz.isArray()) {
            writeArray(clazz, value);
        } else if (clazz.isAnnotationPresent(MySerializable.class)) {
            writeByteBuf.writeBytes(serialize(clazz.cast(value)));
        } else {
            writeNull();
        }
    }

    /**
     * 反序列对象
     *
     * @param classes
     * @param keys
     * @param <T>
     * @return
     */
    public <T> T readAll(Class[] classes, String[] keys) {
        try {
            // 反序列化类信息
            String className = readStr();
            Class<T> tClass = (Class<T>) Class.forName(className);
            T res = tClass.newInstance();
            // 反序列属性
            for (int i = 0; i < classes.length; i++) {
                if (classes[i] == null) {
                    continue;
                }
                Class clazz = classes[i];
                Object value = read(clazz);
                Field field = tClass.getDeclaredField(keys[i]);
                field.setAccessible(true);
                field.set(res, value);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反序列属性
     *
     * @param clazz
     * @return
     */
    private Object read(Class clazz) throws Exception {
        Object value = null;
        if (clazz == byte.class || clazz == Byte.class) {
            value = readByte();
        } else if (clazz == short.class || clazz == Short.class) {
            value = readShort();
        } else if (clazz == char.class || clazz == Character.class) {
            value = readCharcter();
        } else if (clazz == int.class || clazz == Integer.class) {
            value = readInt();
        } else if (clazz == long.class || clazz == Long.class) {
            value = readLong();
        } else if (clazz == float.class || clazz == Float.class) {
            value = readFloat();
        } else if (clazz == double.class || clazz == Double.class) {
            value = readDouble();
        } else if (clazz == String.class) {
            value = readStr();
        } else if (clazz.isArray()) {
            value = readArray();
        } else if (clazz.isAnnotationPresent(MySerializable.class)) {
            value = deserialize(readByteBuf.array(), clazz);
        } else {
            writeNull();
        }
        // 修正指针
        readByteBuf.discardReadBytes();
        return value;
    }

    //-----------------------基础类型读写---------------------

    private void writeNull() {
        writeByteBuf.writeByte(HEAD_NULL);
    }

    private void writeNotNull() {
        writeByteBuf.writeByte(HEAD_NOT_NULL);
    }

    private void writeByte(Byte i) {
        writeByteBuf.writeByte(i);
    }

    private void writeShort(Short num) {
        writeByteBuf.writeShort(num);
//        byte[] bytes = Convert.shortToBytes(num);
//        byteBuf.writeBytes(bytes);
    }

    private void writeChar(char character) {
        writeByteBuf.writeChar(character);
//        byte[] bytes = new byte[2];
//        bytes[0] = (byte) (character);
//        bytes[1] = (byte) (character >> 8);
//        byteBuf.writeBytes(bytes);
    }

    private void writeInt(Integer num) {
        writeByteBuf.writeInt(num);
//        byte[] bytes = Convert.intToBytes(num);
//        byteBuf.writeBytes(bytes);
    }

    private void writeLong(Long num) {
        writeByteBuf.writeLong(num);
//        byte[] bytes = Convert.longToBytes(num);
//        byteBuf.writeBytes(bytes);
    }

    private void writeFloat(Float num) {
        writeByteBuf.writeFloat(num);
//        int temp = Float.floatToIntBits(num);
//        byte[] bytes = Convert.intToBytes(temp);
//        byteBuf.writeBytes(bytes);
    }

    private void writeDouble(Double num) {
        writeByteBuf.writeDouble(num);
//        long temp = Double.doubleToLongBits(num);
//        byte[] bytes = Convert.longToBytes(temp);
//        byteBuf.writeBytes(bytes);
    }

    private byte readByte() {
        return readByteBuf.readByte();
    }

    private short readShort() {
        return readByteBuf.readShort();
//        return Convert.bytesToShort(bytes);
    }

    private char readCharcter() {
        return readByteBuf.readChar();
//        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    private int readInt() {
        return readByteBuf.readInt();
//        return Convert.bytesToInt(bytes);
    }

    private long readLong() {
        return readByteBuf.readLong();
//        return Convert.bytesToLong(bytes);
    }

    private float readFloat() {
        return readByteBuf.readFloat();
//        int temp = Convert.bytesToInt(bytes);
//        return Float.intBitsToFloat(temp);
    }

    private double readDouble() {
        return readByteBuf.readDouble();
//        long temp = Convert.bytesToLong(bytes);
//        return Double.longBitsToDouble(temp);
    }

    // ----------------------复杂类型读写---------------------

    private void writeStr(String s) {
        int len = s.length();
        writeInt(len);
        byte[] bytes = s.getBytes();
        writeByteBuf.writeBytes(bytes);
    }

    private String readStr() {
        int len = readByteBuf.readInt();
        if (len == 0) {
            return "";
        }
        byte[] bytes = new byte[len];
        readByteBuf.readBytes(bytes);
        return new String(bytes, charset);
    }

    private void writeArray(Class classes, Object value) throws Exception {
        // 数组的真正类信息
        Class arrClass = classes.getComponentType();
        // 数组的包装类
        Class tempClass = null;
        if (arrClass == byte.class){
            value = ArrayUtils.toObject((byte[]) value);
            tempClass = Byte.class;
        }else if (arrClass == short.class){
            value = ArrayUtils.toObject((short[]) value);
            tempClass = Short.class;
        }else if (arrClass == char.class){
            value = ArrayUtils.toObject((char[]) value);
            tempClass = Character.class;
        } else if (arrClass == int.class){
            value = ArrayUtils.toObject((int[]) value);
            tempClass = Integer.class;
        } else if (arrClass == long.class){
            value = ArrayUtils.toObject((long[]) value);
            arrClass = Long.class;
        } else if (arrClass == float.class){
            value = ArrayUtils.toObject((float[]) value);
            arrClass = Float.class;
        } else if (arrClass == double.class){
            value = ArrayUtils.toObject((double[]) value);
            arrClass = Double.class;
        }
        Object[] array = ArrayUtil.cast(tempClass == null ? arrClass : tempClass, value);
        // 数组长度
        int len = array.length;
        writeInt(len);
        // 数组的类信息
        writeStr(arrClass.getName());
        // 写数组
        for (Object data : array) {
            write(arrClass, data);
        }
    }

    private Object readArray() throws Exception {
        try {
            // 数组长度
            int len = readByteBuf.readInt();
            // 数组的类信息
            String className = readStr();
            // 数组的基础数据类
            Class arrayClass = null;
            Class tempClass = null;
            if ("byte".equals(className)){
                arrayClass = byte.class;
                tempClass = Byte.class;
            } else if ("short".equals(className)){
                arrayClass = short.class;
                tempClass = Short.class;
            } else if ("char".equals(className)){
                arrayClass = char.class;
                tempClass = Character.class;
            } else if ("int".equals(className)){
                arrayClass = int.class;
                tempClass = Integer.class;
            } else if ("long".equals(className)){
                arrayClass = long.class;
                tempClass = Long.class;
            } else if ("float".equals(className)){
                arrayClass = float.class;
                tempClass = Float.class;
            } else if ("double".equals(className)){
                arrayClass = double.class;
                tempClass = Double.class;
            } else {
                arrayClass = Class.forName(className);
            }
            // 读数组
            Object[] array = (Object[]) Array.newInstance(tempClass == null ? arrayClass : tempClass, len);
            for (int i = 0; i < len; i++) {
                array[i] =  read(arrayClass);
            }
            if (arrayClass == byte.class){
                return ArrayUtils.toPrimitive((Byte[]) array);
            } else if (arrayClass == short.class){
                return ArrayUtils.toPrimitive((Short[]) array);
            } else if (arrayClass == char.class){
                return ArrayUtils.toPrimitive((Character[]) array);
            } else if (arrayClass == int.class){
                return ArrayUtils.toPrimitive((Integer[]) array);
            } else if (arrayClass == long.class){
                return ArrayUtils.toPrimitive((Long[]) array);
            } else if (arrayClass == float.class){
                return ArrayUtils.toPrimitive((Float[]) array);
            } else if (arrayClass == double.class){
                return ArrayUtils.toPrimitive((Double[]) array);
            }else {
                return array;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}