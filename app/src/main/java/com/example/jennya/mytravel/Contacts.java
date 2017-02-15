package com.example.jennya.mytravel;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.*;

public final class Contacts
{
    private final static String TAG = Contacts.class.getCanonicalName();


    private final String myPhoneNumber;

    private final Map<String, String> nameByPhoneNumber = new HashMap<>();




    public Contacts(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = telephonyManager.getLine1Number();
        this.myPhoneNumber = myPhoneNumber != null ? myPhoneNumber : "+99999999";

        loadContacts(context);
    }


    public String getMyPhoneNumber()
    {
        return myPhoneNumber;
    }

    public String getNameByPhoneNumber(String phoneNumber)
    {
        return nameByPhoneNumber.get(phoneNumber);
    }

    public Set<String> getAllPhoneNumbers()
    {
        return nameByPhoneNumber.keySet();
    }


    private void loadContacts(Context context)
    {
        Cursor contacts = context.getContentResolver().query(
            ContactsContract.Contacts.CONTENT_URI,
            new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            },
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

                List<String> phoneNumbers = loadContactPhoneNumbers(context, contactId);

                for (String phoneNumber : phoneNumbers)
                {
                    nameByPhoneNumber.put(phoneNumber, contactName);
                }
            }
        }
        finally
        {
            contacts.close();
        }
    }


    private List<String> loadContactPhoneNumbers(Context context, int contactId)
    {
        List<String> result = new ArrayList<>();

        Cursor numbers = context.getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[] {
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            },
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

                String effectivePhoneNumber = normalizedPhoneNumber != null ? normalizedPhoneNumber : phoneNumber;

                result.add(effectivePhoneNumber);
                Log.e(TAG, effectivePhoneNumber);
            }
        }
        finally
        {
            numbers.close();
        }

        return result;
    }





}
