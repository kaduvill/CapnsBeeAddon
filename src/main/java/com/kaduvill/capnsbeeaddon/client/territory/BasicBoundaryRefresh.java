package com.kaduvill.capnsbeeaddon.client.territory;

/** Client-thread lifecycle for one selected server boundary; no world/housing references. */
final class BasicBoundaryRefresh {
    private static final int TIMEOUT_TICKS = 100;
    private int generation;
    private int age;
    private boolean active;

    void begin() {
        generation++;
        age = 0;
        active = true;
    }

    void clear() {
        generation++;
        age = 0;
        active = false;
    }

    boolean isActive() {
        return active;
    }

    int getGeneration() {
        return generation;
    }

    boolean accept(int incomingGeneration) {
        if (!active || incomingGeneration != generation || age >= TIMEOUT_TICKS) {
            return false;
        }
        age = 0;
        return true;
    }

    boolean tick() {
        return active && ++age >= TIMEOUT_TICKS;
    }
}
