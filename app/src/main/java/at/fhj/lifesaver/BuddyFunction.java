package at.fhj.lifesaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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

        FirebaseApp.initializeApp(this);

        currentUser = userDao.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Kein eingeloggter Benutzer gefunden", Toast.LENGTH_LONG).show();
            // App z. B. zur LoginActivity umleiten
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // beende BuddyFunction
            return;
        }

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
                // Wenn lastClickedUser gesetzt ist, gehe zum Chat
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

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
                                // Den Standort des aktuellen Benutzers aktualisieren
                                currentUser.setLatitude(location.getLatitude());
                                currentUser.setLongitude(location.getLongitude());
                                userDao.updateUser(currentUser);  // Stelle sicher, dass der Benutzer in der lokalen Room-Datenbank gespeichert wird
                                FirebaseSyncHelper.updateUserInFirebase(currentUser);  // Firebase wird auch aktualisiert

                                // Kamera auf den aktuellen Standort zentrieren
                                if (firstLocationUpdate) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));  // 15 ist der Zoom-Level
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
                        FirebaseSyncHelper.updateUserInFirebase(currentUser);

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

        // 1. Lokale Benutzer aus der Room-Datenbank anzeigen
        List<User> localUsers = userDao.getAllUsers();
        Log.d("BuddyFunction", "Anzahl gespeicherter Nutzer: " + localUsers.size());
        for (User user : localUsers) {
            LatLng pos = new LatLng(user.getLatitude(), user.getLongitude());

            // Zeigt nur den grünen Marker für den aktuellen Benutzer an
            if (user.isCurrentUser()) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(user.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Grüner Marker für den aktuellen Benutzer
                if (marker != null) {
                    marker.setTag(user);
                }
            } else {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(user.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))); // Blauer Marker für andere Benutzer
                if (marker != null) {
                    marker.setTag(user);
                }
            }
        }

        // 2. Andere Benutzer aus Firebase abrufen
        FirebaseSyncHelper.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    try {
                        String name = userSnap.child("name").getValue(String.class);
                        Double lat = userSnap.child("latitude").getValue(Double.class);
                        Double lon = userSnap.child("longitude").getValue(Double.class);

                        // 🔁 E-Mail aus Firebase-Key wiederherstellen
                        String emailKey = userSnap.getKey(); // z. B. anja_at_gmx_at
                        String email = emailKey.replace("_at_", "@").replace("_", ".");

                        if (name != null && lat != null && lon != null && email != null) {
                            // ✅ Erstelle temporären User (für Marker)
                            User user = new User();
                            user.setName(name);
                            user.setEmail(email);
                            user.setPassword("demo"); // Dummy, da Room Pflicht
                            user.setLatitude(lat);
                            user.setLongitude(lon);
                            user.setCurrentUser(false);

                            LatLng pos = new LatLng(lat, lon);
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            if (marker != null) {
                                marker.setTag(user); // ✔ Tag: vollständiges User-Objekt
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Map", "Fehler beim Parsen der Firebase-Daten: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Map", "Firebase-Fehler: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof User) {
            User clickedUser = (User) tag;

            if (!clickedUser.isCurrentUser()) {
                // 🔍 Versuche, den User lokal zu finden (anhand E-Mail)
                User localUser = userDao.findByEmail(clickedUser.getEmail());

                if (localUser == null) {
                    // ⬇️ User existiert noch nicht in Room → speichern
                    userDao.insert(clickedUser);
                    localUser = userDao.findByEmail(clickedUser.getEmail()); // ID holen
                }

                lastClickedUser = localUser;
                Toast.makeText(this, "Ausgewählt: " + localUser.getName(), Toast.LENGTH_SHORT).show();
            }
            return true;
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