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

    public static <T> T depend(String name) {
        Global<T> global = null;

        for (int i = 0; i < globals.size(); i++) {
            global = (Global<T>) globals.get(i);

            if (global == null) continue;

            break;

        }

        assert global != null;

        return global.object;
    }
}
