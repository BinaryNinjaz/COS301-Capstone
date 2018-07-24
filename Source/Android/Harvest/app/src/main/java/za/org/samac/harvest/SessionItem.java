package za.org.samac.harvest;

import android.location.Location;

import java.util.Date;
import java.util.HashMap;

import za.org.samac.harvest.adapter.MyData;

public class SessionItem {
    public String key;
    public Date startDate;
    public Date endDate;
    public String foreman;
    public HashMap<String, Pickup> collections = new HashMap<>();

    public void addCollection(String workerName, Location location, Double date){
        if(collections.containsKey(workerName)) {
            Pickup data = collections.get(workerName);
            data.addLocation(location, date);
            collections.put(workerName, data);
        }else {
            Pickup data = new Pickup();
            data.addLocation(location, date);
            collections.put(workerName, data);
        }
    }
}
