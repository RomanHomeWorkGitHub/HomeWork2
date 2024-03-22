import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

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
        proxy.doubleValue();
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
        sleep(2000);
        proxy.doubleValue();
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
