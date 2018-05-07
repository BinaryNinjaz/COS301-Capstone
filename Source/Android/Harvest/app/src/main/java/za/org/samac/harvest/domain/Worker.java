package za.org.samac.harvest.domain;

import java.util.ArrayList;

public class Worker {

    private String name;
    private Integer value;
    private String ID;
    private ArrayList<String> assignedOrchards;//the orchards the worker is assigned to may or may not be more than one

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
