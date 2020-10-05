package com.emekachukwulobe.cruddemo2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {

    private OnItemClickListener listener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Note model) {
        holder.textViewTitle.setText(model.getTitle());
        holder.textViewDescription.setText(model.getDescription());
        holder.textViewPriority.setText(String.valueOf(model.getPriority()));
//        holder.textViewTime.setText(String.valueOf(model.getDate().getTime()));

        String mySpecialDTFormat = DateFormat.getTimeInstance(DateFormat.SHORT).format(model.getDate().getTime())
                + " - "
                + DateFormat.getDateInstance(DateFormat.MONTH_FIELD).format(model.getDate());

        holder.textViewTime.setText(mySpecialDTFormat);

        if (model.getPriority() == 3){
            holder.noteBackground.setCardBackgroundColor(Color.rgb(255, 219, 219));
            holder.textViewPriority.setText(R.string.critical);
            holder.textViewPriority.setTextColor(Color.rgb(243, 0, 0));
            holder.textViewTime.setTextColor(Color.rgb(243, 0, 0));
        } else if (model.getPriority() == 2) {
            holder.noteBackground.setCardBackgroundColor(Color.rgb(255, 254, 219));
            holder.textViewPriority.setText(R.string.important);
            holder.textViewPriority.setTextColor(Color.rgb(134, 138, 0));
            holder.textViewTime.setTextColor(Color.rgb(134, 138, 0));
        } else {
            holder.noteBackground.setCardBackgroundColor(Color.rgb(219, 255, 251));
            holder.textViewPriority.setText(R.string.normal);
            holder.textViewPriority.setTextColor(Color.rgb(0, 163, 243));
            holder.textViewTime.setTextColor(Color.rgb(0, 163, 243));
        }

    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);

        return new NoteHolder(v);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class NoteHolder extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPriority;
        TextView textViewTime;

        CardView noteBackground;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewPriority = itemView.findViewById(R.id.text_view_priority);
            textViewTime = itemView.findViewById(R.id.text_view_time);

            noteBackground = itemView.findViewById(R.id.note_background);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return getSnapshots().size();
//        return super.getItemCount();
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

}
