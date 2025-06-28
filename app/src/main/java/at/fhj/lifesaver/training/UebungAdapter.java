package at.fhj.lifesaver.training;

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

import at.fhj.lifesaver.R;

/**
 * Der {@code UebungAdapter} ist ein RecyclerView.Adapter zur Darstellung einer Liste von Erste-Hilfe-Übungen.
 * Jede Übung enthält ein Bild, einen Titel und eine Beschreibung.
 * Beim Klick auf ein Listenelement wird die entsprechende Aktivität geöffnet.
 */
public class UebungAdapter extends RecyclerView.Adapter<UebungAdapter.UebungViewHolder> {

    private List<Uebung> uebungen;

    /**
     * Erstellt einen neuen Adapter mit einer Liste von Übungen.
     * @param uebungen Liste der darzustellenden Übungsobjekte
     */
    public UebungAdapter(List<Uebung> uebungen) {
        this.uebungen = uebungen;
    }

    /**
     * ViewHolder-Klasse zur Darstellung eines einzelnen Übungseintrags.
     */
    public static class UebungViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titelView, beschreibungView;

        /**
         * Konstruktor initialisiert die Referenzen auf die UI-Elemente.
         * @param itemView Die Ansicht für ein einzelnes Listenelement
         */
        public UebungViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewUebung);
            titelView = itemView.findViewById(R.id.textViewTitel);
            beschreibungView = itemView.findViewById(R.id.textViewBeschreibung);
        }
    }

    /**
     * Erzeugt eine neue ViewHolder-Instanz für ein Listenelement.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return neuer ViewHolder
     */
    @NonNull
    @Override
    public UebungViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uebung_item, parent, false);
        return new UebungViewHolder(view);
    }

    /**
     * Bindet die Daten eines Übungsobjekts an die UI-Elemente.
     * Reagiert auf Klicks, indem die passende Übung geöffnet wird.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UebungViewHolder holder, int position) {
        if (uebungen == null || position >= uebungen.size()) return;
        Uebung uebung = uebungen.get(position);
        holder.imageView.setImageResource(uebung.getBildResId());
        holder.titelView.setText(uebung.getTitel());
        holder.beschreibungView.setText(uebung.getBeschreibung());

        holder.itemView.setOnClickListener(v -> {
            try {
                String titel = uebung.getTitel();
                String herzdruck = v.getContext().getString(R.string.uebung_herzdruckmassage);
                String seitenlage = v.getContext().getString(R.string.uebung_stabile_seitenlage);
                String rautek = v.getContext().getString(R.string.uebung_rautekgriff);

                Intent intent;

                if (titel.equalsIgnoreCase(herzdruck)) {
                    intent = new Intent(v.getContext(), Herzdruckmassage.class);
                } else if (titel.equalsIgnoreCase(seitenlage)) {
                    intent = new Intent(v.getContext(), StabileSeitenlageActivity.class);
                } else if (titel.equalsIgnoreCase(rautek)) {
                    intent = new Intent(v.getContext(), RautekgriffActivity.class);
                } else {
                    Toast.makeText(v.getContext(),
                            v.getContext().getString(R.string.uebung_unbekannt, titel),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                v.getContext().startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(v.getContext(),
                        v.getContext().getString(R.string.uebung_startfehler),
                        Toast.LENGTH_LONG).show();
            }
        });
        }

    /**
     * Gibt die Anzahl der Übungen zurück.
     * @return Anzahl der Listeneinträge
     */
        @Override
    public int getItemCount() {
        return uebungen.size();
    }
}