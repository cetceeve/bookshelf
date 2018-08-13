package com.example.fzeih.bookshelf;

import java.util.ArrayList;

public class ListenerAdministrator {
    private static ArrayList<Object> OBSERVERS = new ArrayList<>();

    // Initialization-on-demand holder idiom from Wikipedia
    private ListenerAdministrator() {
    }

    private static class LazyHolder {
        static final ListenerAdministrator INSTANCE = new ListenerAdministrator();
    }

    public static ListenerAdministrator getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void registerListener(Object listener) {
        OBSERVERS.add(listener);
    }

    public void removeListener(Object listener) {
        OBSERVERS.remove(listener);
    }

    public Object[] getListener(Class listenerClass) {
        ArrayList<Object> res = new ArrayList<>();
        for (Object listener: OBSERVERS) {
            if (listenerClass.isInstance(listener)) {
                res.add(listener);
            }
        }
        return res.toArray();
    }
}
