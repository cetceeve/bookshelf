package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayBookActivity extends AppCompatActivity {

    private Book mBook;
    private String mBooklistName;
    private DatabaseReference mBookDatabaseReference;

    private TextView titleTextView;
    private TextView authorNameTextView;
    private TextView isbnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_book);

        Intent i = getIntent();
        Bundle extra = i.getExtras();
        mBook = (Book)extra.get("book");
        mBooklistName = (String)extra.get("listname");

        getSupportActionBar().setTitle(mBooklistName);

        initViews();
        setBookData();
        mBookDatabaseReference = FirebaseDatabase.getInstance().getReference().child("booklists").child(mBooklistName).child(mBook.getKey());

        Button editButton = (Button)findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayBookActivity.this,editBookActivity.class);
                intent.putExtra("book", mBook);
                intent.putExtra("listname", mBooklistName);
                startActivity(intent);
            }
        });

        ValueEventListener bookListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                Book changedBook = dataSnapshot.getValue(Book.class);
                if (changedBook != null) {
                    setBookData();
                }
                */
                mBook = dataSnapshot.getValue(Book.class);
                setBookData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mBookDatabaseReference.addValueEventListener(bookListener);
    }

    private void initViews() {
        titleTextView = (TextView)findViewById(R.id.textView_title_book);
        authorNameTextView = (TextView)findViewById(R.id.textView_authorName_book);
        isbnTextView = (TextView)findViewById(R.id.textView_isbn_book);
    }

    private void setBookData() {
        titleTextView.setText(mBook.getTitle());
        authorNameTextView.setText(mBook.getAuthorName());
        isbnTextView.setText(mBook.getIsbn());
    }
}
