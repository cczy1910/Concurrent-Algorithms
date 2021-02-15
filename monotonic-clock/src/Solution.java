import org.jetbrains.annotations.NotNull;

/**
 * В теле класса решения разрешено использовать только финальные переменные типа RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author :TODO: Zhukov Zakhar
 */
public class Solution implements MonotonicClock {
    private final RegularInt c1 = new RegularInt(0);
    private final RegularInt c2 = new RegularInt(0);
    private final RegularInt c3 = new RegularInt(0);
    private final RegularInt t1 = new RegularInt(0);
    private final RegularInt t2 = new RegularInt(0);

    @Override
    public void write(@NotNull Time time) {
        t1.setValue(time.getD1());
        t2.setValue(time.getD2());
        c3.setValue(time.getD3());
        c2.setValue(time.getD2());
        c1.setValue(time.getD1());
    }

    @NotNull
    @Override
    public Time read() {
        int curC1 = c1.getValue();
        int curC2 = c2.getValue();
        int curC3 = c3.getValue();
        int curT2 = t2.getValue();
        int curT1 = t1.getValue();
        if (curC1 != curT1) {
            return new Time(curT1, 0, 0);
        } else {
            if (curC2 != curT2) {
                return new Time(curC1, curT2, 0);
            } else {
                return new Time(curC1, curC2, curC3);
            }
        }
    }
}
