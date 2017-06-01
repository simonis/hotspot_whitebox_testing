import java.lang.reflect.Field;

import sun.hotspot.WhiteBox;
import jdk.internal.misc.Unsafe;

/*
 * Compile with: javac -XaddExports:java.base/jdk.internal.misc=ALL-UNNAMED -d my_bin/ -cp /share/output-jdk9-hs-comp-dbg/support/test/lib/wb.jar src/WBTestMemory.java
 * Run with: java -XaddExports:java.base/jdk.internal.misc=ALL-UNNAMED -cp my_bin/ -Xbootclasspath/a:/share/output-jdk9-hs-comp-dbg/support/test/lib/wb.jar -XX:+WhiteBoxAPI WBTestMemory
 */

public class WBTestMemory {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Unsafe UNSAFE;
    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe) unsafeField.get(null);
        }
        catch (Exception e) { throw new Error(e); }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Heap size:");
        WB.printHeapSizes();
        System.out.println("Oop size = " + WB.getHeapOopSize());
        System.out.println("Max compressed oops heap size = " + WB.getCompressedOopsMaxHeapSize());
        System.out.println("OS page size = " +  WB.getVMPageSize());

        boolean compressedOops = WB.getBooleanVMFlag("UseCompressedOops");
        System.out.println("Using compressed oops = " + compressedOops);
        String hello = "Hello Team!";
        System.out.format("&hello          = 0x%x\n", WB.getObjectAddress(hello));
        System.out.println("sizeof(String)  = " + WB.getObjectSize(hello));
        byte[] b = (byte[])UNSAFE.getObject(hello, compressedOops ? 12L : 16L);
        System.out.println("sizeof(\"hello\") = " + (WB.getObjectSize(hello) + WB.getObjectSize(b)));
    }

}
