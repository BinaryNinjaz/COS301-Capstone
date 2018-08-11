package za.org.samac.harvest.util;

public class DBInfoObject {
    public String ID;
    public boolean checked;

    public DBInfoObject(){
        ID = null;
    }

    @Override
    public String toString() {
        return ID;
    }

    public String getId() {
        return ID;
    }
}
