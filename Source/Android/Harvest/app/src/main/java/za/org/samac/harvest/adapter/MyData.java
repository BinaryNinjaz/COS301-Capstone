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

    public void addLocation(Double lat, Double lng) {
        if(lat != null && lng != null) {
            ++size;
            longitude.add(lat);
            latitude.add(lng);
            double currentDate = System.currentTimeMillis() / 1000l;
            date.add(currentDate);
        }
    }

    public void addLocation(Double lat, Double lng, Double adate) {
        if(lat != null && lng != null) {
            ++size;
            longitude.add(lat);
            latitude.add(lng);
            date.add(adate);
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