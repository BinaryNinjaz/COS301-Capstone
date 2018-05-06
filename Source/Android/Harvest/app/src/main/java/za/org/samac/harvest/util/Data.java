package za.org.samac.harvest.util;

import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Stack;
import java.util.Vector;

/**
 * This monster class is used to store and manipulate almost, if not all, information in the database that belongs to the logged in farmer.
 */

public class Data {
    /**
     * The process here to do anything is:
     *  Make the change to the relevant vector
     *  Save the change in the changes vector
     *  Push when the user navigates away from where edits are made, or when a network connection becomes available.
     *   ...To save mobile data, and ensure that, functionality is preserved in rural areas.
     *    that last point means that any changes need to be saved to the device, along with a timestamp, so that when connection
     *    is available, the push can resolve any conflicts.
     */
    protected Vector<Farm> farms;
    protected Vector<Orchard> orchards;
    protected Vector<Worker> workers;
    protected Vector<Changes> changes;

    private FirebaseDatabase database;
    private DatabaseReference userRoot;

    protected Category category = Category.NOTHING;

    /**
     * Constructor
     */
    public Data(){
        database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRoot = database.getReference(uid + "/");
        farms = new Vector<Farm>();
        orchards = new Vector<Orchard>();
        workers = new Vector<Worker>();
        pull();
    }

