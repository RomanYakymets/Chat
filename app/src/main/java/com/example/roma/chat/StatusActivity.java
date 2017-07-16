package com.example.roma.chat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status_textInputLayout);
        mStatus.getEditText().setText(getIntent().getStringExtra("status"));
        mSaveBtn = (Button) findViewById(R.id.status_save_changes_btn);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  if(!TextUtils.isEmpty(mStatus.getEditText().getText())){

                    mProgressDialog = new ProgressDialog(StatusActivity.this);
                    mProgressDialog.setTitle("Saving Changes");
                    mProgressDialog.setTitle("Please vait while we save the changes");
                    mProgressDialog.show();

                    String status = mStatus.getEditText().getText().toString();
                    mDatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mProgressDialog.dismiss();

                            } else{

                                Toast.makeText(getApplicationContext(), "Error in saving changes", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });


               // }


            }
        });

    }
}
