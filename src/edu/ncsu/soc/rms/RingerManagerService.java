package edu.ncsu.soc.rms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;

import android.database.Cursor;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public class RingerManagerService extends Service {
  private static final String TAG = "RingerManagerService";
  public static LocationManager locationManager;
  public static LocationListener locationListener;
  Location lastLocation = null;
  public static String lastLocationName = "";
  boolean dupLocations;
  
  public static AudioManager aManager;
  
  public static TelephonyManager tmanager;
  public static PhoneStateListener tlistener;
  public static int previousCallState = 0;
  public static String message = null;

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public void onCreate() {
    Log.i(TAG, "Service created");
   
    aManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    
    tmanager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);    
    
    tlistener = new PhoneStateListener(){
         @Override	
    	 public void onCallStateChanged(int state, String incomingNumber){
    	    	super.onCallStateChanged(state, incomingNumber);
    	    	System.out.println("Current State is:" + state);
    	    	if (state == TelephonyManager.CALL_STATE_IDLE && state != previousCallState){
    	    		System.out.println("MISSED CALL DETECTED");
    	    		String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
    	    		Cursor mCallCursor = getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI,
    	    	            null, null, null, strOrder);
    	    		
    	    		if (mCallCursor.moveToFirst()){
    	    			boolean missed = mCallCursor.getInt(mCallCursor.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.MISSED_TYPE;
                        System.out.println("Is Missed Call:" + missed);
	    	            if (missed) {
	    	                String name = mCallCursor.getString(mCallCursor
	    	                        .getColumnIndex(CallLog.Calls.CACHED_NAME));

	    	                String number = mCallCursor.getString(mCallCursor
	    	                        .getColumnIndex(CallLog.Calls.NUMBER));

	    	                System.out.println("You have a missed call from " + name + " on " + number);
	    	                
	    	                if (name != null){
	    	                	System.out.println(name);
	    	                	System.out.println(lastLocationName);
	    	                	String id = null;
	    	                	String emailAddress = "";

	    	                	Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

	    	                	Cursor curContactId = getContentResolver().query(lookupUri, null,null, null, null);
	    	                	if (curContactId.moveToFirst()){
	    	                			id = curContactId.getString(curContactId.getColumnIndex(ContactsContract.Contacts._ID));
	    	                	}
	    	                	
	    	                	Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,
	    	                            ContactsContract.CommonDataKinds.Email.CONTACT_ID+ " = " + id, null, null);
	    	                	if (emails.moveToFirst()){
	    	                		emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
	    	                	}

	    	                	System.out.println(name);
	    	                	System.out.println(lastLocationName);
	    	                	System.out.println(emailAddress);
	    	                	System.out.println(RingerManagerActivity.emailId);
	    	                	System.out.println(RingerManagerActivity.pass);
	    	                	
	    	                	if (!lastLocationName.equals(""))
	    	                		message = "Hi " + name + ", I am sorry I couldn't take your call. I am in " + lastLocationName +
    	    	                			". I'll call you when I get back.";
	    	                	else
	    	                		message = "Hi " + name + ", I am sorry I couldn't take your call." +
    	    	                			" I'll call you when I get back.";
	    	                	System.out.println(message);
	    	                	if (!emailAddress.equals("")){
	    	                		try {   
		    	                        GmailSender sender = new GmailSender(RingerManagerActivity.emailId, RingerManagerActivity.pass);
		    	                        sender.sendMail("Missed Call Alert",   
		    	                                message,   
		    	                                emailAddress,   
		    	                                emailAddress);   
		    	                    } catch (Exception e) {   
		    	                        Log.e("SendMail", e.getMessage(), e);   
		    	                    } 
	    	                	}
	    	                }
    	    	    }
    	    		}

    	    	}
    	    	previousCallState = state;
    	    }
    };
    
    tmanager.listen(tlistener, tlistener.LISTEN_CALL_STATE);


 // Acquire a reference to the system Location Manager
    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    // Define a listener that responds to location updates
    locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
          // Called when a new location is found by the network location provider.
          makeUseOfNewLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
      };
      
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 120000, 0, locationListener); 
      	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 0, locationListener); 
  }
  
  private void makeUseOfNewLocation(Location location){
	  dupLocations = false;
	  boolean stopUpdate = false;
	  boolean flag = false;
	  double replatitude, replongitude;
	  int isrep;
	  String replocation, repmode;
	  System.out.println("IN LOCATION");
	  System.out.println(location);
	  System.out.println(lastLocation);
	  Double latitude = location.getLatitude() * 1E6;
      Double longitude = location.getLongitude() * 1E6;

      if (lastLocation != null)
      {
       if ((location.getLatitude() == lastLocation.getLatitude()) && (location.getLongitude() == lastLocation.getLongitude())){
    	   dupLocations = true;
       }
      }
      if (!dupLocations){
      
      lastLocation = location;
      
      Cursor curLocations = getContentResolver().query(RingerManagerLocations.LOCATIONS_URI, null, null, null, null);
      System.out.println(curLocations.moveToFirst());
      if (curLocations.moveToFirst()){
    	  System.out.println("In Modes if");
    	  int idColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_ID);
    	  int replatColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_PLACE_LAT);
       	  int replngColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_PLACE_LNG);
       	  int isrepColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_ISREP);
      	  int replocationColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_LOCATION);
       	  int repmodeColumn = curLocations.getColumnIndex(RingerManagerLocations.KEY_MODE);
      	  do{
      		System.out.println("In Modes do");
      		int id = curLocations.getInt(idColumn);
      		System.out.println("id:" + id);
      	    replatitude = (double)curLocations.getInt(replatColumn);
      		System.out.println("lat:" + replatitude);
    		replongitude = (double)curLocations.getInt(replngColumn);
    		System.out.println("long:" + replongitude);
    		isrep = curLocations.getInt(isrepColumn);
    		System.out.println("isrep:" + isrep);
    		replocation = curLocations.getString(replocationColumn);
    		System.out.println("loca:" + replocation);
    		repmode = curLocations.getString(repmodeColumn);
    		System.out.println("mode:" + repmode);
    		
      		if (getDistance(latitude, longitude, replatitude, replongitude) < 100 && isrep != 0){
      			System.out.println("In Modes inner if");
      			if (isrep > 10){
      				lastLocationName = replocation;
      				System.out.println("REPMODE IS" + repmode);
      				if (!repmode.equals("Default")){
      					if (repmode.equals("Normal"))
      						aManager.setRingerMode(2);
      					if (repmode.equals("Silent"))
      						aManager.setRingerMode(0);
      					if (repmode.equals("Vibrate"))
      						aManager.setRingerMode(1);
      				}
      				stopUpdate = true;
      				break;
      			}
      				
      			ContentValues values = new ContentValues();
                values.put(RingerManagerLocations.KEY_ISREP, 0);
                values.put(RingerManagerLocations.KEY_LOCATION, "");
                values.put(RingerManagerLocations.KEY_MODE, "");
            
                System.out.println("Activity" + values);
                int uri = getContentResolver().update(Uri.parse(RingerManagerLocations.LOCATIONS_URI + "/" + id), values, null, null);
                System.out.println("After Activity Insert");
                flag = true;
                break;
      		}
      	  } while(curLocations.moveToNext());
      	  
      		System.out.println("In Modes after while");
        	ContentValues values = new ContentValues();
            values.put(RingerManagerLocations.KEY_PLACE_LAT, latitude);
            values.put(RingerManagerLocations.KEY_PLACE_LNG, longitude);
            if (flag){
            	values.put(RingerManagerLocations.KEY_ISREP, isrep + 1);
            	values.put(RingerManagerLocations.KEY_LOCATION, replocation);
                values.put(RingerManagerLocations.KEY_MODE, repmode);
            }
            else if (!flag && !stopUpdate){
            	values.put(RingerManagerLocations.KEY_ISREP, 1);
            	values.put(RingerManagerLocations.KEY_LOCATION, "");
                values.put(RingerManagerLocations.KEY_MODE, "");
            }
            if (!stopUpdate){
            	System.out.println("Activity" + values);
                Uri uri = getContentResolver().insert(RingerManagerLocations.LOCATIONS_URI, values);
                System.out.println("After Activity Insert");
            }
      	  }
      else {
    	  System.out.println("In Modes else");
    	  ContentValues values = new ContentValues();
          values.put(RingerManagerLocations.KEY_PLACE_LAT, latitude);
          values.put(RingerManagerLocations.KEY_PLACE_LNG, longitude);
          values.put(RingerManagerLocations.KEY_ISREP, 1);
          values.put(RingerManagerLocations.KEY_LOCATION, "");
          values.put(RingerManagerLocations.KEY_MODE, "");
          
          System.out.println("Activity" + values);
          Uri uri = getContentResolver().insert(RingerManagerLocations.LOCATIONS_URI, values);
          System.out.println("After Activity Insert");
      }
      }
  }
  
  /**
   * Finds distance between two coordinate pairs.
   *
   * @param lat1 First latitude in degrees
   * @param lon1 First longitude in degrees
   * @param lat2 Second latitude in degrees
   * @param lon2 Second longitude in degrees
   * @return distance in meters
   */
  public static double getDistance(double lat1, double lon1, double lat2, double lon2) {

    final double Radius = 6371 * 1E3; // Earth's mean radius

    double dLat = Math.toRadians((lat2/1000000) - (lat1/1000000));
    double dLon = Math.toRadians((lon2/1000000) - (lon1/1000000));
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1/1000000))
        * Math.cos(Math.toRadians(lat2/1000000)) * Math.sin(dLon / 2000000) * Math.sin(dLon / 2000000);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return Radius * c;
  }
}
