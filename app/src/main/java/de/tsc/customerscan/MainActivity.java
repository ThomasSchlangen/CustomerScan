package de.tsc.customerscan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener {

    ArrayList<String> alAddresses;
    customListViewAdapter myAdapter;
    ListView lvGeoAddr;
    int localPosition;
    PopupWindow pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout -> activity_main
        setContentView(R.layout.activity_main);

        // add toolbar to main layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add floating button listener
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // setup list view
        alAddresses = new ArrayList<>();

        lvGeoAddr = findViewById(R.id.lvGeoAddresses);
        myAdapter = new customListViewAdapter(this, alAddresses);
        lvGeoAddr.setAdapter(myAdapter);
        lvGeoAddr.setOnItemClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            int REQUEST_CODE_EXPLICIT = 1;
            Intent QRScanActivity = new Intent(this,QRScanActivity.class);
            startActivityForResult(QRScanActivity,REQUEST_CODE_EXPLICIT);
        } else if (id == R.id.nav_maps) {
            String tmp = getGoogleString(alAddresses);
            if (tmp == null){
                Toast.makeText(this,"JSON Problem",Toast.LENGTH_LONG).show();
            } else{
                Uri gmmUri = Uri.parse(tmp);
                Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            String returnedResult= data.getStringExtra("customer");

            switch (requestCode){
                case 1:
                    alAddresses.add(returnedResult);
                    myAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                int REQUEST_CODE_EXPLICIT = 1;
                Intent QRScanActivity = new Intent(this,QRScanActivity.class);
                startActivityForResult(QRScanActivity,REQUEST_CODE_EXPLICIT);
                break;
            case R.id.btnUp:
                if (localPosition > 0){
                    String tmp = alAddresses.get(localPosition-1);
                    alAddresses.set(localPosition-1,alAddresses.get(localPosition));
                    alAddresses.set(localPosition,tmp);
                    myAdapter.notifyDataSetChanged();
                    localPosition--;
                }
                break;
            case R.id.btnDown:
                if (localPosition < myAdapter.getCount()-1){
                    String tmp = alAddresses.get(localPosition+1);
                    alAddresses.set(localPosition+1,alAddresses.get(localPosition));
                    alAddresses.set(localPosition,tmp);
                    myAdapter.notifyDataSetChanged();
                    localPosition++;
                }
                break;
            case R.id.btnDelete:
                if (localPosition >= 0){
                    pw.dismiss();
                    alAddresses.remove(localPosition);
                    myAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject singleItemJSON;

        View selectedItem = view.findViewById(R.id.lvSingleItem);
        selectedItem.setBackgroundColor(getResources().getColor(R.color.grey));

        switch (parent.getId()){
            case R.id.lvGeoAddresses:
                String singleItem = parent.getItemAtPosition(position).toString();
                try {
                    singleItemJSON = new JSONObject(singleItem);
                    singleItemJSON.getJSONObject("CustomerData").put("Selected","True");
                    alAddresses.set(position,singleItemJSON.toString());
                    myAdapter.notifyDataSetChanged();
                    localPosition = position;
                    showPopup(selectedItem);
                }
                catch(Exception e){
                    Toast.makeText(this, "Unbekannter JSON Objekt Fehler.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void showPopup(View v) {
        String strPos = alAddresses.get(localPosition);
        final View bgdOpa = findViewById(R.id.bgdOpa);

        try {
            // We need to get the instance of the LayoutInflater
            LayoutInflater loInflater = LayoutInflater.from(this);
            View layout = loInflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.mainPopup));
            pw = new PopupWindow(layout, Helpers.dpToPx(this,300), Helpers.dpToPx(this,200), true);
            pw.setAnimationStyle(R.style.popup_window_animation_move);
            bgdOpa.setVisibility(View.VISIBLE);
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    bgdOpa.setVisibility(View.INVISIBLE);

                    String tmp = alAddresses.get(localPosition);
                    try {
                        JSONObject singleItemJSON = new JSONObject(tmp);
                        singleItemJSON.getJSONObject("CustomerData").put("Selected","False");
                        alAddresses.set(localPosition,singleItemJSON.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    myAdapter.notifyDataSetChanged();
                }
            });
            ImageButton btnClose = layout.findViewById(R.id.btnExit);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
                }
            });

            ImageButton btnUp = layout.findViewById(R.id.btnUp);
            btnUp.setOnClickListener(this);

            ImageButton btnDown = layout.findViewById(R.id.btnDown);
            btnDown.setOnClickListener(this);

            Button btnDelete = layout.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(this);

            TextView tvName = layout.findViewById(R.id.tvName);
            JSONObject selectedItemJSON = new JSONObject(strPos);
            tvName.setText(selectedItemJSON.getJSONObject("CustomerData").getString("Name"));

            TextView tvPLZ = layout.findViewById(R.id.tvPLZ);
            tvPLZ.setText(selectedItemJSON.getJSONObject("CustomerData").getString("PLZ"));

            TextView tvCity = layout.findViewById(R.id.tvCity);
            tvCity.setText(selectedItemJSON.getJSONObject("CustomerData").getString("Ort"));

            TextView tvStreet = layout.findViewById(R.id.tvStreet);
            tvStreet.setText(selectedItemJSON.getJSONObject("CustomerData").getString("Strasse"));

            TextView tvStreetNumber = layout.findViewById(R.id.tvStreetNumber);
            tvStreetNumber.setText(selectedItemJSON.getJSONObject("CustomerData").getString("Nummer"));

            TextView tvCountry = layout.findViewById(R.id.tvCountry);
            tvCountry.setText(selectedItemJSON.getJSONObject("CustomerData").getString("Land"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getGoogleString(ArrayList<String> al){
        String strTmpRoute = "http://maps.google.com/maps?&daddr=";

        if (al.size() == 0){
            return null;
        }

        for (int i=0; i <= al.size()-1; i++){
            try {
                JSONObject singleItemJSON = new JSONObject(al.get(i));
                strTmpRoute = strTmpRoute +
                        singleItemJSON.getJSONObject("CustomerData").getString("Strasse") + " " +
                        singleItemJSON.getJSONObject("CustomerData").getString("Nummer") + ", " +
                        singleItemJSON.getJSONObject("CustomerData").getString("PLZ") + " " +
                        singleItemJSON.getJSONObject("CustomerData").getString("Ort") + " " +
                        singleItemJSON.getJSONObject("CustomerData").getString("Land");

                if (i < al.size()-1){
                    strTmpRoute = strTmpRoute + "+to:";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return strTmpRoute;
    }
}
