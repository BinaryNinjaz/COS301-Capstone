package za.org.samac.harvest.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Orchard{
    protected String name;
    protected String crop;
    protected List<LatLng> coordinates;
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
        coordinates = new Vector<LatLng>();
        cultivars = new Vector<>();
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

    public void setCoordinates(List<LatLng> coordinates) {
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

    public List<LatLng> getCoordinates() {
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

    public String getName() {
        return name;
    }

    public Farm getAssignedFarm() {
        return assignedFarm;
    }

    public String getID() {
        return ID;
    }

    private String containsSubText(List<String> list, String text) {
        text = text.toLowerCase();
        for (String item : list) {
            if (item.toLowerCase().compareTo(text) == 0) {
                return item;
            }
        }
        return null;
    }

    public ArrayList<SearchedItem> search(String text) {
        ArrayList<SearchedItem> result = new ArrayList<>();

        text = text.toLowerCase();

        for (SearchedItem item : assignedFarm.search(text)) {
            result.add(new SearchedItem("Farm " + item.property, item.reason));
        }

        if ((getName()).toLowerCase().contains(text)) {
            result.add(new SearchedItem("Name", getName()));
        }

        if (getCrop().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Crop", getCrop()));
        }

        String culti = containsSubText(getCultivars(), text);
        if (culti != null) {
            result.add(new SearchedItem("Cultivar", culti));
        }

        if (getIrrigation().toLowerCase().contains(text)) {
            result.add(new SearchedItem("Irrigation Type", getIrrigation()));
        }

        return result;
    }
}

