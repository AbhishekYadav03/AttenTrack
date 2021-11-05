package tk.jabtk.attentrack.admin.ManageAdmins;


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

public class AllAdminAdapter extends FirestoreRecyclerAdapter<AdminModel, AllAdminAdapter.AllAdminHolder> {
    private AllAdminAdapter.OnItemClickListener listener;

    public AllAdminAdapter(@NonNull FirestoreRecyclerOptions<AdminModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AllAdminAdapter.AllAdminHolder holder, int position, @NonNull AdminModel model) {

        if (model.getProfileUrl() != null) {
            Uri uri = Uri.parse(model.getProfileUrl());
            Picasso.get().load(uri).placeholder(R.drawable.admin_ic_avatar)
                    .resize(200, 200)
                    .centerCrop().into(holder.profileImage);
            Log.i("Profile", "Found " + model.getProfileUrl());
        } else {
            Log.i("Profile", "NotFound");
            holder.profileImage.setImageResource(R.drawable.admin_ic_avatar);
        }
        holder.AdminName.setText(model.getAdminName());
        holder.AdminEmail.setText(model.getAdminEmail());
        holder.RegCode.setText(model.getRegCode());
    }

    @NonNull
    @Override
    public AllAdminAdapter.AllAdminHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_card, parent, false);
        return new AllAdminAdapter.AllAdminHolder(view);
    }


    class AllAdminHolder extends RecyclerView.ViewHolder {
        private final TextView AdminName;
        private final TextView AdminEmail;
        private final TextView RegCode;
        private ImageView profileImage;

        public AllAdminHolder(@NonNull View itemView) {
            super(itemView);
            AdminName = itemView.findViewById(R.id.professorCardName);
            AdminEmail = itemView.findViewById(R.id.professorCardEmail);
            profileImage = itemView.findViewById(R.id.professorCardImage);
            RegCode = itemView.findViewById(R.id.professorCardCode);

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
    public void setItemClickListener(AllAdminAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
