package com.example.merka;

import android.media.Image;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String name;
    public String email;
    public String password;
    public boolean store;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String password, boolean store) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.store=store;
    }
}
