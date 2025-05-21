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

        // Titel und Dateinamen für die Lektionen
        lektionenListe = new ArrayList<>();
        lektionenListe.add(new Lektion("Bewusstlosigkeit/Reaktionslosigkeit", "bewusstlosigkeit.html"));
        lektionenListe.add(new Lektion("Ersticken", "ersticken.html"));
        lektionenListe.add(new Lektion("Verbrennungen", "verbrennungen.html"));
        lektionenListe.add(new Lektion("Asthma", "asthma.htmtl"));
        lektionenListe.add(new Lektion("Allergische Reaktion","allergischeReaktion.html"));
        lektionenListe.add(new Lektion("Schock","schock.html"));
        lektionenListe.add(new Lektion("Krampfanfall","krampfanfall.html"));
        lektionenListe.add(new Lektion("Starke Blutungen","starkeBlutungen.html"));
        lektionenListe.add(new Lektion("Frakturen, Verstauchungen und Zerrungen","frakturen.html"));
        lektionenListe.add(new Lektion("Vergiftungen","vergiftungen.html"));

        adapter = new LektionAdapter(lektionenListe, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }
}
