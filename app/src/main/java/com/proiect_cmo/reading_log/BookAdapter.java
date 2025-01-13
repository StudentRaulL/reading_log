package com.proiect_cmo.reading_log;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder>{
    private List<Book> bookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    public void updateList(List<Book> newList) {
        bookList.clear(); // Golește lista actuală
        bookList.addAll(newList); // Adaugă noile elemente
        notifyDataSetChanged(); // Notifică adapterul că lista s-a schimbat
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.bookNumberTextView.setText(String.format("%d.", position + 1));

        holder.nameTextView.setText(book.getName());
        holder.authorTextView.setText("Author: " + book.getAuthor());
        holder.genreTextView.setText("Genre: " + book.getGenre());
        holder.statusTextView.setText("Status: " + book.getStatus());
        holder.userIdTextView.setText("User ID: " + book.getUserId());


        holder.switchStatus.setChecked(book.getStatus().equals("Read"));

        holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "Read" : "Unread";
            book.setStatus(newStatus);

            notifyItemChanged(position);
        });
    }


    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView bookNumberTextView;
        public TextView nameTextView;
        public TextView authorTextView;
        public TextView genreTextView;
        public TextView statusTextView;
        public TextView userIdTextView;

        Switch switchStatus;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookNumberTextView = itemView.findViewById(R.id.bookNumber);
            nameTextView = itemView.findViewById(R.id.bookName);
            authorTextView = itemView.findViewById(R.id.bookAuthor);
            genreTextView = itemView.findViewById(R.id.bookGenre);
            statusTextView = itemView.findViewById(R.id.bookStatus);
            userIdTextView = itemView.findViewById(R.id.bookUserId);
            switchStatus = itemView.findViewById(R.id.switchStatus);
        }
    }
}

