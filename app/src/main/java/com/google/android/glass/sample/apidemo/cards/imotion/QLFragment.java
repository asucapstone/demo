package com.google.android.glass.sample.apidemo.cards.imotion;

/**
 * Created by Hector on 10/10/15.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
// import java.util.Iterator;
import java.util.Timer;

// import com.eyetech.eyetechusblib.eyetechMessages;
import com.eyetechds.quicklink.*;

import android.app.Activity;
// import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
// import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
// import android.hardware.usb.UsbDevice;
// import android.hardware.usb.UsbManager;
// import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class QLFragment extends Fragment
{
    public QLFragment()
    {
        super();
    }
    public static int               tabBarHeightPx          = 0;
    public static int               statusBarHeightPx       = 0;
    public static int               calibratedHeight        = 0;
    protected String				logTag					= " Fragment";
    public static QLDevice			qlDevice				= null;

    public static QLSettings		qlSettings				= new QLSettings();
    public static QLCalibration		loadedCalibration		= new QLCalibration();
    public static QLCalibrationType	calibrationType			= new QLCalibrationType();
    // TODO: Switch this to a QLDeviceGetStatus call every time we switch fragments
    public static boolean			isStarted				= false;
    public static boolean			isCalibrated			= false;
    public static Bitmap			scoresBitmap			= null;
    protected boolean				visible					= false;
    protected String				objectName;
    // When the android app is first launching it can't enumerate the camera
    // from the attached
    // device immediately for some reason. For now, there is an added delay.
    private static Timer			timer					= new Timer();
    static int						failureCount			= 0;
    boolean							noEyeTrackerFoundPosted	= false;
    private final static int				initialScaleFactor		= 5;

    protected static Handler			deviceMessageHandler	= new DeviceMessageHandler();

    private static class DeviceMessageHandler extends Handler
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case QLDeviceMessage.STATUS:
                    QLDevice device = (QLDevice) msg.obj;
                    QLDeviceStatus deviceStatus = device.getStatus();

                    Log.d("DeviceMessage - " + device, "Device Status = " + deviceStatus);

                    switch(deviceStatus.get())
                    {
                        case QLDeviceStatus.INITIALIZED:
                            // Set the initial scale factor
                            qlDevice = device;

                            QLDouble value = new QLDouble();
                            String scaleFactorSetting = "DeviceDownsampleScaleFactor";
                            qlSettings.add(scaleFactorSetting);
                            value.set(initialScaleFactor);
                            qlSettings.setValue(scaleFactorSetting, value);

//					qlSettings.setValue("DeviceDistance", 30);
//					qlSettings.setValue("DeviceLensFocalLength", 1.2);
                            qlDevice.importSettings(qlSettings);

                            isStarted = false;
                            device.start();
                            break;

                        case QLDeviceStatus.STARTED:
                            Log.d("QL2 ", "Device Started");
                            isStarted = true;
                            break;

                        case QLDeviceStatus.STOPPED:
                            Log.d("QL2 ", "Device Stopped");
                            isStarted = false;
                            break;

                        case QLDeviceStatus.UNAVAILABLE:
                            Log.d("QL2 ", "Device Unavailable");
                            isStarted = false;
                            qlDevice = null;
                            break;
                    }

                    break;
            }
        }
    }

    public void myLog(String str)
    {
        Log.d("QL2 " + objectName, str);
    }

    public void onStart()
    {
        super.onStart();
        visible = true;
        Log.d(objectName + logTag, "onStart");

        // if(timer != null)
        // {
        // timer.cancel();
        // timer = null;
        // }
        // timer = new Timer();
//		if(timer != null && !isStarted)
//			timer.schedule(timerTask, 1000, 1000);
    }

    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        Log.d(objectName + logTag, "onAttach");
        if(qlDevice == null)
        {
            Log.d(objectName + logTag, "started QL instance!");
            // Fragment.getActivity() is null before onAttach!
            QLDevice.enumerate(activity.getApplicationContext(),
                    deviceMessageHandler, false);

            // it typically takes a second or two for the api to get permission
            // to access the device, use a timer task to do it, or start a scheduled
            // timer task in onStart().

            // timer.scheduleAtFixedRate(timerTask, 1000, 1000);
        }
    }

//	TimerTask	timerTask	= new TimerTask()
//							{
//								public void run()
//								{
//									tryToResumeEyeTracking();
//								}
//							};

//	public void tryToResumeEyeTracking()
//	{
//		// if we are already started, get out of this function
//		if(isStarted)
//		{
//			return;
//		}
//
//		// enumeration has already happened, so don't bother with this function.
//		// The Android OS will auto load the application if it is found
//		if(qlDevice == null)
//		{
//			Log.e("QL2", "THIS SHOULD NEVER HAPPEN!");
//			if(getActivity() == null)
//			{
//				Log.w("QL2", "GetActivity was null!");
//				return;
//			}
//			if(getActivity().getApplicationContext() == null)
//			{
//				Log.w("QL2", "GetApplicationContext was null!");
//				return;
//			}
//
//			QLDevice[] qlDevices = QLDevice.enumerate(getActivity().getApplicationContext(),
//					deviceMessageHandler);
//
//			// Enumerate the devices
//			if(qlDevices != null)
//			{
//				qlDevice = qlDevices[0];
//			}
//			else
//			{
//				// the no device detected
//				String resultStr = "Error: no Device Found!";
//				myLog("No devices available!");
//				myLog(resultStr);
//				// if we are unable to start, then we need to try
//				// momentarily
//				failureCount++;
//				if(failureCount >= 3 && visible && !noEyeTrackerFoundPosted)
//				{
//					// Toast.makeText(getActivity(),
//					// "No eye tracker available",
//					// Toast.LENGTH_LONG).show();
//					getActivity().runOnUiThread(new Runnable()
//					{
//						public void run()
//						{
//							Toast.makeText(getActivity(), "No eye tracker available",
//									Toast.LENGTH_LONG).show();
//						}
//					});
//					noEyeTrackerFoundPosted = true;
//				}
//				return;
//			}
//		}
//
//		// if the timer is on a repeated schedule trying to find a device, cancel it.
//		if(timer != null)
//		{
//			timer.cancel();
//			timer = null;
//			timer = new Timer();
//		}
//
//		// ok we have a device
//		// first get the version and show it on the log
//		String version = QLApi.getVersion();
//
//		myLog("QLAPI_GetVersion, version = " + version);
//
//		// get the device information
//		QLDeviceInfo info = qlDevice.getInfo();
//		String text;
//		if(info == null)
//		{
//			text = "Error QLDevice_GetInfo return = " + qlDevice.getLastError();
//			myLog(text);
//		}
//		else
//		{
//			text = "QLDevice_GetInfo modelName = " + info.modelName;
//			myLog(text);
//			text = "QLDevice_GetInfo serialNumber = " + info.serialNumber;
//			myLog(text);
//		}
//
//		// now really start the device
//		if(!isStarted)
//		{
//			// Set the initial scale factor
////			QLDouble value = new QLDouble();
////			String scaleFactorSetting = "DeviceDownsampleScaleFactor";
////			qlSettings.add(scaleFactorSetting);
////			value.set(initialScaleFactor);
////			qlSettings.setValue(scaleFactorSetting, value);
////			qlDevice.importSettings(qlSettings);
//
//			// boolean started = qlDevice.start();
//			// if (!started)
//			// {
//			// text = "Error QLDevice_Start return = "
//			// + qlDevice.getLastError();
//			// myLog(text);
//			// return;
//			// }
//			// else
//			// {
//			// text = "QLDevice_Start";
//			// myLog(text);
//			// }
//			// isStarted = true;
//			failureCount = 0;
//
////			loadCalibration();
//
//			// UpdateDeviceSettings();
//		}
//
//	}

    // Restart only applies to activities, not fragments
    // protected void onRestart()
    // {
    // // super.onRestart();
    // // visible = true;
    // // Log.d("State", "onRestart");
    // }

    private void UpdateDeviceSettings()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        Map<String, ?> prefList = sharedPreferences.getAll();

        // push the setting onto the device
        QLSettings qlSettings = new QLSettings();

        for(Map.Entry<String, ?> entry : prefList.entrySet())
        {
            String key = entry.getKey();
            String value = sharedPreferences.getString(key, null);

            Log.w("QL2", key + ":" + value);

            // convert the lowercase underscore preference key to the QuickLink Settings name
            String settingName = "Device";
            String[] strings = key.split("_");
            for(String str : strings)
            {
                settingName += str.substring(0, 1).toUpperCase() + str.substring(1);
            }
        }

        if(QLFragment.isStarted)
            QLFragment.qlDevice.importSettings(qlSettings);
        else
        {
            Toast.makeText(getActivity(), "QLFragment not started.", Toast.LENGTH_LONG).show();
        }
    }

    public void saveCalibration(QLCalibration cal)
    {
        saveCalibration("QLCalibration_Default_User.cal", cal);
    }

    public void saveCalibration(String filename, QLCalibration cal)
    {
        // save it to disk
        FileOutputStream fOut;
        try
        {
            fOut = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            int bytesSaved = cal.save(fOut.getChannel());
            if(bytesSaved == 0)
            {
                myLog("Calibration_Save failed " + cal.getLastError());
            }
            fOut.close();
        }
        catch(FileNotFoundException e)
        {
            Log.w("QL2", "File Not Found?!?!?");
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void loadCalibration()
    {
        loadCalibration("QLCalibration_Default_User.cal");
    }

    public void loadCalibration(String filename)
    {
        // save it to disk
        FileInputStream fIn;
        try
        {
            fIn = getActivity().openFileInput(filename);
            int bytesRead = loadedCalibration.load(fIn.getChannel());
            if(bytesRead == 0)
            {
                myLog("QLCalibrationLoad failed " + loadedCalibration.getLastError());
                myLog("" + fIn.getChannel().size() + " " + fIn.getChannel().position());
            }
            fIn.close();
        }
        catch(FileNotFoundException e)
        {
            // e.printStackTrace();
            // There is no previously saved data available!
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        boolean appliedCalibration = qlDevice.applyCalibration(loadedCalibration);
        if(!appliedCalibration)
        {
            myLog("ApplyCalibration failed " + qlDevice.getLastError());
        }
        else
        {
            isCalibrated = true;
        }
    }

    public void saveSettings()
    {
        saveSettings("QLSettings_Default_User.txt", qlSettings);
    }

    public void saveSettings(String filename, QLSettings settings)
    {

        boolean exported = qlDevice.exportSettings(settings);
        if(!exported)
        {
            myLog("QLDevice ExportSettings failed: " + qlDevice.getLastError());
            return;
        }

        // save it to disk
        FileOutputStream fOut;
        try
        {
            fOut = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            int bytesSaved = settings.save(fOut.getChannel());
            if(bytesSaved == 0)
            {
                myLog("Settings_Save failed ");
            }
            fOut.close();
        }
        catch(FileNotFoundException e)
        {
            Log.w("QL2", "File Not Found?!?!?");
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void loadSettings()
    {
        loadSettings("QLSettings_Default_User.txt", qlSettings);
    }

    public void loadSettings(String filename, QLSettings settings)
    {
        // save it to disk
        FileInputStream fIn;
        try
        {
            fIn = getActivity().openFileInput(filename);
            int bytesRead = settings.load(fIn.getChannel());
            if(bytesRead == 0)
            {
                myLog("QLSettingsLoad failed ");
                myLog("" + fIn.getChannel().size() + " " + fIn.getChannel().position());
            }
            fIn.close();
        }
        catch(FileNotFoundException e)
        {
            // e.printStackTrace();
            // There is no previously saved data available!
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        boolean imported = qlDevice.importSettings(settings);
        if(!imported)
        {
            myLog("QLDevice ImportSettings failed: " + qlDevice.getLastError());
            return;
        }
    }

    public void onResume()
    {
        super.onResume();
        visible = true;
        Log.d(objectName + logTag, "onResume");

        // in case we came back from being minimized, restart the image

    }

    public void onPause()
    {
        super.onPause();
        visible = false;
        Log.d(objectName + logTag, "onPause");
    }

    public void onStop()
    {
        // There is no need to continue trying to start a device.
        if(timer != null)
        {
            timer.cancel();
            timer = null;
            timer = new Timer();
        }
        super.onStop();
        visible = false;
        Log.d(objectName + logTag, "onStop");
    }

    public void onDestroy()
    {
        super.onDestroy();
        visible = false;
        Log.d(objectName + logTag, "onDestroy");
    }
}
