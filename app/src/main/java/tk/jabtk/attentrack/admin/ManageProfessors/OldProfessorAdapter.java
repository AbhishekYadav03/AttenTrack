package tk.jabtk.attentrack.admin.ManageProfessors;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import tk.jabtk.attentrack.R;

public class OldProfessorAdapter extends FirestoreRecyclerAdapter<OldProfessorModel, OldProfessorAdapter.OldProfessorHolder> {
    private OnItemClickListener listener;

    public OldProfessorAdapter(@NonNull FirestoreRecyclerOptions<OldProfessorModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull OldProfessorHolder holder, int position, @NonNull OldProfessorModel model) {

        if (model.getProfileUrl() != null) {
            Uri uri = Uri.parse(model.getProfileUrl());
            Picasso.get().load(uri).placeholder(R.drawable.professor_ic_avatar).resize(200, 200).centerCrop().into(holder.profileImage);
            Log.i("Profile","Found "+model.getProfileUrl());
        } else {
            Log.i("Profile","NotFounddddd");
            holder.profileImage.setImageResource(R.drawable.professor_ic_avatar);
        }
        holder.professorName.setText(model.getProfessorName());
        holder.regCode.setText(model.getRegCode());
        holder.professorEmail.setText(model.getProfessorEmail());

    }

    @NonNull
    @Override
    public OldProfessorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_card, parent, false);
        return new OldProfessorHolder(view);
    }


    class OldProfessorHolder extends RecyclerView.ViewHolder {
        private final TextView professorName;
        private final TextView professorEmail;
        private final TextView regCode;
        private ImageView profileImage;

        public OldProfessorHolder(@NonNull View itemView) {
            super(itemView);
            professorName = itemView.findViewById(R.id.professorCardName);
            regCode = itemView.findViewById(R.id.professorCardCode);
            professorEmail = itemView.findViewById(R.id.professorCardEmail);
            profileImage=itemView.findViewById(R.id.professorCardImage);

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
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    /////this interface is used for call alertDialog in fragment :
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot Snapshot, int position);

        void onLongItemClick(DocumentSnapshot Snapshot, int position);
    }

    /// this will used in main fragment for call
    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
