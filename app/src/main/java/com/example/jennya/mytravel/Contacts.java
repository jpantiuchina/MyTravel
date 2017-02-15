package com.example.jennya.mytravel;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public final class Contacts
{
    private final static String TAG = Contacts.class.getCanonicalName();

    private static final String[] CONTACT_COLUMNS = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME
    };

    private static final String[] PHONE_COLUMNS = {
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
    };



    void loadContacts(ContentResolver contentResolver)
    {
        Cursor contacts = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_COLUMNS,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            null,
            null
        );
        try
        {
            while (contacts.moveToNext())
            {
                int    contactId   = contacts.getInt   (0);
                String contactName = contacts.getString(1);

                Log.e(TAG, contactName + ' ' + contactId);

                loadContactPhoneNumbers(contentResolver, contactId);
            }
        }
        finally
        {
            contacts.close();
        }
    }


    private void loadContactPhoneNumbers(ContentResolver contentResolver, int contactId)
    {
        Cursor numbers = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PHONE_COLUMNS,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + '=' + contactId,
            null,
            null
        );
        try
        {
            while (numbers.moveToNext())
            {
                String normalizedPhoneNumber = numbers.getString(0);
                String phoneNumber = numbers.getString(1);

                Log.e(TAG, normalizedPhoneNumber != null ? normalizedPhoneNumber : phoneNumber);

            }
        }
        finally
        {
            numbers.close();
        }

    }
}
