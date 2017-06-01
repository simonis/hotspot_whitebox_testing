import java.util.Random;

import sun.hotspot.WhiteBox;

public class WBTestStack {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Random RANDOM = new Random();

    public static int foo() {
        return RANDOM.nextInt();
    }

    public static long frameSize(long callerStack) {
        long remaining = WB.getThreadRemainingStackSize();
        return callerStack - remaining;
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Stack size        = " + WB.getThreadStackSize());
        System.out.println("Stack free        = " + WB.getThreadRemainingStackSize());
        System.out.println("Frame size (int)  = " + frameSize(WB.getThreadRemainingStackSize()));
        for (int i = 0; i < 1_000; i++) {
            frameSize(0);
        }
        System.out.println("Frame size (C1)   = " + frameSize(WB.getThreadRemainingStackSize()));
        for (int i = 0; i < 20_000; i++) {
            frameSize(0);
        }
        System.out.println("Frame size (C2)   = " + frameSize(WB.getThreadRemainingStackSize()));
    }

}
