package at.fhj.lifesaver;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import at.fhj.lifesaver.databinding.ActivityMainBinding;

/**
 * Die Klasse MainActivity ist die zentrale Steuerungsaktivität der Lifesaver-App.
 * Sie enthält eine BottomNavigationView, über die zwischen den Fragmenten
 * LernenFragment, UebungFragment und ProfilFragment gewechselt werden kann.
 * Beim Start wird das LernenFragment standardmäßig angezeigt.
 * ActivityMainBinding ist für das View Binding und der FragmentManager für das Wechseln von Fragmenten.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    /**
     * Initialisiert die Hauptaktivität und zeigt das Startfragment.
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
        replaceFragment(new LernenFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.lektion:
                    replaceFragment(new LernenFragment());
                    break;
                case R.id.uebung:
                    replaceFragment(new UebungFragment());
                    break;
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}