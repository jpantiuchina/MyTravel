package com.example.jennya.mytravel;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class FriendListActivity extends ListActivity
{
    private final static String TAG = FriendListActivity.class.getCanonicalName();

    private static final int LIST_ITEM = R.layout.friend_list_item;
    private static final String[] FROM = {ContactsContract.Contacts.DISPLAY_NAME};
    private static final int[]    TO   = {android.R.id.text1};


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);

        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        ListAdapter listAdapter = new SimpleCursorAdapter(this, LIST_ITEM, cursor, FROM, TO,0);

        setListAdapter(listAdapter);
    }

}




