import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by Роман on 09.03.2024
 **/
class TestClass {

    CacheTestClass clazz = new CacheTestClass(2, 4);
    Cacheable proxy = CacheUtilsClass.cache(clazz);

    @Test
    void cache_ok() {
        proxy.doubleValue();
        proxy.doubleValue();
        assertEquals(1, clazz.count);
    }

    @Test
    void cache_resetCacheOk() {
        proxy.doubleValue();
        proxy.doubleValue();
        proxy.setDenum(8);
        assertEquals(2, clazz.count);
    }

    @Test
    void cache_recalculationCacheOk() {
        proxy.doubleValue();
        proxy.doubleValue();
        proxy.setDenum(8);
        proxy.doubleValue();
        assertEquals(3, clazz.count);
    }
}
