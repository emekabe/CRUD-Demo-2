package com.emekachukwulobe.cruddemo2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewNoteActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle;
    private EditText editTextDescription;

    boolean wannaEdit;
    String editID;

    RadioButton radioButtonNormal;
    RadioButton radioButtonImportant;
    RadioButton radioButtonCritical;

    TextView textViewAuthor;

    int priority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add Note");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);

        radioButtonNormal = findViewById(R.id.radio_button_normal);
        radioButtonImportant = findViewById(R.id.radio_button_important);
        radioButtonCritical = findViewById(R.id.radio_button_critical);

        textViewAuthor = findViewById(R.id.text_view_author);
        textViewAuthor.setVisibility(View.INVISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            textViewAuthor.setText("Author: " + user.getDisplayName());
            textViewAuthor.setVisibility(View.VISIBLE);
        }

        wannaEdit = false;

        radioButtonNormal.setChecked(true);

        if (getIntent().getStringExtra("EXTRA_ID") != null){
            setTitle("Edit Note");

            editTextTitle.setText(getIntent().getStringExtra("EXTRA_TITLE"));
            editTextDescription.setText(getIntent().getStringExtra("EXTRA_DESCRIPTION"));

            priority = getIntent().getIntExtra("EXTRA_PRIORITY",1);

            if (priority == 3){
                radioButtonCritical.setChecked(true);
            } else if (priority == 2){
                radioButtonImportant.setChecked(true);
            } else {
                radioButtonNormal.setChecked(true);
            }

            wannaEdit = true;
            editID = getIntent().getStringExtra("EXTRA_ID");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ((item.getItemId())){
            case R.id.save_note:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        if (radioButtonCritical.isChecked()){
            priority = 3;
        } else if (radioButtonImportant.isChecked()){
            priority = 2;
        } else {
            priority = 1;
        }

        if (description.trim().isEmpty()){
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        title = title.trim().isEmpty() ? "-" : title;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        if (wannaEdit){
            DocumentReference noteRef = FirebaseFirestore.getInstance()
                    .collection("UserNotes")
                    .document(user.getUid())
                    .collection("Notebook")
                    .document(editID);

            Map<String, Object> newNote = new HashMap<>();
            newNote.put("title", title);
            newNote.put("description", description);
            newNote.put("priority", priority);
            newNote.put("date", new Date());

            noteRef.update(newNote);

            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else {
            CollectionReference notebookRef = FirebaseFirestore.getInstance()
                    .collection("UserNotes")
                    .document(user.getUid())
                    .collection("Notebook");
            notebookRef.add(new Note(title, description, priority, new Date()));
            Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        if (editTextDescription.getText().toString().trim().isEmpty()){
            super.onBackPressed();
        } else {
            saveNote();
        }
    }
}
