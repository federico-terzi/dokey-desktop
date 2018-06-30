package tests;

import system.MS.MSIconExtractor;

public class IconTestMain {
    public static void main(String[] args) {
        MSIconExtractor.saveExecutableIcon("C:\\Program Files (x86)\\Adobe\\Adobe Dreamweaver CS6\\Dreamweaver.exe");
        //MSIconExtractor.saveExecutableIcon("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");

        System.exit(0);
    }
}