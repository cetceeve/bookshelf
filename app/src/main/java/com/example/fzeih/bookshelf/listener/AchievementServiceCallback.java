package com.example.fzeih.bookshelf.listener;

import android.support.annotation.NonNull;

import com.example.fzeih.bookshelf.datastructures.Achievement;

public interface AchievementServiceCallback {

    void onNumOfReadBooksChanged(@NonNull Long numOfReadBooks);
    void onAchievementChanged(Achievement highestAchievement);
}
