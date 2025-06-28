package at.fhj.lifesaver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import at.fhj.lifesaver.R;
import at.fhj.lifesaver.data.UserDatabase;

/**
 * Die Klasse ProfilFragment zeigt das Benutzerprofil mit Avatar, Fortschritt und Logout-Funktion.
 * Es ermöglicht die Anzeige und Änderung des gewählten Avatars, die Anzeige des Lernfortschritts
 * sowie das Zurücksetzen des Fortschritts und das Abmelden des Benutzers.
 * Es werden SharedPreferences zur Speicherung von Benutzerdaten und Fortschritt verwendet.
 */
public class ProfilFragment extends Fragment {
    private static final int TOTAL_LESSONS = 13;
    private static final String PREF_USER_DATA = "user_data";
    private static final String PREF_KEY_EMAIL = "user_email";
    private ProgressBar progressBar;
    private TextView progressText;
    private Button logoutButton;
    private ImageView avatarImage;

    private final int[] avatarResIds = {
            R.drawable.lockige_frau,
            R.drawable.braunhaariger_mann,
            R.drawable.dunkelhaarig_frau,
            R.drawable.lockiger_mann,
            R.drawable.blonde_frau,
            R.drawable.schwarz_mann
    };

    private final String[] avatarNames = {
            "lockige frau", "braunhaariger mann", "dunkelhaarige frau", "lockiger mann", "blonde frau", "schwarzhaariger mann"
    };

    /**
     * Inflates das Layout für dieses Fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil, container, false);
    }

    /**
     * Initialisiert UI-Komponenten und zeigt Benutzerdaten sowie Fortschritt an.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        progressText = view.findViewById(R.id.progressText);
        logoutButton = view.findViewById(R.id.button);
        avatarImage = view.findViewById(R.id.image_avatar);
        TextView nameText = view.findViewById(R.id.profile_name);
        TextView emailText = view.findViewById(R.id.profile_email);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String name = prefs.getString("user_name", "Unbekannt");
        String email = prefs.getString(PREF_KEY_EMAIL, "Nicht eingeloggt");
        String avatarName = prefs.getString("avatar_" + email, "lockige frau");

        nameText.setText(name);
        emailText.setText(email);
        avatarImage.setImageResource(getAvatarResIdByName(avatarName));
        avatarImage.setOnClickListener(v -> showAvatarDialog());

        SharedPreferences prefsUser = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String userEmail = prefsUser.getString("user_email", "default");
        SharedPreferences prefsProgress = requireContext().getSharedPreferences("progress_" + userEmail, Context.MODE_PRIVATE);

        int completedLessons = 0;
        String[] lessonTitles = {
                "Bewusstlosigkeit/Reaktionslosigkeit", "Ersticken", "Verbrennungen", "Asthma",
                "Allergische Reaktion", "Schock", "Krampfanfall", "Starke Blutungen",
                "Frakturen, Verstauchungen und Zerrungen", "Vergiftungen", "Schlaganfall",
                "Herzinfarkt", "Verkehrsunfall"
        };
        for (String title : lessonTitles) {
            if (prefsProgress.getBoolean("lesson_" + title, false)) {
                completedLessons++;
            }
        }
        updateProgress(completedLessons);

        Button btnReset = view.findViewById(R.id.btnResetProgress);
        btnReset.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Fortschritt zurücksetzen?")
                    .setMessage("Willst du wirklich deinen Fortschritt für alle Lektionen löschen?")
                    .setPositiveButton("Ja", (dialog, which) -> {
                        SharedPreferences.Editor editor = prefsProgress.edit();
                        editor.clear();
                        editor.apply();

                        updateProgress(0);
                        Toast.makeText(requireContext(), "Fortschritt wurde zurückgesetzt", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Abbrechen", null)
                    .show();
        });

        logoutButton.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                UserDatabase db = UserDatabase.getInstance(requireContext());
                db.userDao().clearCurrentUserFlag();

                requireActivity().runOnUiThread(() -> {
                    prefs.edit()
                            .remove("user_name")
                            .remove("user_email")
                            .putBoolean("is_logged_in", false)
                            .apply();

                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Abmeldung fehlgeschlagen.", Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    /**
     * Aktualisiert den Fortschrittsbalken und Textanzeige.
     * @param completedLessons Anzahl der abgeschlossenen Lektionen
     */
    private void updateProgress(int completedLessons) {
        int progress = (int) ((completedLessons / (float) TOTAL_LESSONS) * 100);
        progressBar.setProgress(progress);
        progressText.setText(progress + " % abgeschlossen");
    }

    /**
     * Zeigt den Dialog zur Avatar-Auswahl an.
     */
    private void showAvatarDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_avatar_selection, null);
        GridView gridView = dialogView.findViewById(R.id.avatarGrid);

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return avatarResIds.length;
            }

            @Override
            public Object getItem(int i) {
                return avatarResIds[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View convertView, ViewGroup parent) {
                ImageView img = new ImageView(requireContext());
                img.setLayoutParams(new GridView.LayoutParams(200, 200));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setImageResource(avatarResIds[i]);
                return img;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wähle einen Avatar");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                avatarImage.setImageResource(avatarResIds[position]);
                SharedPreferences prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);
                String email = prefs.getString("user_email", "default");
                prefs.edit().putString("avatar_" + email, avatarNames[position]).apply();
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Avatar konnte nicht gespeichert werden.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gibt die Ressourcen-ID eines Avatars basierend auf dessen Namen zurück.
     * @param name Avatarname (z.B. "lockige frau")
     * @return Ressourcen-ID des Bildes
     */
    private int getAvatarResIdByName(String name) {
        switch (name) {
            case "braunhaariger mann":
                return R.drawable.braunhaariger_mann;
            case "dunkelhaarige frau":
                return R.drawable.dunkelhaarig_frau;
            case "lockiger mann":
                return R.drawable.lockiger_mann;
            case "schwarzhaariger mann":
                return R.drawable.schwarz_mann;
            case "blonde frau":
                return R.drawable.blonde_frau;
            case "lockige frau":
            default:
                return R.drawable.lockige_frau;
        }
    }
}