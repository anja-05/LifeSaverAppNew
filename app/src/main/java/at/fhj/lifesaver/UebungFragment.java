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


public class UebungFragment extends Fragment {
    private RecyclerView recyclerView;
    private UebungAdapter adapter;
    private List<Uebung> uebungenListe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uebung, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUebung);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        uebungenListe = new ArrayList<>();
        uebungenListe.add(new Uebung("Herzdruckmassage", R.drawable.ic_herzdruckmassage));

        adapter = new UebungAdapter(uebungenListe, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }
}