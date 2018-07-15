package za.org.samac.harvest;

import java.util.Date;
import java.util.HashMap;

public class SessionItem {
    public static class Selection {
        public String key;
        public Date startDate;
        public String foreman;
    }

    public String key;
    public Date startDate;
    public Date endDate;
    public String foreman;
    public HashMap<String, Pickup> collections;
}
