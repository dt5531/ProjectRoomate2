package com.example.david.projectroomate;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by David on 6/28/2016.
 */
public class TodoActivity  extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener
{
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects)
        {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i)
            {
                mIdMap.put(objects.get(i), i);
            }
        }

        public void addItem(String key, int a)
        {
            mIdMap.put(key, 0);
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private static final String TAG = "TODO ACTIVITY";
    private static final String TODO = "todo";

    private ArrayList<String> list;
    private StableArrayAdapter adapter;
    private ListView listview;

    private String mLobby_name;
    private DatabaseReference mTodoDatabaseReference;

    private String addItem = "";
    private EditText edittext;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        listview = (ListView) findViewById(R.id.listview);

        mLobby_name = getIntent().getExtras().getString("lobby");
        mTodoDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(TODO)
                .child(mLobby_name);


        // the values disaplayed TODO will need to change it to dynamic generated
        list = new ArrayList<String>();
        String[] values = new String[0];


        adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_2, list);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(1000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                mTodoDatabaseReference.child(item).setValue(null);
                                list.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }

        });
        mTodoDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    return;
                }
                list = new ArrayList<String>();
                HashMap<String, Integer> todoItems =
                        (HashMap<String, Integer>) dataSnapshot.getValue();
                for (Map.Entry<String, Integer> entry : todoItems.entrySet())
                {
                    String itemName = entry.getKey();
                    list.add(itemName);
                    //Integer number =  entry.getValue();
                }
                refresh();
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
        inflater.inflate(R.menu.todo_menu, menu);
        return true;
    }

    public AlertDialog addItemBuilder()
    {
        edittext = new EditText(TodoActivity.this);
        AlertDialog.Builder addItemBuilder = new AlertDialog.Builder(TodoActivity.this);
        addItemBuilder.setCancelable(true);
        addItemBuilder.setMessage("Add an item");
        addItemBuilder.setView(edittext);
        addItemBuilder.setPositiveButton(
                "Submit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addItem = edittext.getText().toString();
                        mTodoDatabaseReference.child(addItem).setValue(1);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.add_item:
                AlertDialog addItemAlert = addItemBuilder();
                addItemAlert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void refresh()
    {
        adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
