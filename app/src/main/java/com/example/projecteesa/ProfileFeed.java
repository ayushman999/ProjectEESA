package com.example.projecteesa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteesa.Fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFeed extends AppCompatActivity {
    Button logout, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_feed);

        logout = findViewById(R.id.logout);
        b2 = findViewById(R.id.profileVisit);

        b2.setOnClickListener(v -> startActivity(new Intent(ProfileFeed.this, ProfileFragment.class)));
        logout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
    }
}