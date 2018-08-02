package com.example.fzeih.bookshelf;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {
    private ListView mBooklistView;
    private BookAdapter mBookAdapter;

    private ChildEventListener mChildEventListener;
    private DatabaseReference mBooklistDatabaseReference;
    private List<Book> mBooks;
    private String mBooklistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Intent
        readIntent();
        getSupportActionBar().setTitle(mBooklistName);

        // Data
        getDatabaseReference();
        setBookAdapter();

        // Listeners
        attachDatabaseReadListener();

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsToAddBookDialog();
            }
        });
        mBooklistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startDisplayBookActivity(position);
            }
        });
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBooklistName = extras.getString(Constants.key_intent_booklistname);
    }

    private void getDatabaseReference() {
        mBooklistDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.key_db_reference_booklists).child(mBooklistName);
    }

    private void setBookAdapter() {
        mBooks = new ArrayList<>();
        mBooklistView = findViewById(R.id.listview_booklist);
        mBookAdapter = new BookAdapter(this, R.layout.item_book, mBooks);
        mBooklistView.setAdapter(mBookAdapter);
    }

    private void showOptionsToAddBookDialog() {
        // Create Dialog
        String[] optionsToAddBook = {"Add manually", "Search by ISBN", "Scan barcode"};
        AlertDialog.Builder addBookDialog = new AlertDialog.Builder(BookListActivity.this);
        addBookDialog.setItems(optionsToAddBook, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Intent addManuallyIntent = new Intent(BookListActivity.this, AddBookActivity.class);
                        addManuallyIntent.putExtra(Constants.key_intent_booklistname, mBooklistName);
                        startActivity(addManuallyIntent);
                        break;
                    case 1:
                        // TODO: isbn search
                        Intent addByIsbnIntent = new Intent(BookListActivity.this, IsbnSearchActivity.class);
                        startActivity(addByIsbnIntent);
                        break;
                    case 2:
                        // TODO: barcodescanner
                        break;
                    default:
                        break;
                }
            }
        });

        addBookDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        addBookDialog.show();
    }

    private void startDisplayBookActivity(int position) {
        Intent displayBookIntent = new Intent(BookListActivity.this, DisplayBookActivity.class);
        displayBookIntent.putExtra(Constants.key_intent_book, mBooks.get(position));
        displayBookIntent.putExtra(Constants.key_intent_booklistname, mBooklistName);
        startActivity(displayBookIntent);
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
                    // replace updated book at correct position
                    Book changedBook = dataSnapshot.getValue(Book.class);
                    for (int index = 0; index < mBooks.size(); index++) {
                        if (mBooks.get(index).getKey().equals(dataSnapshot.getKey())) {
                            mBooks.remove(index);
                            mBooks.add(index, changedBook);
                            mBookAdapter.notifyDataSetChanged();
                        }
                    }
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
        mBooklistDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

}
