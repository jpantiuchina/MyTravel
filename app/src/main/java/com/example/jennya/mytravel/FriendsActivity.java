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

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                  LocationListener,
                                                                  Callback<List<Friend>>, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener
{
    private final static String TAG = FriendsActivity.class.getCanonicalName();

    private GoogleMap map;
    private FriendsService friendsService;
    private String group = "my-group";
    private String name = "Jenya";


    /**
     * Keeps track of the selected marker.
     */
    private Marker selectedMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);


        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://www2.i-erdve.lt:34712")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        friendsService = retrofit.create(FriendsService.class);
    }


    @Override
    public void onMapReady(GoogleMap map)
    {
        this.map = map;

        // noinspection MissingPermission
        map.setMyLocationEnabled(true);

        // Set listener for marker click event.  See the bottom of this class for its behavior.
        map.setOnMarkerClickListener(this);

        // Set listener for map click event.  See the bottom of this class for its behavior.
        map.setOnMapClickListener(this);
    }



    /******* LOCATION LISTENER *******/


    @Override
    public void onLocationChanged(Location location)
    {
        Log.e(TAG, "Got location " + location.toString());

        friendsService.getMyFriends(group, name, location.getLatitude(), location.getLongitude()).enqueue(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { /* not needed */ }

    @Override
    public void onProviderEnabled(String s) { /* not needed */ }

    @Override
    public void onProviderDisabled(String s) { /* not needed */ }



    /******* WEB SERVICE RESPONSE HANDLER  *******/

    private final List<Marker> markers = new ArrayList<>();

    @Override
    public void onResponse(Call<List<Friend>> call, Response<List<Friend>> response)
    {
        List<Friend> friends = response.body();
        Log.e(TAG, friends.toString());

        for (Marker marker : markers)
        {
            marker.remove();
        }

        for (Friend friend : friends)
        {
            if (!name.equals(friend.name))
            {
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(friend.lat, friend.lng)).title(friend.name);
                Marker marker = map.addMarker(markerOptions);
                markers.add(marker);
            }
        }


    }

    @Override
    public void onFailure(Call<List<Friend>> call, Throwable t)
    {
        Log.e(TAG, "Error contacting friends web service", t);

    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        // The user has re-tapped on the marker which was already showing an info window.
        if (marker.equals(selectedMarker)) {
            // The showing info window has already been closed - that's the first thing to happen
            // when any marker is clicked.
            // Return true to indicate we have consumed the event and that we do not want the
            // the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            selectedMarker = null;
            return true;
        }

        selectedMarker = marker;

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur.
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        // Any showing info window closes when the map is clicked.
        // Clear the currently selected marker.
        selectedMarker = null;
    }


    //----------------


    //--------------------



}


interface FriendsService
{
    @GET("get-my-friends")
    Call<List<Friend>> getMyFriends(@Query("group") String group,
                                    @Query("name")  String name,
                                    @Query("lat")   double lat,
                                    @Query("lng")   double lng);
}


class Friend
{
    public String name;
    public double lat;
    public double lng;
    public String lastUpdated;

    @Override
    public String toString()
    {
        return "Friend{" +
            "name='" + name + '\'' +
            ", lat=" + lat +
            ", lng=" + lng +
            ", lastUpdated='" + lastUpdated + '\'' +
            '}';
    }
}