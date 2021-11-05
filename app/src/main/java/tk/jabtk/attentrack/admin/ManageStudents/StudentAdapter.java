package tk.jabtk.attentrack.admin.ManageStudents;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import tk.jabtk.attentrack.R;

public class StudentAdapter extends FirestoreRecyclerAdapter<StudentModel, StudentAdapter.StudentHolder> {


    public StudentAdapter(@NonNull FirestoreRecyclerOptions<StudentModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull StudentHolder holder, int position, @NonNull StudentModel model) {

        if (model.getProfileUrl() != null) {
            Uri uri = Uri.parse(model.getProfileUrl());
            Picasso.get().load(uri).resize(200, 200).centerCrop().into(holder.UserProfile);
            Log.i("Profile","Found");
        } else {
            Log.i("Profile","NotFound");
            holder.UserProfile.setImageResource(R.drawable.ic_student);
        }
        if (model.getCollegeID() != null) {
            holder.CollegeID.setText(model.getCollegeID().toString());
        }
        holder.Email.setText(model.getStudentEmail());
        holder.Name.setText(model.getStudentName());


    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_manage, parent, false);

        return new StudentHolder(view);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    class StudentHolder extends RecyclerView.ViewHolder {
        CircleImageView UserProfile;
        TextView Name, Email, CollegeID;

        public StudentHolder(@NonNull View itemView) {
            super(itemView);
            UserProfile = itemView.findViewById(R.id.userProfileManage);
            Name = itemView.findViewById(R.id.userName);
            Email = itemView.findViewById(R.id.userEmail);
            CollegeID = itemView.findViewById(R.id.userIdNo);

        }
    }
}
