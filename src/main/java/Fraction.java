import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Created by Роман on 03.03.2024
 **/
public class Fraction implements Fractionable {
    int num;
    int denum;

    public Fraction(int num, int denum) {
        this.num = num;
        this.denum = denum;
    }

    public int getNum() {
        return num;
    }

    @Override
    @Mutator
    public void setNum(int num) {
        this.num = num;
    }

    public int getDenum() {
        return denum;
    }

    @Override
    @Mutator
    public void setDenum(int denum) {
        this.denum = denum;
    }

    @Override
    @Cache()
    public double doubleValue() {
        System.out.println("Мы чегото порешали!!!");
        return (double) num/denum;
    }
}
