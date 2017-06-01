import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Random;

import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;

import sun.hotspot.WhiteBox;
import sun.hotspot.code.NMethod;

/*
 * Compile with: javac -XaddExports:jdk.vm.ci/jdk.vm.ci.code=ALL-UNNAMED -XaddExports:jdk.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED -d my_bin/ -cp /share/output-jdk9-hs-comp-dbg/support/test/lib/wb.jar src/JvmciTest.java
 * Run with: java -XaddExports:jdk.vm.ci/jdk.vm.ci.code=ALL-UNNAMED -XaddExports:jdk.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED -cp my_bin -Xbootclasspath/a:/share/output-jdk9-hs-comp-dbg/support/test/lib/wb.jar -XX:+WhiteBoxAPI -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI JvmciTest
 */

public class JvmciTest {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Random RANDOM = new Random();

    public static int foo() {
        return RANDOM.nextInt();
    }

    public static void main(String[] args) throws Exception {
        long sum = 0;
        int level = 0;
        boolean enqued = false;
        Method m = JvmciTest.class.getMethod("foo");

        for (int i = 0; i < 50_000; i++) {
            sum += foo();
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

        NMethod nm = NMethod.get(m, false);
        System.out.println(nm);

        InstalledCode ic = new NMethodInstalledCode(m.getName(), nm);
        CompilerToVM ctwm = (CompilerToVM)Proxy.newProxyInstance(CompilerToVM.class.getClassLoader(),
                                                                 new Class<?>[] { CompilerToVM.class },
                                                                 new CompilerToVMHelper());
        System.out.println(ctwm.disassembleCodeBlob(ic));
    }

    static class NMethodInstalledCode extends InstalledCode {
        public NMethodInstalledCode(String name, NMethod m) {
            super(name);
            this.address = m.address;
            this.entryPoint = m.entry_point;
        }
    }
    static interface CompilerToVM {
        public String disassembleCodeBlob(InstalledCode ic);
    }

    static class CompilerToVMHelper implements InvocationHandler {
        // The package private class jdk.vm.ci.hotspot.CompilerToVM
        final static Object ctvm = HotSpotJVMCIRuntime.runtime().getCompilerToVM();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println(ctvm.getClass());
            System.out.println(method.getName());
            Method m = ctvm.getClass().getDeclaredMethod(method.getName(), InstalledCode.class);
            m.setAccessible(true);
            return m.invoke(ctvm, args);
        }
    }
}
