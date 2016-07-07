package com.example.david.projectroomate;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by David on 6/28/2016.
 */
public class PaymentActivity  extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    private final String TAG = "PAYMENT_ACTIVITY";
    private final String LOBBY_TABLE = "lobby";
    private final String PAYMENT_TABLE = "payment";
    private final String MEMBER_CHILD = "Member";
    private final String RENT = "rent";
    private final String ELECTRIC = "electric";
    private final String WATER = "water";
    private final String GAS = "gas";
    private final String INTERNET = "internet";

    // Selection
    // Rent - 0, Electric - 1, Water - 2, Gas - 3, Internet - 4
    private int selection;
    private String mLobby;
    private long memberCount;
    private HashMap<String, Boolean> mAllMembers;
    private double add_pay;

    private TextView mPaymentRent;
    private TextView mPaymentElectric;
    private TextView mPaymentWater;
    private TextView mPaymentGas;
    private TextView mPaymentInternet;
    private EditText edittext;

    private String mUsername;
    private DatabaseReference mPaymentReference;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mAllMembers = new HashMap<>();
        add_pay = 0.0;

        memberCount = 1;
        mLobby = getIntent().getExtras().getString("lobby");
        mUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        selection = 0;

        setupText();
        assignEachText();

        final DatabaseReference lobbyReference = FirebaseDatabase.getInstance().getReference()
                .child(LOBBY_TABLE)
                .child(mLobby)
                .child(MEMBER_CHILD);
        lobbyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mAllMembers =
                        (HashMap<String,Boolean>) dataSnapshot.getValue();
                memberCount = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void assignEachText()
    {
        mPaymentReference = FirebaseDatabase.getInstance().getReference()
                .child(PAYMENT_TABLE)
                .child(mLobby)
                .child(mUsername);
        mPaymentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mPaymentReference.child(RENT).setValue(0.001);
                    mPaymentReference.child(ELECTRIC).setValue(0.001);
                    mPaymentReference.child(WATER).setValue(0.001);
                    mPaymentReference.child(GAS).setValue(0.001);
                    mPaymentReference.child(INTERNET).setValue(0.001);
                    return;
                }
                HashMap<String, Double> paymentTable =
                        (HashMap<String, Double>) dataSnapshot.getValue();

                // Displaying Information
                if (paymentTable.containsKey(RENT)) {
                    mPaymentRent.setText
                            ("Rent: $ " + String.format("%.2f", paymentTable.get(RENT)));
                }
                else
                {
                    mPaymentReference.child(RENT).setValue(0.001);
                }
                if (paymentTable.containsKey(ELECTRIC)) {
                    mPaymentElectric.setText
                            ("Electric: $ " + String.format("%.2f", paymentTable.get(ELECTRIC)));
                }
                else
                {
                    mPaymentReference.child(ELECTRIC).setValue(0.001);
                }
                if (paymentTable.containsKey(WATER)) {
                    mPaymentWater.setText
                            ("Water: $ " + String.format("%.2f", paymentTable.get(WATER)));
                }
                else
                {
                    mPaymentReference.child(WATER).setValue(0.001);
                }
                if (paymentTable.containsKey(GAS)) {
                    mPaymentGas.setText
                            ("Gas: $ " + String.format("%.2f", paymentTable.get(GAS)));
                }
                else
                {
                    mPaymentReference.child(GAS).setValue(0.001);
                }
                if (paymentTable.containsKey(INTERNET)) {
                    mPaymentInternet.setText
                            ("Internet: $ " + String.format("%.2f", paymentTable.get(INTERNET)));
                }
                else
                {
                    mPaymentReference.child(INTERNET).setValue(0.001);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.payment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.add_payment_rent:
                selection = 0;
                break;
            case R.id.add_payment_electric:
                selection = 1;
                break;
            case R.id.add_payment_water:
                selection = 2;
                break;
            case R.id.add_payment_gas:
                selection = 3;
                break;
            case R.id.payment_internet:
                selection = 4;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        AlertDialog addItemAlert = addItemBuilder();
        addItemAlert.show();
        return true;
    }


    private void setPayment()
    {
        final Set<String> memberSet = mAllMembers.keySet();
        final DatabaseReference mAllMemberDatabase = FirebaseDatabase.getInstance().getReference()
                .child(PAYMENT_TABLE)
                .child(mLobby);
        mAllMemberDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String key : memberSet)
                {
                    double prevPay = 0.001;
                    switch (selection)
                    {
                        case 0:
                            if (dataSnapshot.child(key).child(RENT).exists())
                            {
                                prevPay = (double) dataSnapshot
                                        .child(key)
                                        .child(RENT)
                                        .getValue();
                            }
                            mAllMemberDatabase
                                    .child(key)
                                    .child(RENT)
                                    .setValue(add_pay + prevPay);
                            break;
                        case 1:
                            if (dataSnapshot.child(key).child(ELECTRIC).exists())
                            {
                                prevPay = (double) dataSnapshot
                                        .child(key)
                                        .child(ELECTRIC)
                                        .getValue();
                            }
                            mAllMemberDatabase
                                    .child(key)
                                    .child(ELECTRIC)
                                    .setValue(add_pay + prevPay);
                            break;
                        case 2:
                            if (dataSnapshot.child(key).child(WATER).exists())
                            {
                                prevPay = (double) dataSnapshot
                                        .child(key)
                                        .child(WATER)
                                        .getValue();
                            }
                            mAllMemberDatabase
                                    .child(key)
                                    .child(WATER)
                                    .setValue(add_pay + prevPay);
                            break;
                        case 3:
                            if (dataSnapshot.child(key).child(GAS).exists())
                            {
                                prevPay = (double) dataSnapshot
                                        .child(key)
                                        .child(GAS)
                                        .getValue();
                            }
                            mAllMemberDatabase
                                    .child(key)
                                    .child(GAS)
                                    .setValue(add_pay + prevPay);
                            break;
                        case 4:
                            if (dataSnapshot.child(key).child(INTERNET).exists())
                            {
                                prevPay = (double) dataSnapshot
                                        .child(key)
                                        .child(INTERNET)
                                        .getValue();
                            }
                            mAllMemberDatabase
                                    .child(key)
                                    .child(INTERNET)
                                    .setValue(add_pay + prevPay);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public AlertDialog addItemBuilder()
    {
        edittext = new EditText(PaymentActivity.this);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder addItemBuilder = new AlertDialog.Builder(PaymentActivity.this);
        addItemBuilder.setCancelable(true);
        addItemBuilder.setMessage("Add a payment for everyone");
        addItemBuilder.setView(edittext);
        addItemBuilder.setPositiveButton(
                "Add",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String addPayment = edittext.getText().toString();
                        Double payment = Double.parseDouble(addPayment);
                        add_pay = payment / memberCount;
                        setPayment();
                        dialog.cancel();
                    }
                }
        );
        addItemBuilder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        return addItemBuilder.create();
    }

    private void setupText()
    {
        mPaymentRent = (TextView) findViewById(R.id.payment_rent);
        mPaymentElectric = (TextView) findViewById(R.id.payment_electric);
        mPaymentWater = (TextView) findViewById(R.id.payment_water);
        mPaymentGas = (TextView) findViewById(R.id.payment_gas);
        mPaymentInternet = (TextView) findViewById(R.id.payment_internet);

        mPaymentRent.setOnClickListener(this);
        mPaymentElectric.setOnClickListener(this);
        mPaymentWater.setOnClickListener(this);
        mPaymentGas.setOnClickListener(this);
        mPaymentInternet.setOnClickListener(this);
    }

    @Override public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.payment_rent:
                mPaymentReference.child(RENT).setValue(0.001);
                break;
            case R.id.payment_electric:
                mPaymentReference.child(ELECTRIC).setValue(0.001);
                break;
            case R.id.payment_water:
                mPaymentReference.child(WATER).setValue(0.001);
                break;
            case R.id.payment_gas:
                mPaymentReference.child(GAS).setValue(0.001);
                break;
            case R.id.payment_internet:
                mPaymentReference.child(INTERNET).setValue(0.001);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
