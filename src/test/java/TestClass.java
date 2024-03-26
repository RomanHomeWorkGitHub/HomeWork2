import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.lang.reflect.Method;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


/**
 * Created by Роман on 09.03.2024
 **/
class TestClass {

    CacheTestClass clazz;
    Cacheable proxy;

    @BeforeEach
    void setUp() {
        clazz = spy(new CacheTestClass(2, 40));
        proxy = CacheUtilsClass.cache(clazz);
    }

    @AfterEach
    void clear() {
        InterfaceInvocationHandler.concurrentHashMap.clear();
    }

    @RepeatedTest(10)
    void cache_ok() {
        proxy.doubleValue();
        StateObject stateObject = InterfaceInvocationHandler.concurrentHashMap.keySet().iterator().next();
        Method method = InterfaceInvocationHandler.concurrentHashMap.get(stateObject).keySet().iterator().next();
        assertEquals(1, InterfaceInvocationHandler.concurrentHashMap.size());
        assertEquals((double) 2/40,
                InterfaceInvocationHandler.concurrentHashMap.get(stateObject).get(method));
        verify(clazz, times(1)).doubleValue();
    }

    @RepeatedTest(10)
    @DisplayName("Не кэшируется метод без аннотации @Cache")
    void cache_methodWithoutAnnotationNotCached() {
        proxy.doubleValue(2, 4);
        assertEquals(0, InterfaceInvocationHandler.concurrentHashMap.size());
        verify(clazz, times(0)).doubleValue();
        verify(clazz, times(1)).doubleValue(anyInt(), anyInt());
        proxy.doubleValue();
        assertEquals(1, InterfaceInvocationHandler.concurrentHashMap.size());
        verify(clazz, times(1)).doubleValue();
    }


    @RepeatedTest(10)
    void cache_resetCacheOk() {
        proxy.doubleValue();
        proxy.doubleValue();
        proxy.setDenum(8);
        proxy.doubleValue();
        proxy.doubleValue();
        proxy.setDenum(40);
        proxy.doubleValue();
        proxy.doubleValue();
        proxy.setDenum(8);
        proxy.doubleValue();
        verify(clazz, times(2)).doubleValue();
    }

    @RepeatedTest(10)
    void cache_recalculationCacheOk() throws InterruptedException {
        proxy.doubleValue();
        proxy.doubleValue();
        proxy.setDenum(8);
        proxy.doubleValue();
        //ждем пока протухнит cache
        sleep(3000);
        proxy.doubleValue();
        verify(clazz, times(3)).doubleValue();
    }

    @RepeatedTest(10)
    @DisplayName("Cache обновляет время")
    void cache_updateLocalTimeStateObjectOk() throws InterruptedException {
        proxy.doubleValue();
        sleep(2000);
        proxy.setDenum(3);
        proxy.setDenum(40);
        proxy.doubleValue();
        //поидее должен протухнуть cache в настройках он 3000 ms
        sleep(2000);
        proxy.doubleValue();
        //но он не протух, вызов метода всего один раз
        verify(clazz, times(1)).doubleValue();
    }

    @RepeatedTest(10)
    @DisplayName("Cache очищается")
    void cache_clearCacheOk() throws InterruptedException {
        for (int i = 1; i < 14; i++) {
            proxy.doubleValue();
            proxy.setDenum(i);
        }
        proxy.doubleValue();
        //ждем пока протухнит cache
        sleep(3000);
        assertEquals(0 , InterfaceInvocationHandler.concurrentHashMap.size());
    }
}
