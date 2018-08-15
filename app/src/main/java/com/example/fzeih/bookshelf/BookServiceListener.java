package com.example.fzeih.bookshelf;

import android.support.annotation.NonNull;

public interface BookServiceListener {

    void onTotalNumOfBooksChanged(@NonNull Long totalNumOfBooks);
}
