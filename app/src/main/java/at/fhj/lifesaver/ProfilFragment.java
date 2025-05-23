package at.fhj.lifesaver;

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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProfilFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView progressText;
    private Button logoutButton;

    private static final int TOTAL_LESSONS = 10;

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
        TextView nameText = view.findViewById(R.id.profile_name);
        TextView emailText = view.findViewById(R.id.profile_email);

        SharedPreferences userPrefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String name = userPrefs.getString("user_name", "Unbekannt");
        String email = userPrefs.getString("user_email", "Nicht eingeloggt");

        nameText.setText(name);
        emailText.setText(email);

        // Fortschritt laden
        SharedPreferences prefs = requireContext().getSharedPreferences("progress", Context.MODE_PRIVATE);
        int completedLessons = prefs.getInt("completedLessons", 0);

        updateProgress(completedLessons);

        logoutButton.setOnClickListener(v -> {
            userPrefs.edit().clear().apply();

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
}