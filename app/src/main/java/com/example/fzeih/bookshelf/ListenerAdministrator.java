package com.example.fzeih.bookshelf;

import java.util.ArrayList;

public class ListenerAdministrator {
    private static ArrayList<Object> OBSERVERS = new ArrayList<>();

    public ListenerAdministrator() {
    }

    public static void registerListener(Object listener) {
        OBSERVERS.add(listener);
    }

    public static void removeListener(Object listener) {
        OBSERVERS.remove(listener);
    }

    public static Object[] getListener(Class listenerClass) {
        ArrayList<Object> res = new ArrayList<>();
        for (Object listener: OBSERVERS) {
            if (listenerClass.isInstance(listener)) {
                res.add(listener);
            }
        }
        return res.toArray();
    }
}
