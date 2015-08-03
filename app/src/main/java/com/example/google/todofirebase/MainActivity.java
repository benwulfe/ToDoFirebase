package com.example.google.todofirebase;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.google.todofirebase.databinding.ActivityMainBinding;
import com.firebase.client.Firebase;

import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        firebase = new Firebase("https://incandescent-torch-2575.firebaseio.com/messages");
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setFirebaseQuery(firebase);
        binding.activityUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            HashMap<String, Object> newItem = new HashMap<>();
            newItem.put("date", new Date().toString());
            newItem.put("message", "<new item>");
            firebase.push().setValue(newItem);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
