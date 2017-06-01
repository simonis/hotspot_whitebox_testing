import java.lang.reflect.Method;
import java.util.Random;

import sun.hotspot.WhiteBox;

public class WBTestCompCont {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Random RANDOM = new Random();

    public static int foo() {
        return RANDOM.nextInt();
    }

    public static void main(String[] args) throws Exception {

        int level = 0;
        boolean enqued = false;
        Method m = WBTestCompCont.class.getMethod("foo");

        for (int i = 0; i < 500_000; i++) {
            if (!enqued && WB.isMethodQueuedForCompilation(m)) {
                System.out.println(m + " enqued for compilation in iteration " + i);
                enqued = true;
            }
            if (WB.isMethodCompiled(m)) {
                if (WB.getMethodCompilationLevel(m) != level) {
                    level = WB.getMethodCompilationLevel(m);
                    System.out.println(m + " compiled at level " + level + " in iteration " + i);
                    enqued = false;
                }
            }
        }

        WB.deoptimizeMethod(m);
        WB.enqueueMethodForCompilation(m, 4);
        while (!WB.isMethodCompiled(m)) Thread.sleep(100);
    }

}
