package at.fhj.lifesaver.lesson;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import at.fhj.lifesaver.R;

/**
 * Die Klasse LernenFragment zeigt eine Liste von Erste-Hilfe-Lektionen in einer RecyclerView an.
 * Die Inhalte der Lektionen (Titel, HTML-Datei, Bild) werden direkt im Code initialisiert.
 * Der LektionAdapter stellt die Daten in einer vertikalen Liste dar.
 * Die Daten werden im Fragment direkt statisch initialisiert.
 */
public class LernenFragment extends Fragment {

    private RecyclerView recyclerView;
    private LektionAdapter adapter;
    private List<Lektion> lektionenListe;

    /**
     * Wird beim Erstellen der View aufgerufen.
     * Initialisiert das RecyclerView, füllt die Liste der Lektionen mit statischen Daten
     * und setzt den LektionAdapter, sofern der Kontext verfügbar ist.
     * @param inflater LayoutInflater zum Erzeugen der View
     * @param container  Eltern-View-Gruppe
     * @param savedInstanceState Wiederherzustellender Zustand (nicht verwendet)
     * @return Die fertig aufgebaute View des Fragments
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lernen, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLektion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        lektionenListe = new ArrayList<>();
        initLektionListe();

        Context context = getContext();
        if (context != null){
            adapter = new LektionAdapter(lektionenListe, context);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_context_unavailable), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Initialisiert die Liste der verfügbaren Erste-Hilfe-Lektionen.
     * Jede Lektion enthält einen Titel, den Namen der HTML-Datei im assets-Ordner und eine Bildressource.
     * Diese Methode wird einmal beim Start des Fragments aufgerufen.
     */
    private void initLektionListe() {
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
    }

    /**
     * Wird aufgerufen, wenn das Fragment wieder sichtbar wird.
     * Aktualisiert den Adapter, um eventuell geänderten Fortschritt anzuzeigen.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}