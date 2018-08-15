package com.example.fzeih.bookshelf;

import android.support.annotation.NonNull;

public interface AchievementServiceListener {

    void onNumOfReadBooksChance(@NonNull Long numOfReadBooks);
    void onAchievementChanged(@NonNull Achievement highestAchievement);
}
