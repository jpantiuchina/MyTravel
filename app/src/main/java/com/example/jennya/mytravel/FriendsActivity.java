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

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class FriendsActivity extends AppCompatActivity
{
    private final static String TAG = FriendsActivity.class.getCanonicalName();

//    private final static String FRIEND_SERVER = "http://www2.i-erdve.lt:34712";
    private final static String FRIEND_SERVER = "http://192.168.45.102:34712";


    private GoogleMap map;
    private FriendsService friendsService;
    private LatLng origin;

    // Keep track of the selected marker.
    private Marker selectedMarker;
    private Polyline currentPathToAFriend;
    private Contacts contacts;
    private final List<Marker> markers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        contacts = new Contacts(this);

        setupMap(); // This calls setupLocationListening when maps is ready

        setupFriendService();
    }


    private void setupMap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                map = googleMap;

                map.setMyLocationEnabled(true);

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                {
                    @Override
                    public boolean onMarkerClick(Marker marker)
                    {
                        // The user has re-tapped on the marker which was already showing an info window.
                        if (marker.equals(selectedMarker))
                        {
                            // The showing info window has already been closed - that's the first thing to happen
                            // when any marker is clicked.
                            // Return true to indicate we have consumed the event and that we do not want the
                            // the default behavior to occur (which is for the camera to move such that the
                            // marker is centered and for the marker's info window to open, if it has one).
                            selectedMarker = null;
                            return true;
                        }

                        removeCurrentPathIfExists();


                        selectedMarker = marker;


                        LatLng destination = marker.getPosition();

                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, destination);

                        DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                        downloadTask.execute(url);


                        Log.e(TAG, "onMarkerClick: dddd");

                        // Return false to indicate that we have not consumed the event and that we wish
                        // for the default behavior to occur.
                        return false;
                    }
                });

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
                {
                    @Override
                    public void onMapClick(LatLng latLng)
                    {
                        // Any showing info window closes when the map is clicked.
                        // Clear the currently selected marker.
                        selectedMarker = null;
                        removeCurrentPathIfExists();

                    }
                });

                setupLocationListening();
            }
        });
    }


    private void setupLocationListening()
    {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                Log.e(TAG, "Got location " + location.toString());

                friendsService.getMyFriends(contacts.getMyPhoneNumber(), location.getLatitude(), location.getLongitude()).enqueue(new Callback<List<Friend>>()
                {
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
                            if (!friend.phoneNumber.equals(contacts.getMyPhoneNumber()))
                            {
                                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(friend.lat, friend.lng)).title(contacts.getNameByPhoneNumber(friend.phoneNumber));
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
                });

                origin = new LatLng(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        });
    }


    private void setupFriendService()
    {
        friendsService = new Retrofit.Builder()
            .baseUrl(FRIEND_SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FriendsService.class);

        friendsService.registerMe(contacts.getMyPhoneNumber(), contacts.getAllPhoneNumbers()).enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                Log.i(TAG, "Registered friends");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "Friend registration failed", t);
            }
        });
    }


    /******* WEB SERVICE RESPONSE HANDLER  *******/





    private String getDirectionsUrl(LatLng origin, LatLng dest)
    {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        Log.e(TAG, "getDirectionsUrl: " + url);

        return url;
    }


    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            Log.e(TAG, "read");


            String line;
            while ((line = br.readLine()) != null)
            {
                Log.e(TAG, "read: " + line);

                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception downloading url", e);
        }
        finally
        {
            if (iStream != null)
            {
                iStream.close();
            }
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>
    {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url)
        {

            // For storing data from web service
            String data = "";

            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.e(TAG, "down");
            }
            catch (Exception e)
            {
                Log.e(TAG, "down faied");
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result)
        {
            Log.e(TAG, "down done");

            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
    {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
        {
            Log.e(TAG, "par start" + jsonData[0]);


            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try
            {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }
            catch (Exception e)
            {
                Log.e(TAG, "par fail");

                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            Log.e(TAG, "par done" + result);

            ArrayList<LatLng> points;
            PolylineOptions lineOptions;
//            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++)
            {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++)
                {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(11);
                lineOptions.color(Color.argb(127, 255, 0, 0));

                // Drawing path in the Google Map for the i-th route
                currentPathToAFriend = map.addPolyline(lineOptions);
            }


        }
    }




    private void removeCurrentPathIfExists()
    {
        if (currentPathToAFriend != null)
        {
            currentPathToAFriend.remove();
            currentPathToAFriend = null;
        }
    }


    interface FriendsService
    {
        @GET("report-my-location-and-get-my-friend-locations")
        Call<List<Friend>> getMyFriends(@Query("my-phone") String myPhone,
                                        @Query("my-lat")   double lat,
                                        @Query("my-lat")   double lng);

        @POST("tell-my-friend-phone-numbers")
        Call<String> registerMe(@Query("my-phone") String      myPhone,
                                @Body              Set<String> myFriendPhoneNumbers);
    }


    private static class Friend
    {
        public String phoneNumber;
        public double lat;
        public double lng;
        public String lastUpdated;

        @Override
        public String toString()
        {
            return "Friend" + phoneNumber + ": " + lat + ", " + lng + " - last updated on " + lastUpdated;
        }
    }
}


