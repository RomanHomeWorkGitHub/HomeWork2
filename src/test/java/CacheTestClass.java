/**
 * Created by Роман on 03.03.2024
 **/
public class CacheTestClass implements Cacheable {
    int num;
    int denum;
    int count;

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
        count++;
    }

    public int getDenum() {
        return denum;
    }

    @Override
    @Mutator
    public void setDenum(int denum) {
        System.out.println("Мы чего-то поменяли в объекте!!!");
        this.denum = denum;
        count++;
    }

    @Override
    @Cache
    public double doubleValue() {
        System.out.println("Мы чего-то порешали!!!");
        count++;
        return (double) num/denum;
    }
}
