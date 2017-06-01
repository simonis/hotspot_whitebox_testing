import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class DiagnosticCommandTest {

    public static void main(String ... args) throws Exception{
        ObjectName oname = ObjectName.getInstance("com.sun.management:type=DiagnosticCommand");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        String result = (String) mbs.invoke(oname, "compilerDirectivesPrint", null, null);
        System.out.println(result);
    }
}
