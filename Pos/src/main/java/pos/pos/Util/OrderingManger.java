package pos.pos.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class OrderingManger {

    public static final BigDecimal STEP = new BigDecimal("1000");
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final int SCALE = 6;
    public static final BigDecimal EPS = new BigDecimal("0.000001");

    private OrderingManger() {}

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

    public static int toZeroBasedForInsert(Integer requested1Based, long count) {
        int req = (requested1Based == null) ? (int) (count + 1) : requested1Based;
        return toZeroBasedForMove(req, count);
    }

    public static int toZeroBasedForMove(int newSortOrder1Based, long total) {
        int pos1 = clamp(newSortOrder1Based, 1, (int) total + 1);
        return pos1 - 1;
    }

    public static BigDecimal mid(BigDecimal a, BigDecimal b) {
        return a.add(b).divide(new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);
    }

    public static boolean tooClose(BigDecimal left, BigDecimal right) {
        return right.subtract(left).compareTo(EPS) <= 0;
    }

    public static BigDecimal computeInsertKeyDecimal(
            Integer target1Based,
            long count,
            Supplier<BigDecimal> firstKey,
            Supplier<BigDecimal> lastKey,
            IntFunction<BigDecimal> nthKey,
            Runnable rebalanceIfNeeded
    ) {
        if (count == 0) return STEP;
        int pos = toZeroBasedForInsert(target1Based, count);
        if (pos == 0) {
            return firstKey.get().subtract(ONE);
        } else if (pos == count) {
            return lastKey.get().add(STEP);
        } else {
            return getBigDecimal(nthKey, rebalanceIfNeeded, pos);
        }
    }

    private static BigDecimal getBigDecimal(IntFunction<BigDecimal> nthKey, Runnable rebalanceIfNeeded, int pos) {
        BigDecimal left = nthKey.apply(pos - 1);
        BigDecimal right = nthKey.apply(pos);
        if (tooClose(left, right)) {
            rebalanceIfNeeded.run();
            left = nthKey.apply(pos - 1);
            right = nthKey.apply(pos);
        }
        return mid(left, right);
    }

    public static BigDecimal computeMoveKeyDecimal(
            int newSortOrder1Based,
            long total,
            Supplier<BigDecimal> firstExcludingKey,
            Supplier<BigDecimal> lastKey,
            IntFunction<BigDecimal> nthExcludingKey,
            Runnable rebalanceIfNeeded,
            BigDecimal currentKey
    ) {
        if (total == 0) return STEP;
        int pos = toZeroBasedForMove(newSortOrder1Based, total);
        if (pos == 0) {
            return firstExcludingKey.get().subtract(ONE);
        } else if (pos == total) {
            BigDecimal last = lastKey.get();
            if (currentKey != null && last.compareTo(currentKey) == 0) return last;
            return last.add(STEP);
        } else {
            return getBigDecimal(nthExcludingKey, rebalanceIfNeeded, pos);
        }
    }
}
