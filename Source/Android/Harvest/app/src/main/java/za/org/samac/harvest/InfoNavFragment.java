package za.org.samac.harvest;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.io.File;

public class InfoNavFragment extends Fragment {

    private RelativeLayout farmPic = null;
    private RelativeLayout orchardsPic =  null;
    private RelativeLayout workersPic =  null;

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

}
