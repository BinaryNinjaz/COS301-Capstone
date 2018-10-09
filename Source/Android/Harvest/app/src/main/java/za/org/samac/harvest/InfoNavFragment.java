package za.org.samac.harvest;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import za.org.samac.harvest.util.Data;

public class InfoNavFragment extends Fragment {

    private RelativeLayout farmPic = null;
    private RelativeLayout orchardsPic =  null;
    private RelativeLayout workersPic =  null;

    private CardView farmButton, orchardButton, workerButton;
    private TextView tutText;

    public InfoNavFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_info_nav, container, false);

        /*String fileName = "agriculturebarnbuildings.jpg";
        String completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        farmPic = viewGroup.findViewById(R.id.farmPic);
        orchardsPic =  viewGroup.findViewById(R.id.orchardsPic);
        workersPic =  viewGroup.findViewById(R.id.workersPic);

        Glide.with(InfoNavFragment.this)
                .load(imageUri)
                .into(farmPic);

        fileName = "nutsontreethree.jpg";
        completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        file = new File(completePath);
        imageUri = Uri.fromFile(file);

        Glide.with(InfoNavFragment.this)
                .load(imageUri)
                .into(orchardsPic);

        fileName = "nih.jpg";
        completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        file = new File(completePath);
        imageUri = Uri.fromFile(file);

        Glide.with(InfoNavFragment.this)
                .load(imageUri)
                .into(workersPic);*/

        // Inflate the uberParentLayout for this fragment
        return inflater.inflate(R.layout.fragment_info_nav, container, false);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        farmButton = view.findViewById(R.id.buttFarms);
        orchardButton = view.findViewById(R.id.buttOrchards);
        workerButton = view.findViewById(R.id.buttWorkers);
        tutText = view.findViewById(R.id.tutText);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Data.hasFarm()){
            farmButton.setVisibility(View.VISIBLE);
            orchardButton.setVisibility(View.GONE);
            workerButton.setVisibility(View.GONE);
            tutText.setVisibility(View.VISIBLE);
            tutText.setText(getText(R.string.info_tut_cFarm));
        }
        else if(!Data.hasOrchard()){
            farmButton.setVisibility(View.VISIBLE);
            orchardButton.setVisibility(View.VISIBLE);
            workerButton.setVisibility(View.GONE);
            tutText.setVisibility(View.VISIBLE);
            tutText.setText(getText(R.string.info_tut_cOrchard));
        }
        else if (!Data.hasWorker()){
            farmButton.setVisibility(View.VISIBLE);
            orchardButton.setVisibility(View.VISIBLE);
            workerButton.setVisibility(View.VISIBLE);
            tutText.setVisibility(View.VISIBLE);
            tutText.setText(getText(R.string.info_tut_cWorker));
        }
        else {
            farmButton.setVisibility(View.VISIBLE);
            orchardButton.setVisibility(View.VISIBLE);
            workerButton.setVisibility(View.VISIBLE);
            tutText.setVisibility(View.GONE);
        }
    }
}
