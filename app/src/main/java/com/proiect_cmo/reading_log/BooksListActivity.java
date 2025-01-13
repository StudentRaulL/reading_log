package com.proiect_cmo.reading_log;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BooksListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_list);

        // Get logged-in user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return; // If no user is logged in, don't proceed
        }

        Spinner filterSortSpinner = findViewById(R.id.filterSortSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSortSpinner.setAdapter(adapter);

        bookList = new ArrayList<>();

        EditText genreFilterInput = findViewById(R.id.genreFilterInput);

        Spinner statusFilterSpinner = findViewById(R.id.statusFilterSpinner);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilterSpinner.setAdapter(statusAdapter);

        Button applyFilterButton = findViewById(R.id.applyFilterButton);

        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        bookAdapter = new BookAdapter(bookList);
        recyclerView.setAdapter(bookAdapter);

        filterSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0: // Sort by Title
                        sortBooksByTitle();
                        break;
                    case 1: // Sort by Author
                        sortBooksByAuthor();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        applyFilterButton.setOnClickListener(v -> {
            String genre = genreFilterInput.getText().toString().trim();
            String status = statusFilterSpinner.getSelectedItem().toString();

            if (!genre.isEmpty()) {
                filterBooksByGenre(genre);
            } else if (!status.equals("All statuses")) {
                filterBooksByStatus(status);  // Filter by selected status (Read/Unread)
            } else {
                // If no filter is applied, load all books
                loadBooksFromFirestore(userId, null, null);
            }
        });

        loadBooksFromFirestore(userId, null, null);

        Button addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(v -> showAddBookDialog(userId));
    }

    private void sortBooksByTitle() {
        Collections.sort(bookList, (book1, book2) -> book1.getName().compareTo(book2.getName()));
        bookAdapter.notifyDataSetChanged();
    }

    private void sortBooksByAuthor() {
        Collections.sort(bookList, (book1, book2) -> book1.getAuthor().compareTo(book2.getAuthor()));
        bookAdapter.notifyDataSetChanged();
    }

    private void filterBooksByGenre(String genre) {
        List<Book> filteredList = new ArrayList<>();
        for (Book book : bookList) {
            if (book.getGenre() != null && book.getGenre().equalsIgnoreCase(genre)) {
                filteredList.add(book);
            }
        }
        bookAdapter.updateList(filteredList);
    }

    private void filterBooksByStatus(String status) {
        List<Book> filteredList = new ArrayList<>();
        for (Book book : bookList) {
            if (book.getStatus() != null && book.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(book);
            }
        }
        bookAdapter.updateList(filteredList);
    }

    private void showAddBookDialog(String userId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_book);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.5));
        }
        dialog.setCancelable(true);

        EditText titleInput = dialog.findViewById(R.id.editTextBookTitle);
        EditText authorInput = dialog.findViewById(R.id.editTextBookAuthor);
        EditText genreInput = dialog.findViewById(R.id.editTextBookGenre);
        Button saveButton = dialog.findViewById(R.id.saveBookButton);

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String author = authorInput.getText().toString().trim();
            String genre = genreInput.getText().toString().trim();

            if (!title.isEmpty() && !author.isEmpty() && !genre.isEmpty()) {
                Book newBook = new Book();
                newBook.setName(title);
                newBook.setAuthor(author);
                newBook.setGenre(genre);
                newBook.setStatus("Unread");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(userId); // Use the logged-in user's ID
                newBook.setUserId(userRef);

                db.collection("books").add(newBook)
                        .addOnSuccessListener(documentReference -> {
                            bookList.add(newBook);
                            bookAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                            showToast("Book added successfully!");
                        })
                        .addOnFailureListener(e -> {
                            showToast("Failed to add book.");
                            e.printStackTrace();
                        });
            } else {
                showToast("Please fill out all fields.");
            }
        });

        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadBooksFromFirestore(String userId, String genre, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("books")
                .whereEqualTo("userId", db.collection("users").document(userId)) // Filter by user ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot doc : documents) {
                                try {
                                    Book book = doc.toObject(Book.class);
                                    bookList.add(book);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            bookAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Firestore", "Error loading books: ", task.getException());
                    }
                });
    }
}
