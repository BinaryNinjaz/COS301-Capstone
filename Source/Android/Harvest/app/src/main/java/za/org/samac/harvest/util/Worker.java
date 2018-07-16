package za.org.samac.harvest.util;

import java.util.List;
import java.util.Vector;

public class Worker{
    protected String fName, sName;
    protected List<Orchard> assignedOrchards;
    protected WorkerType workerType;
    protected boolean wasForeman = false;
    protected String further;
    protected String phone, oldPhone;
    protected String fID;
    protected String nID;

    public Worker(){
        oldPhone = null;
    }

    public void setFurther(String further) {
        this.further = further;
    }

    public void setPhone(String phone) {
        if(this.phone == null){
            this.oldPhone = phone;
            this.phone = phone;
        }
        else {
            this.phone = phone;
        }
    }

    public void setAssignedOrchards(List<Orchard> assignedOrchards) {
        this.assignedOrchards = assignedOrchards;
    }

    public void copyAssignedOrchards(List<Orchard> assignedOrchards){
        this.assignedOrchards = new Vector<>(assignedOrchards);
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
        if (workerType == WorkerType.WORKER && this.workerType == WorkerType.FOREMAN){
            wasForeman = true;
        }
        this.workerType = workerType;
    }

    public void setOldPhone(String oldPhone) {
        this.oldPhone = oldPhone;
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

    public String getOldPhone() {
        return oldPhone;
    }

    public boolean isWasForeman() {
        return wasForeman;
    }

    @Override
    public String toString() {
        return sName + ", " + fName;
    }

    public void addOrchard (Orchard orchard){
        assignedOrchards.add(orchard);
    }

    public void removeOrchard (String ID){
        for (Orchard current : assignedOrchards){
            if (current.getID().equals(ID)){
                assignedOrchards.remove(current);
                return;
            }
        }
    }
}

