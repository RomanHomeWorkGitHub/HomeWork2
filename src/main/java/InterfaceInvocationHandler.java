import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Роман on 03.03.2024
 **/

class InterfaceInvocationHandler<T> implements InvocationHandler {

    Logger log = LoggerFactory.getLogger(InterfaceInvocationHandler.class);

    private T object;
    private StateObject memento;
    private Map<Method, Object> cache = new HashMap<>();
    public static final ConcurrentMap<StateObject, Map<Method, Object>> concurrentHashMap = new ConcurrentHashMap<>();
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public InterfaceInvocationHandler(T object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        Map<Method, Object> value;
        Method objectMethod = object.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (objectMethod.isAnnotationPresent(Cache.class)) {
            log.info("objectMethod.isAnnotationPresent(Cache.class) - Thread - {}.", Thread.currentThread().getName());
            long timeout = objectMethod.getAnnotation(Cache.class).value();
            log.info("long timeout = objectMethod.getAnnotation(Cache.class).value() - Thread - {}.", Thread.currentThread().getName());
            checkOrCreateStateObject();
            log.info("checkOrCreateStateObject() - Thread - {}.", Thread.currentThread().getName());
            value = concurrentHashMap.get(memento);
            if (value != null) {
                log.info("value != null - Thread - {}.", Thread.currentThread().getName());
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
        for (Field field : fields) {
            str.append(field.getName()).append(": ");
            if (field.trySetAccessible()) {
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
        Future future = null;
        double size = concurrentHashMap.size();
        size /= 16;
        if (size - Math.floor(size) > 0.75) {
            if (future == null || future.isDone()) {
                future = executor.submit(() -> {
                    System.out.println(String.format("checkTimeout(memento, timeout - Thread - %s.", Thread.currentThread().getName()));
                    removeConcurrentHashMap(timeout);
                });
            }
        }
    }

    private void removeConcurrentHashMap(long timeout) {
        for (StateObject s : concurrentHashMap.keySet()) {
            if (checkTimeout(s, timeout)) {
                System.out.println(String.format("Запущен процесс очистки cache - Thread - %s", Thread.currentThread().getName()));
                concurrentHashMap.remove(s);
            }
        }
    }

    private boolean checkTimeout(StateObject stateObject, long timeout) {
        return LocalDateTime.now().minus(timeout, ChronoUnit.MILLIS)
                .isBefore(LocalDateTime.parse(stateObject.getLocalDateTime()));
    }
}
