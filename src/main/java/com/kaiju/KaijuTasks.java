package com.kaiju;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/** Tiny server-side scheduler: repeat(ticks, i -> ...) runs once per tick, i = 0..ticks-1. */
public class KaijuTasks {
    private static class Task { int i = 0; final int total; final IntConsumer fn; Task(int t, IntConsumer f) { total = t; fn = f; } }
    private static final List<Task> TASKS = new ArrayList<>();

    public static void repeat(int ticks, IntConsumer fn) { TASKS.add(new Task(ticks, fn)); }

    public static void tick() {
        if (TASKS.isEmpty()) return;
        for (Task t : new ArrayList<>(TASKS)) {
            try { t.fn.accept(t.i); } catch (Exception e) { t.i = t.total; }
            t.i++;
        }
        TASKS.removeIf(t -> t.i >= t.total);
    }
}
