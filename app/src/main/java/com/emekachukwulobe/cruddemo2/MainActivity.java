package com.emekachukwulobe.cruddemo2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private CollectionReference notebookRef = db.collection("Notebook");
private CollectionReference notebookRef;

    private NoteAdapter adapter;

    private static final int RC_SIGN_IN = 123;

    FirestoreRecyclerOptions<Note> options1;
    FirestoreRecyclerOptions<Note> options2;

    ImageView imageViewNoteIllustration;

    TextView profileNameTextView;
    TextView profileEmailTextView;
    CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageViewNoteIllustration = findViewById(R.id.image_view_note_illustration);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);
        profileNameTextView = navHeader.findViewById(R.id.profile_name);
        profileEmailTextView = navHeader.findViewById(R.id.profile_email);
        profileImageView = navHeader.findViewById(R.id.profile_image);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in

            // Show user info on the nav menu
            updateNavHeaderViews();

            FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
            buttonAddNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, NewNoteActivity.class));
                }
            });

            setUpRecyclerView();
        } else {
            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.FacebookBuilder().build()))
                            .setLogo(R.drawable.ic_notebook)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private void updateNavHeaderViews() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        profileNameTextView.setText(user.getDisplayName());
        profileEmailTextView.setText(user.getEmail());

        // TODone: Load profile Image
        try {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_account_white)
                    .into(profileImageView);
        } catch (Exception e){
            Toast.makeText(this, "Could not load an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpRecyclerView() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        notebookRef = db.collection("UserNotes").document(user.getUid()).collection("Notebook");

        Query query1 = notebookRef.orderBy("date", Query.Direction.DESCENDING);
        Query query2 = notebookRef.orderBy("priority", Query.Direction.DESCENDING).orderBy("date", Query.Direction.DESCENDING);

        options1 = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query1, Note.class)
                .build();

        options2 = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query2, Note.class)
                .build();

        adapter = new NoteAdapter(options1);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
                changeIllustrationVisibility();
            }
        });

        recyclerView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                changeIllustrationVisibility();
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                changeIllustrationVisibility();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
                Toast.makeText(MainActivity.this, "Note deleted successfully.", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Note note = documentSnapshot.toObject(Note.class);
                String id = documentSnapshot.getId();

//                Toast.makeText(MainActivity.this,
//                        "Position: " + position + " ID: " + id, Toast.LENGTH_SHORT)
//                        .show();

                Intent intent = new Intent(MainActivity.this, ReadNoteActivity.class);
                assert note != null;
                intent.putExtra("EXTRA_TITLE", note.getTitle());
                intent.putExtra("EXTRA_DESCRIPTION", note.getDescription());
                intent.putExtra("EXTRA_PRIORITY", note.getPriority());
                intent.putExtra("EXTRA_ID", id);
                intent.putExtra("EXTRA_TIME", note.getDate().getTime());

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            if (adapter != null)
                adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.create_new_note:
                startActivity(new Intent(MainActivity.this, NewNoteActivity.class));
                return true;
            case R.id.delete_all_notes:
                deleteAllNotes();
                return true;
            case R.id.sort_by_modified_time:
                sortByModifiedTime();
                return true;
            case R.id.sort_by_priority:
                sortByPriority();
                return true;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortByModifiedTime() {
        adapter.updateOptions(options1);
    }

    private void sortByPriority() {
        adapter.updateOptions(options2);
    }

    private void deleteAllNotes() {
        // TODO: write code to delete all notes here

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        DocumentReference allNotesRef = FirebaseFirestore.getInstance()
                .collection("UserNotes")
                .document(user.getUid());

        CollectionReference collectionReference = FirebaseFirestore.getInstance()
                .collection("UserNotes")
                .document(user.getUid())
                .collection("Notebook");

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    documentSnapshot.getReference().delete();
                }
                Toast.makeText(MainActivity.this, "Deleted all notes successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "An error occurred. Couldn't delete all notes.\n" + e.getMessage(),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });





//        allNotesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Toast.makeText(MainActivity.this, "Deleted all notes successfully", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MainActivity.this,
//                        "An error occurred. Couldn't delete all notes.\n" + e.getMessage(),
//                        Toast.LENGTH_SHORT)
//                        .show();
//            }
//        });
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        // TODO: Looks ridiculous ... confirm this ...  but if it makes snese and works, wrap it in a function
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                new AuthUI.IdpConfig.FacebookBuilder().build()))
                                        .setLogo(R.drawable.ic_notebook)
                                        .build(),
                                RC_SIGN_IN);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null)
                    Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                updateNavHeaderViews();


                // todo function it!
                // From the on create
                FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
                buttonAddNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, NewNoteActivity.class));
                    }
                });

                setUpRecyclerView();
                adapter.startListening();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response != null)
                    Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                finish();

//                // TODO: Looks ridiculous ... confirm this
//                startActivityForResult(
//                        AuthUI.getInstance()
//                                .createSignInIntentBuilder()
//                                .setAvailableProviders(Arrays.asList(
//                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
//                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
//                                .build(),
//                        RC_SIGN_IN);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
//        super.onBackPressed();
    }


    private void changeIllustrationVisibility(){
        if (adapter.getItemCount() > 0 && adapter != null) {
            imageViewNoteIllustration.setVisibility(View.INVISIBLE);
        } else {
            imageViewNoteIllustration.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_create_note:
                startActivity(new Intent(MainActivity.this, NewNoteActivity.class));
                break;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.nav_exit:
                finish();
                break;

            case R.id.nav_sign_out:
                signOut();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

}
