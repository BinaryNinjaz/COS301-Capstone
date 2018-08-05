package za.org.samac.harvest.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import za.org.samac.harvest.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BackgroundService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private final String TAG = getClass().getName();
    public static final String ACTION_LOCATION_UPDATES = "za.org.samac.harvest.service.action.ACTION_LOCATION_UPDATES";
    public static final String ACTION_LOCATION_BROADCAST = "za.org.samac.harvest.service.action.ACTION_LOCATION_BROADCAST";
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public static LocationManager locationManager;

    private static final long LOCATION_REFRESH_TIME = 60000;
    private static final float LOCATION_REFRESH_DISTANCE = 3;
    public static Location location;
    public BackgroundService() {
        super("BackgroundService");
    }
    int trackCount = 0;
    private Map<Integer, Location> track = new HashMap<>();
    public static boolean locationEnabled = false;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location locationChange) {
            if (location == null) {
                Intent intent = new Intent();
                intent.setAction(ACTION_LOCATION_BROADCAST);
                intent.putExtra("latitude", locationChange.getLatitude());
                intent.putExtra("longitude", locationChange.getLongitude());
                sendBroadcast(intent);
            }
            location = locationChange;
            trackCount++;
            track.put(trackCount, location);
            MainActivity.adapter.setLatLng(locationChange.getLatitude(), locationChange.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(ACTION_LOCATION_UPDATES);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            locationEnabled = true;
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);//changed to network provider as GPS wasn't working

            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                location = locationManager.getLastKnownLocation(provider);
                //Log.d("last known location, provider: %s, location: %s", provider, location);

                if (location != null) {
                    break;
                }
                    /*if (bestLocation == null
                            || location.getAccuracy() < bestLocation.getAccuracy()) {
                        //Log.d("found best last known location: %s", location);
                        bestLocation = location;
                    }*/
            }

            if (location == null) {
                location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//changed to network provider as GPS wasn't working
                //adapter.setLocation(location);
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOCATION_UPDATES.equals(action)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (location != null) {
                            Log.i(TAG, "########################## " + location.getLatitude() + " ; " + location.getLongitude());
                            Intent intent = new Intent();
                            intent.setAction(ACTION_LOCATION_BROADCAST);
                            intent.putExtra("latitude", location.getLatitude());
                            intent.putExtra("longitude", location.getLongitude());
                            sendBroadcast(intent);
                            if (MainActivity.sessionEnded == true) {
                                scheduledExecutorService.shutdown();
                                try {
                                    if(!scheduledExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                                        scheduledExecutorService.shutdownNow();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    scheduledExecutorService.shutdownNow();
                                }
                                stopSelf();
                            }
                        }
                    }
                };
                scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 120, TimeUnit.SECONDS);
            }
        }
    }

    /*@Override
    public void onDestroy() {
        scheduledExecutorService.shutdown();
        try {
            if(!scheduledExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            scheduledExecutorService.shutdownNow();
        }
        super.onDestroy();
    }*/
}
