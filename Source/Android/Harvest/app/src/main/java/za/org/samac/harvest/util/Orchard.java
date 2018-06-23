package za.org.samac.harvest.util;

import android.location.Location;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class Orchard{
    protected String name;
    protected String crop;
    protected Coordinates coordinates;
    protected Float meanBagMass;
    protected String irrigation;
    protected Calendar datePlanted;
    protected Farm assignedFarm;
    protected Float row;
    protected Float tree;
    protected Vector<String> cultivars;
    protected String further;
    protected String ID;

    public Orchard(){
        coordinates = new Coordinates();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setFurther(String further) {
        this.further = further;
    }

    public void setAssignedFarm(Farm assignedFarm) {
        this.assignedFarm = assignedFarm;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public void setCultivars(Vector<String> cultivars) {
        this.cultivars = cultivars;
    }

    public void setDatePlanted(Calendar datePlanted) {
        this.datePlanted = datePlanted;
    }

    public void setIrrigation(String irrigation) {
        this.irrigation = irrigation;
    }

    public void setMeanBagMass(Float meanBagMass) {
        this.meanBagMass = meanBagMass;
    }

    public void setRow(Float row) {
        this.row = row;
    }

    public void setTree(Float tree) {
        this.tree = tree;
    }

    public String getFurther() {
        return further;
    }

    public Calendar getDatePlanted() {
        return datePlanted;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Float getRow() {
        return row;
    }

    public Float getTree() {
        return tree;
    }

    public Float getMeanBagMass() {
        return meanBagMass;
    }

    public String getCrop() {
        return crop;
    }

    public String getIrrigation() {
        return irrigation;
    }

    public Vector<String> getCultivars() {
        return cultivars;
    }

    public void addCultivar(String addMe){
        cultivars.addElement(addMe);
    }

    public void addCoordinates(Location loc){
        coordinates.pushLocation(loc);
    }

    public void addCoordinates(double lat, double lng){
        coordinates.pushLocation(lat, lng);
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
