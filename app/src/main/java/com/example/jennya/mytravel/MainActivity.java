/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jennya.mytravel;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


public final class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    private static final MenuItem[] MAIN_MENU_ITEMS = {
        new MenuItem(R.string.map_label,     R.string.map_description,     MapsActivity.class),
        new MenuItem(R.string.friends_label, R.string.friends_description, FriendsActivity.class),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView list = (ListView) findViewById(R.id.list);

        list.setAdapter(new CustomArrayAdapter(this, MAIN_MENU_ITEMS));
        list.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        MenuItem demo = (MenuItem) parent.getAdapter().getItem(position);
        startActivity(new Intent(this, demo.activityClass));
    }


}


/**
 * A widget that describes an activity that demonstrates a feature.
 */
final class FeatureView extends FrameLayout
{

    /**
     * Constructs a feature view by inflating layout/feature.xml.
     */
    public FeatureView(Context context)
    {
        super(context);

        LayoutInflater layoutInflater =
            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.feature, this);
    }

    /**
     * Set the resource id of the title of the demo.
     *
     * @param titleId the resource id of the title of the demo
     */
    public synchronized void setTitleId(int titleId)
    {
        ((TextView) (findViewById(R.id.title))).setText(titleId);
    }

    /**
     * Set the resource id of the description of the demo.
     *
     * @param descriptionId the resource id of the description of the demo
     */
    public synchronized void setDescriptionId(int descriptionId)
    {
        ((TextView) (findViewById(R.id.description))).setText(descriptionId);
    }

}



/**
 * A simple POJO that holds the details about the demo that are used by the List Adapter.
 */
final class MenuItem
{

    /**
     * The resource id of the title of the demo.
     */
    public final int titleId;

    /**
     * The resources id of the description of the demo.
     */
    public final int descriptionId;

    /**
     * The demo activity's class.
     */
    public final Class<? extends AppCompatActivity> activityClass;

    private int i = 5;

    MenuItem(int titleId, int descriptionId, Class<? extends AppCompatActivity> activityClass)
    {
        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.activityClass = activityClass;
    }
}



/**
 * A custom array adapter that shows a {@link FeatureView} containing details about the demo.
 */
final class CustomArrayAdapter extends ArrayAdapter<MenuItem>
{

    /**
     * @param demos An array containing the details of the demos to be displayed.
     */
    public CustomArrayAdapter(Context context, MenuItem[] demos)
    {
        super(context, R.layout.feature, R.id.title, demos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        FeatureView featureView;
        if (convertView instanceof FeatureView)
        {
            featureView = (FeatureView) convertView;
        } else
        {
            featureView = new FeatureView(getContext());
        }

        MenuItem demo = getItem(position);

        featureView.setTitleId(demo.titleId);
        featureView.setDescriptionId(demo.descriptionId);

        Resources resources = getContext().getResources();
        String title = resources.getString(demo.titleId);
        String description = resources.getString(demo.descriptionId);
        featureView.setContentDescription(title + ". " + description);

        return featureView;
    }
}