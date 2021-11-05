package tk.jabtk.attentrack.admin.ManageClassrooms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;

import tk.jabtk.attentrack.R;

public class ClassAdapter extends FirebaseRecyclerAdapter<ClassModel, ClassAdapter.ClassHolder> {
    private OnItemClickListener listener;

    public ClassAdapter(@NonNull FirebaseRecyclerOptions<ClassModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ClassHolder holder, int position, @NonNull ClassModel model) {
        holder.className.setText(model.getClassName());
        holder.classCode.setText(model.getClassCode());
    }

    @NonNull
    @Override
    public ClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_card, parent, false);
        return new ClassHolder(view);
    }




    class ClassHolder extends RecyclerView.ViewHolder {
        private final TextView classCode;
        private final TextView className;

        public ClassHolder(@NonNull View itemView) {
            super(itemView);

            className = itemView.findViewById(R.id.className);
            classCode = itemView.findViewById(R.id.classCode);

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
