package tk.jabtk.attentrack.professor.model;

public class LectureModel {

    int TotalLectCount, AbsentLectCount, PresentLectCount;
    String StudentID;

    public String getStudentId() {
        return StudentID;
    }

    public LectureModel() {
    }

    public int getTotalLectCount() {
        return TotalLectCount;
    }

    public int getAbsentLectCount() {
        return AbsentLectCount;
    }

    public int getPresentLectCount() {
        return PresentLectCount;
    }
}