    /**
     * Replace all local information from Firebase
     */
    public void pull(){
        /*
         * This may, or may not be real time, for now it's not, because my data
         * D:
         * Swipe to refresh will be the way to go, methinks.
         * TODO: Add Swipe to refresh in list
         */

        userRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                 * Structure is:
                 *
                 *  Total Root of the database
                 *   User Root (uid)
                 *    Set of data (farms, orchards, etc)
                 *     Data sets (ID of data set)
                 *      Data pair (key : data (name : Joe))
                 */
                //Iterate through every set of data that the user has (farms, orchards, etc)
                for (DataSnapshot setOData : dataSnapshot.getChildren()){
                    switch (setOData.getKey()) {
                        //If the data set is for farms
                        case "farms":
                            //Iterate through every data set
                            for (DataSnapshot dataSet : setOData.getChildren()) {
                                String name = dataSet.child("name").getValue(String.class);
                                String further = dataSet.child("further").getValue(String.class);
                                String ID = dataSet.getKey();
                                Farm temp = new Farm(name, further, ID);
                                farms.add(temp);
                            }
                            break;

                        //If the data set is for orchards
                        case "orchards":
                            //Iterate through every data set
                            for (DataSnapshot dataSet : setOData.getChildren()) {
                                String name = dataSet.child("name").getValue(String.class);
                                String crop = dataSet.child("crop").getValue(String.class);
                                //Iterate through coordinate sets
                                Coordinates coords = new Coordinates();
                                for (DataSnapshot coord : setOData.child("coords").getChildren()){
                                    // Iterate through
                                    Location tempLoc = new Location("");
                                    coords.pushLocation(coord.child("lat").getValue(double.class), coord.child("lng").getValue(double.class));
                                }
                                String smeanBagMass = dataSet.child("bagMass").getValue(String.class);
                                float meanBagMass = Float.parseFloat(smeanBagMass);
                                Calendar cal = new Calendar() {
                                    @Override
                                    protected void computeTime() {

                                    }

                                    @Override
                                    protected void computeFields() {

                                    }

                                    @Override
                                    public void add(int field, int amount) {

                                    }

                                    @Override
                                    public void roll(int field, boolean up) {

                                    }

                                    @Override
                                    public int getMinimum(int field) {
                                        return 0;
                                    }

                                    @Override
                                    public int getMaximum(int field) {
                                        return 0;
                                    }

                                    @Override
                                    public int getGreatestMinimum(int field) {
                                        return 0;
                                    }

                                    @Override
                                    public int getLeastMaximum(int field) {
                                        return 0;
                                    }
                                };
                                //Get and manipulate the date.
                                String dateString[] = dataSet.child("date").getValue(String.class).split("-");
                                cal.set(Integer.valueOf(dateString[0]), Integer.valueOf(dateString[1]) - 1, Integer.valueOf(dateString[2]));
                                String sDimX = dataSet.child("xDim").getValue(String.class);
                                String sDimY = dataSet.child("yDim").getValue(String.class);
                                float dimX = Float.parseFloat(sDimX);
                                float dimY = Float.parseFloat(sDimX);
                                String dimUnit = dataSet.child("unit").getValue(String.class);
                                String further = dataSet.child("further").getValue(String.class);
                                String assignedFarmString = dataSet.child("farm").getValue(String.class);
                                Farm assignedFarm = getFarmFromIDString(assignedFarmString);
                                orchards.addElement(new Orchard(name, crop, coords, meanBagMass, cal, dimX, dimY, dimUnit, further, assignedFarm, dataSet.getKey()));
                            }
                            break;

                        //If the data set is for workers
                        case "workers":
                            //Iterate through every data set
                            for (DataSnapshot dataSet : setOData.getChildren()) {
                                String fName = dataSet.child("name").getValue(String.class);
                                String sName = dataSet.child("surname").getValue(String.class);
                                Orchard assignedOrchard = getOrchardFromIDString(dataSet.child("orchard").getValue(String.class));
                                String sType = dataSet.child("type").getValue(String.class);
                                WorkerType type = WorkerType.WORKER;
                                if (sType.equals("Foreman")){
                                    type = WorkerType.FOREMAN;
                                }
                                String further = dataSet.child("info").getValue(String.class);
                                String email = dataSet.child("email").getValue(String.class);
                                String ID = dataSet.getKey();
                                workers.addElement(new Worker(fName, sName, assignedOrchard, type, further, email, ID));
                            }
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Apply all changes to Firebase
     */
    public void push(){

    }

    //TODO: Below needs to be a whole bunch of gets and sets

    public void setCategory(Category category){
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String[] toNamesAsStringArray(){
        String[] result;
        if (category == Category.FARM){
            result = new String[farms.size()];
            for (int i = 0; i < farms.size(); i++) {
                result[i] = farms.elementAt(i).name;
            }
            return result;
        }
        else if(category == Category.ORCHARD){
            result = new String[orchards.size()];
            for (int i = 0; i < orchards.size(); i++) {
                result[i] = orchards.elementAt(i).name;
            }
            return result;
        }
        else if(category == Category.WORKER){
            result = new String[workers.size()];
            for (int i = 0; i < workers.size(); i++) {
                result[i] = workers.elementAt(i).fName + " " + workers.elementAt(i).sName;
            }
            return result;
        }
        return null;
    }

    public Farm getFarmFromIDString(String findMe){
        for (Farm current : farms) {
            if (current.ID.equals(findMe)){
                return current;
            }
        }
        return null;
    }

    public Orchard getOrchardFromIDString(String findMe){
        for (Orchard current: orchards){
            if (current.ID.equals(findMe)){
                return current;
            }
        }
        return null;
    }
}

/**
 * Below are all of the classes that will store and manipulate all of the individual information.
 */

class Farm{
    protected String further;
    protected String name;
    protected String ID;

    public Farm(String name, String further, String  ID){
        this.further = further;
        this.name = name;
        this.ID = ID;
    }
}

class Orchard{
    protected String name;
    protected String crop;
    protected Coordinates coordinates;
    protected float meanBagMass;
    protected Calendar datePlanted;
    protected float dimX, dimY;
    protected String dimUnit;
    protected String further;
    protected Farm assignedFarm;
    protected String ID;

    public Orchard(String name, String crop, Coordinates coordinates, float meanBagMass, Calendar datePlanted, float dimX, float dimY, String dimUnit, String further, Farm assignedFarm, String ID){
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
}

enum WorkerType{
    NOTHING,
    WORKER,
    FOREMAN,
    FARMER
}

class Worker{
    protected String fName, sName;
    protected Orchard assignedOrchard;
    protected WorkerType workerType;
    protected String further;
    protected String email;
    protected String ID;

    public Worker(String fName, String sName, Orchard assignedOrchard, WorkerType workerType, String further, String email, String ID){
        this.fName = fName;
        this.sName = sName;
        this.assignedOrchard = assignedOrchard;
        this.workerType = workerType;
        this.further = further;
        this.email = email;
        this.ID = ID;
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

/**
 * Below is a method of keeping track of all changes made, so that pushing the database can be quick and easy.
 */

enum ChangeType{
    NOTHING,
    DELETE,
    ADD,
    MODIFY
}

enum DataType{
    NOTHING,
    FARM,
    ORCHARD,
    WORKER
}

class Change{
    protected ChangeType changeType;
    protected DataType dataType;
    protected String ID;
    protected Calendar timestamp;

    public Change(ChangeType changeType, DataType dataType, String ID){
        this.changeType = changeType;
        this.dataType = dataType;
        this.ID = ID;
        timestamp = Calendar.getInstance();
    }
}

class Changes{
    //Turn and face the strange
    Stack<Change> changes;

    public Changes(){

    }

    public void Delete(DataType dataType, String ID){
        Change temp = new Change(ChangeType.DELETE, dataType, ID);
        changes.addElement(temp);
    }

    public void Modify(DataType dataType, String ID){
        Change temp = new Change(ChangeType.MODIFY, dataType, ID);
        changes.addElement(temp);
    }

    public void Add(DataType dataType, String ID){
        Change temp = new Change(ChangeType.ADD, dataType, ID);
        changes.addElement(temp);
    }
}
