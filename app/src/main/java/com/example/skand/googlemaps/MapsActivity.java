package com.example.skand.googlemaps;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.Manifest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap mMap;
    EditText origin;
    EditText destination;
    Button searchButton;
    Marker mMarker,originMarker,destinationMarker;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //add a line in gradle file to import location services
        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)//make this implement the parameters onConnected and onConnectionSuspended
                .addOnConnectionFailedListener(this)// make this implement the parameters onConnectionFailed
                .build();

        mGoogleApiClient.connect();

        mapFragment.getMapAsync(this);
        origin =(EditText)findViewById(R.id.source);
        destination=(EditText)findViewById(R.id.destination);
        searchButton=(Button)findViewById(R.id.search);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locorigin =origin.getText().toString();
                String dest=destination.getText().toString();

                if(dest==null&&locorigin==null) {
                    Toast.makeText(getApplicationContext(), "Location can't be empty,", Toast.LENGTH_LONG).show();
                    return;
                }

                    try {
                        if(dest!=null)
                        {
                            fetchLocation(locorigin,dest);
                        }
                        else
                        {
                            fetchLocation(locorigin);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


            }
        });
    }


    private void fetchLocation(String locorigin, String dest) throws IOException {

        Geocoder geo= new Geocoder(this);
        if(geo.isPresent())
        {
            //accessing the first most accurate location from the array of address fetched
            ArrayList<Address> origin=(ArrayList<Address>) geo.getFromLocationName(locorigin,1);
            ArrayList<Address> destination=(ArrayList<Address>) geo.getFromLocationName(dest,1);
            Double latOrigin=0.0,latDest=0.0,longOrigin=0.0,longDest=0.0;
            LatLng originPos,destPos;
            //accesing the latitude and longitude of the location
            if(origin!=null)
            {
                latOrigin=origin.get(0).getLatitude();
                longOrigin=origin.get(0).getLongitude();
            }
            if(destination!=null)
            {
                latDest=destination.get(0).getLatitude();
                longDest=destination.get(0).getLongitude();
            }
            //making it to latlong object to locate them
            originPos=new LatLng(latOrigin,longOrigin);
            destPos=new LatLng(latDest,longDest);
            //removing all previous markers
            if(mMarker!=null)
            {
                mMarker.remove();
            }
            if(originMarker!=null)
            {
                originMarker.remove();
            }
            if(destinationMarker!=null)
            {
                destinationMarker.remove();
            }
            //positioning the marker
            originMarker=mMap.addMarker(new MarkerOptions()
            .position(originPos)
            .draggable(true)
            .title("origin"));
            destinationMarker=mMap.addMarker(new MarkerOptions()
            .position(destPos)
            .draggable(true)
            .title("destination"));
            //animating the camera
            CameraUpdate cam=  CameraUpdateFactory.newLatLngZoom(originPos,5);
            mMap.animateCamera(cam);
            //showing the route between the locations
            //include compile 'com.github.jd-alexander:library:1.1.0' in the gradle
            Routing routing = new Routing.Builder()
                    .waypoints(originPos,destPos)
                    .withListener(this)//make this implement onRoutingSucces, Cancelled, Faliure and start
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .build();
            routing.execute();


        }
    }

    private void fetchLocation(String locorigin) throws IOException {
        Geocoder geo= new Geocoder(getApplicationContext());
        if (geo.isPresent()) {
            ArrayList<Address> address= (ArrayList<Address>) geo.getFromLocationName(locorigin,1);
            Double originlong,originlat;
            if(address!=null)
            {
                originlat=address.get(0).getLatitude();
                originlong=address.get(0).getLongitude();
                LatLng originlatlong=new LatLng(originlat,originlong);
                if(mMarker!=null)
                {
                    mMarker.remove();
                }
                mMarker=mMap.addMarker(new MarkerOptions()
                .position(originlatlong)
                .title("origin"));
                mMarker.setSnippet(String.valueOf(originlatlong));
               /* mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originlatlong,15));
                mMap.animateCamera(camera);*/
                CameraUpdate cam=CameraUpdateFactory.newLatLngZoom(originlatlong,15);
                mMap.animateCamera(cam);
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Toast.makeText(this, String.valueOf(lastLocation), Toast.LENGTH_SHORT).show();

        startLocationUpdates();


    }

    private void startLocationUpdates() {

        final LocationRequest locationRequest= LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(3000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //TODO FROM LINE 124

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }
}
