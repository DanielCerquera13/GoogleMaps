package com.example.googlemapsreto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private LatLng lastUbication;
    private ArrayList<Marker> markersSaved;
    private FloatingActionButton butMarker;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Location user;
    private boolean userAdded = false;
    private Marker userMarker;
    private Geocoder geocoder;
    private boolean fixedMarker = false;
    private List<Address> positions;







    public MapsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getLocationPermission();

        geocoder = new Geocoder(this);
        markersSaved = new ArrayList<>();



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
       mMap.getUiSettings().setZoomControlsEnabled(true);
       mMap.getUiSettings().setCompassEnabled(true);
       mMap.setMyLocationEnabled(true);
       mMap.getUiSettings().setMyLocationButtonEnabled(false);
       mMap.setOnMyLocationButtonClickListener(this);
       mMap.setOnMyLocationClickListener(this);
       mMap.setOnMarkerClickListener(this);
       userLocation();






    }

    public void map(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void userLocation(){

        if(mLocationPermissionGranted){

            mMap.setMyLocationEnabled(false);
            centerCamera();

        }

    }



    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }



    public boolean onMyLocationButtonClick(){

        Toast.makeText(this,"My Location button clicked", Toast.LENGTH_SHORT).show();

        return false;
    }


    private void getLocationPermission() {


        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},11);

       if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

           mLocationPermissionGranted = true;
           map();

        }else{

           ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

       }

    }


    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

       switch (requestCode){

           case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
               if(permissions.length == 1 && permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                   mLocationPermissionGranted=true;

               }
               break;

       }
    }



   @Override
    public boolean onMarkerClick(Marker marker) {


        if(marker.getTitle().equalsIgnoreCase("My Current Location")){

            List<Address> addresses = null;

            try{

                addresses = (List<Address>) geocoder.getFromLocation(user.getLatitude(), user.getLongitude(), 1);

                if(addresses!= null){
                    userMarker.setTitle("My Current Location");
                    userMarker.setSnippet(addresses.get(0).getAddressLine(0));
                    userMarker.showInfoWindow();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }else{


            LatLng userlocation = new LatLng(user.getLatitude(), user.getLongitude());
            //double difference = SphericaUtil.computeDistanceBetween(marker.getPosition(), userLocation());

            //marker.setSnippet("Estás a "+difference+" metros de "+ marker.getTitle());
            marker.showInfoWindow();

        }


        return false;
    }


   public void centerCamera(){


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback(){

            @Override
            public void onLocationResult (LocationResult locationResult){

                user = locationResult.getLastLocation();
                LatLng userPosition = new LatLng(user.getLatitude(),user.getLongitude());

                if(!userAdded){

                    userMarker = mMap.addMarker(new MarkerOptions().position(userPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.user)).title("My Current Location"));
                    userAdded = true;

                }else{
                    
                    userMarker.setPosition(userPosition);
                    
                }
                
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition,18));

            }


        }, Looper.myLooper());



    }



    private void getLatLng(){

        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.e("Latitude: ",+location.getLatitude()+"Longitude: "+location.getLongitude());
                        }
                    }
                });

    }


    public void addMarkerInTheMap(String titleMarker){

        Marker marker = mMap.addMarker(new MarkerOptions().position(lastUbication).title(titleMarker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        markersSaved.add(marker);


    }

    public String nearestLocationToMe() {

        String locationNearest = "";
        double [] meters = new double [markersSaved.size()];

        LatLng userLocation = new LatLng(user.getLatitude(),user.getLongitude());

        for(int i=0; i< meters.length; i++){

            //meters[i] = SphericalUtil.computeDistanceBetween(userLocation, markersSaved.get(i).getPosition());

        }

        double minimum = meters [0];
        int indexMinimum = 0;

        for(int i =1; i<meters.length; i++){

            if(meters[i] < minimum){

                minimum = meters [i];
                indexMinimum = i;

            }

        }

        if(meters[indexMinimum] < 10){

            locationNearest += "Usted se encuentra en: \n"+ markersSaved.get(indexMinimum).getTitle();

        }else{

            locationNearest += "Usted se encuentra a pocos metros de: \n"+markersSaved.get(indexMinimum).getTitle();

        }

        return locationNearest;


    }





}