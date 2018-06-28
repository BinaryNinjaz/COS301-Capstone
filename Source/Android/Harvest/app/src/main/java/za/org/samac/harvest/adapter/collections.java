package za.org.samac.harvest.adapter;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.adapter.MyData;

public class collections {

    private Map<String, MyData> individualCollections;//map of each individual
    private ArrayList<Location> track;
    private String foremanEmail = "";
    private double start_date, end_date;

    collections(String email) {
        individualCollections = new HashMap<>();
        foremanEmail = email;
        start_date = System.currentTimeMillis() / 1000.0;
        track = new ArrayList<>();
    }

    public void addCollection(String workerName, Location location){
        if(individualCollections.containsKey(workerName)) {
            MyData data = individualCollections.get(workerName);
            data.addLocation(location);
            individualCollections.put(workerName, data);
        }else {
            MyData data = new MyData();
            data.addLocation(location);
            individualCollections.put(workerName, data);
        }
    }

    public void addCollection(String workerName, Location location, Double date){
        if(individualCollections.containsKey(workerName)) {
            MyData data = individualCollections.get(workerName);
            data.addLocation(location, date);
            individualCollections.put(workerName, data);
        }else {
            MyData data = new MyData();
            data.addLocation(location, date);
            individualCollections.put(workerName, data);
        }
    }

    public void removeCollection(String workerName){
        MyData data = individualCollections.get(workerName);
        if(data!=null) {
            data.removeLocation();
            if (data.size == 0) {
                individualCollections.remove(workerName);
            } else {
                individualCollections.put(workerName, data);
            }
        }
    }

    public void addTrack(Location location) {
        track.add(location);
    }

    public void sessionEnd() {
        end_date = System.currentTimeMillis() / 1000.0;
    }

    public Map<String, MyData> getIndividualCollections() {
        return individualCollections;
    }

    public String getForemanEmail() {
        return foremanEmail;
    }

    public double getStart_date() {
        return start_date;
    }

    public double getEnd_date() {
        return end_date;
    }
}