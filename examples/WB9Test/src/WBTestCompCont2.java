import java.lang.reflect.Method;
import java.util.Random;

import sun.hotspot.WhiteBox;

public class WBTestCompCont2 {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Random RANDOM = new Random();

    public static int foo() {
        return RANDOM.nextInt();
    }

    public static void main(String[] args) throws Exception {

        Method m = WBTestCompCont2.class.getMethod("foo");
        int[] levels = { 1, 2, 3, 4 };

        // Blocking compilations on all levels, using the default versions of
        // WB.enqueueMethodForCompilation() and manually setting compiler directives.
        String directive = "[{ match: \"WBTestCompCont2.foo\", BackgroundCompilation: false }]";
        if (WB.addCompilerDirective(directive) != 1) {
            throw new Exception("Failed to add compiler directive");
        }

        try {
            for (int l : levels) {
                // Make uncompiled
                WB.deoptimizeMethod(m);

                // Verify that it's not compiled
                if (WB.isMethodCompiled(m)) {
                    throw new Exception("Should not be compiled after deoptimization");
                }
                if (WB.isMethodQueuedForCompilation(m)) {
                    throw new Exception("Should not be enqueued on any level");
                }

                // Add to queue and verify that it went well
                if (!WB.enqueueMethodForCompilation(m, l)) {
                    throw new Exception("Could not be enqueued for compilation");
                }

                // Verify that it is compiled
                if (!WB.isMethodCompiled(m)) {
                    throw new Exception("Must be compiled here");
                }
                // And verify the level
                if (WB.getMethodCompilationLevel(m) != l) {
                    String msg = m + " should be compiled at level " + l +
                                 "(but is actually compiled at level " +
                                 WB.getMethodCompilationLevel(m) + ")";
                    System.out.println("==> " + msg);
                    throw new Exception(msg);
                }
                System.out.println(m + " compiled at level " + WB.getMethodCompilationLevel(m));
            }
        } finally {
            WB.removeCompilerDirective(1);
        }
    }

}
