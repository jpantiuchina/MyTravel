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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

public class ARActivity extends AppCompatActivity
{
    private final static String TAG = ARActivity.class.getCanonicalName();
    private BeyondarFragmentSupport mBeyondarFragment;
    private World mWorld;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
       // requestWindowFeature(Window.FEATURE_NO_TITLE);

        mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.beyondarFragment);



        mWorld = new World(this);

        // User position (you can change it using the GPS listeners form Android API)
        mWorld.setGeoPosition(41.26533734214473d, 1.925848038959814d);

        // Create an object with an image in the app resources.
        GeoObject go1 = new GeoObject(1);
        go1.setGeoPosition(41.26523339794433d, 1.926036406654116d);
        go1.setImageResource(R.drawable.creature_7);
        go1.setName("Creature 1");
        mWorld.addBeyondarObject(go1);

        // Finally we add the Wold data in to the fragment
        mBeyondarFragment.setWorld(mWorld);

        // We also can see the Frames per seconds
        mBeyondarFragment.showFPS(true);
    }


}

