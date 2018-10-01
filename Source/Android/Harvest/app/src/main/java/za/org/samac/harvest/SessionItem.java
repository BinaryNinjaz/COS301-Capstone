package za.org.samac.harvest;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import za.org.samac.harvest.adapter.MyData;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.SearchedItem;
import za.org.samac.harvest.util.Worker;

public class SessionItem {
    public String key;
    public Date startDate;
    public Date endDate;
    public String foreman;
    public String foremanId;
    public HashMap<String, ArrayList<Pickup>> collectionPoints = new HashMap<>();
    public ArrayList<Location> track = new ArrayList<>();

    public void addCollection(String workerId, String workerName, Location location, Double date){
        if(collectionPoints.containsKey(workerId)) {
            collectionPoints.get(workerId).add(new Pickup(workerName, location, date));
        }else {
            collectionPoints.put(workerId, new ArrayList<Pickup>());
            collectionPoints.get(workerId).add(new Pickup(workerName, location, date));
        }
    }

    public void addTrack(Location loc) {
        track.add(loc);
    }

    protected Worker workerWithId(List<Worker> workers, String id) {
        for (Worker worker : workers) {
            if (worker.getfID().compareTo(id) == 0) {
                return worker;
            }
        }
        return null;
    }

    protected Boolean orchardsContainsOrchard(List<Orchard> orchards, Orchard orchard) {
        for (Orchard o : orchards) {
            if (o.getID().compareTo(orchard.getID()) == 0) {
                return true;
            }
        }
        return false;
    }

    protected Orchard orchardWithId(List<Orchard> orchards, String id) {
        for (Orchard orchard : orchards) {
            if (orchard.getID().compareTo(id) == 0) {
                return orchard;
            }
        }
        return null;
    }

    protected Orchard orchardAtPoint(List<Orchard> orchards, Double x, Double y) {
        for (Orchard o : orchards) {
            if (orchardContainsPoint(o, x, y)) {
                return o;
            }
        }
        return null;
    }

    protected Boolean orchardContainsPoint(Orchard o, Double x, Double y) {
        ArrayList<Double> xcoords = new ArrayList<>();
        ArrayList<Double> ycoords = new ArrayList<>();

        for (LatLng coord : o.getCoordinates()) {
            xcoords.add(coord.latitude);
            ycoords.add(coord.longitude);
        }

        return polygonContainsPoint(xcoords, ycoords, x, y);
    }

    public static Boolean polygonContainsPoint(ArrayList<Double> px, ArrayList<Double> py, Double pointx, Double pointy) {
        int i = 0;
        int j = px.size() - 1;
        Boolean c = false;
        for (; i < px.size(); j = i++) {
            final Boolean yValid = (py.get(i) > pointy) != (py.get(j) > pointy);
            final Double xValidCond = (px.get(j) - px.get(i)) * (pointy - py.get(i)) / (py.get(j) - py.get(i)) + px.get(i);

            if (yValid && pointx < xValidCond) {
                c = !c;
            }
        }

        return c;
    }

    public ArrayList<SearchedItem> search(String text, List<Worker> workers, List<Worker> foremen, List<Orchard> orchards) {
        ArrayList<SearchedItem> result = new ArrayList<>();

        Worker foremanObj = workerWithId(foremen, foremanId);
        
        if (foremanObj != null) {
            for (SearchedItem searchedItem : foremanObj.search(text, true)) {
                result.add(new SearchedItem("Foreman " + searchedItem.property, searchedItem.reason));
            }
        }

        for (String workerId : collectionPoints.keySet()) {
            Worker w = workerWithId(workers, workerId);
            System.out.println(w);
            if (w != null) {
                for (SearchedItem searchedItem : w.search(text, true)) {
                    result.add(new SearchedItem("Worker " + searchedItem.property, searchedItem.reason));
                }
            }

            ArrayList<Orchard> orchardsForSession = new ArrayList<>();
            ArrayList<Pickup> points = collectionPoints.get(workerId);
            for (Pickup point : points) {
                Orchard o = orchardAtPoint(orchards, point.lat, point.lng);
                if (o != null && !orchardsContainsOrchard(orchardsForSession, o)) {
                    orchardsForSession.add(o);
                    for (SearchedItem searchedItem : o.search(text, true)) {
                        result.add(new SearchedItem("Orchard " + searchedItem.property, searchedItem.reason));
                    }
                }
            }
        }

        return result;

    }
}
