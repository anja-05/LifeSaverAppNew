package at.fhj.lifesaver;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UebungAdapter extends RecyclerView.Adapter<UebungAdapter.UebungViewHolder> {

    private List<Uebung> uebungen;

    public UebungAdapter(List<Uebung> uebungen) {
        this.uebungen = uebungen;
    }

    public static class UebungViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titelView, beschreibungView;

        public UebungViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewUebung);
            titelView = itemView.findViewById(R.id.textViewTitel);
            beschreibungView = itemView.findViewById(R.id.textViewBeschreibung);
        }
    }

    @NonNull
    @Override
    public UebungViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uebung_item, parent, false);
        return new UebungViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UebungViewHolder holder, int position) {
        Uebung uebung = uebungen.get(position);
        holder.imageView.setImageResource(uebung.getBildResId());
        holder.titelView.setText(uebung.getTitel());
        holder.beschreibungView.setText(uebung.getBeschreibung());

        holder.itemView.setOnClickListener(v -> {
            // Überprüfen, ob der Titel der Übung "Herzdruckmassage" ist
            if (uebung.getTitel().equals("Herzdruckmassage")) {
                // Starte die Herzdruckmassage Activity
                Intent intent = new Intent(v.getContext(), Herzdruckmassage.class);
                v.getContext().startActivity(intent);
            } else {
                // Hier kannst du ähnliche Aktionen für andere Übungen hinzufügen
                Toast.makeText(v.getContext(), "Übung " + uebung.getTitel() + " ausgewählt!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return uebungen.size();
    }
}