package edu.ncsu.soc.rms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import android.graphics.drawable.Drawable;
import com.google.android.maps.Projection;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

public class RingerManagerPositionOverlay extends ItemizedOverlay{
	Context context;
	String repmode;
	public ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

	  public RingerManagerPositionOverlay(Context _context, Drawable marker) {
		super(boundCenterBottom(marker));
	    this.context = _context;
	  }

	  public void addOverlayItem(OverlayItem item) {
	        items.add(item);
	        populate();
	    }
	  
	  public void deleteAllItems(){
		  items.clear();
	  }

	    @Override
	    protected OverlayItem createItem(int i) {
	        return items.get(i);
	    }

	    @Override
	    public int size() {
	        return items.size();
	    }

	    @Override
	    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	      Projection projection = mapView.getProjection();

	      for (int i = 0; i < items.size(); i++){
	    	  if (items.get(i).getPoint() == null)
	  	        return;

	  	      if (shadow == false) {
	  	        // Convert the location to screen pixels
	  	        Point point = new Point();
	  	        projection.toPixels(items.get(i).getPoint(), point);
   	  	        String id = items.get(i).getTitle();
   	  	        Cursor curMode = context.getContentResolver().query(Uri.parse(RingerManagerLocations.LOCATIONS_URI + "/" + id), null, null, null, null);
   	  	        if (curMode.moveToFirst()){
   	  	             repmode = curMode.getString(curMode.getColumnIndex(RingerManagerLocations.KEY_MODE));
   	  	        }
	  	        // Setup the paint
	  	        Paint paint = new Paint();
	  	        paint.setARGB(255, 255, 255, 255);
	  	        paint.setAntiAlias(true);
	  	        paint.setFakeBoldText(true);

	  	        Paint backPaint = new Paint();
	  	        backPaint.setARGB(180, 50, 50, 50);
	  	        backPaint.setAntiAlias(true);

	  	        RectF locRect = new RectF(point.x + 10, point.y - 15, point.x + 85,
	  	            point.y + 5);
	  	        RectF modeRect = new RectF(point.x + 25, point.y - 95, point.x + 125,
		  	            point.y - 75);

	  	        // Draw the marker
	  	        canvas.drawRoundRect(locRect, 7, 5, backPaint);
	  	        canvas.drawRoundRect(modeRect, 7, 5, backPaint);
	  	        System.out.println(items.get(i).getSnippet());
	  	        canvas.drawText(items.get(i).getSnippet(), point.x + 25, point.y, paint);
	  	        canvas.drawText("Mode: " + repmode, point.x + 40, point.y - 80, paint);
	  	      }
	      }
	      
	      super.draw(canvas, mapView, shadow);
	    }

	    
	    @Override
	    protected boolean onTap(int index) {
	      OverlayItem item = items.get(index);
	      final String id = item.getTitle();
	      
	      final FrameLayout fl = new FrameLayout(context);
		    final FrameLayout f2 = new FrameLayout(context);
		    final EditText input = new EditText(context);
		    final Spinner spinner = new Spinner(context);
		    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		            this.context, R.array.ringermodes_array, android.R.layout.simple_spinner_item);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    spinner.setAdapter(adapter);

		    input.setGravity(Gravity.CENTER);
		    
		    fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
		      FrameLayout.LayoutParams.WRAP_CONTENT));
		    f2.addView(spinner, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
			        FrameLayout.LayoutParams.WRAP_CONTENT));
            if (!item.getSnippet().equals("")){
            	input.setText(item.getSnippet());
            }
            else{
            	input.setText("Enter location");
            }
		    
		    new AlertDialog.Builder(context)
	        .setView(fl)
	        .setTitle("Please enter the location...")
	        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	          @Override
	          public void onClick(DialogInterface d, int which) {
	            d.dismiss();
	            
	            ContentValues values = new ContentValues();
                values.put(RingerManagerLocations.KEY_LOCATION, input.getText().toString());
            
                System.out.println("Activity On CLick" + values);
                int uri = context.getContentResolver().update(Uri.parse(RingerManagerLocations.LOCATIONS_URI + "/" + id), values, null, null);
                System.out.println("After Activity Insert"); 
	            
	            new AlertDialog.Builder(context)
		        .setView(f2)
		        .setTitle("Please select the mode...")
		        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		          @Override
		          public void onClick(DialogInterface d, int which) {
		            d.dismiss();
		            
		            ContentValues values = new ContentValues();
	                values.put(RingerManagerLocations.KEY_MODE, spinner.getSelectedItem().toString());
	                System.out.println(spinner.getSelectedItem().toString());
	            
	                System.out.println("Activity On CLick" + values);
	                int uri = context.getContentResolver().update(Uri.parse(RingerManagerLocations.LOCATIONS_URI + "/" + id), values, null, null);
	                System.out.println("After Activity Insert"); 
		        
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

	      return true;
	    }
	  
}
