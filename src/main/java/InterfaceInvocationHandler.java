import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Роман on 03.03.2024
 **/

class InterfaceInvocationHandler<T> implements InvocationHandler {

    Logger log = LoggerFactory.getLogger(InvocationHandler.class);

    private T object;
    private StateObject memento;
    private Map<Method, Object> cache = new HashMap<>();
    public static final ConcurrentMap<StateObject, Map<Method, Object>> concurrentHashMap = new ConcurrentHashMap<>();

    public InterfaceInvocationHandler(T object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        Method objectMethod = object.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (objectMethod.isAnnotationPresent(Cache.class)) {
            log.info("objectMethod.isAnnotationPresent(Cache.class) - Thread - {}.", Thread.currentThread().getName());
            long timeout = objectMethod.getAnnotation(Cache.class).value();
            log.info("long timeout = objectMethod.getAnnotation(Cache.class).value() - Thread - {}.", Thread.currentThread().getName());
            checkOrCreateStateObject();
            log.info("checkOrCreateStateObject() - Thread - {}.", Thread.currentThread().getName());
            if (concurrentHashMap.size() > 0 && concurrentHashMap.containsKey(memento)) {
                log.info("concurrentHashMap.size() > 0 && concurrentHashMap.containsKey(memento) - Thread - {}.", Thread.currentThread().getName());
                if (checkTimeout(memento, timeout)) {
                    log.info("checkTimeout(memento, timeout - Thread - {}.", Thread.currentThread().getName());
                    memento.setLocalDateTime();
                    return cache.get(objectMethod);
                }
            }
            result = method.invoke(object, args);
            cache.put(objectMethod, result);
            startCleanConcurrentHashMap(timeout);
            concurrentHashMap.put(memento, cache);
            return result;
        }
        return method.invoke(object, args);
    }

    private void checkOrCreateStateObject() {
        String state = createFields();
        boolean flag = false;
        if (memento != null) {
           flag = memento.getState().equals(state) || concurrentHashMap.keySet().stream().anyMatch(s -> s.getState().equals(state));
        }
        if (!flag) {
            memento = new StateObject(state);
        }
    }

    private String createFields() {
        StringBuilder str = new StringBuilder();
        Field[] fields = object.getClass().getDeclaredFields();
        for(Field field : fields) {
            str.append(field.getName()).append(": ");
            if(field.trySetAccessible()) {
                try {
                    str.append(field.get(object).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else str.append("null");
            str.append(";");
        }
        return str.toString();
    }

    private void startCleanConcurrentHashMap(long timeout) {
        double size = concurrentHashMap.size();
        size /= 16;
        if (size - Math.floor(size) > 0.75) {
            Thread thread = new Thread(() -> removeConcurrentHashMap(timeout));
            thread.start();
        }
    }

    private void removeConcurrentHashMap(long timeout) {
        for (StateObject s : concurrentHashMap.keySet()) {
            if (checkTimeout(s, timeout)) {
                System.out.println("Запущен процесс очистки cache");
                concurrentHashMap.remove(s);
            }
        }
    }

    private boolean checkTimeout(StateObject stateObject, long timeout) {
        return LocalDateTime.now().minus(timeout, ChronoUnit.MILLIS)
                .isBefore(LocalDateTime.parse(stateObject.getLocalDateTime()));
    }
}
