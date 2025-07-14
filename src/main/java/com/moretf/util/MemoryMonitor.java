package com.moretf.util;

public class MemoryMonitor {
    public static void logMemoryUsage(String tag) {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory() / (1024 * 1024);
        long free = runtime.freeMemory() / (1024 * 1024);
        long used = total - free;
        System.out.printf("[MEMORY] %s - Used: %dMB, Free: %dMB, Total: %dMB%n", tag, used, free, total);
    }
}
