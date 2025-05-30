package at.fhj.lifesaver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class UebungFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uebung, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.viewPagerUebung);

        // Liste der Übungen mit Bild, Titel und Beschreibung
        List<Uebung> uebungList = new ArrayList<>();
        uebungList.add(new Uebung(
                R.drawable.uebung_herzdruckmassage,
                "Herzdruckmassage",
                "Lerne, wie du bei einem Herzstillstand richtig reagierst"
        ));
        uebungList.add(new Uebung(
                R.drawable.uebung_stabileseitenlage,
                "Stabile Seitenlage",
                "Lerne, wie du bewusstlose Personen sicher lagerst"
        ));
        uebungList.add(new Uebung(
                R.drawable.uebung_herzdruckmassage,
                "Rautekgriff",
                "Lerne, wie du Verletzte schnell aus der Gefahrenzone bringst"
        ));

        UebungAdapter adapter = new UebungAdapter(uebungList);
        viewPager.setAdapter(adapter);

        return view;
    }
}
