package com.example.fzeih.bookshelf.database_service;

import android.content.Context;

public class DatabaseService {
    private static final DatabaseService ourInstance = new DatabaseService();
    private static boolean isStarted = false;
    private static AchievementService achievementService;
    private static BookService bookService;

    public static DatabaseService getInstance() {
        return ourInstance;
    }

    private DatabaseService() {
    }

    public void startServices(Context context) {
        if (!isStarted) {
            achievementService = new AchievementService(context);
            achievementService.getAchievementList();
            bookService = new BookService(context);
            isStarted = true;
        }
    }

    public AchievementService getAchievementService() {
        if (isStarted) {
            return achievementService;
        } else {
            return null;
        }
    }

    public BookService getBookService() {
        if (isStarted) {
            return bookService;
        } else {
            return null;
        }
    }
}
