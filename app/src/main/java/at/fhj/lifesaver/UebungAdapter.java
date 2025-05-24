package at.fhj.lifesaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UebungAdapter extends RecyclerView.Adapter<UebungAdapter.UebungViewHolder>{
    private List<Uebung> uebungen;
    private Context context;

    public UebungAdapter(List<Uebung> uebungen, Context context) {
        this.uebungen = uebungen;
        this.context = context;
    }

    @NonNull
    @Override
    public UebungViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.uebung_item, parent, false);
        return new UebungAdapter.UebungViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UebungViewHolder holder, int position) {
        Uebung uebung = uebungen.get(position);
        holder.textViewNummer.setText((position + 1) + ".");
        holder.textViewTitel.setText(uebung.titel);
        holder.imageViewUebung.setImageResource(uebung.bildResId);
    }

    @Override
    public int getItemCount() {
        return uebungen.size();
    }

    static class UebungViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNummer, textViewTitel;
        ImageView imageViewUebung;

        public UebungViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNummer = itemView.findViewById(R.id.textViewNummer);
            textViewTitel = itemView.findViewById(R.id.textViewTitel);
            imageViewUebung = itemView.findViewById(R.id.imageViewUebung);
        }
    }
}
