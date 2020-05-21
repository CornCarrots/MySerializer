package annotation;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: linhao
 * @date: 2020/05/19/17:37
 * @description: 可序列化的属性
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MySerialize {
    /**
     * 序列化顺序，以0开始
     * @return 顺序
     */
    int order();
}
