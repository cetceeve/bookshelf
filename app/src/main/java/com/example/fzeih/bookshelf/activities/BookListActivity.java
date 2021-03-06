package com.example.fzeih.bookshelf.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.adapter.BookAdapter;
import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.datastructures.Book;
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

        /*
        reference to speed dial:
         Ivanov, Yavor. https://github.com/yavski/fab-speed-dial
         */

public class BookListActivity extends AppCompatActivity {
    private BroadcastReceiver mBookDeletionBroadcastReceiver;

    private DatabaseReference mBookListDatabaseReference;
    private DatabaseReference mListNameDatabaseReference;

    private ChildEventListener mBookListChildEventListener;
    private ValueEventListener mListNameValueEventListener;

    private String mBookListKey;
    private String mBookListName;

    private ListView mBookListView;
    private List<Book> mBooks;
    private BookAdapter mBookAdapter;

    private Book mDeletedBook = null;

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

        // Broadcast Receiver
        initBookDeletionBroadcastReceiver();

        // Listeners
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.fab_speeddial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
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
    }

    private void onMenuItemClicked(int itemId) {
        switch (itemId) {
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
                // start BarcodeScannerActivity
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
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBookDeletionBroadcastReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachBookListDatabaseReadListener();
        attachListNameDatabaseReadListener();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBookDeletionBroadcastReceiver, new IntentFilter(Constants.event_book_deletion));
        showUndoBookDeletionSnackbar();
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mBookListName = extras.getString(Constants.key_intent_booklistname);
            mBookListKey = extras.getString(Constants.key_intent_booklistkey);
        }
    }

    private void initBookDeletionBroadcastReceiver() {
        mBookDeletionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    mDeletedBook = (Book) extras.get(Constants.key_intent_book);
                }
            }
        };
    }

    private void getDatabaseReference() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mBookListDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklists).child(mBookListKey);
            mListNameDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklistnames).child(mBookListKey);
        } else {
            Toast.makeText(BookListActivity.this, "ERROR: User is not signed in", Toast.LENGTH_SHORT).show();
            finish();
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
        detachListNameDatabaseReadListener();

        // remove data
        mListNameDatabaseReference.removeValue();
        mBookListDatabaseReference.removeValue();

        DatabaseService.getInstance().getBookService().decrementTotalNumOfBooks(mBooks.size());

        finish();
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
        renameBookListDialog.setView(input)
                .setPositiveButton(R.string.dialog_rename, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadBookListName(input.getText().toString());
                    }
                }).setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
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
                .setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
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

    private void showUndoBookDeletionSnackbar() {
        if (mDeletedBook != null) {
            Snackbar.make(mBookListView, R.string.snackbar_message_book_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_action_undo, new UndoBookDeletionListener(mDeletedBook))
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            mDeletedBook = null;
                        }
                    }).show();
        }
    }

    private class UndoBookDeletionListener implements View.OnClickListener {
        private Book deletedBook;
        private DatabaseReference deletedBookDatabaseReference;

        private UndoBookDeletionListener(@NonNull Book deletedBook) {
            this.deletedBook = deletedBook;
            this.deletedBookDatabaseReference = mBookListDatabaseReference.child(deletedBook.getKey());
        }

        @Override
        public void onClick(View v) {
            // undo book deletion
            deletedBookDatabaseReference.setValue(deletedBook);
            DatabaseService.getInstance().getBookService().incrementTotalNumOfBooks();
        }
    }
}
