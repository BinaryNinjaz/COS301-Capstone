package za.org.samac.harvest.util;

import za.org.samac.harvest.SessionItem;

public class SearchedItem {
    public static class Session {
        public SessionItem session;
        public String reason;

        public Session(SessionItem session, String reason) {
            this.session = session;
            this.reason = reason;
        }
    }

    public String property;
    public String reason;

    public SearchedItem(String prop, String reason) {
        this.property = prop;
        this.reason = reason;
    }
}
