package com.example.roma.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roma.chat.data.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRequestBtn;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrentFirebaseUser;

    private int mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id =  getIntent().getStringExtra("user_id");
//        mToolbar = (Toolbar) findViewById(R.id.profile_app_bar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("Profile " + user_id);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mCurrentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_name_tv);
        mProfileStatus = (TextView) findViewById(R.id.profile_status_tv);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_friends_tv);
        mProfileSendRequestBtn = (Button) findViewById(R.id.profile_send_req_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mCurrentState = Constants.NOT_FRIEND;

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                mProfileFriendsCount.setText(display_name);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                // Friends List
                mFriendReqDatabase.child(mCurrentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){

                            Integer req_type =Integer.valueOf(dataSnapshot.child(user_id).child("request_type").getValue().toString());

                            if(req_type == Constants.RECEIVE_FRIEND_REQ){

                                mCurrentState = Constants.RECEIVE_FRIEND_REQ;
                                mProfileSendRequestBtn.setText("Accept Friend Request");

                            }else if(req_type == Constants.SEND_FRIEND_REQ){

                                mCurrentState = Constants.RECEIVE_FRIEND_REQ;
                                mProfileSendRequestBtn.setText("Cancel Friend Request");

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mProgressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendRequestBtn.setEnabled(false);
                // Not friend
                if(mCurrentState == Constants.NOT_FRIEND){

                    onClickIfNotFriend(user_id);
                }
                // Cancel Request state
                if(mCurrentState == Constants.SEND_FRIEND_REQ){

                    onClickIfSendFriendReq(user_id, Constants.NOT_FRIEND, "Send Friend Request");
                }
                // Request received state
                if(mCurrentState == Constants.RECEIVE_FRIEND_REQ){

                    onClickIfReceiveFriendReq(user_id);
                }
            }
        });


    }

    private void onClickIfReceiveFriendReq(final String user_id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        final String currentData = sdf.format(new Date());
        mFriendDatabase.child(mCurrentFirebaseUser.getUid()).child(user_id).setValue(currentData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendDatabase.child(user_id).child(mCurrentFirebaseUser.getUid())
                                .setValue(currentData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        onClickIfSendFriendReq(user_id, Constants.FRIEND, "Remove this person from friends");
                                    }
                                });
                    }
                });
    }

    private void onClickIfSendFriendReq(final String user_id, final int mStateSet, final String mButtonTxt) {
        mFriendReqDatabase.child(mCurrentFirebaseUser.getUid()).child(user_id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendReqDatabase.child(user_id).child(mCurrentFirebaseUser.getUid()).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProfileSendRequestBtn.setEnabled(true);
                                        mCurrentState = mStateSet;
                                        mProfileSendRequestBtn.setText(mButtonTxt);
                                    }
                                });
                        mProfileSendRequestBtn.setEnabled(true);
                    }
        });
    }

    private void onClickIfNotFriend(final String user_id) {
        mFriendReqDatabase.child(mCurrentFirebaseUser.getUid())
                .child(user_id)
                .child("request_type")
                .setValue(Constants.SEND_FRIEND_REQ)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            mFriendReqDatabase.child(user_id)
                                    .child(mCurrentFirebaseUser.getUid())
                                    .child("request_type")
                                    .setValue(Constants.RECEIVE_FRIEND_REQ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(ProfileActivity.this, "Request sent successfully", Toast.LENGTH_SHORT).show();

                                    mCurrentState = Constants.SEND_FRIEND_REQ;
                                    mProfileSendRequestBtn.setText("Cancel Friend Request");

                                }
                            });

                        }else {
                            Toast.makeText(ProfileActivity.this, "Error while adding friend", Toast.LENGTH_SHORT).show();
                        }
                        mProfileSendRequestBtn.setEnabled(true);
                    }
                });
    }
}
