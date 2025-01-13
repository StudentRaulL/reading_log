package com.proiect_cmo.reading_log;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    // Crează un interface de callback
    public interface DataLoadedCallback {
        void onDataLoaded();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_list);

        Spinner filterSortSpinner = findViewById(R.id.filterSortSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSortSpinner.setAdapter(adapter);

        bookList = new ArrayList<>();

        EditText genreFilterInput = findViewById(R.id.genreFilterInput);
        EditText statusFilterInput = findViewById(R.id.statusFilterInput);
        Button applyFilterButton = findViewById(R.id.applyFilterButton);

        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 coloane

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
                // Nu se face nimic când nu este selectată nicio opțiune
            }
        });

        applyFilterButton.setOnClickListener(v -> {
            String genre = genreFilterInput.getText().toString().trim();
            String status = statusFilterInput.getText().toString().trim();

            if (!genre.isEmpty()) {
                filterBooksByGenre(genre);
            } else if (!status.isEmpty()) {
                filterBooksByStatus(status);
            } else {
                // Dacă nu se introduce nimic, afișăm toate cărțile
                loadBooksFromFirestore(new DataLoadedCallback() {
                    @Override
                    public void onDataLoaded() {
                        bookAdapter.notifyDataSetChanged(); // Notifică adapterul că datele s-au încărcat
                    }
                });
            }
        });


        loadBooksFromFirestore(new DataLoadedCallback() {
            @Override
            public void onDataLoaded() {
                bookAdapter.notifyDataSetChanged(); // Notifică adapterul că datele s-au încărcat
            }
        });

        Button addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(v -> showAddBookDialog());
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
        bookAdapter.updateList(filteredList); // Actualizează adapterul cu lista filtrată
    }

    private void filterBooksByStatus(String status) {
        List<Book> filteredList = new ArrayList<>();
        for (Book book : bookList) {
            if (book.getStatus() != null && book.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(book);
            }
        }
        bookAdapter.updateList(filteredList); // Actualizează adapterul cu lista filtrată
    }


    private void showAddBookDialog() {
        // Creează dialogul
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_book);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.5));
        }
        dialog.setCancelable(true);

        // Referințe la elementele din dialog
        EditText titleInput = dialog.findViewById(R.id.editTextBookTitle);
        EditText authorInput = dialog.findViewById(R.id.editTextBookAuthor);
        EditText genreInput = dialog.findViewById(R.id.editTextBookGenre);
        Button saveButton = dialog.findViewById(R.id.saveBookButton);

        // Setare logică pentru salvarea cărții
        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String author = authorInput.getText().toString().trim();
            String genre = genreInput.getText().toString().trim();

            if (!title.isEmpty() && !author.isEmpty() && !genre.isEmpty()) {

                Book newBook = new Book();
                newBook.setName(title);
                newBook.setAuthor(author);
                newBook.setGenre(genre);
                newBook.setStatus("Available");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document("JIZq0thaESEbRSSRKnBy");  // Înlocuiește cu ID-ul corect
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


    private void loadBooksFromFirestore(final DataLoadedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("books")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear(); // Golește lista înainte de a adăuga date noi
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
                            if (callback != null) {
                                callback.onDataLoaded(); // Apelează callback-ul după încărcare
                            }
                        }
                    } else {
                        System.err.println("Firestore task failed: " + task.getException());
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

}
