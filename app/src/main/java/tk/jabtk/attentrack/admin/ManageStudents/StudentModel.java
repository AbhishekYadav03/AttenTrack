package tk.jabtk.attentrack.admin.ManageStudents;

public class StudentModel {
    String StudentName, StudentEmail, StudentID, RegisteredOn, ClassCode, ProfileUrl;
    Long StudentRollNo,CollegeID;

    public StudentModel() {
    }

    public String getStudentName() {
        return StudentName;
    }

    public String getStudentEmail() {
        return StudentEmail;
    }

    public String getStudentID() {
        return StudentID;
    }

    public String getRegisteredOn() {
        return RegisteredOn;
    }

    public String getClassCode() {
        return ClassCode;
    }

    public Long getStudentRollNo() {
        return StudentRollNo;
    }

    public String getProfileUrl() {
        return ProfileUrl;
    }

    public Long getCollegeID() {
        return CollegeID;
    }
}
