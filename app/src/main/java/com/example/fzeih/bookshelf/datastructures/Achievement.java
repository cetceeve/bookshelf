package com.example.fzeih.bookshelf.datastructures;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class Achievement {
    private int mLevel;
    private String mAchievementText;
    private Drawable mDrawableResourceColor;
    private Drawable mDrawableResourceGrey;
    private boolean mColored;

    public Achievement (Context context, int level, int achievementStringResource, int drawableResourceColor, int drawableResourceGrey, boolean colored) {
        mLevel = level;
        mAchievementText = context.getString(achievementStringResource);
        mDrawableResourceColor = context.getDrawable(drawableResourceColor);
        mDrawableResourceGrey = context.getDrawable(drawableResourceGrey);
        mColored = colored;
    }

    public Achievement (Context context, int level, int achievementStringResource, int drawableResourceColor, int drawableResourceGrey) {
        this(context, level, achievementStringResource, drawableResourceColor, drawableResourceGrey, false);
    }

    public int getLevel() {
        return mLevel;
    }

    public boolean getColored() { return mColored; }

    public boolean setColored() {
        if (!mColored) {
            mColored = true;
            return true;
        }
        return false;
    }

    public boolean removeColored() {
        if (mColored) {
            mColored = false;
            return true;
        }
        return false;
    }

    public String getAchievementText() {
        return mAchievementText;
    }

    public Drawable getDrawableResource() {
        if (mColored) {
            return mDrawableResourceColor;
        } else {
            return mDrawableResourceGrey;
        }
    }
}
