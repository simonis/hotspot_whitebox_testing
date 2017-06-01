import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Random;

import sun.hotspot.WhiteBox;
import sun.hotspot.code.NMethod;
import sun.hotspot.cpuinfo.CPUInfo;

public class WBTest {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Random RANDOM = new Random();

    static jdk.internal.misc.Unsafe UNSAFE;
    static {
      try {
        java.lang.reflect.Constructor<jdk.internal.misc.Unsafe> unsafeConstructor =
          jdk.internal.misc.Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        UNSAFE = unsafeConstructor.newInstance();
      } catch (Exception e) {}
    }

    public static int foo() {
        return RANDOM.nextInt();
    }

    public static void frameSize(long callerStack) {
        long remaining = WB.getThreadRemainingStackSize();
        System.out.println("Frame size   = " + (callerStack - remaining));
    }

    public static void main(String[] args) throws Exception {

        System.out.println("CPU features = " + WB.getCPUFeatures());

        System.out.println(CPUInfo.getFeatures());
        System.out.println(CPUInfo.getAdditionalCPUInfo());
        System.out.println("CPU supports sse3 : " + CPUInfo.hasFeature("sse3"));

        System.out.println("Stack size   = " + WB.getThreadStackSize());
        System.out.println("Stack free   = " + WB.getThreadRemainingStackSize());
        frameSize(WB.getThreadRemainingStackSize());

        System.out.println("Heap size:");
        WB.printHeapSizes();
        System.out.println("Oop size = " + WB.getHeapOopSize());
        System.out.println("Max compressed oops heap size = " + WB.getCompressedOopsMaxHeapSize());
        System.out.println("OS page size = " +  WB.getVMPageSize());

        boolean compressedOops = WB.getBooleanVMFlag("UseCompressedOops");
        System.out.println("Using compressed oops = " + compressedOops);
        String hello = "Hello FOSDEM!";
        System.out.format("&hello          = 0x%x\n", WB.getObjectAddress(hello));
        System.out.println("sizeof(String)  = " + WB.getObjectSize(hello));
        byte[] b = (byte[])UNSAFE.getObject(hello, compressedOops ? 12L : 16L);
        System.out.println("sizeof(\"hello\") = " + (WB.getObjectSize(hello) + WB.getObjectSize(b)));


        long sum = 0;
        int level = 0;
        boolean enqued = false;
        Method m = WBTest.class.getMethod("foo");

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
        File tmp = File.createTempFile("assembly", null);
        tmp.deleteOnExit();
        Files.write(Paths.get(tmp.getAbsolutePath()), nm.insts);
        ProcessBuilder pb = new ProcessBuilder("objdump", "-D", "--target=binary", "-mi386", "-Mx86-64", tmp.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (br.ready()) System.out.println(br.readLine());

        WB.deoptimizeMethod(m);
        WB.addCompilerDirective("[{ match: \"WBTest.foo\", BackgroundCompilation: false }]");

        for (int i = 0; i < 50_000; i++) {
            sum += foo();
            WB.enqueueMethodForCompilation(m, 4/*, true block*/);
            if (WB.isMethodCompiled(m)) {
                System.out.println(m + " compiled at level " + WB.getMethodCompilationLevel(m) + " in iteration " + i);
                break;
            }
            else {
                System.out.println(m + " not compiled in iteration " + i + "(ERROR - should be compiled)");
            }
        }
    }
}
