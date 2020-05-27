package serializer;

import annotation.MySerializable;
import annotation.MySerialize;
import base.MySerializer;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.sun.corba.se.impl.transport.ByteBufferPoolImpl;
import com.sun.corba.se.pept.transport.ByteBufferPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linhao
 * @date 2020/5/19 11:25
 * @description: 自定义序列化协议，底层使用Netty的通道
 */
public class NettySerializer implements MySerializer {
    private Logger logger = LoggerFactory.getLogger(NettySerializer.class);
    /**
     * 缓存序列化的类和属性
     */
    private static HashMap<Class, List<Field>> classInfoCache;

    private ByteBuf writeByteBuf;

    private ByteBuf readByteBuf;

    static {
        classInfoCache = new HashMap<>();
    }

    /**
     * 序列化多个对象
     * @param params
     * @return
     * @throws Exception
     */
    public byte[] serialize(Object...params) throws Exception {
        try {
            if (writeByteBuf == null){
                // 堆
                // writeByteBuf = Unpooled.buffer();
                // 堆外内存
                writeByteBuf = PooledByteBufAllocator.DEFAULT.buffer();
            }
            // 对象数量
            short size = (short) params.length;
            writeByteBuf.writeShort(size);
            logger.info("[serialize] num：{}", size);
            // 逐个序列化
            for (int i = 0; i < params.length; i++) {
                write(params[i]);
            }
//             return writeByteBuf.array();
            int len = writeByteBuf.readableBytes();
            byte[] arr = new byte[len];
            writeByteBuf.getBytes(0, arr);

            logger.info("[serialize] success! byte length:{}",len);
            return arr;
        }catch (Exception e){
            logger.error("[serialize] fail!");
            throw e;
        }finally {
            // 清除
            if (writeByteBuf != null) {
                writeByteBuf.clear();
            }
        }
    }

    /**
     * 反序列化
     * @param data 字节数组
     * @return 对象列表
     * @throws Exception
     */
    public List deserialize(byte[] data) throws Exception {
        List res = new ArrayList();
        int len = ArrayUtil.length(data);
        logger.info("[deserialize] byte length:{}", len);
        try {
            if (len == 0) {
                return null;
            }
            if (readByteBuf == null || readByteBuf.readableBytes() == 0) {
                readByteBuf =  Unpooled.copiedBuffer(data);
            }
            short size = readByteBuf.readShort();
            for (int i = 0; i < size; i++) {
                Object obj = read();
                res.add(obj);
            }
            logger.info("[deserialize] success! object size:{}", res.size());
            return res;
        }catch (Exception e){
            logger.error("[deserialize] fail!");
            throw e;
        }finally {
            if (readByteBuf != null && readByteBuf.readableBytes() == 0) {
                readByteBuf = null;
            }
        }
    }

    /**
     * 初始化 序列化类的所有可序列化属性
     *
     * @param tClass
     * @return
     */
    private List<Field> initClass(Class tClass) {
        // 可序列化对象
        if (!tClass.isAnnotationPresent(MySerializable.class)) {
            logger.info("[init class] fail! class {} don't have annotation MySerializable", tClass);
            return CollUtil.newArrayList();
        }
        // 如果之前有序列化过，直接从缓存拿
        if (classInfoCache.containsKey(tClass)) {
            return classInfoCache.get(tClass);
        }
        // 获取所有字段
        Field[] fields = tClass.getDeclaredFields();
        List<Field> list = Arrays.stream(fields).filter(field -> field.isAnnotationPresent(MySerialize.class)).collect(Collectors.toList());
        logger.info("[init class] success! class:{}, property size:{}", tClass, list.size());
        classInfoCache.put(tClass, list);
        return list;
    }

    /**
     * 初始化 可序列化的属性信息
     *
     * @param fields
//     * @param classes
     * @param keys
     * @param values
     */
    private <T> void initFields(T obj, List<Field> fields, String[] keys, Object[] values) throws Exception {
        try {
            for (Field field : fields) {
                // 获取字段的属性名和属性值
                field.setAccessible(true);
                // 序列化的顺序
                MySerialize mySerialize = field.getAnnotation(MySerialize.class);
                int order = mySerialize.order();
                if (ObjectUtil.isNotNull(keys)) {
                    String name = field.getName();
                    keys[order] = name;
                }
                if (ObjectUtil.isNotNull(values)) {
                    Object value = field.get(obj);
                    values[order] = value;
                }
            }
        }catch (Exception e){
            logger.error("[init field] fail!", e);
            throw e;
        }
    }

    /**
     * 序列化对象(非集合型)
     */
    private <T> void write(T obj) throws Exception {
        write(obj, null);
    }

