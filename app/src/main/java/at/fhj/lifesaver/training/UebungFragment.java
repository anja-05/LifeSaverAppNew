package at.fhj.lifesaver.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import at.fhj.lifesaver.R;

/**
 * Das Fragment {@code UebungFragment} zeigt eine horizontale Liste von Erste-Hilfe-Übungen,
 * die mittels ViewPager2 durchgeblättert werden können.
 * Jede Übung enthält ein Bild, einen Titel und eine Kurzbeschreibung.
 * Beim Klick auf ein Element wird die entsprechende Aktivität gestartet (über {@link UebungAdapter}).
 */
public class UebungFragment extends Fragment {

    /**
     * Wird aufgerufen, wenn das Fragment die Benutzeroberfläche erstellen soll.
     * Initialisiert den ViewPager2 und befüllt ihn mit einer Liste von Übungen
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Die erzeugte Ansicht des Fragments
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view;
        try {
            view = inflater.inflate(R.layout.fragment_uebung, container, false);
            ViewPager2 viewPager = view.findViewById(R.id.viewPagerUebung);

            List<Uebung> uebungList = new ArrayList<>();
            uebungList.add(new Uebung(
                    R.drawable.uebung_herzdruckmassage,
                    getString(R.string.uebung_herzdruckmassage_titel),
                    getString(R.string.uebung_herzdruckmassage_beschreibung)
            ));
            uebungList.add(new Uebung(
                    R.drawable.uebung_stabileseitenlage,
                    getString(R.string.uebung_stabile_seitenlage_titel),
                    getString(R.string.uebung_stabile_seitenlage_beschreibung)
            ));
            uebungList.add(new Uebung(
                    R.drawable.uebung_rautekgriff,
                    getString(R.string.uebung_rautekgriff_titel),
                    getString(R.string.uebung_rautekgriff_beschreibung)
            ));

            UebungAdapter adapter = new UebungAdapter(uebungList);
            viewPager.setAdapter(adapter);

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.uebung_ladefehler), Toast.LENGTH_LONG).show();
            view = new View(requireContext());
        }
        return view;
    }
}
