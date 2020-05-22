# MySerializer
手写一个基于Netty实现的序列化器
- 使用自定义注解：表示可序列化的类及其属性，对于每个属性，需要通过注解来表明序列化的顺序。
- 使用本地缓存，来记录序列化过的类信息，对于涉及公共类的序列化能提高一定速度
- 使用Netty的 ByteBuf 缓冲区：优化JDK原生序列化使用的对象流(ObjectInputStream/ObjectOutputStream)，ByteBuf支持同时读写，并且支持动态扩展缓冲区

### JDK原生序列化协议和自定义序列化协议的比较
#### 空间（从传输的字节数组看信息密度）
- 使用堆外内存序列化：自定义序列化器减小约76%（205/609）
- 使用堆上内存序列化：自定义序列化器减小约68%（256/609）

由此可以发现，自定义序列化器极大压缩了信息传输的内容
#### 时间（从序列化时间看序列化速度）
- 序列化：两者序列化时间大体一致
- 反序列化：堆外内存时间减小了80%（37/68），堆上内存时间复杂度为接近O(1)

由此可以发现，自定义序列化器极大提高了信息传输的速度