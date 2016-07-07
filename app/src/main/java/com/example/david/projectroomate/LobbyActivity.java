package com.example.david.projectroomate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David on 7/4/2016.
 */
public class LobbyActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    private static final String TAG = "LOBBY_ACTIVITY";
    private static final String LOBBY_TABLE = "lobby";
    private DatabaseReference mDatabaseReference;

    private EditText mLobbySearchText;
    private EditText mPassword;
    private TextView mPasswordText;
    private TextView mTipsText;
    private Button mSearchButton;
    private Button mCreateButton;
    private Button mPasswordButton;

    private String mUsername;
    private String mLobbyPassword;
    private String mLobbyName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        mSearchButton = (Button) findViewById(R.id.find_lobby);
        mCreateButton = (Button) findViewById(R.id.create_lobby);
        mPasswordButton = (Button) findViewById(R.id.password_confirm);
        mLobbySearchText = (EditText) findViewById(R.id.lobby_search_text);
        mPassword = (EditText) findViewById(R.id.lobby_password);
        mTipsText = (TextView) findViewById(R.id.lobby_tips);
        mPasswordText = (TextView) findViewById(R.id.password_text);

        mLobbyName = "";
        mUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        defaultButtonSetup();

        mLobbySearchText.setFilters(new InputFilter[]
                {
                        new InputFilter.LengthFilter(256)
                });
        mLobbySearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mSearchButton.setEnabled(true);
                    mCreateButton.setEnabled(true);
                }
                else {
                    mSearchButton.setEnabled(false);
                    mCreateButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mSearchButton.setOnClickListener(this);
        mCreateButton.setOnClickListener(this);
        mPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.find_lobby:
                defaultButtonSetup();
                join_lobby();
                break;
            case R.id.create_lobby:
                defaultButtonSetup();
                create_lobby();
                break;
            case R.id.password_confirm:
                confirmPassword();
                break;
        }
    }

    public void create_lobby()
    {
        mLobbyName = mLobbySearchText.getText().toString().trim();
        final DatabaseReference lobbyDatabaseReference = mDatabaseReference
                .child(LOBBY_TABLE);

        lobbyDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mLobbyName))
                {
                    // new user data entry
                    Map<String, String> newLobbyMap = new HashMap<String, String>();
                    newLobbyMap.put("Name", mLobbyName);
                    newLobbyMap.put("Password", "");
                    lobbyDatabaseReference.child(mLobbyName).setValue(newLobbyMap);
                    lobbyDatabaseReference.child(mLobbyName)
                            .child("Member")
                            .child(mUsername)
                            .setValue(true);
                    activityFinish();
                }
                else
                {
                    mTipsText.setText("Lobby Exists!");
                    mTipsText.setTextColor(Color.RED);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void confirmPassword()
    {
        final String userpwd = mPassword.getText().toString();
        if (userpwd.equals(mLobbyPassword))
        {
            mDatabaseReference
                    .child(LOBBY_TABLE)
                    .child(mLobbyName)
                    .child("Member")
                    .child(mUsername)
                    .setValue(true);
            activityFinish();
        }
        else
        {
            mTipsText.setEnabled(true);
            mTipsText.setVisibility(View.VISIBLE);
            mTipsText.setText("Wrong Password");
            mTipsText.setTextColor(Color.RED);
        }
    }

    public void activityFinish()
    {
        Intent data = new Intent();
        data.putExtra("LobbyName", mLobbyName);
        if (getParent() == null)
        {
            setResult(Activity.RESULT_OK, data);
        }
        else
        {
            getParent().setResult(Activity.RESULT_OK, data);
        }
        finish();
    }

    public void defaultButtonSetup()
    {
        mPasswordButton.setVisibility(View.INVISIBLE);
        mPasswordButton.setEnabled(false);
        mPasswordText.setVisibility(View.INVISIBLE);
        mTipsText.setText("");
        mPassword.setVisibility(View.INVISIBLE);
        mPassword.setEnabled(false);
        mLobbyPassword = "";
    }

    public void join_lobby()
    {
        mLobbyName = mLobbySearchText.getText().toString().trim();
        final DatabaseReference lobbyDatabaseReference = mDatabaseReference
                .child(LOBBY_TABLE)
                .child(mLobbyName);
        lobbyDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    mTipsText.setText("Lobby does not exist!");
                    mTipsText.setTextColor(Color.RED);
                    return;
                }
                else
                {
                    mLobbyPassword = dataSnapshot.child("Password").getValue(String.class);
                    mPassword.setEnabled(true);
                    mPassword.setVisibility(View.VISIBLE);
                    mPasswordText.setVisibility(View.VISIBLE);
                    mPasswordButton.setEnabled(true);
                    mPasswordButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
