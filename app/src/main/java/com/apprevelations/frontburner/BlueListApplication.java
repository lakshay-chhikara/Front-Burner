
package com.apprevelations.frontburner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.data.IBMData;
import com.ibm.mobile.services.push.IBMPush;
import com.ibm.mobile.services.push.IBMPushNotificationListener;
import com.ibm.mobile.services.push.IBMSimplePushNotification;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class BlueListApplication extends Application {
	public static final int EDIT_ACTIVITY_RC = 1;
	public static IBMPush push = null;
	private Activity mActivity;
	private static final String CLASS_NAME = BlueListApplication.class.getSimpleName();
	private static final String APP_ID = "applicationID";
	private static final String APP_SECRET = "applicationSecret";
	private static final String APP_ROUTE = "applicationRoute";
	private static final String PROPS_FILE = "bluelist.properties";

	private IBMPushNotificationListener notificationListener = null;
	List<Item> itemList;

	public BlueListApplication() {
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity,Bundle savedInstanceState) {
				Log.d(CLASS_NAME, "Activity created: " + activity.getLocalClassName());
				mActivity = activity;
				
				/* Define IBMPushNotificationListener behavior on push notifications. */
				notificationListener = new IBMPushNotificationListener() {
					@Override
					public void onReceive(final IBMSimplePushNotification message) {
						mActivity.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								Class<? extends Activity> actClass = mActivity.getClass();
								if (actClass == MainActivity.class) {
									((MainActivity)mActivity).listItems();
									Log.e(CLASS_NAME, "Notification message received: " + message.toString());

									/* Present the message when sent from Push notification console. */
									if(!message.getAlert().contains("ItemList was updated")){								
										mActivity.runOnUiThread(new Runnable() {
											public void run() {
												new AlertDialog.Builder(mActivity)
										        .setTitle("Push notification received")
										        .setMessage(message.getAlert())
										        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										        	public void onClick(DialogInterface dialog, int whichButton) {
										            }
										         })
										        .show();
											}
										});
										
									}											
								}
							}
						});
					}					
				};
			}
			@Override
			public void onActivityStarted(Activity activity) {
				mActivity = activity;
				Log.d(CLASS_NAME, "Activity started: " + activity.getLocalClassName());
			}
			@Override
			public void onActivityResumed(Activity activity) {
				mActivity = activity;
				Log.d(CLASS_NAME, "Activity resumed: " + activity.getLocalClassName());
				if (push != null) {
					push.listen(notificationListener);
				}
			}
			@Override
			public void onActivitySaveInstanceState(Activity activity,Bundle outState) {
				Log.d(CLASS_NAME, "Activity saved instance state: " + activity.getLocalClassName());
			}
			@Override
			public void onActivityPaused(Activity activity) {
				if (push != null) {
					push.hold();
				}
				Log.d(CLASS_NAME, "Activity paused: " + activity.getLocalClassName());
				if (activity != null && activity.equals(mActivity))
		    		mActivity = null;
			}
			@Override
			public void onActivityStopped(Activity activity) {
				Log.d(CLASS_NAME, "Activity stopped: " + activity.getLocalClassName());
			}
			@Override
			public void onActivityDestroyed(Activity activity) {
				Log.d(CLASS_NAME, "Activity destroyed: " + activity.getLocalClassName());
			}
		});
	}
	
	/**
	 * (non-Javadoc)
	 * Called when the application is starting, before any activity, service, 
	 * or receiver objects (excluding content providers) have been created.
	 * 
	 * @see android.app.Application#onCreate()
	 * 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		itemList = new ArrayList<Item>();
		
		// Read from properties file.
		Properties props = new Properties();
		Context context = getApplicationContext();
		try {
			AssetManager assetManager = context.getAssets();					
			props.load(assetManager.open(PROPS_FILE));
			Log.i(CLASS_NAME, "Found configuration file: " + PROPS_FILE);
		} catch (FileNotFoundException e) {
			Log.e(CLASS_NAME, "The bluelist.properties file was not found.", e);
		} catch (IOException e) {
			Log.e(CLASS_NAME, "The bluelist.properties file could not be read properly.", e);
		}
		Log.i(CLASS_NAME, "Application ID is: " + props.getProperty(APP_ID));
		
		// Initialize the IBM core backend-as-a-service.
		IBMBluemix.initialize(this, props.getProperty(APP_ID), props.getProperty(APP_SECRET), props.getProperty(APP_ROUTE));

        IBMData.initializeService();

        Item.registerSpecialization(Item.class);
	}
	
	/**
	 * Returns the itemList, an array of Item objects.
	 * @return itemList
	 */
	public List<Item> getItemList() {
		return itemList;
	}
}