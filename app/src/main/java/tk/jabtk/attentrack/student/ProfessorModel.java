package tk.jabtk.attentrack.student;

public class ProfessorModel {
    String ProfessorName;
    String RegCode;
    String ProfessorEmail;
    String AddedOn;
    String AddedBy;
    String ModifiedBy;
    String ModifiedOn;
    String ProfessorID;
    String ProfileUrl;

    public ProfessorModel() {
    }

    @Override
    public String toString() {
        return "ProfessorModel{" +
                "ProfessorName='" + ProfessorName + '\'' +
                ", RegCode='" + RegCode + '\'' +
                ", ProfessorEmail='" + ProfessorEmail + '\'' +
                ", AddedOn='" + AddedOn + '\'' +
                ", AddedBy='" + AddedBy + '\'' +
                ", ModifiedBy='" + ModifiedBy + '\'' +
                ", ModifiedOn='" + ModifiedOn + '\'' +
                ", ProfessorID='" + ProfessorID + '\'' +
                ", ProfileUrl='" + ProfileUrl + '\'' +
                '}';
    }


    public String getProfileUrl() {
        return ProfileUrl;
    }

    public String getProfessorName() {
        return ProfessorName;
    }

    public String getRegCode() {
        return RegCode;
    }

    public String getProfessorEmail() {
        return ProfessorEmail;
    }

    public String getAddedOn() {
        return AddedOn;
    }

    public String getAddedBy() {
        return AddedBy;
    }

    public String getModifiedBy() {
        return ModifiedBy;
    }

    public String getModifiedOn() {
        return ModifiedOn;
    }

    public String getProfessorID() {
        return ProfessorID;
    }

}




