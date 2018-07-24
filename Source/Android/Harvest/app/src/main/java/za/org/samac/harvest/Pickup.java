package za.org.samac.harvest;

import android.location.Location;

public class Pickup {
    public Double pickedUpOn;
    public Double lat;
    public Double lng;

    public void addLocation(Location location, Double adate) {
        if(location!=null) {
            lng = (location.getLongitude());
            lat = (location.getLatitude());
            pickedUpOn = (adate);
        }
    }
}
