package com.example.fzeih.bookshelf.database_service;

import android.content.Context;

public class DatabaseService {
    private static final DatabaseService INSTANCE = new DatabaseService();
    private boolean isStarted = false;
    private AchievementService achievementService;
    private BookService bookService;

    public static DatabaseService getInstance() {
        return INSTANCE;
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
