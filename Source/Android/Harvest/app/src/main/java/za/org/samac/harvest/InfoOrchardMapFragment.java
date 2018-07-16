package za.org.samac.harvest;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Orchard;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoOrchardMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{

    private boolean show = false;
    private boolean explanationShown = false;
    private boolean permissionAskedInSession;
    private boolean locationInformationAskedInSession;
    private GoogleMap gMap;
    private MapView mView;
    private Data data;
    private List<LatLng> coordinates;
    private Polygon polygon;
    private boolean pSet = false;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    LocNotAskAgain locCallback;

    public InfoOrchardMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_orchard_map, container, false);
    }

    public interface LocNotAskAgain{
        public void LocationInformationAsked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            locCallback = (LocNotAskAgain) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement LocationInformationAsked");
        }
    }

    public void setPermissionAskedInSession(boolean permissionAskedInSession){
        this.permissionAskedInSession = permissionAskedInSession;
    }

    public void setLocationInformationAskedInSession(boolean locationInformationAskedInSession){
        this.locationInformationAskedInSession = locationInformationAskedInSession;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        if (show){
            getView().findViewById(R.id.info_orch_map_bottom_bit).setVisibility(View.VISIBLE);
        }
        else {
            getView().findViewById(R.id.info_orch_map_bottom_bit).setVisibility(View.GONE);
        }

        mView = getView().findViewById(R.id.info_orch_map_map);
        mView.onCreate(savedInstanceState);
        mView.getMapAsync(this);

//        data.findObject(ID);
        Orchard orchard = data.getActiveOrchard();
        coordinates = orchard.getCoordinates();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;

        redraw();

        if (show) {
            gMap.setOnMapLongClickListener(this);
            activateLocation();
        }
        else {
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    public void activateLocation(){
        //Check if location permission is granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            gMap.setMyLocationEnabled(true);
            gMap.setOnMyLocationClickListener(this);
            gMap.setOnMyLocationButtonClickListener(this);
            if (!locationInformationAskedInSession) {
                LocationInformationCheck();
            }
        }
        else if (!permissionAskedInSession){
            permissionAsk();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    public void permissionAsk(){
        if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.info_orch_map_permissionDialog_message);
            builder.setTitle(R.string.info_orch_map_permissionDialog_title);
            builder.setPositiveButton(R.string.info_orch_map_permissionDialog_okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            AlertDialog explain = builder.create();
            explain.show();
            explanationShown = true;
        }
        else {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    public boolean isExplanationShown(){
        return explanationShown;
    }

    public void LocationInformationCheck() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.info_orch_map_locTitle)
                .setMessage(R.string.info_orch_map_locMes)
                .setCancelable(false)
                .setPositiveButton(R.string.info_orch_map_locYes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton(R.string.info_orch_map_locNo, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                }).setNeutralButton(R.string.info_orch_map_locNever, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locCallback.LocationInformationAsked();
                        dialog.cancel();
                    }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        coordinates.add(latLng);
        redraw();
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mView.onLowMemory();
    }

    public void setMapShowBottomBit(boolean show){
        this.show = show;
    }

    public void setDataAndOrchardID(Data data, String ID){
        this.data = data;
    }

    public void redraw(){

        if (coordinates.size() > 0) {
            if (!pSet) {
                PolygonOptions polygonOptions = new PolygonOptions();
                for (int i = 0; i < coordinates.size(); i++) {
                    polygonOptions.add(coordinates.get(i));
                }
                polygonOptions.strokeColor(R.color.info_orchard_map_polygon_stroke);
                polygonOptions.strokeWidth(3);
                polygonOptions.fillColor(R.color.info_orchard_map_polygon_fill);

                polygon = gMap.addPolygon(polygonOptions);
                pSet = true;
            } else {
                polygon.setPoints(coordinates);
            }
        }
    }

    public void erase(){
        coordinates.clear();
        gMap.clear();
        pSet = false;
        redraw();
    }

    public void removeLast(){
        coordinates.remove(coordinates.size() - 1);
        redraw();
    }
}
