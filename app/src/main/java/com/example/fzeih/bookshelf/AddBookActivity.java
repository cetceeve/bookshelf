package com.example.fzeih.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {

    private DatabaseReference mBooklistDatabaseReference;

    private Button createButton;
    private EditText titleEditText;
    private EditText authorNameEditText;
    private EditText isbnEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String listname = extras.getString("listname");
        getSupportActionBar().setTitle(listname);

        mBooklistDatabaseReference = FirebaseDatabase.getInstance().getReference().child("booklists").child(listname);

        createButton = (Button) findViewById(R.id.button_create_bookitem);
        titleEditText = (EditText) findViewById(R.id.editText_title);
        authorNameEditText = (EditText) findViewById(R.id.editText_authorName);
        isbnEditText = (EditText) findViewById(R.id.editText_isbn);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameText = authorNameEditText.getText().toString();
                String titleText = titleEditText.getText().toString();
                String isbnText = isbnEditText.getText().toString();

                DatabaseReference nextBookDatabaseReference = mBooklistDatabaseReference.push();
                Book bookItem = new Book(nextBookDatabaseReference.getKey(), nameText,titleText,isbnText);
                nextBookDatabaseReference.setValue(bookItem);
                finish();
            }
        });
    }
}
