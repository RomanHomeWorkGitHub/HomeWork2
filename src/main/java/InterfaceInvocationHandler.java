import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Роман on 03.03.2024
 **/

class InterfaceInvocationHandler <T> implements InvocationHandler {

    private T object;
    private Map <Method, Object> cache = new HashMap<>();

    public InterfaceInvocationHandler(T object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        Method objectMethod = object.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (objectMethod.getAnnotation(Cache.class) != null) {
            if (cache.get(objectMethod) != null) {
                return cache.get(objectMethod);
            }
            result = method.invoke(object, args);
            cache.put(objectMethod, result);
            return result;
        }

        if (objectMethod.getAnnotation(Mutator.class) != null) {
            cache.clear();
        }
        return method.invoke(object, args);
    }
}
