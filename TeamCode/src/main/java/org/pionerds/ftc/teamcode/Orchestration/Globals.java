package org.pionerds.ftc.teamcode.Orchestration;

import java.util.ArrayList;

public class Globals {

    // Guys I think I generic-ed too close to the sun
    private static final ArrayList<Global<?>> globals = new ArrayList<>();

    public static class Global<T> {
        public T object;
        public String name;

        public Global(String name, T object) {
            this.object = object;
            this.name = name;
        }
    }

    public static <T> void add(String name, T object) {
        globals.add(new Global<T>(name, object));
    }

    /**
     * @return the most recently added global with this name
     * @throws NullPointerException if no global with this name exists
     */
    public static <T> T depend(String name) {
        for (int i = globals.size() - 1; i >= 0; i--) {
            Global<T> global = (Global<T>) globals.get(i);

            if (global == null) continue;

            if (global.name.equals(name)) {
                return global.object;
            }
        }

        throw new NullPointerException("Global '" + name + "' does not exist");
    }

    public static boolean has(String name) {
        for (int i = 0; i < globals.size(); i++) {
            Global<?> global = globals.get(i);

            if (global != null && global.name.equals(name)) return true;
        }

        return false;
    }

    static {
        Scheduler.addTask("exit", (obj) -> {
            while (!globals.isEmpty()) globals.remove(0);
        });
    }
}
