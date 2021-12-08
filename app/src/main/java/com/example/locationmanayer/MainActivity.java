package com.example.locationmanayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.security.interfaces.RSAKey;

public class MainActivity extends AppCompatActivity {
    private static final long MIN_TIEMPO_ENTRE_UPDATES = 1000 * 30; //
    //Minima distancia para updates en metros.
    private static final long MIN_CAMBIO_DISTANCIA_PARA_UPDATES = 0; //

    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;
            for (Location location : locationResult.getLocations()){
                Log.d("MainActivity","LOCATION"+location.toString());

            }
        }
    };
    //
    private LocationManager ubicacion;
    public TextView lon, lati, altu, precision, velocidad, tiempo;

    //
    public FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localizacion();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //getlastlocalizacion();
            checksettings();
        } else {
            asklocalizacion();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopupdates();
    }

    private void checksettings() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startupdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MainActivity.this, 1001);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    private void startupdates() {
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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    private void stopupdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    private void getlastlocalizacion() {
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
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d("MainActivity","latitud"+location.getLatitude());
                Log.d("MainActivity","longitud"+location.getLongitude());
                Log.d("MainActivity","altitud"+location.getAltitude());
                Log.d("MainActivity","precision"+location.getAccuracy());
                Log.d("MainActivity","velocidad"+location.getSpeed());
                Log.d("MainActivity","tiempo"+location.getTime());
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void asklocalizacion() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10001 );
            }else {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10001 );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 10001){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                //getlastlocalizacion();
                checksettings();
            }else{
                //permission denied
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void localizacion(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1000 );
        }
        lon = (TextView)findViewById(R.id.textView);
        lati = (TextView)findViewById(R.id.textView2);
        altu = (TextView)findViewById(R.id.textView3);
        precision = (TextView)findViewById(R.id.textView4);
        velocidad = (TextView)findViewById(R.id.textView5);
        tiempo = (TextView)findViewById(R.id.textView6);
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // Location loc = ubicacion.requestLocationUpdates(ubicacion.GPS_PROVIDER,MIN_TIEMPO_ENTRE_UPDATES,MIN_CAMBIO_DISTANCIA_PARA_UPDATES,(LocationListener) this,null);
        if(ubicacion != null){

            lon.setText("latitud  :"+ String.valueOf(loc.getLatitude()));
            lati.setText("longitud  :"+String.valueOf(loc.getLongitude()));
            altu.setText("Altitud  :"+String.valueOf(loc.getAltitude()));
            precision.setText("Presicion  :"+String.valueOf(loc.getAccuracy()));
            velocidad.setText("Velocidad  :"+String.valueOf(loc.getSpeed()));
            tiempo.setText("Tiempe  :"+String.valueOf(loc.getTime()));
        }
        /*ubicacion.requestLocationUpdates(ubicacion.GPS_PROVIDER,MIN_TIEMPO_ENTRE_UPDATES,MIN_CAMBIO_DISTANCIA_PARA_UPDATES, (LocationListener) this);
        loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lon.setText("latitud  :"+ String.valueOf(loc.getLatitude()));
        lati.setText("longitud  :"+String.valueOf(loc.getLongitude()));*/
    }
    /*private void startReceivingLocationUpdates(){
        ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locListener, Looper.getMainLooper());
    }*/
}