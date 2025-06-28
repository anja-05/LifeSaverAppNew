package at.fhj.lifesaver.lesson;

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

import at.fhj.lifesaver.R;

/**
 * Die Klasse "LektionAdapter" ist für die Darstellung einer Liste von den Lektionen in einer RecyclerView zuständig.
 * Es wird der Titel, die Nummer, ein Bild und eine Fortschrittsanzeige (Haken bei erfolgreichem Lektionsabschluss) für jede Lektion gezeigt.
 * Beim Klicken auf die Lektion wird die genauer Datailansicht der jeweiligen Lektion geöffnet.
 */
public class LektionAdapter extends RecyclerView.Adapter<LektionAdapter.LektionViewHolder> {

    private List<Lektion> lektionen;
    private Context context;

    /**
     * Konsrukor für den LektionAdapter
     * @param lektionen Liste der darzustellenden Lektionen
     * @param context Kontext, z.B. Activity oder Fragment
     * @throws IllegalArgumentException falls die Parameter null sind
     */
    public LektionAdapter(List<Lektion> lektionen, Context context) {
        if (lektionen == null) {
            throw new IllegalArgumentException("Lektionliste darf nicht null sein.");
        }
        if (context == null) {
            throw new IllegalArgumentException("Context darf nicht null sein.");
        }

        this.lektionen = lektionen;
        this.context = context;
    }

    /**
     * Erzeugt ein neues LektionViewHolder-Objekt und verbindet es mit dem Layout.
     * @param parent  Elternansicht (RecyclerView)
     * @param viewType The view type of the new View.(Nicht verwendet)
     *
     * @return ein initialisierter LektionViewHolder
     */
    @NonNull
    @Override
    public LektionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lektion_item, parent, false);
        return new LektionViewHolder(view);
    }

    /**
     * Bindet Daten an den übergebenen ViewHolder für die entsprechende Position.
     * @param holder ViewHolder mit UI-Elementen
     * @param position Position in der Liste
     */
    @Override
    public void onBindViewHolder(@NonNull LektionViewHolder holder, int position) {
        Lektion lektion = lektionen.get(position);

        holder.textViewNummer.setText((position + 1) + ".");
        holder.textViewTitel.setText(lektion.getTitel());
        holder.imageViewLektion.setImageResource(lektion.getBildResId());

        SharedPreferences prefsUser = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String userEmail = prefsUser.getString("user_email", "default");

        SharedPreferences prefsProgress = context.getSharedPreferences("progress_" + userEmail, Context.MODE_PRIVATE);
        boolean isDone = prefsProgress.getBoolean("lesson_" + lektion.getTitel(), false);

        if (isDone) {
            holder.imageViewCheckmark.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewCheckmark.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LektionDetailActivity.class);
            intent.putExtra("TITEL", lektion.getTitel());
            intent.putExtra("DATEINAME", lektion.getDateiname());
            context.startActivity(intent);
        });

    }

    /**
     * Gibt die Anzahl der Elemente (Lektionseinträge) zurück.
     * @return Anzahl der Lektionen
     */
    @Override
    public int getItemCount() {
        return lektionen.size();
    }

    /**
     * ViewHolder-Klasse für eine einzelne Lektion.
     */
    public static class LektionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNummer;
        TextView textViewTitel;
        ImageView imageViewLektion;
        ImageView imageViewCheckmark;

        /**
         * Konstruktor für den ViewHolder, initialisiert alle UI-Komponenten.
         * @param itemView Einzelnes Item-Layout
         */
        public LektionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNummer = itemView.findViewById(R.id.textViewNummer);
            textViewTitel = itemView.findViewById(R.id.textViewTitel);
            imageViewLektion = itemView.findViewById(R.id.imageViewLektion);
            imageViewCheckmark = itemView.findViewById(R.id.imageViewCheckmark);
        }
    }
}