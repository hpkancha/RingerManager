package edu.ncsu.soc.rms;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Button;
import android.text.InputType;

import java.util.List;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.database.Cursor;

import android.text.method.*;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import android.graphics.drawable.Drawable;

public class RingerManagerActivity extends MapActivity {
	String latLongString;
	Context context;
	TextView myLocationText;
	List<Overlay> overlays;
	
	public static String emailId = "";
	public static String pass = "";
	
	MapController mapController;
	RingerManagerPositionOverlay positionOverlay;
	
	@Override
	  protected boolean isRouteDisplayed() {
	    return false;
	  }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startService(new Intent(this, RingerManagerService.class));
      }
    
    @Override
    public void onStart(){
    	super.onStart();
    	
    	double replatitude = 0;
    	double replongitude = 0;
    	GeoPoint point = null;
    	
    	MapView myMapView = (MapView) findViewById(R.id.myMapView);
        mapController = myMapView.getController();

        // Configure the map display options
        myMapView.setSatellite(true);

        // Zoom in
        mapController.setZoom(17);
    	
    	Drawable defaultMarker = getResources().getDrawable(R.drawable.androidmarker);
        positionOverlay = new RingerManagerPositionOverlay(this, defaultMarker);
        overlays = myMapView.getOverlays();
        overlays.add(positionOverlay);
        System.out.println("After adding");
    	
        Cursor curLocations = managedQuery(RingerManagerLocations.LOCATIONS_URI, null, null, null, null);
        System.out.println(curLocations.moveToFirst());
        if (curLocations.moveToFirst()){
          int idColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_ID);
      	  int replatColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_PLACE_LAT);
      	  int replngColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_PLACE_LNG);
      	  int isrepColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_ISREP);
          int replocColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_LOCATION);
          int repmodeColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_MODE);
      	  
      	  do{
      		  System.out.println("In Locations do");
      		String id = curLocations.getString(idColumn);
      		System.out.println("id:" + id);
      	    replatitude = (double)curLocations.getInt(replatColumn);
      		System.out.println("lat:" + replatitude);
    		replongitude = (double)curLocations.getInt(replngColumn);
    		System.out.println("long:" + replongitude);
    		int isrep = curLocations.getInt(isrepColumn);
    		System.out.println("isrep:" + isrep);
    		String replocation = curLocations.getString(replocColumn);
    		System.out.println("loca:" + replocation);
    		String repmode = curLocations.getString(repmodeColumn);
    		System.out.println("mode:" + repmode);
    		//myLocationText.append("\nStored Locations:\n" + replatitude/1000000 + "\n" + replongitude/1000000 + "\t" + isrep);  
         	  System.out.println(replatitude + "\t" + replongitude);
         	 if (isrep > 10){ 
         	 point = new GeoPoint((int) replatitude, (int) replongitude);
         	 positionOverlay.addOverlayItem(new OverlayItem(point, id, replocation));
         	 //myLocationText.append("\nYour Current Position is:\n" + replatitude/1000000 + "\n" + replongitude/1000000);
         	 }
       	  } while(curLocations.moveToNext());
        }
        
     // Update the map location.
        if (point == null)
        	point = new GeoPoint((int) replatitude, (int) replongitude);
        mapController.animateTo(point);        
        }
    
    @Override
    public void onPause(){
    	super.onPause();
    	overlays.clear();
    } 
    
    public void Settings(View view){
    	context = this;
    	System.out.println("In OnClick");
		final FrameLayout fl = new FrameLayout(context);
		System.out.println("After FrameLayout f1");
	    final FrameLayout f2 = new FrameLayout(context);
	    System.out.println("After FrameLayout");
	    final EditText email = new EditText(context);
	    final EditText password = new EditText(context);
	    
	    email.setGravity(Gravity.CENTER);
	    password.setGravity(Gravity.CENTER);
	    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	    
	    fl.addView(email, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
			      FrameLayout.LayoutParams.WRAP_CONTENT));
	    f2.addView(password, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
			      FrameLayout.LayoutParams.WRAP_CONTENT));
	    System.out.println("Before Checking EmailId");
	    if (!emailId.equals(""))
	    	email.setText(emailId);
	    else
	    	email.setText("");
	    if (!pass.equals(""))
	    	password.setText(pass);
	    else
	    	password.setText("");
	    
	    System.out.println("Before FrameLayout");
	    
	    new AlertDialog.Builder(context)
        .setView(fl)
        .setTitle("Please enter your EmailId...")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface d, int which) {
            d.dismiss();
            
            emailId = email.getText().toString();
            
            new AlertDialog.Builder(context)
	        .setView(f2)
	        .setTitle("Please enter your password...")
	        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	          @Override
	          public void onClick(DialogInterface d, int which) {
	            d.dismiss();
	            
	            pass = password.getText().toString();
	        
	          }
	        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	          @Override
	          public void onClick(DialogInterface d, int which) {
	            d.dismiss();
	          }
	        }).create().show();
            
          }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface d, int which) {
            d.dismiss();
          }
        }).create().show();
	    
	    System.out.println("After FrameLayout");
    }
}