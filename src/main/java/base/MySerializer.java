package base;

import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: linhao
 * @date: 2020/05/18/22:50
 * @description: 自定义序列化协议的接口
 */
public interface MySerializer {

    Charset charset = CharsetUtil.UTF_8;

    Byte HEAD_NULL = -128;

    Byte HEAD_NOT_NULL = 127;

//    <T> byte[] serialize(T obj) throws Exception;

//    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;
}
