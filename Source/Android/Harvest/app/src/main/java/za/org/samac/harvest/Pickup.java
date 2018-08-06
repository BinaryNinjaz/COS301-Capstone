package za.org.samac.harvest;

import android.location.Location;

public class Pickup {
    public String workerName;
    public Double pickedUpOn;
    public Double lat;
    public Double lng;

    public Pickup(String workerName, Location location, Double adate) {
        if(location!=null) {
            this.workerName = workerName;
            lng = location.getLongitude();
            lat = location.getLatitude();
            pickedUpOn = adate;
        }
    }
}
