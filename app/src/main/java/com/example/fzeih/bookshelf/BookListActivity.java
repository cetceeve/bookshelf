package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {
    private ListView mBooklistView;
    private BookAdapter mBookAdapter;

    private ChildEventListener mChildEventListener;
    private DatabaseReference mBooklistDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addBookIntent = new Intent(BookListActivity.this, AddBookActivity.class);
                addBookIntent.putExtra("booklist_database_reference", (Parcelable) mBooklistDatabaseReference);
                startActivity(addBookIntent);
            }
        });

        // Hook Bookadapter
        List<Book> books = new ArrayList<>();
        mBooklistView = findViewById(R.id.listview_booklist);
        mBookAdapter = new BookAdapter(this, R.layout.item_book, books);
        mBooklistView.setAdapter(mBookAdapter);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mBooklistDatabaseReference = (DatabaseReference) bundle.get("booklist_database_reference");

        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Book book = dataSnapshot.getValue(Book.class);
                    mBookAdapter.add(book);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

        }
    }

}
