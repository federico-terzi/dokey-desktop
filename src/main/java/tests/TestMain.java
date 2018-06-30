package tests;

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

        double t1 = 0;
        double t2 = 0;

        int tries = 100;

        for (int i = 0; i< tries; i++) {
            StopWatch timer = new StopWatch();
            timer.start();
            List<Application> apps1 = macApplicationManager.getActiveApplications();
            timer.stop();
            t1+=timer.getTime();

            StopWatch timer2 = new StopWatch();
            timer2.start();
            List<Application> apps2 = macApplicationManager.getActiveApplicationsWithApplescript();
            timer2.stop();
            t2+=timer2.getTime();

        }
        t1 = t1/tries;
        t2 = t2/tries;

        System.out.println("NATIVE: "+t1);
        System.out.println("APPLE: "+t2);

        System.exit(0);
    }
}