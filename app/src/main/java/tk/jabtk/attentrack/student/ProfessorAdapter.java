package tk.jabtk.attentrack.student;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import tk.jabtk.attentrack.R;

public class ProfessorAdapter extends FirestoreRecyclerAdapter<ProfessorModel, ProfessorAdapter.ProfessorHolder> {
    private static final String TAG = "ProfessorAdapter";
    DatabaseReference query;

    public ProfessorAdapter(@NonNull FirestoreRecyclerOptions<ProfessorModel> options, DatabaseReference queryOld) {
        super(options);
        query = queryOld;
    }

    @NonNull
    @Override
    public ProfessorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_card, null, false);

        return new ProfessorHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProfessorHolder holder, int position, @NonNull ProfessorModel model) {

        if (model.getProfileUrl() != null) {
            Uri uri = Uri.parse(model.getProfileUrl());

            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.professor_ic_avatar)
                    .resize(200, 200)
                    .centerCrop()
                    .into(holder.ProfessorProfile);
        } else {
            Picasso.get().load(R.drawable.professor_ic_avatar)
                    .placeholder(R.drawable.professor_ic_avatar)
                    .into(holder.ProfessorProfile);

            Log.d(TAG, "onBindViewHolder: profile not found");
        }


        holder.ProfessorsName.setText(model.getProfessorName());
        holder.ProfessorEmail.setText(model.getProfessorEmail());

        Log.d(TAG, "onBindViewHolder: " + model.toString());

        query.child(model.getProfessorID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String[] splitString = snapshot.getValue().toString().split("=");
                    String splitString2 = splitString[0];
                    int length = splitString2.length();

                    String subject = splitString2.substring(1, length).trim();
                    Log.d(TAG, "onDataChange: " + subject);
                    holder.subjectText.setText(subject);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    class ProfessorHolder extends RecyclerView.ViewHolder {
        TextView ProfessorsName, ProfessorEmail, subjectText;
        CircleImageView ProfessorProfile;

        public ProfessorHolder(@NonNull View itemView) {
            super(itemView);
            ProfessorsName = itemView.findViewById(R.id.professorCardName);
            ProfessorEmail = itemView.findViewById(R.id.professorCardEmail);
            ProfessorProfile = itemView.findViewById(R.id.professorCardImage);
            subjectText = itemView.findViewById(R.id.professorCardCode);

            subjectText.setTextSize(18f);
        }
    }
}
