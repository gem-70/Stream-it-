package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.exoplayer2.C;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {


    private RecyclerView recyclerView_comments;
    private Button post_comments;
    private EditText editText_comment_input;
    DatabaseReference databaseReference,postref;
    private String post_key;
    Comments comments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        post_key = getIntent().getExtras().getString("postkey");


        comments = new Comments();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Username");

        postref = FirebaseDatabase.getInstance().getReference().child("video").child(post_key).child("comments");


        recyclerView_comments = findViewById(R.id.recyclerview_comments);
        recyclerView_comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView_comments.setLayoutManager(linearLayoutManager);

        post_comments = findViewById(R.id.comment_btn_post);
        editText_comment_input = findViewById(R.id.comment_et);


        post_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String username = dataSnapshot.child("name").getValue().toString();

                            Commentfeature(username);

                            editText_comment_input.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            private void Commentfeature(String username) {

                String comment_post = editText_comment_input.getText().toString();
                if (comment_post.isEmpty()){
                    Toast.makeText(CommentsActivity.this, "please write a comment", Toast.LENGTH_SHORT).show();
                }else {

                    Calendar callfordate = Calendar.getInstance();
                    SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
                    final String savecurrentdate = currentdate.format(callfordate.getTime());

                    Calendar callfortime = Calendar.getInstance();
                    SimpleDateFormat currenttime  = new SimpleDateFormat("HH:mm");
                    final  String savecurrenttime = currenttime.format(callfortime.getTime());

                    final  String randomkey = userId + savecurrentdate + savecurrenttime;


                    HashMap commentMap = new HashMap();
                    commentMap.put("uid",userId);
                    commentMap.put("commment",comment_post);
                    commentMap.put("date",savecurrentdate);
                    commentMap.put("time",savecurrenttime);
                    commentMap.put("username",username);

                    postref.child(randomkey).updateChildren(commentMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){
                                        Toast.makeText(CommentsActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(CommentsActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments>options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postref,Comments.class)
                        .build();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                        holder.Comments(getApplication(),model.getCommment(),model.getDate(),model.getTime(),model.getUsername());
                    }

                    @NonNull
                    @Override
                    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.comment_item,parent,false);

                        return new CommentsViewHolder(view);
                    }
                };

        firebaseRecyclerAdapter.startListening();
        recyclerView_comments.setAdapter(firebaseRecyclerAdapter);
    }
}





