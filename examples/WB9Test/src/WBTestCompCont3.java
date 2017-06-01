import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import sun.hotspot.WhiteBox;
import sun.hotspot.code.NMethod;

public class WBTestCompCont3 {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();
    private static final Random RANDOM = new Random();

    public static int foo() {
        return RANDOM.nextInt();
    }

    public static void main(String[] args) throws Exception {

        Method m = WBTestCompCont3.class.getMethod("foo");
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
    }

}
