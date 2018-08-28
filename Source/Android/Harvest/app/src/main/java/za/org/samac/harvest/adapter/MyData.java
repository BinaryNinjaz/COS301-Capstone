package za.org.samac.harvest.adapter;

import android.location.Location;

import java.util.ArrayList;

public class MyData {
    public int size = 0;
    public ArrayList<Location> locations;
    public ArrayList<Double> longitude;
    public ArrayList<Double> latitude;
    public ArrayList<Double> date;
    public ArrayList<String> selectedOrchards;

    MyData(){
        locations = new ArrayList<>();
        longitude = new ArrayList<>();
        latitude = new ArrayList<>();
        date = new ArrayList<>();
        selectedOrchards = new ArrayList<>();
    }

    public void addLocation(Location location, String selectedOrchard) {
        if(location!=null) {
            ++size;
            longitude.add(location.getLongitude());
            latitude.add(location.getLatitude());
            double currentDate = System.currentTimeMillis() / 1000l;
            date.add(currentDate);
            selectedOrchards.add(selectedOrchard);
        }
    }

    public void addLocation(Location location, String selectedOrchard, Double adate) {
        if(location!=null) {
            ++size;
            longitude.add(location.getLongitude());
            latitude.add(location.getLatitude());
            date.add(adate);
            selectedOrchards.add(selectedOrchard);
        }
    }

    public void removeLocation() {
        if(longitude.size()>0){
            --size;
            longitude.remove(longitude.size()-1);
            latitude.remove(latitude.size()-1);
            date.remove(date.size()-1);
            selectedOrchards.remove(selectedOrchards.size()-1);
        }
    }
}