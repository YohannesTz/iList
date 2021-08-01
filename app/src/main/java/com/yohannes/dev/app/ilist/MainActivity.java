package com.yohannes.dev.app.ilist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.yohannes.dev.app.ilist.activities.AddEditNoteActivity;
import com.yohannes.dev.app.ilist.activities.WelcomeActivity;
import com.yohannes.dev.app.ilist.adapters.NoteAdapter;
import com.yohannes.dev.app.ilist.data.Note;
import com.yohannes.dev.app.ilist.viewmodels.NoteViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NoteViewModel noteViewModel;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityResultLauncher<Intent> addNoteResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                        String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
                        int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);
                        String firebaseid = data.getStringExtra(AddEditNoteActivity.EXTRA_FIREBASEID);


                        Note note = new Note(firebaseid, title, description, priority);
                        noteViewModel.insert(note);
                        Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
                    }
                });

        ActivityResultLauncher<Intent> editNoteResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

                        if(id == -1) {
                            Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                        String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
                        int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);
                        String firebaseid = data.getStringExtra(AddEditNoteActivity.EXTRA_FIREBASEID);

                        Note note = new Note(firebaseid, title, description, priority);
                        note.setId(id);
                        noteViewModel.update(note);

                        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show();
                    }
                });

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(view -> {
            Intent addIntent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            addNoteResultLauncher.launch(addIntent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> adapter.setNotes(notes));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(note -> {
            Intent editIntent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            editIntent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
            editIntent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
            editIntent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
            editIntent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());
            editNoteResultLauncher.launch(editIntent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllNotes:
                noteViewModel.deleteAllNotes();
                Toast.makeText(this, "All notes Deleted", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                finish();
            /*case R.id.syncCloud:
                syncWithCloud();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void syncWithCloud(){

    }
}