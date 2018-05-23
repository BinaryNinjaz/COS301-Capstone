package za.org.samac.harvest;

import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class ForemenPerSession extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private Button perSesWorkerComparison;
}
