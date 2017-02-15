package com.example.jennya.mytravel;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class FriendListActivity extends ListActivity
{
    private final static String TAG = FriendListActivity.class.getCanonicalName();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);

        Contacts contacts = new Contacts();

        contacts.loadContacts(getContentResolver());


//        @SuppressWarnings("deprecation")
//        Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//
//        ListAdapter listAdapter = new SimpleCursorAdapter(this, LIST_ITEM, cursor, FROM, TO,0);
//
//        setListAdapter(listAdapter);
    }




}




