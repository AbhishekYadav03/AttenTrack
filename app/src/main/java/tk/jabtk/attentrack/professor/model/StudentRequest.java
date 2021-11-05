package tk.jabtk.attentrack.professor.model;

public class StudentRequest {

    String ClassCode, RequestTimeStamp, StudentEmail, StudentID, StudentName, ProfileUrl,ClassName;
    Long StudentRollNo,CollegeID;

    public StudentRequest() {
    }

    public String getClassName() {
        return ClassName;
    }

    public String getClassCode() {
        return ClassCode;
    }

    public String getRequestTimeStamp() {
        return RequestTimeStamp;
    }

    public String getStudentEmail() {
        return StudentEmail;
    }

    public String getStudentID() {
        return StudentID;
    }

    public String getStudentName() {
        return StudentName;
    }

    public String getProfileUrl() {
        return ProfileUrl;
    }

    public Long getStudentRollNo() {
        return StudentRollNo;
    }

    public Long getCollegeID() {
        return CollegeID;
    }
}
