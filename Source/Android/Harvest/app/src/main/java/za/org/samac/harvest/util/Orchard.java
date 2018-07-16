package za.org.samac.harvest.util;

import android.location.Location;

import java.util.Calendar;
import java.util.Vector;

public class Orchard{
    protected String name;
    protected String crop;
    protected Coordinates coordinates;
    protected Float meanBagMass;
    protected Calendar datePlanted;
    protected Float dimX, dimY;
    protected String dimUnit;
    protected String further;
    protected Farm assignedFarm;
    protected String ID;

    public Orchard(String name, String crop, Coordinates coordinates, Float meanBagMass, Calendar datePlanted, Float dimX, Float dimY, String dimUnit, String further, Farm assignedFarm, String ID){
        this.name = name;
        this.crop = crop;
        this.meanBagMass = meanBagMass;
        this.dimX = dimX;
        this.dimY = dimY;
        this.dimUnit = dimUnit;
        this.further = further;
        this.assignedFarm = assignedFarm;
        this.coordinates = coordinates;
        this.datePlanted = datePlanted;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public Farm getAssignedFarm() {
        return assignedFarm;
    }

    public String getID() {
        return ID;
    }
}

class Coordinates{
    private Vector<Location> coordinates;

    public Coordinates(){
        coordinates = new Vector<Location>();
    }

    public void pushLocation(Location location){
        coordinates.addElement(location);
    }

    public void pushLocation(double lat, double lng){
        Location temp = new Location("");
        temp.setLatitude(lat);
        temp.setLongitude(lng);
        temp.setAccuracy(0);
        coordinates.addElement(temp);
    }

    public int getSize(){
        return coordinates.size();
    }

    public Location getCoordinate(int at){
        return coordinates.elementAt(at);
    }
}
