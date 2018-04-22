package za.org.samac.harvest.domain;

public class Worker {

    private String name;
    private Integer value;
    private Object ID;

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

    public Object getID() {
        return ID;
    }

    public void setID(Object value) {
        this.ID = ID;
    }
}
