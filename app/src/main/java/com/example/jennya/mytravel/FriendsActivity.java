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
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
    private LatLng origin;


    /**
     * Keeps track of the selected marker.
     */
    private Marker selectedMarker;
    private Polyline currentPathToAFriend;


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

//        GPSTracker myCoordinates = new GPSTracker(this);
//        Log.e("mycoordinate", myCoordinates.toString());

        origin = new LatLng(location.getLatitude(), location.getLongitude());

//        if (!myCoordinates.canGetLocation())
//        {
//            myCoordinates.showSettingsAlert();
//        }
//        else
//        {
//            myCoordinates.getLocation();
//            origin = new LatLng(myCoordinates.getLatitude(), myCoordinates.getLongitude());
//
//            Log.e("myorigin", origin.toString());
//
//        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    { /* not needed */ }

    @Override
    public void onProviderEnabled(String s)
    { /* not needed */ }

    @Override
    public void onProviderDisabled(String s)
    { /* not needed */ }


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
            if (!name.equals(friend.name) && map != null) // there was a strange situation and map was null
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
        Log.e(TAG, "Error contacting friends web service", t);

    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
//         The user has re-tapped on the marker which was already showing an info window.
        if (marker.equals(selectedMarker)) {
//         The showing info window has already been closed - that's the first thing to happen
//         when any marker is clicked.
//         Return true to indicate we have consumed the event and that we do not want the
//         the default behavior to occur (which is for the camera to move such that the
//         marker is centered and for the marker's info window to open, if it has one).
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

            StringBuffer sb = new StringBuffer();

            Log.e(TAG, "read");


            String line = "";
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
            Log.d("Excep downloading url", e.toString());
        }
        finally
        {
            iStream.close();
            urlConnection.disconnect();
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

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++)
            {
                points = new ArrayList<LatLng>();
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


    @Override
    public void onMapClick(LatLng latLng)
    {
        // Any showing info window closes when the map is clicked.
        // Clear the currently selected marker.
        selectedMarker = null;
        removeCurrentPathIfExists();
    }


    private void removeCurrentPathIfExists()
    {
        if (currentPathToAFriend != null)
        {
            currentPathToAFriend.remove();
            currentPathToAFriend = null;
        }
    }
}


interface FriendsService
{
    @GET("get-my-friends")
    Call<List<Friend>> getMyFriends(@Query("group") String group,
                                    @Query("name") String name,
                                    @Query("lat") double lat,
                                    @Query("lng") double lng);
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