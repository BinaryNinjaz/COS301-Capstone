package za.org.samac.harvest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.DBInfoObject;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.Worker;


/**
 * Displays and does the search
 */
public class InfoSearchFragment extends Fragment {

    Data data;
    List<DBInfoObject> allThings;
    List<LinearLayout> layouts;

    LinearLayout uberParentLayout;
    Category category;

    LayoutInflater inflater;

    View view;

    public InfoSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        this.view = view;

        inflater = getLayoutInflater();

        allThings = data.getThings(category);
        uberParentLayout = view.findViewById(R.id.info_searchResults);

        layouts = new ArrayList<>();

    }

    public void setData(Data data){
        this.data = data;
    }

    public void setCategory(Category category){
        this.category = category;
    }
    
    public void searchForQuery(String query){
        uberParentLayout.removeAllViews();
        layouts.clear();

        List<DBInfoObject> foundMegas = data.search(query, category);
        addResults(foundMegas);
    }

    //Add the given mega results
    private void addResults(List<DBInfoObject> results){
        for(DBInfoObject object : results){
            if (object.getClass() == Farm.class){
                addResult(" " + object.toString(), "Farm " + object.getId(), "Farms");
            }
            else if(object.getClass() == Orchard.class){
                addResult(" " + object.toString(), "Orchard " + object.getId(), "Orchards");
            }
            else if (object.getClass() == Worker.class) {
                addResult(" " + object.toString(), "Worker " + object.getId(), "Workers");
            }
        }
    }

    //tag: "Worker " + id

    /**
     * Adds the result
     * @param name the name of the result, to be shown on the button
     * @param tag the tag for the button, so it goes to the correct place
     * @param title the title under which the result should be added
     */
    private void addResult(String name, String tag, String title){
        LinearLayout linearLayout = findLayout(title);
        LinearLayout gotten = (LinearLayout) inflater.inflate(R.layout.info_search_result, null, false);
        Button button = gotten.findViewById(R.id.info_search_result);
        button.setText(name);
        button.setTag(tag);
        linearLayout.addView(gotten);
    }

    /**
     * Returns and, if necessary, constructs, the layout that represents the results
     * @param title title of the set
     * @return the layout
     */
    private LinearLayout findLayout(String title){
        for (LinearLayout layout : layouts){
            if (layout.getTag().toString().equals(title)){
                return layout;
            }
        }
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.info_search_layout, null, false);
        linearLayout.setTag(title);
        TextView textView =  linearLayout.findViewById(R.id.info_search_title_title);
        textView.setText(title);
        layouts.add(linearLayout);
        uberParentLayout.addView(linearLayout);
        return linearLayout;
    }
}

class SearcherAdapter extends RecyclerView.Adapter<SearcherAdapter.ViewHolder>{

    private List<DBInfoObject> showUs;
    
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button mButton;

        public ViewHolder(View view){
            super(view);

            mButton = view.findViewById(R.id.info_list_butt);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_goto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mButton.setText(showUs.get(position).toString());
        if (showUs.get(position).getClass() == Worker.class) {
            holder.mButton.setTag("Worker " + showUs.get(position).getId());
        }
        else if (showUs.get(position).getClass() == Orchard.class){
            holder.mButton.setTag("Orchard " + showUs.get(position).getId());
        }
        else if (showUs.get(position).getClass() == Farm.class){
            holder.mButton.setTag("Farm " + showUs.get(position).getId());
        }
    }

    @Override
    public int getItemCount(){
        return showUs.size();
    }
}
