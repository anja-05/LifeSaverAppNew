package at.fhj.lifesaver.training;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import at.fhj.lifesaver.R;

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
            String titel = uebung.getTitel();

            if (titel.equalsIgnoreCase("Herzdruckmassage")) {
                Intent intent = new Intent(v.getContext(), Herzdruckmassage.class);
                v.getContext().startActivity(intent);
            } else if (titel.equalsIgnoreCase("Stabile Seitenlage")) {
                Intent intent = new Intent(v.getContext(), StabileSeitenlageActivity.class);
                v.getContext().startActivity(intent);
            } else if (titel.equalsIgnoreCase("Rautekgriff")) {
                Intent intent = new Intent(v.getContext(), RautekgriffActivity.class);
                v.getContext().startActivity(intent);
            }
        });
        }

    @Override
    public int getItemCount() {
        return uebungen.size();
    }
}