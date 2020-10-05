package com.emekachukwulobe.cruddemo2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;

public class ReadNoteActivity extends AppCompatActivity {

    RelativeLayout readNoteBackground;

    TextView textViewTitleRead;
    TextView textViewPriorityRead;
    TextView textViewDescriptionRead;
    TextView textViewTimeRead;

    String editID;

    String title;
    String description;
    int priority;
    long timeStamp;

    Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_note);

        setTitle("Note");

        readNoteBackground = findViewById(R.id.read_note_background);

        textViewTitleRead = findViewById(R.id.text_view_title_read);
        textViewPriorityRead = findViewById(R.id.text_view_priority_read);
        textViewDescriptionRead = findViewById(R.id.text_view_description_read);
        textViewTimeRead = findViewById(R.id.text_view_time_read);

        if (getIntent().getStringExtra("EXTRA_ID") != null) {
            editID = getIntent().getStringExtra("EXTRA_ID");

            title = getIntent().getStringExtra("EXTRA_TITLE");
            description = getIntent().getStringExtra("EXTRA_DESCRIPTION");
            priority = getIntent().getIntExtra("EXTRA_PRIORITY",1);
            timeStamp = getIntent().getLongExtra("EXTRA_TIME", 0);

            textViewTitleRead.setText(title);
            textViewDescriptionRead.setText(description);
            textViewPriorityRead.setText(String.valueOf(priority));
//            textViewTimeRead.setText(String.valueOf(timeStamp));

            String mySpecialDTFormat = DateFormat.getTimeInstance(DateFormat.SHORT).format(timeStamp)
                    + " - "
                    + DateFormat.getDateInstance(DateFormat.MONTH_FIELD).format(timeStamp);
            textViewTimeRead.setText(mySpecialDTFormat);

            if (priority == 3){
                readNoteBackground.setBackgroundColor(Color.rgb(255, 219, 219));
                textViewPriorityRead.setText(R.string.critical);
                textViewPriorityRead.setTextColor(Color.rgb(243, 0, 0));
                textViewTimeRead.setTextColor(Color.rgb(243, 0, 0));
            } else if (priority == 2) {
                readNoteBackground.setBackgroundColor(Color.rgb(255, 254, 219));
                textViewPriorityRead.setText(R.string.important);
                textViewPriorityRead.setTextColor(Color.rgb(134, 138, 0));
                textViewTimeRead.setTextColor(Color.rgb(134, 138, 0));
            } else {
                readNoteBackground.setBackgroundColor(Color.rgb(219, 255, 251));
                textViewPriorityRead.setText(R.string.normal);
                textViewPriorityRead.setTextColor(Color.rgb(0, 163, 243));
                textViewTimeRead.setTextColor(Color.rgb(0, 163, 243));
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.read_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ((item.getItemId())){
            case R.id.edit_note:
                editNote();
                finish();
                return true;
            case R.id.delete_note:
                deleteNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteNote() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        DocumentReference noteRef = FirebaseFirestore.getInstance()
                .collection("UserNotes")
                .document(user.getUid())
                .collection("Notebook")
                .document(editID);

        Task<Void> deleteTask = noteRef.delete().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                finish();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ReadNoteActivity.this, "Note deleted successfully.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ReadNoteActivity.this,
                        "Note couldn't be deleted\n" + e.getMessage(),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

//        if (deleteTask.isComplete()){
//            finish();
//        }
        finish();
    }

    private void editNote() {
        Intent intent = new Intent(this, NewNoteActivity.class);

        intent.putExtra("EXTRA_TITLE", title);
        intent.putExtra("EXTRA_DESCRIPTION", description);
        intent.putExtra("EXTRA_PRIORITY", priority);
        intent.putExtra("EXTRA_ID", editID);

        startActivity(intent);
    }
}
