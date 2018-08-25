package za.org.samac.harvest.adapter;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Orchard;

public class collections {

    private Map<String, MyData> individualCollections;//map of each individual
    private ArrayList<Location> track;
    private String foremanEmail = "";
    private double start_date, end_date;

    collections(String email) {
        individualCollections = new HashMap<>();
        foremanEmail = email;
        start_date = System.currentTimeMillis() / 1000.0;
        track = new ArrayList<>();
    }

    public void addCollection(String workerName, Location location, String selectedOrchard){
        if(individualCollections.containsKey(workerName)) {
            MyData data = individualCollections.get(workerName);
            data.addLocation(location, selectedOrchard);
            individualCollections.put(workerName, data);
        }else {
            MyData data = new MyData();
            data.addLocation(location, selectedOrchard);
            individualCollections.put(workerName, data);
        }
    }

    public void addCollection(String workerName, Location location, String selectedOrchard, Double date){
        if(individualCollections.containsKey(workerName)) {
            MyData data = individualCollections.get(workerName);
            data.addLocation(location, selectedOrchard, date);
            individualCollections.put(workerName, data);
        }else {
            MyData data = new MyData();
            data.addLocation(location, selectedOrchard, date);
            individualCollections.put(workerName, data);
        }
    }

    public void removeCollection(String workerName){
        MyData data = individualCollections.get(workerName);
        if(data!=null) {
            data.removeLocation();
            if (data.size == 0) {
                individualCollections.remove(workerName);
            } else {
                individualCollections.put(workerName, data);
            }
        }
    }

    public void addTrack(Location location) {
        track.add(location);
    }

    public void sessionEnd() {
        end_date = System.currentTimeMillis() / 1000.0;
    }

    public Map<String, MyData> getIndividualCollections() {
        return individualCollections;
    }

    public ArrayList<Location> getTrack() {
        return track;
    }

    public String getForemanEmail() {
        return foremanEmail;
    }

    public double getStart_date() {
        return start_date;
    }

    public double getEnd_date() {
        return end_date;
    }

    private ArrayList<LatLng> allPickupPoints(String selectedOrchard) {
        ArrayList<LatLng> result = new ArrayList<>();
        for (String key : individualCollections.keySet()) {
            MyData cs = individualCollections.get(key);
            for (int i = 0; i < cs.size; i++) {
                if (selectedOrchard.compareTo(cs.selectedOrchards.get(i)) == 0) {
                    LatLng loc = new LatLng(cs.latitude.get(i), cs.longitude.get(i));
                    result.add(loc);
                }
            }
        }
        return result;
    }

    public void modifyOrchardAreas() {
        Data data = new Data();

        Vector<Orchard> orchards = data.getOrchards();
        for (Orchard orchard : orchards) {
            if (orchard.getInferArea()) {
                List<LatLng> currentCoords = orchard.getCoordinates();
                ArrayList<LatLng> points = convexHull(orchard.getID(), currentCoords);

                if (!points.isEmpty()) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference userRef =  FirebaseDatabase.getInstance().getReference(uid + "/");
                    DatabaseReference coordRef = userRef.child("orchards/" + orchard.getID() + "/coords");
                    ArrayList<FirebaseLocation> firPoints = new ArrayList<>();
                    for (int i = 0; i < points.size(); i++) {
                        firPoints.add(new FirebaseLocation(points.get(i).latitude, points.get(i).longitude));
                    }
                    coordRef.setValue(firPoints);
                }
            }
        }

    }

    public ArrayList<LatLng> convexHull(String selectedOrchard, List<LatLng> additionalPoints) {
        ArrayList<LatLng> points = allPickupPoints(selectedOrchard);
        if (points.size() < 3) {
            return new ArrayList<LatLng>();
        }

        points.addAll(additionalPoints);
        for (int p = 0; p < points.size(); p++) {
            System.out.println("----------------");
            System.out.println(points.get(p).latitude);
            System.out.println(points.get(p).longitude);
        }

        ArrayList<LatLng> result = new ArrayList<>();

        LatLng temp = points.get(0);
        int minPos = minIndex(points);
        points.set(0, points.get(minPos));
        points.set(minPos, temp);

        LatLng pivot = points.get(0);

        Collections.sort(points, new ClockOrder(pivot));

        result.add(points.get(0));
        result.add(points.get(1));
        result.add(points.get(2));

        for (int i = 3; i < points.size(); i++) {
            LatLng last = result.get(result.size() - 1);
            LatLng secondLast = result.get(result.size() - 2);
            while (ccw(secondLast, last, points.get(i)).compareTo(-1) != 0) {
                result.remove(result.size() - 1);
                if (result.size() < 3) {
                    break;
                }
                last = result.get(result.size() - 1);
                secondLast = result.get(result.size() - 2);
            }
            result.add(points.get(i));
        }

        return result;
    }

    public static Integer ccw(LatLng p, LatLng q, LatLng r) {
        Double t = (r.latitude - q.latitude) * (q.longitude - p.longitude) - (r.longitude - q.longitude) * (q.latitude - p.latitude);
        if (t < 0) {
            return -1;
        } else if (t > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static Double euclideanDistance(LatLng a, LatLng b) {
        Double dx = a.latitude - b.latitude;
        Double dy = a.longitude- b.longitude;
        return dx * dx + dy * dy;
    }

    private int minIndex(ArrayList<LatLng> points) {
        LatLng min = null;
        int result = -1;
        for (int i = 0; i < points.size(); i++) {
            LatLng p = points.get(i);
            if (min == null) {
                min = p;
                result = i;
            } else if (p.longitude < min.longitude) {
                min = p;
                result = i;
            } else if (p.longitude == min.longitude && p.latitude < min.latitude) {
                min = p;
                result = i;
            }
        }

        return result;
    }

    static class ClockOrder implements Comparator<LatLng> {
        LatLng pivot;

        public ClockOrder(LatLng pivot) {
            this.pivot = pivot;
        }

        @Override
        public int compare(LatLng a, LatLng b) {
            Integer order = collections.ccw(pivot, a, b);
            if (order.compareTo(0) == 0) {
                return collections.euclideanDistance(pivot, a).compareTo(collections.euclideanDistance(pivot, b));
            }
            return order;
        }
    }

    static class FirebaseLocation {
        double lat;
        double lng;

        public FirebaseLocation(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
}