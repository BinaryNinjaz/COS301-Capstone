package za.org.samac.harvest;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Orchard;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoOrchardMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    boolean show = false;
    GoogleMap gMap;
    MapView mView;
    Data data;
    Orchard orchard;
    String ID;
    List<LatLng> coordinates;
    Polygon polygon;
    boolean pSet = false;


    public InfoOrchardMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_orchard_map, container, false);
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
        orchard = data.getActiveOrchard();
        coordinates = orchard.getCoordinates();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;

        redraw();

        if (show) {
            gMap.setOnMapLongClickListener(this);
        }
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
        this.ID = ID;
    }

    public void save(){
        orchard.setCoordinates(coordinates);
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
