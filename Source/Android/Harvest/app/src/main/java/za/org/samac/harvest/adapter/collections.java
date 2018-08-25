package za.org.samac.harvest.adapter;

import android.location.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.adapter.MyData;

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

    private ArrayList<Location> allPickupPoints() {
        ArrayList<Location> result = new ArrayList<>();
        for (String key : individualCollections.keySet()) {
            MyData cs = individualCollections.get(key);
            for (int i = 0; i < cs.size; i++) {
                Location loc = new Location("");
                loc.setLatitude(cs.latitude.get(i));
                loc.setLongitude(cs.longitude.get(i));
                result.add(loc);
            }
        }
        return result;
    }

    public ArrayList<Location> convexHull() {
        ArrayList<Location> points = allPickupPoints();

        if (points.size() < 3) {
            return new ArrayList<Location>();
        }

        ArrayList<Location> result = new ArrayList<>();

        Location temp = points.get(0);
        int minPos = minIndex(points);
        points.set(0, points.get(minPos));
        points.set(minPos, temp);

        Location pivot = points.get(0);

        Collections.sort(points, new ClockOrder(pivot));

        result.add(points.get(0));
        result.add(points.get(1));
        result.add(points.get(2));

        for (int i = 3; i < points.size(); i++) {
            Location last = result.get(result.size() - 1);
            Location secondLast = result.get(result.size() - 2);
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

    public static Integer ccw(Location p, Location q, Location r) {
        Double t = (r.getLatitude() - q.getLatitude()) * (q.getLongitude() - p.getLongitude()) - (r.getLongitude() - q.getLongitude()) * (q.getLatitude() - p.getLatitude());
        if (t < 0) {
            return -1;
        } else if (t > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static Double euclideanDistance(Location a, Location b) {
        Double dx = a.getLatitude() - b.getLatitude();
        Double dy = a.getLongitude() - b.getLongitude();
        return dx * dx + dy * dy;
    }

    private int minIndex(ArrayList<Location> points) {
        Location min = null;
        int result = -1;
        for (int i = 0; i < points.size(); i++) {
            Location p = points.get(i);
            if (min == null) {
                min = p;
                result = i;
            } else if (p.getLongitude() < min.getLongitude()) {
                min = p;
                result = i;
            } else if (p.getLongitude() == min.getLongitude() && p.getLatitude() < min.getLatitude()) {
                min = p;
                result = i;
            }
        }

        return result;
    }

    static class ClockOrder implements Comparator<Location> {
        Location pivot;

        public ClockOrder(Location pivot) {
            this.pivot = pivot;
        }

        @Override
        public int compare(Location a, Location b) {
            Integer order = collections.ccw(pivot, a, b);
            if (order.compareTo(0) == 0) {
                return collections.euclideanDistance(pivot, a).compareTo(collections.euclideanDistance(pivot, b));
            }
            return order;
        }
    }
}