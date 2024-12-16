package com.proiect_cmo.reading_log;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText inputUser;
    private EditText inputPassword;
    private Button saveInfoButton;
    private Button loadButton;
    private TextView textDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a first, middle, and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Alan");
        user.put("middle", "Mathison");
        user.put("last", "Turing");
        user.put("born", 1912);

// Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        inputUser = findViewById(R.id.inputUser);
        inputPassword = findViewById(R.id.inputPassword);
        saveInfoButton = findViewById(R.id.saveInfo);
        loadButton = findViewById(R.id.loadButton);
        textDisplay = findViewById(R.id.textDisplay);

        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());

        saveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = inputUser.getText().toString();
                String password = inputPassword.getText().toString();

                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("username", username);
                values.put("password", password);

                long newRowId = db.insert("users", null, values);

                if (newRowId != -1) {
                    Toast.makeText(getApplicationContext(), "Information saved", Toast.LENGTH_SHORT).show();
                    inputUser.setText("");
                    inputPassword.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Error saving information", Toast.LENGTH_SHORT).show();
                }

                db.close();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                try {
                    Cursor cursor = db.rawQuery("SELECT * FROM users", null);

                    if (cursor != null && cursor.moveToFirst()) {

                        StringBuilder output = new StringBuilder();
                        while (cursor.moveToNext()) {
                            String user = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                            output.append(user).append(" : ").append(password).append("\n");
                        }
                        cursor.close();
                        db.close();

                        textDisplay.setText(output.toString());
                    } else {
                        // Empty database or read error.
                        Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle database access/permissions error
                    Toast.makeText(getApplicationContext(), "Database error", Toast.LENGTH_SHORT).show();
                } finally {
                    db.close(); // Ensure the database is closed
                }
            }
        });

    }
}