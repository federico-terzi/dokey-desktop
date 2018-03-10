
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.apache.commons.lang3.time.StopWatch;
import system.MAC.MACApplicationManager;
import system.MAC.MACUtils;
import system.model.Application;

import java.util.List;


public class TestMain {
    public static void main(String[] args) {
        MACApplicationManager macApplicationManager = new MACApplicationManager(null);

        StopWatch timer2 = new StopWatch();
        timer2.start();
        List<Application> apps2 = macApplicationManager.getActiveApplicationsWithApplescript();
        timer2.stop();
        System.out.println("APPLE: "+timer2.getTime());

        StopWatch timer = new StopWatch();
        timer.start();
        List<Application> apps1 = macApplicationManager.getActiveApplications();
        timer.stop();
        System.out.println("NATIVE: "+timer.getTime());

        System.exit(0);
    }
}