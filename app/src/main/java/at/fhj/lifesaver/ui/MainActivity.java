package at.fhj.lifesaver.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.fhj.lifesaver.map.BuddyFunction;
import at.fhj.lifesaver.lesson.LernenFragment;
import at.fhj.lifesaver.R;
import at.fhj.lifesaver.training.UebungFragment;
import at.fhj.lifesaver.data.User;
import at.fhj.lifesaver.data.UserDatabase;
import at.fhj.lifesaver.databinding.ActivityMainBinding;

/**
 * Die Klasse MainActivity ist die zentrale Steuerungsaktivität der Lifesaver-App.
 * Sie enthält eine BottomNavigationView, über die zwischen den Fragmenten
 * LernenFragment, UebungFragment, Location (BuddyFunction) und ProfilFragment gewechselt werden kann.
 * Beim Start wird das LernenFragment standardmäßig angezeigt.
 * ActivityMainBinding ist für das View Binding und der FragmentManager für das Wechseln von Fragmenten.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    /**
     * Wird beim Starten der App aufgerufen. Initialisiert UI, Navigation und Standortaktualisierung.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                UserDatabase db = UserDatabase.getInstance(this);
                User currentUser = db.userDao().getCurrentUser();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && currentUser != null) {

                    FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
                    locationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            currentUser.setLatitude(location.getLatitude());
                            currentUser.setLongitude(location.getLongitude());

                            executor.execute(() -> db.userDao().updateUser(currentUser));
                        }
                    });
                }
            } catch (Exception e) {
                Toast.makeText(this, "Standort konnte nicht aktualisiert werden. Funktionen sind ggf. eingeschränkt.", Toast.LENGTH_LONG).show();
            }
        });

        replaceFragment(new LernenFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.lektion:
                    replaceFragment(new LernenFragment());
                    break;
                case R.id.uebung:
                    replaceFragment(new UebungFragment());
                    break;
                case R.id.buddy:
                    startActivity(new Intent(this, BuddyFunction.class));
                    return true;
                case R.id.profil:
                    replaceFragment(new ProfilFragment());
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    /**
     * Ersetzt das aktuell angezeigte Fragment durch ein neues Fragment.
     * @param fragment das Fragment, das angezeigt werden soll
     */
    private void replaceFragment(Fragment fragment){
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
        } catch (Exception e) {
            Toast.makeText(this, "Fehler beim Laden der Ansicht. Bitte erneut versuchen.", Toast.LENGTH_SHORT).show();
        }
    }
}