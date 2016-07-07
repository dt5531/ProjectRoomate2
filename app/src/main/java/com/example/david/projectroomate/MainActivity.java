package com.example.david.projectroomate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    public static final String ANONYMOUS = "anonymous";
    public static final String MEMBER_CHILD = "Member";
    public static final String LOBBY_CHILD = "Lobby";
    public static final String PAYMENT_TABLE = "payment";
    public static final String LOBBY_TABLE = "lobby";
    public static final String USER_TABLE = "user";
    public static final String TAG = "MAIN_ACTIVITY";
    private static final String LOBBY_LESS = "NOT_IN_LOBBY";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_LOBBY = 2;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private String mLobby;

    private GoogleApiClient mGoogleApiClient;

    private Button mProfileButton;
    private Button mChatButton;
    private Button mTodoButton;
    private Button mPaymentButton;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null)
        {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        else
        {
            mUsername = mFirebaseUser.getDisplayName();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        mLobby = LOBBY_LESS;
        // Set up the database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // get reference of Userdatabase Table
        final DatabaseReference userDatabaseReference = mFirebaseDatabaseReference
                .child(USER_TABLE)
                .child(mFirebaseAuth.getCurrentUser().getUid());
        userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // User Exists, continue, if not create users database
                if (!dataSnapshot.exists())
                {
                    // new user data entry
                    Map<String, String> newUserMap = new HashMap<String, String>();
                    newUserMap.put("Name", mUsername);
                    newUserMap.put("Email", mFirebaseUser.getEmail());
                    newUserMap.put("Lobby", LOBBY_LESS);
                    mLobby = LOBBY_LESS;
                    userDatabaseReference.setValue(newUserMap);
                }
                else
                {
                    mLobby = dataSnapshot.child("Lobby").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Set up the 4 buttons
        mProfileButton = (Button) findViewById(R.id.profilePage);
        mProfileButton.setOnClickListener(this);
        mChatButton = (Button) findViewById(R.id.chatButton);
        mChatButton.setOnClickListener(this);
        mTodoButton = (Button) findViewById(R.id.todoButton);
        mTodoButton.setOnClickListener(this);
        mPaymentButton = (Button) findViewById(R.id.paymentButton);
        mPaymentButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (mLobby.equals(LOBBY_LESS))
        {
            // start activity for choosing a Lobby
            Intent intent = new Intent(this, LobbyActivity.class);
            startActivityForResult(intent, REQUEST_LOBBY);
            return;
        }
        switch (v.getId())
        {
            case R.id.profilePage:
                redirectProfile();
                break;
            case R.id.chatButton:
                redirectChat();
                break;
            case R.id.todoButton:
                redirectTodo();
                break;
            case R.id.paymentButton:
                redirectPayment();
                break;
        }
    }

    private void redirectProfile()
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("username", mUsername);
        intent.putExtra("email", mFirebaseAuth.getCurrentUser().getEmail());
        intent.putExtra("lobby", mLobby);
        startActivity(intent);
        return;
    }

    private void redirectChat()
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("lobby", mLobby);
        startActivity(intent);
        return;
    }

    private void redirectTodo()
    {
        Intent intent = new Intent(this, TodoActivity.class);
        intent.putExtra("lobby", mLobby);
        startActivity(intent);
        return;
    }

    private void redirectPayment()
    {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("lobby", mLobby);
        startActivity(intent);
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                mLobby = LOBBY_LESS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.leave_lobby:
                leaveLobby();
                Intent intent = new Intent(this, LobbyActivity.class);
                startActivityForResult(intent, REQUEST_LOBBY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void leaveLobby()
    {
        mFirebaseDatabaseReference
                .child(USER_TABLE)
                .child(mFirebaseUser.getUid())
                .child(LOBBY_CHILD)
                .setValue(LOBBY_LESS);
        mFirebaseDatabaseReference
                .child(PAYMENT_TABLE)
                .child(mLobby)
                .child(mUsername)
                .setValue(null);
        mFirebaseDatabaseReference
                .child(LOBBY_TABLE)
                .child(mLobby)
                .child(MEMBER_CHILD)
                .child(mUsername)
                .setValue(null);
    }

    private void sendInvitation()
    {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent.
                String[] ids = AppInviteInvitation
                        .getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Sending failed or it was canceled, show failure message to
                // the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
        if (requestCode == REQUEST_LOBBY)
        {
            // Get the LobbyName and store it to personal profile
            mLobby = data.getStringExtra("LobbyName");
            mFirebaseDatabaseReference
                    .child(USER_TABLE)
                    .child(mFirebaseAuth.getCurrentUser().getUid())
                    .child("Lobby")
                    .setValue(mLobby);
        }
    }
}
