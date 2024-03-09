import java.lang.reflect.Proxy;

/**
 * Created by Роман on 05.03.2024
 **/


public class CacheUtilsClass {

    public static <T> T cache(T object) {
        return  (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                new InterfaceInvocationHandler(object));
    }
}
