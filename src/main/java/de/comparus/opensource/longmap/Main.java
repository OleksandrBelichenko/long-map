package de.comparus.opensource.longmap;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * @author Oleksandr Belichenko
 */
public class Main {
    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);

        memory(decimalFormat);
        perfomance(decimalFormat);
    }

    private static void memory(DecimalFormat decimalFormat) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        LongMap<String> longMap = new LongMapImpl<>();
        for (int i = 0; i < 100; i++) {
            longMap.put(i, "" + i);
        }
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long longMapMem = memAfter - memBefore;

        Runtime.getRuntime().gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();
        HashMap<Long, String> hashMap = new HashMap<>();
        for (Long i = 0L; i < 100L; i++) {
            hashMap.put(i, "" + i);
        }
        memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long hashMapMem = memAfter - memBefore;
        double difference = (double) longMapMem / hashMapMem;

        System.out.println("longMapMem: " + longMapMem + "; hashMapMem: " + hashMapMem
                + "; difference: " + decimalFormat.format(difference));
    }


    private static void perfomance(DecimalFormat decimalFormat) {
        long timeBefore = System.nanoTime();
        LongMap<String> longMap = new LongMapImpl<>();
        for (int i = 0; i < 100; i++) {
            longMap.put(i, "" + i);
        }
        for (int i = 0; i < 100; i++) {
            longMap.get(i);
        }
        for (int i = 0; i < 100; i++) {
            longMap.remove(i);
        }

        long timeAfter = System.nanoTime();
        long longMapTime = timeAfter - timeBefore;


        timeBefore = System.nanoTime();
        HashMap<Long, String> hashMap = new HashMap<>();
        for (Long i = 0L; i < 100L; i++) {
            hashMap.put(i, "" + i);
        }
        for (Long i = 0L; i < 100L; i++) {
            hashMap.get(i);
        }
        for (Long i = 0L; i < 100L; i++) {
            hashMap.remove(i);
        }
        timeAfter = System.nanoTime();
        long hashMapTime = timeAfter - timeBefore;
        double difference = (double) longMapTime / hashMapTime;

        System.out.println("longMapTime: " + longMapTime + "; hashMapTime: " + hashMapTime
                + "; difference: " + decimalFormat.format(difference));
    }
}