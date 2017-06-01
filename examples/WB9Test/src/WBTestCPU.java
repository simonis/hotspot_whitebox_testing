import sun.hotspot.WhiteBox;
import sun.hotspot.cpuinfo.CPUInfo;

public class WBTestCPU {
    private static final WhiteBox WB = WhiteBox.getWhiteBox();

    public static void main(String[] args) throws Exception {

        System.out.println("CPU features = " + WB.getCPUFeatures());

        System.out.println(CPUInfo.getFeatures());
        System.out.println(CPUInfo.getAdditionalCPUInfo());
        System.out.println("CPU supports sse3 : " + CPUInfo.hasFeature("sse3"));
    }

}
