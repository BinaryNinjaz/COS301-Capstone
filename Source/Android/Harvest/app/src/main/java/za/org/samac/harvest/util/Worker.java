package za.org.samac.harvest.util;

import java.util.List;

public class Worker{
    protected String fName, sName;
    protected List<Orchard> assignedOrchards;
    protected WorkerType workerType;
    protected String further;
    protected String phone;
    protected String fID;
    protected String nID;

    public Worker(){
    }

    public void setFurther(String further) {
        this.further = further;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAssignedOrchards(List<Orchard> assignedOrchards) {
        this.assignedOrchards = assignedOrchards;
    }

    public void setfID(String fID) {
        this.fID = fID;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setnID(String nID) {
        this.nID = nID;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public void setWorkerType(WorkerType workerType) {
        this.workerType = workerType;
    }

    public String getFurther() {
        return further;
    }

    public String getPhone() {
        return phone;
    }

    public List<Orchard> getAssignedOrchards() {
        return assignedOrchards;
    }

    public String getfID() {
        return fID;
    }

    public String getnID() {
        return nID;
    }

    public WorkerType getWorkerType() {
        return workerType;
    }

    public String getfName() {
        return fName;
    }

    public String getsName() {
        return sName;
    }


    public void addOrchard (Orchard orchard){
        assignedOrchards.add(orchard);
    }

    public void removeOrchard (String ID){
        for (Orchard current : assignedOrchards){
            if (current.getID().equals(ID)){
                assignedOrchards.remove(current);
            }
        }
    }
}

