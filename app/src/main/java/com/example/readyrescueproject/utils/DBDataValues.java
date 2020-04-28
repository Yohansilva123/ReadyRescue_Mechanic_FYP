package com.example.readyrescueproject.utils;

import com.google.firebase.database.DataSnapshot;

public class DBDataValues {
    public DataSnapshot dataSnapshot;

    public void getWorkingDetails(DataSnapshot dataSnapshots) {
        dataSnapshot = dataSnapshots;
    }

}
