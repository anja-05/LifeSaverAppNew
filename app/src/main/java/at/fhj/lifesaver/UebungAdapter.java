package at.fhj.lifesaver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
    }

    @Override
    public int getItemCount() {
        return uebungen.size();
    }
}

