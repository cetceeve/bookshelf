package com.example.fzeih.bookshelf.listener;

import android.support.annotation.NonNull;

public interface BookServiceCallback {

    void onTotalNumOfBooksChanged(@NonNull Long totalNumOfBooks);
}
