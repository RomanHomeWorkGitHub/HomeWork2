/**
 * Created by Роман on 03.03.2024
 **/
public class CacheTestClass implements Cacheable {
    int num;
    int denum;

    public CacheTestClass(int num, int denum) {
        this.num = num;
        this.denum = denum;
    }

    public int getNum() {
        return num;
    }

    @Override
    @Mutator
    public void setNum(int num) {
        System.out.println("Мы чего-то поменяли в объекте!!!");
        this.num = num;
    }

    public int getDenum() {
        return denum;
    }

    @Override
    @Mutator
    public void setDenum(int denum) {
        System.out.println("Мы чего-то поменяли в объекте!!!");
        this.denum = denum;
    }

    @Override
    @Cache(3000)
    public double doubleValue() {
        System.out.println("Мы чего-то порешали!!!");
        return (double) num/denum;
    }

    public double doubleValue(int num, int denum) {
        System.out.println("Мы чего-то порешали!!!");
        return (double) num/denum;
    }
}
