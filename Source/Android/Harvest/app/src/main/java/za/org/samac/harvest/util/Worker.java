package za.org.samac.harvest.util;

public class Worker{
    protected String fName, sName;
    protected Orchard assignedOrchard;
    protected WorkerType workerType;
    protected String further;
    protected String email;
    protected String ID;

    public Worker(String fName, String sName, Orchard assignedOrchard, WorkerType workerType, String further, String email, String ID){
        this.fName = fName;
        this.sName = sName;
        this.assignedOrchard = assignedOrchard;
        this.workerType = workerType;
        this.further = further;
        this.email = email;
        this.ID = ID;
    }

    public String getfName() {
        return fName;
    }

    public String getsName() {
        return sName;
    }

    public String getID() {
        return ID;
    }

    public Orchard getAssignedOrchard() {
        return assignedOrchard;
    }
}

enum WorkerType{
    NOTHING,
    WORKER,
    FOREMAN,
    FARMER
}
