package tk.jabtk.attentrack.admin.ManageProfessors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import tk.jabtk.attentrack.R;

public class NewProfessorAdapter extends FirebaseRecyclerAdapter<NewProfessorModel, NewProfessorAdapter.NewProfessorHolder> {
    private OnItemClickListener listener;

    public NewProfessorAdapter(@NonNull FirebaseRecyclerOptions<NewProfessorModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NewProfessorHolder holder, int position, @NonNull NewProfessorModel model) {
        holder.professorName.setText(model.getProfessorName());
        holder.regCode.setText(model.getRegCode());
        holder.professorEmail.setText(model.getProfessorEmail());
        holder.circleImageView.setImageResource(R.drawable.professor_ic_avatar);
    }

    @NonNull
    @Override
    public NewProfessorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_card, parent, false);
        return new NewProfessorHolder(view);
    }


    class NewProfessorHolder extends RecyclerView.ViewHolder {
        private final TextView professorName;
        private final TextView professorEmail;
        private final TextView regCode;
        private CircleImageView circleImageView;

        public NewProfessorHolder(@NonNull View itemView) {
            super(itemView);
            professorName = itemView.findViewById(R.id.professorCardName);
            regCode = itemView.findViewById(R.id.professorCardCode);
            professorEmail = itemView.findViewById(R.id.professorCardEmail);
            circleImageView=itemView.findViewById(R.id.professorCardImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onLongItemClick(getSnapshots().getSnapshot(position), position);
                        return true;
                    }
                    return false;
                }
            });
        }
    }


    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getRef().removeValue();
    }

    /////this interface is used for call alertDialog in fragment :
    public interface OnItemClickListener {
        void onItemClick(DataSnapshot Snapshot, int position);

        void onLongItemClick(DataSnapshot Snapshot, int position);
    }

    /// this will used in main fragment for call
    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
