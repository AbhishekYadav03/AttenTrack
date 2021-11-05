package tk.jabtk.attentrack.professor.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.professor.model.StudentRequest;

import static com.google.firebase.firestore.SetOptions.merge;

public class StudentAdapterRequest extends FirebaseRecyclerAdapter<StudentRequest, StudentAdapterRequest.StudentRequestHolder> {

    public StudentAdapterRequest(@NonNull FirebaseRecyclerOptions<StudentRequest> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull StudentRequestHolder holder, int position, @NonNull StudentRequest model) {

        if (model.getProfileUrl() != null) {
            Uri uri = Uri.parse(model.getProfileUrl());
            Picasso.get().load(uri).resize(200, 200).centerCrop().into(holder.UserProfile);
            Log.i("Profile","Found");
        } else {
            Log.i("Profile","NotFound");
            holder.UserProfile.setImageResource(R.drawable.ic_student);
        }
        holder.Name.setText(model.getStudentName());
        holder.Email.setText(model.getStudentEmail());

        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference db = FirebaseFirestore.getInstance().collection("ClassList")
                        .document(model.getClassCode())
                        .collection("Students").document(model.getStudentID());

                Map<String, Object> AcceptInfo = new HashMap<>();
                AcceptInfo.put("StudentEmail", model.getStudentEmail());
                AcceptInfo.put("StudentID", model.getStudentID());
                AcceptInfo.put("StudentName", model.getStudentName());
                AcceptInfo.put("AcceptedOn", FieldValue.serverTimestamp());
                AcceptInfo.put("ProfileUrl",model.getProfileUrl());
                AcceptInfo.put("StudentRollNo", model.getStudentRollNo());
                AcceptInfo.put("CollegeID",model.getCollegeID());
                AcceptInfo.put("ClassName",model.getClassName());
                AcceptInfo.put("ClassCode",model.getClassCode());

                db.set(AcceptInfo);

                Log.i("AcceptInfo",AcceptInfo.toString());

                DocumentReference db2=FirebaseFirestore.getInstance().collection("Students").document(model.getStudentID());
                Map<String, Object> classMap = new HashMap<>();
                classMap.put("ClassCode", model.getClassCode());
                classMap.put("ClassName",model.getClassName());
                db2.set(classMap, merge());

                Log.i("AcceptInfoCLassMap",classMap.toString());

                getSnapshots().getSnapshot(position).getRef().removeValue();

            }
        });
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSnapshots().getSnapshot(position).getRef().removeValue();
            }
        });
    }

    @NonNull
    @Override
    public StudentRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_request, parent, false);
        return new StudentRequestHolder(view);

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    static class StudentRequestHolder extends RecyclerView.ViewHolder {
        CircleImageView UserProfile;
        TextView Name, Email;
        MaterialButton acceptBtn, deleteBtn;

        public StudentRequestHolder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.userName);
            Email = itemView.findViewById(R.id.userEmail);
            UserProfile = itemView.findViewById(R.id.userProfile);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);

        }
    }
}