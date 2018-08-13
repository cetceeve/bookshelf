package com.example.fzeih.bookshelf;

public class Achievement {
    private String mAchievementText;
    private int mDrawableResource;

    public Achievement (String achievementText, int drawableResource) {
        mAchievementText = achievementText;
        mDrawableResource = drawableResource;
    }

    public String getAchievementText() {
        return mAchievementText;
    }

    public int getDrawableResource() {
        return mDrawableResource;
    }
}
