package org.pionerds.ftc.teamcode.Orchestration;

import java.util.ArrayList;

public class Globals {

    private static final ArrayList<Global<?>> globals = new ArrayList();

    public static class Global<T> {
        public T object;
        public String name;

        public Global(String name, T object) {
            this.object = object;
            this.name = name;
        }
    }

    public static void add(Global global) {
        globals.add(global);
    }

    public static <T> T depend(String name) {
        Global<?> global;
        for (int i = 0; i < globals.size(); i++) {
            global = globals.get(i);

            if (global.name.equals(name)) return (T) global.object;
        }

        return null;
    }
}
