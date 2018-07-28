package za.org.samac.harvest.util;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewDebug;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import za.org.samac.harvest.Analytics;
import za.org.samac.harvest.InfoListFragment;
import za.org.samac.harvest.InformationActivity;

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

    private static Vector<Farm> farms;
    private static Vector<Orchard> orchards;
    private static Vector<Worker> workers;
    private Changes changes;

    private static FirebaseDatabase database;
    private static DatabaseReference userRoot;

    private Farm activeFarm;
    private Orchard activeOrchard;
    private Worker activeWorker;

    private int nextID = 0;

    protected Category category = Category.NOTHING;

    private static boolean pulling = false;
    private boolean pFarms = false;
    private boolean pOrchards = false;
    private boolean pWorkers = false;

    private static boolean needsPull = true;


    private Activity act = null;

    /**
     * Constructor
     */
    public Data(){
        database = FirebaseDatabase.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRoot = database.getReference(uid + "/");
        changes = new Changes();
        if (needsPull){
            Log.i("Data", "Pulling for the first time.");
            pull();
        }
    }

    public static boolean isPulling() {
        return pulling;
    }

    /**
     * Replace all local information from Firebase, TODO: while preserving local changes.
     */
    public void pull(){

        needsPull = false;

        tellMeWhenDonePulling(Category.NOTHING);

        farms = new Vector<>();
        orchards = new Vector<>();
        workers = new Vector<>();
        changes = new Changes();

        DatabaseReference curRef = userRoot.child("farms");
        curRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Iterate through every data set
                for (DataSnapshot dataSet : dataSnapshot.getChildren()) {
                    Farm temp = new Farm();
                    temp.setName(dataSet.child("name").getValue(String.class));
                    temp.setCompany(dataSet.child("companyName").getValue(String.class));
                    temp.setEmail(dataSet.child("email").getValue(String.class));
                    temp.setPhone(dataSet.child("contactNumber").getValue(String.class));
                    temp.setProvince(dataSet.child("province").getValue(String.class));
                    temp.setTown(dataSet.child("town").getValue(String.class));
                    temp.setFurther(dataSet.child("further").getValue(String.class));
                    temp.setID(dataSet.getKey());
                    farms.add(temp);
                }
                tellMeWhenDonePulling(Category.FARM);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        curRef = userRoot.child("orchards");
        curRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Iterate through every data set
                for (DataSnapshot dataSet : dataSnapshot.getChildren()) {
                    Orchard temp = new Orchard();
                    temp.setName(dataSet.child("name").getValue(String.class));
                    temp.setCrop(dataSet.child("crop").getValue(String.class));

                    //Iterate through coordinate sets
                    List<LatLng> coords = new Vector<>();
                    for (DataSnapshot coord : dataSet.child("coords").getChildren()){
                        // Iterate through
                        Double lats = coord.child("lat").getValue(Double.class);
                        Double lngs = coord.child("lng").getValue(Double.class);
                        coords.add(new LatLng(lats, lngs));
                    }
                    temp.setCoordinates(coords);

                    try {
                        String smeanBagMass = dataSet.child("bagMass").getValue(String.class);
                        Float meanBagMass = null;
                        if (smeanBagMass != null) {
                            if (!smeanBagMass.equals("")) {
                                meanBagMass = Float.parseFloat(smeanBagMass);
                            }
                        }
//                                else {
//                                    meanBagMass = 0;
//                                }
                        temp.setMeanBagMass(meanBagMass);
                    }
                    catch (com.google.firebase.database.DatabaseException e){
                        Long meanBagBass = dataSet.child("bagMass").getValue(Long.class);
                        if (meanBagBass != null) {
                            Float beanBagBass = meanBagBass.floatValue();
                            temp.setMeanBagMass(beanBagBass);
                        }
                    }

                    temp.setIrrigation(dataSet.child("irrigation").getValue(String.class));

                    Long tempL = dataSet.child("date").getValue(Long.class);
                    Date date;
                    Calendar c;
                    if (tempL != null){
                        c = Calendar.getInstance();
                        date = new Date(tempL);
                        c.setTime(date);
                        temp.setDatePlanted(c);
                    }

                    Farm assignedFarm = new Farm();
                    assignedFarm.setID(dataSet.child("farm").getValue(String.class));
                    temp.setAssignedFarm(assignedFarm);

                    Float row = null, tree = null;
                    try {
                        String sRow = dataSet.child("rowSpacing").getValue(String.class);
                        if (sRow != null) {
                            if (!sRow.equals("")) {
                                row = Float.parseFloat(sRow);
                            }
                        }
                    }
                    catch (com.google.firebase.database.DatabaseException e){
                        Long t = dataSet.child("rowSpacing").getValue(Long.class);
                        if (t != null){
                            row = t.floatValue();
                        }
                    }
                    try{
                        String sTree = dataSet.child("treeSpacing").getValue(String.class);
                        if (sTree != null) {
                            if(!sTree.equals("")){
                                tree = Float.parseFloat(sTree);
                            }
                        }
                    }
                    catch (com.google.firebase.database.DatabaseException e){
                        Long t = dataSet.child("rowSpacing").getValue(Long.class);
                        if (t != null){
                            tree = t.floatValue();
                        }
                    }

                    temp.setRow(row);
                    temp.setTree(tree);

                    //Cultivars
                    Vector<String> cultivars;
                    for (DataSnapshot cultivar : dataSet.child("cultivars").getChildren()){
                        temp.addCultivar(cultivar.getValue(String.class));
                    }

                    temp.setFurther(dataSet.child("further").getValue(String.class));

                    temp.setID(dataSet.getKey());

                    orchards.addElement(temp);
                }
                tellMeWhenDonePulling(Category.ORCHARD);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        curRef = userRoot.child("workers");
        curRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Iterate through every data set
                for (DataSnapshot dataSet : dataSnapshot.getChildren()) {
                    Worker temp = new Worker();
                    temp.setfID(dataSet.getKey());
                    temp.setfName(dataSet.child("name").getValue(String.class));
                    temp.setsName(dataSet.child("surname").getValue(String.class));

                    //Orchards
                    List<Orchard> newOrhards = new Vector<>();
                    for (DataSnapshot orchard : dataSet.child("orchards").getChildren()){
                        Orchard newOrchard = getOrchardFromIDString(orchard.getValue(String.class));
                        if (newOrchard != null) {
                            newOrhards.add(newOrchard);
                        }
                        else {
                            //orchard does not exist, mark the worker for change, and it'll update.
                            changes.Modify(Category.WORKER, temp.getfID());
                        }
                    }
                    temp.setAssignedOrchards(newOrhards);

                    //Type
                    String sType = dataSet.child("type").getValue(String.class);
                    WorkerType type = WorkerType.WORKER;
                    assert sType != null;
                    if (sType.equals("Foreman")){
                        type = WorkerType.FOREMAN;
                    }
                    temp.setWorkerType(type);

                    temp.setnID(dataSet.child("idNumber").getValue(String.class));
                    temp.setFurther(dataSet.child("info").getValue(String.class));
                    temp.setPhone(dataSet.child("phoneNumber").getValue(String.class));

                    workers.addElement(temp);
                }
                tellMeWhenDonePulling(Category.WORKER);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void notifyMe(Activity act){
        this.act = act;
    }

    private void tellMeWhenDonePulling(Category cat){
        switch (cat){
            case FARM:
                pFarms = false;
                break;
            case ORCHARD:
                pOrchards = false;
                break;
            case WORKER:
                pWorkers = false;
                break;
            case NOTHING:
                pFarms = true;
                pWorkers = true;
                pOrchards = true;
                pulling = true;
                break;
        }
        if (!(pFarms | pOrchards | pWorkers)){
            pulling = false;

            //Fix orchard farms
            for (Orchard orchard : orchards){
                orchard.setAssignedFarm(getFarmFromIDString(orchard.getAssignedFarm().getID()));
            }

            if (act != null){
                if (act.getClass() == InformationActivity.class){
                    InformationActivity temp = (InformationActivity) act;
                    temp.tellAllPullDone();
                }
                else if (act.getClass() == Analytics.class){
                    Analytics temp = (Analytics) act;
                    temp.pullDone();
                }
            }
        }

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
                            objectRoot.child("companyName").setValue(newFarm.company);
                            objectRoot.child("email").setValue(newFarm.email);
                            objectRoot.child("contactNumber").setValue(newFarm.phone);
                            objectRoot.child("province").setValue(newFarm.province);
                            objectRoot.child("town").setValue(newFarm.town);
                            objectRoot.child("further").setValue(newFarm.further);
                            break;
                        case ORCHARD:
                            objectRoot = userRoot.child("orchards");
                            String newKey1 = objectRoot.push().getKey();
                            Orchard newOrchard = getOrchardFromIDString(currentChange.ID);
                            newOrchard.setID(newKey1);
                            objectRoot = objectRoot.child(newKey1);
                            objectRoot.child("name").setValue(newOrchard.name);
                            objectRoot.child("crop").setValue(newOrchard.crop);
                            DatabaseReference coordsRoot = objectRoot.child("coords");
                            for(int i = 0; i < newOrchard.coordinates.size(); i++){
//                                Location loc = newOrchard.coordinates.getCoordinate(i);
                                LatLng loc = newOrchard.coordinates.get(i);
                                coordsRoot.child(Integer.toString(i)).child("lat").setValue(loc.latitude);
                                coordsRoot.child(Integer.toString(i)).child("lng").setValue(loc.longitude);
                            }
                            if (newOrchard.meanBagMass != null){
//                                objectRoot.child("bagMass").setValue(Float.toString(newOrchard.meanBagMass));
                                objectRoot.child("bagMass").setValue(newOrchard.meanBagMass.longValue());
                            }
                            objectRoot.child("irrigation").setValue(newOrchard.irrigation);
                            if (newOrchard.datePlanted != null) {
                                objectRoot.child("date").setValue(newOrchard.datePlanted.getTime().getTime());
                            }
                            if (newOrchard.getAssignedFarm() != null) {
                                objectRoot.child("farm").setValue(newOrchard.assignedFarm.ID);
                            }
                            if (newOrchard.row != null) {
                                objectRoot.child("rowSpacing").setValue(newOrchard.row);
                            }
                            if (newOrchard.tree != null) {
                                objectRoot.child("treeSpacing").setValue(newOrchard.tree);
                            }
                            coordsRoot = objectRoot.child("cultivars");
                            if (newOrchard.getCultivars() != null){
                                for (int i = 0; i < newOrchard.cultivars.size(); i++){
                                    coordsRoot.child(Integer.toString(i)).setValue(newOrchard.cultivars.elementAt(i));
                                }
                            }
                            objectRoot.child("further").setValue(newOrchard.further);
                            break;
                        case WORKER:
                            objectRoot = userRoot.child("workers");
                            String newKey2 = objectRoot.push().getKey();
                            Worker newWorker = getWorkerFromIDString(currentChange.ID);
                            newWorker.setfID(newKey2);
                            objectRoot = objectRoot.child(newKey2);
                            
                            //Begin
                            objectRoot.child("idNumber").setValue(newWorker.nID);
                            objectRoot.child("info").setValue(newWorker.further);
                            objectRoot.child("name").setValue(newWorker.fName);
                            
                            //Orchards
                            DatabaseReference thingRoot = objectRoot.child("orchards");
                            if (newWorker.getAssignedOrchards() != null) {
                                for (int i = 0; i < newWorker.assignedOrchards.size(); i++) {
                                    thingRoot.child(Integer.toString(i)).setValue(newWorker.assignedOrchards.get(i).ID);
                                }
                            }
                            
                            objectRoot.child("phoneNumber").setValue(newWorker.phone);
                            objectRoot.child("surname").setValue(newWorker.sName);
                            
                            //Type
                            if (newWorker.workerType == WorkerType.FOREMAN){
                                objectRoot.child("type").setValue("Foreman");
                                //Add to WorkingFor
                                DatabaseReference workingFor = database.getReference("WorkingFor/");
                                DatabaseReference workerWorking = workingFor.child(newWorker.phone);
                                workerWorking.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newWorker.ID);
                            }
                            else{
                                objectRoot.child("type").setValue("Worker");
                            }


                            break;
                    }
                    break;

                case MODIFY:
                    findObject(currentChange.ID, currentChange.category);
                    switch (currentChange.category){
                        case FARM:
                            objectRoot = objectRoot.child("farms").child(currentChange.ID);
                            objectRoot.child("name").setValue(activeFarm.name);
                            objectRoot.child("companyName").setValue(activeFarm.company);
                            objectRoot.child("email").setValue(activeFarm.email);
                            objectRoot.child("contactNumber").setValue(activeFarm.phone);
                            objectRoot.child("province").setValue(activeFarm.province);
                            objectRoot.child("town").setValue(activeFarm.town);
                            objectRoot.child("further").setValue(activeFarm.further);
                            break;
                        case ORCHARD:
                            objectRoot = objectRoot.child("orchards").child(currentChange.ID);
                            objectRoot.child("name").setValue(activeOrchard.name);
                            objectRoot.child("crop").setValue(activeOrchard.crop);

                            //Location
                            DatabaseReference coordsRoot = objectRoot.child("coords");
                            coordsRoot.setValue(null);
                            for(int i = 0; i < activeOrchard.coordinates.size(); i++){
                                LatLng loc = activeOrchard.coordinates.get(i);
                                coordsRoot.child(Integer.toString(i)). child("lat").setValue(loc.latitude);
                                coordsRoot.child(Integer.toString(i)).child("lng").setValue(loc.longitude);
                            }

                            if (activeOrchard.meanBagMass != null) {
//                                objectRoot.child("bagMass").setValue(activeOrchard.meanBagMass);
                                objectRoot.child("bagMass").setValue(activeOrchard.meanBagMass.longValue());
                            }
                            objectRoot.child("irrigation").setValue(activeOrchard.irrigation);
                            if (activeOrchard.datePlanted != null) {
                                objectRoot.child("date").setValue(activeOrchard.datePlanted.getTime().getTime());
                            }
                            coordsRoot = objectRoot.child("cultivars");
                            coordsRoot.setValue(null);
                            if (activeOrchard.getCultivars() != null){
                                for (int i = 0; i < activeOrchard.cultivars.size(); i++){
                                    coordsRoot.child(Integer.toString(i)).setValue(activeOrchard.cultivars.elementAt(i));
                                }
                            }
                            if(activeOrchard.row != null) {
                                objectRoot.child("rowSpacing").setValue(activeOrchard.row);
                            }
                            if (activeOrchard.tree != null) {
                                objectRoot.child("treeSpacing").setValue(activeOrchard.tree);
                            }
                            if (activeOrchard.assignedFarm != null) {
                                objectRoot.child("farm").setValue(activeOrchard.assignedFarm.ID);
                            }
                            objectRoot.child("further").setValue(activeOrchard.further);
                            break;
                        case WORKER:
                            objectRoot = objectRoot.child("workers").child(currentChange.ID);

                            //Begin
                            objectRoot.child("idNumber").setValue(activeWorker.nID);
                            objectRoot.child("info").setValue(activeWorker.further);
                            objectRoot.child("name").setValue(activeWorker.fName);

                            //Orchards
                            DatabaseReference thingRoot = objectRoot.child("orchards");
                            thingRoot.setValue(null);
                            if (activeWorker.getAssignedOrchards() != null) {
                                for (int i = 0; i < activeWorker.assignedOrchards.size(); i++) {
                                    thingRoot.child(Integer.toString(i)).setValue(activeWorker.assignedOrchards.get(i).ID);
                                }
                            }

                            objectRoot.child("phoneNumber").setValue(activeWorker.phone);
                            objectRoot.child("surname").setValue(activeWorker.sName);

                            //Type
                            if (activeWorker.workerType == WorkerType.FOREMAN){
                                objectRoot.child("type").setValue("Foreman");
                                //Modify WorkingFor
//                                if(!activeWorker.oldPhone.equals(activeWorker.phone)) {
                                    DatabaseReference workingFor1 = database.getReference("WorkingFor/");
//                                    workingFor1.child(activeWorker.oldPhone).setValue(null);
                                    DatabaseReference workingForworker = workingFor1.child(activeWorker.oldPhone);
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    if (uid != null && workingForworker != null){
                                        workingForworker.child(uid).setValue(null);
                                    }
                                    DatabaseReference workerWorking = workingFor1.child(activeWorker.phone);
                                    workerWorking.child(uid).setValue(activeWorker.ID);
                                    activeWorker.oldPhone = activeWorker.phone;
//                                }
                            }
                            else if(activeWorker.workerType == WorkerType.WORKER){
                                objectRoot.child("type").setValue("Worker");
                                if (activeWorker.isWasForeman()){
                                    DatabaseReference working = database.getReference("WorkingFor/");
                                    working = working.child(activeWorker.oldPhone);
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    if (uid != null && working != null){
                                        working.child(uid).setValue(null);
                                    }
                                }
                            }


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
                            findObject(currentChange.ID);
//                            database.getReference("WorkingFor/").child(activeWorker.oldPhone).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
                            DatabaseReference working = database.getReference("WorkingFor");
                            working = working.child(activeWorker.oldPhone);
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            if (uid != null && working != null){
                                working.child(uid).setValue(null);
                            }
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
                    if (current.getfID().equals(ID)){
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

    public String getNamedCategory(){
        switch (category){
            case ORCHARD:
                return "ORCHARD";
            case WORKER:
                return "WORKER";
            case FARM:
                return "FARM";
            default:
                return null;
        }
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
//                result[i] = workers.elementAt(i).sName + ", " + workers.elementAt(i).fName;
                result[i] = workers.elementAt(i).toString();
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
    public Orchard getOrchardFromIDString(String findMe){
        for (Orchard current: orchards){
            if (current.ID.equals(findMe)){
                return current;
            }
        }
        return null;
    }
    
    @Nullable
    public Worker getWorkerFromIDString(String findMe){
        for (Worker current: workers){
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

    public String getIDFromPosInArray(int pos, Category cat){
        Category temp = category;
        category = cat;
        String sTemp = getIDFromPosInArray(pos);
        category = temp;
        return sTemp;
    }

    public void findObject(String ID){
        if(category == Category.FARM){
            for (Farm current : farms){
                if(current.getID().equals(ID)){
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

    public void clearActiveObjects(){
        activeWorker = null;
        activeOrchard = null;
        activeFarm = null;
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

    //If overwriteID is false, then the id of the new object and the active object must match

    public boolean modifyActiveFarm(Farm activeFarm, boolean overwriteID) {
        if ((!this.activeFarm.ID.equals(activeFarm.ID) && overwriteID) || this.activeFarm.ID.equals(activeFarm.ID)) {
            this.activeFarm = activeFarm;
            changes.Modify(category, activeFarm.ID);
            return true;
        }
        return false;
    }

    public boolean modifyActiveOrchard(Orchard activeOrchard, boolean overwriteID) {
        if ((!this.activeOrchard.ID.equals(activeOrchard.ID) && overwriteID) || this.activeOrchard.ID.equals(activeOrchard.ID)) {
            this.activeOrchard = activeOrchard;
            changes.Modify(category, activeOrchard.ID);
            return true;
        }
        return false;
    }

    public boolean modifyActiveWorker(Worker activeWorker, boolean overwriteID) {
        if ((!this.activeWorker.ID.equals(activeWorker.ID) && overwriteID) || this.activeWorker.ID.equals(activeWorker.ID)) {
            this.activeWorker = activeWorker;
            changes.Modify(category, activeWorker.ID);
            return true;
        }
        return false;
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
        changes.Add(Category.WORKER, addMe.getfID());
    }


}

/**
 * Below is a method of keeping track of all changes made, so that pushing the database can be quick and 'easy'.
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