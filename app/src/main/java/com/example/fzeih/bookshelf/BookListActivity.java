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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {
    private ListView mBookListView;
    private BookAdapter mBookAdapter;

    private ChildEventListener mChildEventListener;
    private DatabaseReference mBookListDatabaseReference;
    private DatabaseReference mListnamesDatabaseReference;
    private String mBookListKey;

    private String mBookListName;
    private List<Book> mBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Intent
        readIntent();
        getSupportActionBar().setTitle(mBookListName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Data
        getDatabaseReference();
        setBookAdapter();

        // Listeners
        // attachDatabaseReadListener();

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsToAddBookDialog();
            }
        });
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startDisplayBookActivity(position);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_booklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_rename:
                showRenameBookListDialog();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachReadDatabaseListener();
        mBookAdapter.clear();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachDatabaseReadListener();
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBookListName = extras.getString(Constants.key_intent_booklistname);
        mBookListKey = extras.getString(Constants.key_intent_booklistkey);
    }

    private void getDatabaseReference() {
        mBookListDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_booklists).child(mBookListKey);
        mListnamesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_booklistnames);
    }

    private void setBookAdapter() {
        mBooks = new ArrayList<>();
        mBookListView = findViewById(R.id.listview_booklist);
        mBookAdapter = new BookAdapter(this, R.layout.item_book, mBooks);
        mBookListView.setAdapter(mBookAdapter);
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
                    Book book = dataSnapshot.getValue(Book.class);
                    mBookAdapter.remove(book);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mBookListDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachReadDatabaseListener() {
        if (mChildEventListener != null) {
            mBookListDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void startDisplayBookActivity(int position) {
        Intent displayBookIntent = new Intent(BookListActivity.this, DisplayBookActivity.class);
        displayBookIntent.putExtra(Constants.key_intent_book, mBooks.get(position));
        displayBookIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
        startActivity(displayBookIntent);
    }

    private void deleteBookList() {
        detachReadDatabaseListener();
        mListnamesDatabaseReference.child(mBookListKey).removeValue();
        mBookListDatabaseReference.removeValue();
    }

    private void updateBookListName(String updatedBookListName) {
        mListnamesDatabaseReference.child(mBookListKey).setValue(updatedBookListName);
        mBookListName = updatedBookListName;
        getSupportActionBar().setTitle(mBookListName);
    }

    private void showRenameBookListDialog() {
        AlertDialog.Builder renameBookListDialog = new AlertDialog.Builder(BookListActivity.this);
        renameBookListDialog.setMessage(R.string.dialog_message_rename_booklist);
        final EditText input = new EditText(BookListActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(params);
        renameBookListDialog.setView(input);

        renameBookListDialog.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateBookListName(input.getText().toString());
            }
        });
        renameBookListDialog.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        renameBookListDialog.show();
    }

    private void showOptionsToAddBookDialog() {
        // Create Dialog
        String[] optionsToAddBook = {getString(R.string.dialog_option_add_book_manually), getString(R.string.dialog_option_isbn_search), getString(R.string.dialog_option_barcode_scanner)};
        AlertDialog.Builder addBookDialog = new AlertDialog.Builder(BookListActivity.this);
        addBookDialog.setItems(optionsToAddBook, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // start AddBookActivity
                        Intent addManuallyIntent = new Intent(BookListActivity.this, AddBookActivity.class);
                        addManuallyIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
                        startActivity(addManuallyIntent);
                        break;
                    case 1:
                        Intent addByIsbnIntent = new Intent(BookListActivity.this, IsbnSearchActivity.class);
                        addByIsbnIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
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

        addBookDialog.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        addBookDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder deleteConfirmationDialog = new AlertDialog.Builder(BookListActivity.this);
        deleteConfirmationDialog.setMessage(R.string.dialog_message_delete_confirmation)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBookList();
                        finish();
                    }
                }).setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteConfirmationDialog.show();
    }
}
