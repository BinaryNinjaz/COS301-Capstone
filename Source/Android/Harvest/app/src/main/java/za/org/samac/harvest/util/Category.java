package za.org.samac.harvest.util;

public enum Category{
    FARM,
    ORCHARD,
    WORKER,
    NOTHING,
    NAV,
    FOREMAN;

    @Override
    public String toString() {
        switch (this){
            case ORCHARD:
                return "orchard";
            case FARM:
                return "farm";
            case FOREMAN:
                return "foreman";
            case WORKER:
                return "worker";
            case NOTHING:
                return null;
            case NAV:
                return "nav";
        }
        return null;
    }
}
