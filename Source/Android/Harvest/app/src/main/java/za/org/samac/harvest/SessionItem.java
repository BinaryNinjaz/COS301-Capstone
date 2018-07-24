package za.org.samac.harvest;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import za.org.samac.harvest.adapter.MyData;

public class SessionItem {
    public String key;
    public Date startDate;
    public Date endDate;
    public String foreman;
    public HashMap<String, ArrayList<Pickup>> collections = new HashMap<>();
    public ArrayList<Location> track = new ArrayList<>();

    public void addCollection(String workerName, Location location, Double date){
        if(collections.containsKey(workerName)) {
            collections.get(workerName).add(new Pickup(location, date));
        }else {
            collections.put(workerName, new ArrayList<Pickup>());
            collections.get(workerName).add(new Pickup(location, date));
        }
    }

    public void addTrack(Location loc) {
        track.add(loc);
    }
}