    /**
     * 序列化对象(集合型)
     * @param obj 对象值
     * @param <T> 对象类型
     * @throws Exception
     */
    private <T> void write(T obj, Class<T> clazz) throws Exception {
        // 对象为空
        if (ObjectUtil.isNull(obj)) {
            writeNull();
            return;
        }
        // 对象不为空
        writeNotNull();
        if (clazz == null){
            clazz = (Class<T>) obj.getClass();
            writeStr(clazz.getName());
        }
        // 判断类型
        if (clazz == byte.class || clazz == Byte.class) {
            writeByte((Byte) obj);
        } else if (clazz == short.class || clazz == Short.class) {
            writeShort((Short) obj);
        } else if (clazz == char.class || clazz == Character.class) {
            writeChar((Character) obj);
        } else if (clazz == int.class || clazz == Integer.class) {
            writeInt((Integer) obj);
        } else if (clazz == long.class || clazz == Long.class) {
            writeLong((Long) obj);
        } else if (clazz == float.class || clazz == Float.class) {
            writeFloat((Float) obj);
        } else if (clazz == double.class || clazz == Double.class) {
            writeDouble((Double) obj);
        } else if (clazz == String.class) {
            writeStr((String) obj);
        } else if (clazz.isArray()) {
            writeArray(clazz, obj);
        } else if (clazz == List.class || List.class.isAssignableFrom(clazz)) {
            writeList(clazz, obj);
        } else if (clazz == Set.class || Set.class.isAssignableFrom(clazz)) {
            writeSet(clazz, obj);
        } else if (clazz == Map.class || Map.class.isAssignableFrom(clazz)) {
            writeMap(clazz, obj);
        }
        // 复杂对象
        else {
            List<Field> fields = initClass(clazz);
            // 对象没有可序列化的属性
            if (fields.size() == 0) {
                writeNull();
            } else {
                // 序列化属性
                writeNotNull();
                Object[] values = new Object[fields.size()];
                initFields(obj, fields, null, values);
                for (int i = 0; i < values.length; i++) {
                    write(values[i]);
                }
            }
        }
    }

    /**
     * 反序列化(非集合型对象)
     */
    private <T> Object read() throws Exception {
        return read(null);
    }

