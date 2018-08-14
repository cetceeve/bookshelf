package com.example.fzeih.bookshelf;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class BookListActivity extends AppCompatActivity implements BookDeletionListener {

    private DatabaseReference mBookListDatabaseReference;
    private DatabaseReference mListNameDatabaseReference;

    private ChildEventListener mBookListChildEventListener;
    private ValueEventListener mListNameValueEventListener;

    private String mBookListKey;
    private String mBookListName;

    private ListView mBookListView;
    private List<Book> mBooks;
    private BookAdapter mBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Intent
        readIntent();
        getSupportActionBar().setTitle(mBookListName); // from intent

        // Data
        getDatabaseReference();
        setBookAdapter();

        // Listeners
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speeddial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter(){
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem){
                onMenuItemClicked(menuItem.getItemId());
                return false;
            }
        });
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startDisplayBookActivity(position);
            }
        });

        // register as listener
        ListenerAdministrator.getInstance().registerListener(this);
    }

    private void onMenuItemClicked(int itemId) {
        switch (itemId){
            case R.id.fab_action_manually:
                // start AddBookActivity
                Intent addManuallyIntent = new Intent(BookListActivity.this, AddBookActivity.class);
                addManuallyIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
                startActivity(addManuallyIntent);
                break;
            case R.id.fab_action_isbn:
                // start IsbnSearchActivity
                Intent addByIsbnIntent = new Intent(BookListActivity.this, IsbnSearchActivity.class);
                addByIsbnIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
                startActivity(addByIsbnIntent);
                break;
            case R.id.fab_action_barcodescanner:
                // start Barcode Scanner
                Intent addByBarcodeIntent = new Intent(BookListActivity.this, BarcodeScannerActivity.class);
                addByBarcodeIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
                startActivity(addByBarcodeIntent);
                break;
            default:
                break;

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_booklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        detachBookListDatabaseReadListener();
        detachListNameDatabaseReadListener();
        mBookAdapter.clear();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachBookListDatabaseReadListener();
        attachListNameDatabaseReadListener();
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mBookListName = extras.getString(Constants.key_intent_booklistname);
            mBookListKey = extras.getString(Constants.key_intent_booklistkey);
        }
    }

    private void getDatabaseReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mBookListDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklists).child(mBookListKey);
            mListNameDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklistnames).child(mBookListKey);
        } else {
            Toast.makeText(BookListActivity.this, "ERROR: User is not signed in", Toast.LENGTH_SHORT).show();
            closeActivity();
        }
    }

    private void setBookAdapter() {
        mBooks = new ArrayList<>();
        mBookListView = findViewById(R.id.listview_booklist);
        mBookAdapter = new BookAdapter(this, R.layout.item_book, mBooks);
        mBookListView.setAdapter(mBookAdapter);
    }

    private void attachBookListDatabaseReadListener() {
        if (mBookListChildEventListener == null) {
            mBookListChildEventListener = new ChildEventListener() {
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
                    // delete book by id, ensures correct deletion if triggered from another device
                    String bookId = dataSnapshot.getKey();
                    for (Book book : mBooks) {
                        if (book.getKey().equals(bookId)) {
                            mBooks.remove(book);
                            mBookAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mBookListDatabaseReference.addChildEventListener(mBookListChildEventListener);
        }
    }

    private void attachListNameDatabaseReadListener() {
        mListNameValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateBookListName((String) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mListNameDatabaseReference.addValueEventListener(mListNameValueEventListener);
    }

    private void detachBookListDatabaseReadListener() {
        if (mBookListChildEventListener != null) {
            mBookListDatabaseReference.removeEventListener(mBookListChildEventListener);
            mBookListChildEventListener = null;
        }
    }

    private void detachListNameDatabaseReadListener() {
        if (mListNameValueEventListener != null) {
            mListNameDatabaseReference.removeEventListener(mListNameValueEventListener);
            mListNameValueEventListener = null;
        }
    }

    private void deleteBookList() {
        detachBookListDatabaseReadListener();

        // remove data
        mListNameDatabaseReference.removeValue();
        mBookListDatabaseReference.removeValue();

        closeActivity();
    }

    private void uploadBookListName(String updatedBookListName) {
        if (updatedBookListName != null) {
            mListNameDatabaseReference.setValue(updatedBookListName);
        }
    }

    private void updateBookListName(String updatedBookListName) {
        if (updatedBookListName != null) {
            mBookListName = updatedBookListName;
            getSupportActionBar().setTitle(mBookListName);
        }
    }

    private void startDisplayBookActivity(int position) {
        Intent displayBookIntent = new Intent(BookListActivity.this, DisplayBookActivity.class);
        displayBookIntent.putExtra(Constants.key_intent_book, mBooks.get(position));
        displayBookIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
        startActivity(displayBookIntent);
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
                uploadBookListName(input.getText().toString());
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

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder deleteConfirmationDialog = new AlertDialog.Builder(BookListActivity.this);
        deleteConfirmationDialog.setMessage(R.string.dialog_message_delete_confirmation)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBookList();
                    }
                }).setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteConfirmationDialog.show();
    }

    @Override
    public void bookDeleted(DatabaseReference deletedBookDatabaseReference, Book deletedBook) {
        Snackbar.make(mBookListView, "Book deleted!", Snackbar.LENGTH_LONG)
                .setAction("Undo", new UndoBookDeletionListener(deletedBookDatabaseReference, deletedBook)).show();
    }

    private class UndoBookDeletionListener implements View.OnClickListener {
        private Book deletedBook;
        private DatabaseReference deletedBookDatabaseReference;

        private UndoBookDeletionListener(DatabaseReference deletedBookDatabaseReference, Book deletedBook) {
            this.deletedBook = deletedBook;
            this.deletedBookDatabaseReference = deletedBookDatabaseReference;
        }

        @Override
        public void onClick(View v) {
            // undo book deletion
            deletedBookDatabaseReference.setValue(deletedBook);
        }
    }

    private void closeActivity() {
        ListenerAdministrator.getInstance().removeListener(this);
        finish();
    }
}
