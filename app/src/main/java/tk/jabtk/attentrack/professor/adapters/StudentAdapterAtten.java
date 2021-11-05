package tk.jabtk.attentrack.professor.adapters;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.admin.ManageStudents.StudentModel;

public class StudentAdapterAtten extends FirestoreRecyclerAdapter<StudentModel, StudentAdapterAtten.StudentHolder> {
    StudentListener studentListener;

    public StudentAdapterAtten(@NonNull FirestoreRecyclerOptions<StudentModel> options, StudentListener studentListener) {
        super(options);
        this.studentListener = studentListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull StudentHolder holder, int position, @NonNull StudentModel model) {

        if (model.getProfileUrl() != null) {
            Uri uri = Uri.parse(model.getProfileUrl());
            Picasso.get().load(uri).placeholder(R.drawable.ic_student).resize(200, 200).centerCrop().into(holder.UserProfile);
            Log.i("Profile", "Found");
        }
        holder.CollegeID.setText(model.getCollegeID().toString());
        holder.Email.setText(model.getStudentEmail());
        holder.Name.setText(model.getStudentName());
        holder.RollNo.setText(model.getStudentRollNo().toString());
    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_id_card, parent, false);

        return new StudentHolder(view);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public class StudentHolder extends RecyclerView.ViewHolder {
        CircleImageView UserProfile;
        TextView Name, Email, RollNo, CollegeID;

        public StudentHolder(@NonNull View itemView) {
            super(itemView);
            UserProfile = itemView.findViewById(R.id.studentProfile);
            Name = itemView.findViewById(R.id.StudentName);
            Email = itemView.findViewById(R.id.StudentEmail);
            CollegeID = itemView.findViewById(R.id.StudentCollegeID);
            RollNo = itemView.findViewById(R.id.StudentRollNo);

        }

        public void isAbsent() {
            studentListener.handleAbsent(getSnapshots().getSnapshot(getAdapterPosition()));
            Log.d("TAG", "isAbsent");
        }

        public void isPresent() {
            studentListener.handlePresent(getSnapshots().getSnapshot(getAdapterPosition()));
            Log.d("TAG", "isPresent");
        }

    }


    public interface StudentListener {
        void handleAbsent(DocumentSnapshot snapshot);

        void handlePresent(DocumentSnapshot snapshot);

    }


}