    /**
     * 反序列化(集合型对象)
     * @param <T> 对象类型
     * @return 对象
     * @throws Exception
     */
    private <T> Object read(Class<T> clazz) throws Exception {
        Object value = null;
        byte head = readByte();
        // 序列化的对象为空
        if (head == HEAD_NULL) {
            return null;
        }
        // 对象不为空
        if (head == HEAD_NOT_NULL) {
            if (clazz == null) {
                String className = readStr();
                clazz = (Class<T>) Class.forName(className);
            }
            // 判断类型
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
            } else if (clazz == List.class || List.class.isAssignableFrom(clazz)) {
                value = readList();
            } else if (clazz == Set.class || Set.class.isAssignableFrom(clazz)) {
                value = readSet();
            } else if (clazz == Map.class || Map.class.isAssignableFrom(clazz)) {
                value = readMap();
            }
            // 复杂类型
            else {
                byte objHead = readByte();
                if (objHead == HEAD_NOT_NULL) {
                    value = clazz.newInstance();
                    List<Field> fields = initClass(clazz);
                    String[] keys = new String[fields.size()];
                    // 反序列化
                    initFields(null, fields, keys, null);
                    // 反序列属性
                    for (int i = 0; i < keys.length; i++) {
                        Object paramValue = read();
                        Field field = clazz.getDeclaredField(keys[i]);
                        field.setAccessible(true);
                        field.set(value, paramValue);
                    }
                }
            }
        }
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
    }

    private void writeChar(char character) {
        writeByteBuf.writeChar(character);
    }

    private void writeInt(Integer num) {
        writeByteBuf.writeInt(num);
    }

    private void writeLong(Long num) {
        writeByteBuf.writeLong(num);
    }

    private void writeFloat(Float num) {
        writeByteBuf.writeFloat(num);
    }

    private void writeDouble(Double num) {
        writeByteBuf.writeDouble(num);
    }

    private byte readByte() {
        return readByteBuf.readByte();
    }

    private short readShort() {
        return readByteBuf.readShort();
    }

    private char readCharcter() {
        return readByteBuf.readChar();
    }

    private int readInt() {
        return readByteBuf.readInt();
    }

    private long readLong() {
        return readByteBuf.readLong();
    }

    private float readFloat() {
        return readByteBuf.readFloat();
    }

    private double readDouble() {
        return readByteBuf.readDouble();
    }

    // ----------------------复杂类型读写---------------------

    /**
     * 写字符串
     * @param s
     */
    private void writeStr(String s) {
        try {
            if (StrUtil.isEmpty(s)) {
                writeShort((short) 0);
                return;
            }
            // 当前指针
            int index = writeByteBuf.markWriterIndex().writerIndex();
            // 写入暂时长度
            writeShort((short) 0);
            // 写入编码UTF8的字符串
            int len = ByteBufUtil.writeUtf8(writeByteBuf, s);
            // 修正长度
            writeByteBuf.resetWriterIndex();
            writeShort((short) len);
            // 修正指针
            writeByteBuf.writerIndex(index + Short.BYTES + len + 1);
        }catch (Exception e){
            logger.error("[write string] fail!", e);
            throw e;
        }
    }

    /**
     * 读字符串
     * @return
     */
    private String readStr(){
        try {
            // 当前指针
            int index = readByteBuf.readerIndex();
            // 读取长度
            short len = readShort();
            if (len == 0) {
                return "";
            }
            // 读取字符串
            CharSequence charSequence = readByteBuf.readCharSequence(len, StandardCharsets.UTF_8);
            // 修正指针
            readByteBuf.readerIndex(index + Short.BYTES + len + 1);
            return charSequence.toString();
        } catch (Exception e) {
            logger.error("[read string] fail!", e);
            throw e;
        }
    }

    /**
     * 写数组
     * @param classes
     * @param value
     * @throws Exception
     */
    private void writeArray(Class classes, Object value) throws Exception {
        try {
            if (value == null) {
                writeShort((short) 0);
                return;
            }
            // 数组的真正类信息
            Class arrClass = classes.getComponentType();
            // 数组的包装类
            Class tempClass = null;
            if (arrClass == byte.class) {
                value = ArrayUtils.toObject((byte[]) value);
                tempClass = Byte.class;
            } else if (arrClass == short.class) {
                value = ArrayUtils.toObject((short[]) value);
                tempClass = Short.class;
            } else if (arrClass == char.class) {
                value = ArrayUtils.toObject((char[]) value);
                tempClass = Character.class;
            } else if (arrClass == int.class) {
                value = ArrayUtils.toObject((int[]) value);
                tempClass = Integer.class;
            } else if (arrClass == long.class) {
                value = ArrayUtils.toObject((long[]) value);
                arrClass = Long.class;
            } else if (arrClass == float.class) {
                value = ArrayUtils.toObject((float[]) value);
                arrClass = Float.class;
            } else if (arrClass == double.class) {
                value = ArrayUtils.toObject((double[]) value);
                arrClass = Double.class;
            }
            Object[] array = ArrayUtil.cast(tempClass == null ? arrClass : tempClass, value);
            // 数组长度
            int len = array.length;
            writeShort((short) len);
            // 数组的类信息
            writeStr(arrClass.getName());
            // 写数组
            for (Object data : array) {
                write(data, arrClass);
            }
        }catch (Exception e){
            logger.error("[write array] fail!", e);
            throw e;
        }
    }

    /**
     * 读数组
     * @return
     * @throws Exception
     */
    private Object readArray() throws Exception {
        try {
            // 数组长度
            short len = readShort();
            if (len == 0) {
                return null;
            }
            // 数组的类信息
            String className = readStr();
            // 数组的基础数据类
            Class arrayClass;
            // 数组的包装类
            Class tempClass = null;
            if (byte.class.getName().equals(className)) {
                arrayClass = byte.class;
                tempClass = Byte.class;
            } else if (short.class.getName().equals(className)) {
                arrayClass = short.class;
                tempClass = Short.class;
            } else if (char.class.getName().equals(className)) {
                arrayClass = char.class;
                tempClass = Character.class;
            } else if (int.class.getName().equals(className)) {
                arrayClass = int.class;
                tempClass = Integer.class;
            } else if (long.class.getName().equals(className)) {
                arrayClass = long.class;
                tempClass = Long.class;
            } else if (float.class.getName().equals(className)) {
                arrayClass = float.class;
                tempClass = Float.class;
            } else if (double.class.getName().equals(className)) {
                arrayClass = double.class;
                tempClass = Double.class;
            } else {
                arrayClass = Class.forName(className);
            }
            // 读数组
            Object[] array = (Object[]) Array.newInstance(tempClass == null ? arrayClass : tempClass, len);
            for (int i = 0; i < len; i++) {
                array[i] = read(arrayClass);
            }
            // 包装
            if (arrayClass == byte.class) {
                return ArrayUtils.toPrimitive((Byte[]) array);
            } else if (arrayClass == short.class) {
                return ArrayUtils.toPrimitive((Short[]) array);
            } else if (arrayClass == char.class) {
                return ArrayUtils.toPrimitive((Character[]) array);
            } else if (arrayClass == int.class) {
                return ArrayUtils.toPrimitive((Integer[]) array);
            } else if (arrayClass == long.class) {
                return ArrayUtils.toPrimitive((Long[]) array);
            } else if (arrayClass == float.class) {
                return ArrayUtils.toPrimitive((Float[]) array);
            } else if (arrayClass == double.class) {
                return ArrayUtils.toPrimitive((Double[]) array);
            } else {
                return array;
            }
        } catch (Exception e) {
            logger.error("[write array] fail!", e);
            throw e;
        }
    }

    private <T> void writeList(Class cla, Object value) throws Exception {
        try {
            List<T> list = (List<T>) value;
            if (CollUtil.isEmpty(list)) {
                writeShort((short) 0);
                return;
            }
            // 链表长度
            int size = list.size();
            writeShort((short) size);
            // 链表的类信息
            Class<T> tClass = (Class<T>) list.get(0).getClass();
            writeStr(tClass.getName());
            // 链表数据
            for (T data : list) {
                write(data, tClass);
            }
        } catch (Exception e) {
            logger.error("[write list] fail!", e);
            throw e;
        }
    }

    private <T> List<T> readList() throws Exception {
        try {
            // 链表长度
            short size = readShort();
            if (size == 0) {
                return null;
            }
            List<T> list = CollUtil.newArrayList();
            // 链表的类信息
            String className = readStr();
            Class<T> clazz = (Class<T>) Class.forName(className);
            // 链表的数据
            for (int i = 0; i < size; i++) {
                list.add((T) read(clazz));
            }
            return list;
        } catch (Exception e) {
            logger.error("[read list] fail!", e);
            throw e;
        }
    }

    private <T> void writeSet(Class cla, Object value) throws Exception {
        try {
            Set<T> set = (Set<T>) value;
            if (CollUtil.isEmpty(set)) {
                writeShort((short) 0);
                return;
            }
            // 集合长度
            int size = set.size();
            writeShort((short) size);
            // 集合类型
            Iterator<T> iterator = set.iterator();
            // 集合的类信息
            Class<T> tClass = null;
            // 集合数据
            for (Iterator<T> it = iterator; it.hasNext(); ) {
                T data = it.next();
                if (tClass == null) {
                    tClass = (Class<T>) data.getClass();
                    writeStr(tClass.getName());
                }
                write(data, tClass);
            }
        } catch (Exception e) {
            logger.error("[write set] fail!", e);
            throw e;
        }
    }

    private <T> Set<T> readSet() throws Exception {
        try {
            // 集合长度
            short size = readShort();
            if (size == 0) {
                return null;
            }
            Set<T> set = CollUtil.newHashSet();
            // 集合的类信息
            String className = readStr();
            Class clazz = Class.forName(className);
            // 集合的数据
            for (int i = 0; i < size; i++) {
                set.add((T) read(clazz));
            }
            return set;
        } catch (Exception e) {
            logger.error("[read set] fail!", e);
            throw e;
        }
    }

    private <K, V> void writeMap(Class cla, Object value) throws Exception {
        try {
            Map<K, V> map = (Map<K, V>) value;
            // 映射数量
            if (CollUtil.isEmpty(map)) {
                writeShort((short) 0);
                return;
            }
            int size = map.size();
            writeShort((short) size);
            // key集合
            Set<K> keySet = map.keySet();
            Class keySetClass = map.keySet().getClass();
            writeSet(keySetClass, keySet);
            // value类型
            Class<V> vClass = null;
            // 写入key-value
            for (Iterator<K> it = keySet.iterator(); it.hasNext(); ) {
                K key = it.next();
                V val = map.get(key);
                if (vClass == null) {
                    vClass = (Class<V>) val.getClass();
                    String className = vClass.getName();
                    writeStr(className);
                }
                write(val, cla);
            }
        } catch (Exception e) {
            logger.error("[write map] fail!", e);
            throw e;
        }
    }

    private <K, V> Map<K, V> readMap() throws Exception {
        try {
            // 映射数量
            short size = readShort();
            if (size == 0) {
                return null;
            }
            Map<K, V> map = CollUtil.newHashMap();
            // key集合
            Set<K> keySet = readSet();
            if (keySet == null) {
                return null;
            }
            // value类型
            String className = readStr();
            Class<V> vClass = (Class<V>) Class.forName(className);
            // 获取key-value
            for (Iterator<K> it = keySet.iterator(); it.hasNext(); ) {
                K key = it.next();
                V val = (V) read(vClass);
                map.put(key, val);
            }
            return map;
        } catch (Exception e) {
            logger.error("[read map] fail!", e);
            throw e;
        }
    }
}