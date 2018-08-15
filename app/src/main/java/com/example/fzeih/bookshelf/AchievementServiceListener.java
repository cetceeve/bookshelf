package com.example.fzeih.bookshelf;

import android.support.annotation.NonNull;

public interface AchievementServiceListener {

    void onNumOfReadBooksChanged(@NonNull Long numOfReadBooks);
    void onAchievementChanged(Achievement highestAchievement);
}
