package at.fhj.lifesaver.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.fhj.lifesaver.chat.ChatActivity;
import at.fhj.lifesaver.R;
import at.fhj.lifesaver.data.User;
import at.fhj.lifesaver.data.UserDAO;
import at.fhj.lifesaver.data.UserDatabase;
import at.fhj.lifesaver.ui.LoginActivity;
import at.fhj.lifesaver.ui.MainActivity;
import at.fhj.lifesaver.utils.FirebaseSyncHelper;

/**
 * Die Klasse BuddyFunction zeigt eine Google Map mit der Position des aktuellen Nutzers
 * und anderer Nutzer in der Umgebung. Es ermöglicht, einen Nutzer auszuwählen
 * und mit ihm in einen Chat zu wechseln.
 */
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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Initialisiert die Ansicht, prüft ob ein eingeloggter Nutzer vorhanden ist,
     * und initialisiert Karte und Standortdienste.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddyfunction);

        executor.execute(() -> {
            try {
                database = UserDatabase.getInstance(this);
                userDao = database.userDao();
                currentUser = userDao.getCurrentUser();

                runOnUiThread(() -> {
                    if (currentUser == null) {
                        Toast.makeText(this, getString(R.string.error_no_user), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(this);
                    }

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                    FloatingActionButton profileButton = findViewById(R.id.profile_button);
                    profileButton.setOnClickListener(v -> {
                        if (lastClickedUser != null) {
                            Intent intent = new Intent(BuddyFunction.this, ChatActivity.class);
                            intent.putExtra("USER_EMAIL", lastClickedUser.getEmail());
                            startActivity(intent);
                        } else {
                            Toast.makeText(BuddyFunction.this, getString(R.string.error_select_user_first), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, getString(R.string.error_initialization) + ": " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    /**
     * Wird aufgerufen, wenn die Karte bereit ist. Initialisiert Listener und Standortanzeige.
     * @param googleMap die bereitgestellte GoogleMap-Instanz, die auf der Benutzeroberfläche angezeigt wird.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

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

    /**
     * Aktiviert die Standortanzeige und startet regelmäßige Standortupdates.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                mMap.setMyLocationEnabled(true);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentUser.setLatitude(location.getLatitude());
                                currentUser.setLongitude(location.getLongitude());
                                executor.execute(() -> userDao.updateUser(currentUser));
                                FirebaseSyncHelper.updateUserInFirebase(currentUser);

                                if (firstLocationUpdate) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                    firstLocationUpdate = false;
                                }

                                displayAllUsers();
                            }
                        });
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(5000);

                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) return;

                        for (Location location : locationResult.getLocations()) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            currentUser.setLatitude(lat);
                            currentUser.setLongitude(lon);
                            executor.execute(() -> userDao.updateUser(currentUser));
                            FirebaseSyncHelper.updateUserInFirebase(currentUser);

                            displayAllUsers();
                        }
                    }
                };
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException e) {
                Toast.makeText(this, getString(R.string.error_location) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Zeigt alle bekannten Nutzer (lokal und Firebase) auf der Karte an.
     */
    private void displayAllUsers() {
        executor.execute(() -> {
            try {
                List<User> localUsers = userDao.getAllUsers();

                runOnUiThread(() -> {
                    mMap.clear();
                    nearbyUsers.clear();

                    for (User user : localUsers) {
                        LatLng pos = new LatLng(user.getLatitude(), user.getLongitude());

                        Marker marker;
                        if (user.isCurrentUser()) {
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(user.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        } else {
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(user.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        }

                        if (marker != null) {
                            marker.setTag(user);
                        }
                    }
                    loadFirebaseUsers();
                });
            }catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, getString(R.string.error_loading_users) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Lädt Firebase-Nutzer und fügt Marker zur Karte hinzu.
     */
    private void loadFirebaseUsers() {
        FirebaseSyncHelper.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    try {
                        String name = userSnap.child("name").getValue(String.class);
                        Double lat = userSnap.child("latitude").getValue(Double.class);
                        Double lon = userSnap.child("longitude").getValue(Double.class);

                        String emailKey = userSnap.getKey();
                        if (emailKey == null || name == null || lat == null || lon == null) continue;

                        String email = emailKey.replace("_at_", "@").replace("_", ".");
                        if (email.equalsIgnoreCase(currentUser.getEmail())) continue;

                            User user = new User();
                            user.setName(name);
                            user.setEmail(email);
                            user.setPassword("demo");
                            user.setLatitude(lat);
                            user.setLongitude(lon);
                            user.setCurrentUser(false);

                            LatLng pos = new LatLng(lat, lon);
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            if (marker != null) {
                                marker.setTag(user);
                            }
                    } catch (Exception ignored) {
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BuddyFunction.this, getString(R.string.error_firebase) + ": " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Wird aufgerufen, wenn ein Marker angeklickt wird.
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof User) {
            User clickedUser = (User) tag;

            if (!clickedUser.isCurrentUser()) {
                executor.execute(() -> {
                    try {
                        User localUser = userDao.findByEmail(clickedUser.getEmail());

                        if (localUser == null) {
                            userDao.insert(clickedUser);
                            localUser = userDao.findByEmail(clickedUser.getEmail());
                        }

                        User finalUser = localUser;
                        runOnUiThread(() -> {
                            lastClickedUser = finalUser;
                            Toast.makeText(this, getString(R.string.toast_selected_user, finalUser.getName()), Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(this, getString(R.string.error_selection) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }
            return true;
        }
        return false;
    }

    /**
     * Behandelt das Ergebnis der Berechtigungsanfrage.
     * @param requestCode The request code
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, getString(R.string.toast_location_permission_needed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Öffnet beim Zurückgehen die Hauptansicht.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(BuddyFunction.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}