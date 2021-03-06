package com.example.fzeih.bookshelf.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.adapter.BookListInformationAdapter;
import com.example.fzeih.bookshelf.database_service.DatabaseService;
import com.example.fzeih.bookshelf.datastructures.BookListInformation;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

        /*
        reference to Google Firebase:
        https://firebase.google.com/docs/database/android/start/
         */

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference mListNamesDatabaseReference;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ChildEventListener mChildEventListener;

    private ListView mListListView;
    private ArrayList<BookListInformation> mBookListInformationArray;
    private ArrayAdapter<BookListInformation> mBookListInformationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_red_round);

        // Data
        initAuthentication();
        setAdapter();

        // Listeners
        attachAuthStateChangedListener();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewBookListDialog();
            }
        });
        mListListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startBookListActivity(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                // start ProfileActivity
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.action_sign_out:
                // sign out
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.action_wish_list:
                // start WishListActivity
                Intent wishGalleryIntent = new Intent(this, WishGalleryActivity.class);
                startActivity(wishGalleryIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (resultCode == RESULT_FIRST_USER) {
                // Successfully signed in
                Snackbar.make(mListListView, "Welcome to Bookshelf " + user.getDisplayName() + ".", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else if (resultCode == RESULT_OK) {
                Snackbar.make(mListListView, "Welcome back " + user.getDisplayName() + ".", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button.
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong during sign-in!", Toast.LENGTH_LONG).show();
                    finish();
                }

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mBookListInformationAdapter.clear();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void initAuthentication() {
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void setAdapter() {
        mBookListInformationArray = new ArrayList<>();
        mBookListInformationAdapter = new BookListInformationAdapter(this, android.R.layout.simple_list_item_1, mBookListInformationArray);
        mListListView = (ListView) findViewById(R.id.listview_listlist);
        mListListView.setAdapter(mBookListInformationAdapter);
    }

    private void attachAuthStateChangedListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user);
                } else {
                    // No user is signed in
                    onSignedOutCleanup();
                    displaySignInUI();
                }
            }
        };
    }

    private void onSignedInInitialize(FirebaseUser user) {
        getDatabaseReference(user);
        attachDatabaseReadListener();
        DatabaseService.getInstance().startServices(this);
    }

    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
        mBookListInformationAdapter.clear();
    }

    private void displaySignInUI() {
        // check for network connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // start authUI only if user is connected
        if (isConnected) {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        } else {
            showInternetInformationDialog();
        }
    }

    private void getDatabaseReference(@NonNull FirebaseUser user) {
        mListNamesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child(Constants.key_db_reference_booklistnames);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    BookListInformation bookListInformation = new BookListInformation(dataSnapshot.getKey(), (String) dataSnapshot.getValue());
                    mBookListInformationAdapter.add(bookListInformation);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    for (BookListInformation bookListInformation : mBookListInformationArray) {
                        if (bookListInformation.getBookListKey().equals(dataSnapshot.getKey())) {
                            bookListInformation.setBookListName((String) dataSnapshot.getValue());
                            mBookListInformationAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    for (BookListInformation bookListInformation : mBookListInformationArray) {
                        if (bookListInformation.getBookListKey().equals(dataSnapshot.getKey())) {
                            mBookListInformationAdapter.remove(bookListInformation);
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
            mListNamesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mListNamesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void showNewBookListDialog() {
        AlertDialog.Builder newBookListDialog = new AlertDialog.Builder(MainActivity.this);
        newBookListDialog.setMessage(R.string.dialog_message_enter_listname);
        // create EditText
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(params);
        // set EditText
        newBookListDialog.setView(input);

        newBookListDialog.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newBookListName = input.getText().toString();
                if (newBookListName.length() != 0) {
                    String newBookListKey = pushListNameToDatabase(newBookListName);
                    startBookListActivity(newBookListKey, newBookListName);
                }
            }
        });
        newBookListDialog.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newBookListDialog.show();
    }

    private String pushListNameToDatabase(String listName) {
        DatabaseReference newBookListReference = mListNamesDatabaseReference.push();
        newBookListReference.setValue(listName);
        return newBookListReference.getKey();
    }

    private void startBookListActivity(int position) {
        Intent bookListIntent = new Intent(MainActivity.this, BookListActivity.class);
        bookListIntent.putExtra(Constants.key_intent_booklistname, mBookListInformationArray.get(position).getBookListName());
        bookListIntent.putExtra(Constants.key_intent_booklistkey, mBookListInformationArray.get(position).getBookListKey());
        startActivity(bookListIntent);
    }

    private void startBookListActivity(String bookListKey, String bookListName) {
        Intent newListIntent = new Intent(MainActivity.this, BookListActivity.class);
        newListIntent.putExtra(Constants.key_intent_booklistname, bookListName);
        newListIntent.putExtra(Constants.key_intent_booklistkey, bookListKey);
        startActivity(newListIntent);
    }

    private void showInternetInformationDialog() {
        AlertDialog.Builder internetInformationDialog = new AlertDialog.Builder(MainActivity.this);
        internetInformationDialog.setTitle(R.string.dialog_title_internet_information).setMessage(R.string.dialog_message_internet_information)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
        internetInformationDialog.show();
    }
}
