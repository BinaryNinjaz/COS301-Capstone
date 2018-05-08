package za.org.samac.harvest.util;

import android.location.Location;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
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
    protected Changes changes;

    private FirebaseDatabase database;
    private DatabaseReference userRoot;
    private Farm activeFarm;
    private Orchard activeOrchard;
    private Worker activeWorker;

    private int nextID = 0;

    protected Category category = Category.NOTHING;

    /**
     * Constructor
     */
    public Data(){
        database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRoot = database.getReference(uid + "/");
        farms = new Vector<>();
        orchards = new Vector<>();
        workers = new Vector<>();
        changes = new Changes();
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
                                    String lats = coord.child("lat").getValue(String.class);
                                    String lngs = coord.child("lng").getValue(String.class);
                                    if (!lats.equals("") && !lngs.equals("")) {
                                        coords.pushLocation(Double.parseDouble(lats), Double.parseDouble(lngs));
                                    }
                                }
                                String smeanBagMass = dataSet.child("bagMass").getValue(String.class);
                                Float meanBagMass = null;
                                if (!smeanBagMass.equals("")) {
                                    meanBagMass = Float.parseFloat(smeanBagMass);
                                }
//                                else {
//                                    meanBagMass = 0;
//                                }
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
                                if (dateString.length == 3) {
                                    cal.set(Integer.valueOf(dateString[0]), Integer.valueOf(dateString[1]) - 1, Integer.valueOf(dateString[2]));
                                }
                                String sDimX = dataSet.child("xDim").getValue(String.class);
                                String sDimY = dataSet.child("yDim").getValue(String.class);
                                Float dimX = null, dimY = null;
                                if (!sDimX.equals("")) {
                                    dimX = Float.parseFloat(sDimX);
                                }
                                if (!sDimY.equals("")) {
                                    dimY = Float.parseFloat(sDimX);
                                }
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
        nextID = 0;
        while (changes.unSavedChange()){
            Change currentChange = changes.getNextChange(true);
            DatabaseReference objectRoot = userRoot;
            switch (currentChange.changeType){
                case ADD:
                    switch (currentChange.category){
                        case FARM:
                            objectRoot = userRoot.child("farms");
                            String newKey = objectRoot.push().getKey();
                            Farm newFarm = getFarmFromIDString(currentChange.ID);
                            newFarm.setID(newKey);
                            objectRoot = objectRoot.child(newKey);
                            objectRoot.child("name").setValue(newFarm.name);
                            objectRoot.child("further").setValue(newFarm.further);
                            break;
                        case ORCHARD:
                            break;
                        case WORKER:
                            break;
                    }
                    break;

                case MODIFY:
                    findObject(currentChange.ID, currentChange.category);
                    switch (currentChange.category){
                        case FARM:
                            objectRoot = objectRoot.child("farms").child(currentChange.ID);
                            objectRoot.child("name").setValue(activeFarm.name);
                            objectRoot.child("further").setValue(activeFarm.further);
                            break;
                        case ORCHARD:
                            objectRoot = database.getReference(userRoot.toString() + "/orchards/" + currentChange.ID);
                            break;
                        case WORKER:
                            objectRoot = database.getReference(userRoot.toString() + "/workers/" + currentChange.ID);
                            break;
                    }
                    break;
                case DELETE:
                    findObject(currentChange.ID, currentChange.category);
                    switch (currentChange.category){
                        case FARM:
                            userRoot.child("farms").child(currentChange.ID).setValue(null);
                            break;
                        case ORCHARD:
                            userRoot.child("orchards").child(currentChange.ID).setValue(null);
                            break;
                        case WORKER:
                            userRoot.child("workers").child(currentChange.ID).setValue(null);
                            break;
                    }
                    break;
            }
        }
    }

    public void deleteObject(Category category, String ID){
        changes.Delete(category, ID);
        switch (category){
            case FARM:
                for(Farm current: farms){
                    if (current.getID().equals(ID)){
                        farms.remove(current);
                        return;
                    }
                }
            case ORCHARD:
                for(Orchard current: orchards){
                    if (current.getID().equals(ID)){
                        orchards.remove(current);
                        return;
                    }
                }
            case WORKER:
                for(Worker current: workers){
                    if (current.getID().equals(ID)){
                        workers.remove(current);
                        return;
                    }
                }
        }
    }

    public void setCategory(Category category){
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String[] toNamesAsStringArray(Category cat){
        Category temp = category;
        this.category = cat;
        String[] result = toNamesAsStringArray();
        this.category = temp;
        return result;
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

    @Nullable
    private Farm getFarmFromIDString(String findMe){
        for (Farm current : farms) {
            if (current.ID.equals(findMe)){
                return current;
            }
        }
        return null;
    }

    @Nullable
    private Orchard getOrchardFromIDString(String findMe){
        for (Orchard current: orchards){
            if (current.ID.equals(findMe)){
                return current;
            }
        }
        return null;
    }

    public String getIDFromPosInArray(int pos){
        try {
            switch (category) {
                case ORCHARD:
                    return orchards.elementAt(pos).ID;
                case WORKER:
                    return workers.elementAt(pos).ID;
                case FARM:
                    return farms.elementAt(pos).ID;
            }
            return null;
        }
        catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public void findObject(String ID){
        if(category == Category.FARM){
            for (Farm current : farms){
                if(current.ID.equals(ID)){
                    activeFarm = current;
                    return;
                }
            }
        }
        else if(category == Category.ORCHARD){
            for (Orchard current : orchards){
                if(current.ID.equals(ID)){
                    activeOrchard = current;
                    return;
                }
            }
        }
        else if(category == Category.WORKER){
            for (Worker current : workers){
                if(current.ID.equals(ID)){
                    activeWorker = current;
                    return;
                }
            }
        }
    }

    public void findObject(String ID, Category cat){
        Category temp = this.category;
        category = cat;
        findObject(ID);
        category = temp;
    }

    public Worker getActiveWorker(){
        return activeWorker;
    }

    public Orchard getActiveOrchard() {
        return activeOrchard;
    }

    public Farm getActiveFarm() {
        return activeFarm;
    }

    public Vector<Farm> getFarms() {
        return farms;
    }

    public Vector<Worker> getWorkers() {
        return workers;
    }

    public Vector<Orchard> getOrchards() {
        return orchards;
    }

    public void modifyActiveFarm(Farm activeFarm, boolean overwriteID) {
        if ((!this.activeFarm.ID.equals(activeFarm.ID) && overwriteID) || this.activeFarm.ID.equals(activeFarm.ID)) {
            this.activeFarm = activeFarm;
            changes.Modify(category, activeFarm.ID);
        }
    }

    public void modifyActiveOrchard(Orchard activeOrchard, boolean overwriteID) {
        if ((!this.activeOrchard.ID.equals(activeOrchard.ID) && overwriteID) || this.activeOrchard.ID.equals(activeOrchard.ID)) {
            this.activeOrchard = activeOrchard;
            changes.Modify(category, activeOrchard.ID);
        }
    }

    public void modifyActiveWorker(Worker activeWorker, boolean overwriteID) {
        if ((!this.activeWorker.ID.equals(activeWorker.ID) && overwriteID) || this.activeWorker.ID.equals(activeWorker.ID)) {
            this.activeWorker = activeWorker;
            changes.Modify(category, activeWorker.ID);
        }
    }

    public String getNextIDForAddition(){
        return "N00B - " + nextID++;
    }

    public void addFarm(Farm addMe){
        farms.addElement(addMe);
        changes.Add(Category.FARM, addMe.getID());
    }

    public void addOrchard(Orchard addMe){
        orchards.addElement(addMe);
        changes.Add(Category.ORCHARD, addMe.getID());
    }

    public void addWorker(Worker addMe){
        workers.addElement(addMe);
        changes.Add(Category.WORKER, addMe.getID());
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

class Change{
    protected ChangeType changeType;
    protected Category category;
    protected String ID;
    protected Calendar timestamp;

    public Change(ChangeType changeType, Category category, String ID){
        this.changeType = changeType;
        this.category = category;
        this.ID = ID;
        timestamp = Calendar.getInstance();
    }
}

class Changes{
    //Turn and face the strange
    Stack<Change> localChanges;

    public Changes(){
        localChanges = new Stack<>();
    }

    public void Delete(Category category, String ID){
        Change temp = new Change(ChangeType.DELETE, category, ID);
        localChanges.addElement(temp);
    }

    public void Modify(Category category, String ID){
        Change temp = new Change(ChangeType.MODIFY, category, ID);
        localChanges.addElement(temp);
    }

    public void Add(Category category, String ID){
        Change temp = new Change(ChangeType.ADD, category, ID);
        localChanges.addElement(temp);
    }

    public Change getNextChange(boolean pop){
        if (pop){
            return localChanges.pop();
        }
        return localChanges.peek();
    }

    public boolean unSavedChange(){
        return !localChanges.empty();
    }
}