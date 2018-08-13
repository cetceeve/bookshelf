package com.example.fzeih.bookshelf;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class Achievement {
    private int mLevel;
    private String mAchievementText;
    private Drawable mDrawableResource;

    public Achievement (Context context, int level, int achievementStringResource, int drawableResource) {
        mLevel = level;
        mAchievementText = context.getString(achievementStringResource);
        mDrawableResource = context.getDrawable(drawableResource);
    }

    public int getLevel() {
        return mLevel;
    }

    public String getAchievementText() {
        return mAchievementText;
    }

    public Drawable getDrawableResource() {
        return mDrawableResource;
    }
}
