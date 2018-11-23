package de.tsc.customerscan;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

class customListViewAdapter extends ArrayAdapter<String> {

    public customListViewAdapter(Context context, ArrayList<String> waypoints) {
        super(context,R.layout.list_view_item,waypoints);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //String strPosition = Integer.toString(position);
        JSONObject singleItemJSON = null;

        LayoutInflater loInflater = LayoutInflater.from(getContext());
        View customView = loInflater.inflate(R.layout.list_view_item,parent,false);

        // get handles to each item in our custom view
        TextView tvMain = customView.findViewById(R.id.tvMainItem);
        TextView tvCountry = customView.findViewById(R.id.tvCountryItem);
        TextView tvSub1 = customView.findViewById(R.id.tvSubItem1);
        TextView tvSub2 = customView.findViewById(R.id.tvSubItem2);
        TextView tvSub3 = customView.findViewById(R.id.tvSubItem3);
        TextView tvSub4 = customView.findViewById(R.id.tvSubItem4);

        // get json string from ArrayList
        String singleWaypointItem = getItem(position);
        try {
            singleItemJSON = new JSONObject(singleWaypointItem);
        }
        catch(Exception e){
            Toast.makeText(getContext(), "Scan " + Integer.toString(position) + " kein JSON Objekt.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }

        try {
            tvMain.setText(singleItemJSON.getJSONObject("CustomerData").getString("Name"));
            tvCountry.setText(singleItemJSON.getJSONObject("CustomerData").getString("Land"));
            tvSub1.setText(singleItemJSON.getJSONObject("CustomerData").getString("PLZ"));
            tvSub2.setText(singleItemJSON.getJSONObject("CustomerData").getString("Ort"));
            tvSub3.setText(singleItemJSON.getJSONObject("CustomerData").getString("Strasse"));
            tvSub4.setText(singleItemJSON.getJSONObject("CustomerData").getString("Nummer"));
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Scan " + Integer.toString(position) + " JSON Item nicht gefunden.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }

        // Set background for selected / moving item
        customView.setBackgroundColor(Color.WHITE);
        try {
            String tmpSelected = singleItemJSON.getJSONObject("CustomerData").getString("Selected");
            if ( tmpSelected.equals("True") ){
                customView.setBackgroundColor(Color.LTGRAY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return customView;
    }
}
