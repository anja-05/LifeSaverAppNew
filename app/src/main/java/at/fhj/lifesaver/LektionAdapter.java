package at.fhj.lifesaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LektionAdapter extends RecyclerView.Adapter<LektionAdapter.LektionViewHolder> {

    private List<Lektion> lektionen;
    private Context context;

    public LektionAdapter(List<Lektion> lektionen, Context context) {
        this.lektionen = lektionen;
        this.context = context;
    }

    @NonNull
    @Override
    public LektionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lektion_item, parent, false);
        return new LektionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LektionViewHolder holder, int position) {
        Lektion lektion = lektionen.get(position);

        // Nummer, Titel und Bild setzen
        holder.textViewNummer.setText((position + 1) + ".");
        holder.textViewTitel.setText(lektion.getTitel());
        holder.imageViewLektion.setImageResource(lektion.getBildResId());

        // Fortschritt prüfen und Haken anzeigen
        SharedPreferences prefsUser = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String userEmail = prefsUser.getString("user_email", "default");
        SharedPreferences prefsProgress = context.getSharedPreferences("progress_" + userEmail, Context.MODE_PRIVATE);
        boolean isDone = prefsProgress.getBoolean("lesson_" + lektion.getTitel(), false);

        if (isDone) {
            holder.imageViewCheckmark.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewCheckmark.setVisibility(View.GONE);
        }


        // Beim Klicken neue Activity starten
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LektionDetailActivity.class);
            intent.putExtra("TITEL", lektion.getTitel());
            intent.putExtra("DATEINAME", lektion.getDateiname());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return lektionen.size();
    }

    public static class LektionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNummer;
        TextView textViewTitel;
        ImageView imageViewLektion;
        ImageView imageViewCheckmark;

        public LektionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNummer = itemView.findViewById(R.id.textViewNummer);
            textViewTitel = itemView.findViewById(R.id.textViewTitel);
            imageViewLektion = itemView.findViewById(R.id.imageViewLektion);
            imageViewCheckmark = itemView.findViewById(R.id.imageViewCheckmark);

        }
    }
}
