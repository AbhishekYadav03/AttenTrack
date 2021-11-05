package tk.jabtk.attentrack.professor.adapters;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;

import tk.jabtk.attentrack.R;
import tk.jabtk.attentrack.professor.model.LectureModel;

public class LectureAdapter extends FirestoreRecyclerAdapter<LectureModel, LectureAdapter.StudentLectHolder> {
    Query query;
    private LectureAdapter.OnItemClickListener listener;

    public LectureAdapter(@NonNull FirestoreRecyclerOptions<LectureModel> options, Query queryOld) {
        super(options);
        query = queryOld;
    }

    @Override
    protected void onBindViewHolder(@NonNull StudentLectHolder holder, int position, @NonNull LectureModel model) {
        int TotalLectCount, AbsentLectCount, PresentLectCount;
        TotalLectCount = model.getTotalLectCount();
        AbsentLectCount = model.getAbsentLectCount();
        PresentLectCount = model.getPresentLectCount();

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    Map<String, Object> Student = document.getData();

                    //Log.d("QUERY : \n", document.getId() + " => \n" + model.getStudentId());

                    String documentId = document.getId();
                    String studentId = model.getStudentId();

                    if (documentId.equals(studentId)) {
                        Uri uri = Uri.parse(String.valueOf(Student.get("ProfileUrl")));

                        holder.StudentName.setText(Student.get("StudentName").toString());
                        holder.TotalLecture.setText(String.valueOf(TotalLectCount));
                        holder.AbsentLecture.setText(String.valueOf(AbsentLectCount));
                        holder.PresentLecture.setText(String.valueOf(PresentLectCount));

                        if (uri != null) {

                            //Uri uri = Uri.parse(model.getProfileUrl());
                            Picasso.get().load(uri).placeholder(R.drawable.ic_student).resize(200, 200).centerCrop().into(holder.studentProfile);
                            Log.i("QUERY : ProfileUri", "Found");
                        }
                        //Log.d("QUERY : ", document.getId() + " => " + Student);
                    } else
                        //Log.d("QUERY : ID not Matched ", document.getId() + " => " + Student);
                        Log.d("QUERY : ID not Matched ", "\n" + documentId + "\n" + studentId);


                }
            }
        });


    }

    @NonNull
    @Override
    public StudentLectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_report_item, parent, false);
        return new StudentLectHolder(view);
    }

    class StudentLectHolder extends RecyclerView.ViewHolder {
        TextView StudentName, TotalLecture, AbsentLecture, PresentLecture;
        ImageView studentProfile;

        public StudentLectHolder(@NonNull View itemView) {

            super(itemView);

            StudentName = (TextView) itemView.findViewById(R.id.StudentName);
            studentProfile = itemView.findViewById(R.id.studentProfile);
            TotalLecture = (TextView) itemView.findViewById(R.id.totalLectCount);
            AbsentLecture = (TextView) itemView.findViewById(R.id.absentLectCount);
            PresentLecture = (TextView) itemView.findViewById(R.id.presentLectCount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position,StudentName.getText().toString());
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

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot Snapshot, int position,String name);

        void onLongItemClick(DocumentSnapshot Snapshot, int position);
    }

    /// this will used in main fragment for call
    public void setItemClickListener(LectureAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
