package com.example.fzeih.bookshelf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class IsbnSearchActivity extends AppCompatActivity {

    private EditText isbnEditText;
    private Button searchButton;
    private ListView resultListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn_search);

        isbnEditText = (EditText) findViewById(R.id.edittext_isbn);
        searchButton = (Button) findViewById(R.id.button_searchByIsbn);
        resultListView = (ListView) findViewById(R.id.listview_isbnResultList);
    }
}
