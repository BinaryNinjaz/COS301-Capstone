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
import java.util.Map;
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

    /**
     * Constructor
     */
    public Data(){
        database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRoot = database.getReference(uid + "/");
        pull();
    }

    /**
     * Replace all local information from Firebase
     */
    public void pull(){
        /*
         * This may, or may not be real time, for now it's not, because my data
         * D:
         * Would be nice to have a setting for it though. Looking at you Kevin, who needs to make the settings page.
         * TODO: Add setting to configure if database is real time or not.
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
                                farms.addElement(new Farm(dataSet.child("name").getValue(String.class), dataSet.child("further").getValue(String.class), dataSet.getKey()));
                            }
                            break;

                        //If the data set is for orchards
                        case "orchards":
                            //Iterate through every data set
                            for (DataSnapshot dataSet : setOData.getChildren()) {

                            }
                            break;

                        //If the data set is for workers
                        case "workers":
                            //Iterate through every data set
                            for (DataSnapshot dataSet : setOData.getChildren()) {

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
    protected String  ID;

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
