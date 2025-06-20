package at.fhj.lifesaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
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
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

public class BuddyFunction extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private UserDatabase database;
    private UserDAO userDao;
    private List<User> nearbyUsers = new ArrayList<>();
    private User currentUser;
    private boolean firstLocationUpdate = true;
    private boolean userMovedMap = false;
    private User lastClickedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddyfunction);

        // Initialisiere die Datenbank
        database = UserDatabase.getInstance(this);
        userDao = database.userDao();

        // Hole den aktuellen Benutzer (normalerweise würde dies nach dem Login geschehen)
        currentUser = userDao.getCurrentUser();
        if (currentUser == null) {
            // Für Demo-Zwecke erstellen wir einen Benutzer, wenn keiner existiert
            currentUser = new User("Du", "demo@emial.com", "pass", 52.520008, 13.404954);
            currentUser.setCurrentUser(true);
            userDao.insert(currentUser);
        }

        addDemoUsers();

        // Initialisiere die Karte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null){
            mapFragment.getMapAsync(this);
        }

        // Initialisiere den Standortclient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Profil-Button
        FloatingActionButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastClickedUser != null) {
                    Intent intent = new Intent(BuddyFunction.this, ChatActivity.class);
                    intent.putExtra("USER_ID", lastClickedUser.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(BuddyFunction.this, "Bitte wähle zuerst eine Person auf der Karte aus", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

   private void addDemoUsers() {
        // Demo-Benutzer für Testzwecke
       insertIfNotExists("Anna", 52.520908, 13.406954);
       insertIfNotExists("Max", 52.519108, 13.403954);
       insertIfNotExists("Sophie", 52.521008, 13.402954);
       insertIfNotExists("Leon", 52.518008, 13.405954);
    }

    private void insertIfNotExists(String name, double lat, double lon) {
        String email = name.toLowerCase() + "@example.com";
        if (userDao.findByEmail(email) == null) {
            User user = new User(name, lat, lon);
            userDao.insert(user);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (userDao.getAllUsers().size() <= 1) {
            addDemoUsers();
        }

        // Überprüfe Standortberechtigungen
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                userMovedMap = true;
            }
        });

        displayAllUsers();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Standortanzeige in der Karte aktivieren
            mMap.setMyLocationEnabled(true);

            // 1. Einmaliger Standortabruf (z. B. initial)
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Benutzerstandort in DB aktualisieren
                                currentUser.setLatitude(location.getLatitude());
                                currentUser.setLongitude(location.getLongitude());
                                userDao.updateUser(currentUser);

                                // Kamera auf aktuellen Standort zentrieren
                                if (firstLocationUpdate) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                    firstLocationUpdate = false;
                                }

                                // Marker setzen
                                displayAllUsers();
                            }
                        }
                    });

            // 2. Standort regelmäßig updaten
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000); // alle 5 Sekunden

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;

                    for (Location location : locationResult.getLocations()) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        // Standort in der DB aktualisieren
                        currentUser.setLatitude(lat);
                        currentUser.setLongitude(lon);
                        userDao.updateUser(currentUser);

                        //LatLng updatedLocation = new LatLng(lat, lon);

                        // Marker aktualisieren
                        displayAllUsers();
                    }
                }
            };

            // Starte das Live-Tracking
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void displayAllUsers() {
        mMap.clear();
        nearbyUsers.clear();
        List<User> allUsers = userDao.getAllUsers();

        for (User user : allUsers) {
            LatLng position = new LatLng(user.getLatitude(), user.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(user.getName());

            if (user.isCurrentUser()) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                nearbyUsers.add(user);
            }

            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(user.getId());
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();

        if (tag != null && tag instanceof Integer) {
            int clickedUserId = (int) tag;

            if (clickedUserId != currentUser.getId()) {
                // Zeige nur den Namen in einem Dialog oder Toast
                lastClickedUser = userDao.getUserById(clickedUserId);
                if (lastClickedUser != null) {
                    Toast.makeText(this, "Name: " + lastClickedUser.getName(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Standortberechtigung erforderlich", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Starte LektionenActivity statt einfach zurückzugehen
        super.onBackPressed();
        Intent intent = new Intent(BuddyFunction.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}