package com.example.fzeih.bookshelf;

public interface AchievementServiceListener {

    void onNumOfReadBooksChance(Long numOfReadBooks);
    void onAchievementChanged(Achievement highestAchievement);
}
