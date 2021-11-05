package tk.jabtk.attentrack.admin.ManageAdmins;

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

public class NewAdminAdapter extends FirebaseRecyclerAdapter<AdminModel, NewAdminAdapter.NewAdminHolder> {
    private NewAdminAdapter.OnItemClickListener listener;


    public NewAdminAdapter(@NonNull FirebaseRecyclerOptions<AdminModel> options) {
        super(options);
    }


    @NonNull
    @Override
    public NewAdminAdapter.NewAdminHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_card, parent, false);
        return new NewAdminHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull NewAdminHolder holder, int position, @NonNull AdminModel model) {
        holder.AdminName.setText(model.getAdminName());
        holder.AdminEmail.setText(model.getAdminEmail());
        holder.circleImageView.setImageResource(R.drawable.admin_ic_avatar);
        holder.regCode.setText(model.getRegCode());
    }


    class NewAdminHolder extends RecyclerView.ViewHolder {
        private final TextView AdminName;
        private final TextView AdminEmail;
        private final TextView regCode;
        private CircleImageView circleImageView;

        public NewAdminHolder(@NonNull View itemView) {
            super(itemView);
            AdminName = itemView.findViewById(R.id.professorCardName);
            regCode = itemView.findViewById(R.id.professorCardCode);
            AdminEmail = itemView.findViewById(R.id.professorCardEmail);
            circleImageView = itemView.findViewById(R.id.professorCardImage);

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
    public void setItemClickListener(NewAdminAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
