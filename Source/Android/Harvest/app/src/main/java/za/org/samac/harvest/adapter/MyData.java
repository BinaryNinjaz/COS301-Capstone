package za.org.samac.harvest.adapter;

import android.location.Location;

import java.util.ArrayList;

public class MyData {
    public int size = 0;
    public ArrayList<Location> locations;
    public ArrayList<Double> longitude;
    public ArrayList<Double> latitude;
    public ArrayList<Double> date;

    MyData(){
        locations = new ArrayList<>();
        longitude = new ArrayList<>();
        latitude = new ArrayList<>();
        date = new ArrayList<>();
    }

    public void addLocation(Location location) {
        if(location!=null) {
            ++size;
            longitude.add(location.getLongitude());
            latitude.add(location.getLatitude());
            double currentDate = System.currentTimeMillis() / 1000l;
            date.add(currentDate);
        }
    }

    public void removeLocation() {
        if(longitude.size()>0){
            --size;
            longitude.remove(longitude.size()-1);
            latitude.remove(latitude.size()-1);
            date.remove(date.size()-1);
        }
    }
}