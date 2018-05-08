package za.org.samac.harvest.util;

/**
 * Below are all of the classes that will store and manipulate all of the individual information.
 */

public class Farm{
    protected String further;
    protected String name;
    protected String ID;

    public Farm(String name, String further, String  ID){
        this.further = further;
        this.name = name;
        this.ID = ID;
    }

    public String getFurther() {
        return further;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setFurther(String further) {
        this.further = further;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }
}
