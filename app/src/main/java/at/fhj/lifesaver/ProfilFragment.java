package at.fhj.lifesaver;

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

public class ProfilFragment extends Fragment {
    private ProgressBar progressBar;
    private TextView progressText;
    private Button logoutButton;
    private ImageView avatarImage;

    private static final int TOTAL_LESSONS = 13;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil, container, false);
    }

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
        String email = prefs.getString("user_email", "Nicht eingeloggt");
        String avatarName = prefs.getString("avatar", "frau");

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
            prefs.edit().clear().apply();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void updateProgress(int completedLessons) {
        int progress = (int) ((completedLessons / (float) TOTAL_LESSONS) * 100);
        progressBar.setProgress(progress);
        progressText.setText(progress + " % abgeschlossen");
    }

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
            avatarImage.setImageResource(avatarResIds[position]);
            SharedPreferences prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);
            prefs.edit().putString("avatar", avatarNames[position]).apply();
            dialog.dismiss();
        });
    }

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