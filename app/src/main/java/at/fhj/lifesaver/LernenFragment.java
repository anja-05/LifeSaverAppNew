package at.fhj.lifesaver;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class LernenFragment extends Fragment {

    private RecyclerView recyclerView;
    private LektionAdapter adapter;
    private List<Lektion> lektionenListe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lernen, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLektion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        lektionenListe = new ArrayList<>();
        lektionenListe.add(new Lektion("Bewusstlosigkeit/Reaktionslosigkeit", "html/bewusstlosigkeit.html", R.drawable.ic_bewusstlos));
        lektionenListe.add(new Lektion("Ersticken", "html/ersticken.html", R.drawable.ic_ersticken));
        lektionenListe.add(new Lektion("Verbrennungen", "html/verbrennungen.html", R.drawable.ic_verbrennung));
        lektionenListe.add(new Lektion("Asthma", "html/asthma.html", R.drawable.ic_asthma));
        lektionenListe.add(new Lektion("Allergische Reaktion", "html/allergischeReaktion.html", R.drawable.ic_allergie));
        lektionenListe.add(new Lektion("Schock", "html/schock.html", R.drawable.ic_schock));
        lektionenListe.add(new Lektion("Krampfanfall", "html/krampfanfall.html", R.drawable.ic_krampf));
        lektionenListe.add(new Lektion("Starke Blutungen", "html/starkeBlutungen.html", R.drawable.ic_blutung));
        lektionenListe.add(new Lektion("Frakturen, Verstauchungen und Zerrungen", "html/frakturen.html", R.drawable.ic_fraktur));
        lektionenListe.add(new Lektion("Vergiftungen", "html/vergiftungen.html", R.drawable.ic_vergiftung));
        lektionenListe.add(new Lektion("Schlaganfall", "html/schlaganfall.html", R.drawable.ic_schlaganfall));
        lektionenListe.add(new Lektion("Herzinfarkt", "html/herzinfarkt.html", R.drawable.ic_herzinfarkt));
        lektionenListe.add(new Lektion("Verkehrsunfall", "html/verkehrsunfall.html", R.drawable.ic_verkehrsunfall));

        adapter = new LektionAdapter(lektionenListe, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

}
