package com.example.david.projectroomate;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 * Created by David on 6/28/2016.
 */
public class ProfileActivity  extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener
{

    private static final String TAG = "PROFILE_ACTIVITY";
    public static final String LOBBY_LIST = "lobby";

    private DatabaseReference mLobbyReference;

    private String mLobby;

    private Button mPasswordButton;
    private TextView mUsername;
    private TextView mEmail;
    private TextView mLobbyName;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setup();
    }

    public void showInfo()
    {
        Bundle b = getIntent().getExtras();
        mUsername.setText("Username: \n" + b.getString("username"));
        mEmail.setText("Email: \n" + b.getString("email"));
        mLobby = b.getString("lobby");
        mLobbyName.setText("Lobby: \n" + mLobby + "\n\nPassword:");
        mPassword.setText("");
    }

    public void buttonSetup()
    {
        mUsername = (TextView) findViewById(R.id.profileUsername);
        mEmail = (TextView) findViewById(R.id.profileEmail);
        mLobbyName =  (TextView) findViewById(R.id.profileLobby);
        mPassword = (EditText) findViewById(R.id.profilePassword);
        mPasswordButton = (Button) findViewById(R.id.profileUpdate);
        mPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0)
                {
                    mPasswordButton.setEnabled(true);
                }
                else
                {
                    mPasswordButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setup()
    {
       buttonSetup();
        showInfo();
        mLobbyReference = FirebaseDatabase.getInstance().getReference()
                .child(LOBBY_LIST).child(mLobby);
        mPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLobbyReference.child("Password").setValue(mPassword.getText().toString());
                mPassword.setText("");
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
