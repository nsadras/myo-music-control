package com.nitinsadras.myomusiccontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.scanner.ScanActivity;


public class MainActivity extends Activity {

	private DeviceListener mListener = new AbstractDeviceListener() {
		boolean volumeMode = false;
		@Override
	    public void onConnect(Myo myo, long timestamp) {
			Log.d("pose", "connected");
	    }

	    @Override
	    public void onDisconnect(Myo myo, long timestamp) {
	    	Log.d("pose", "disconnected");
	    }

	    @Override
	    public void onPose(Myo myo, long timestamp, Pose pose) {
	        Log.i("pose", "Pose: " + pose);
	        boolean sleep = true;

	        switch (pose) {
            case UNKNOWN:
                break;
            case FINGERS_SPREAD:
            	volumeMode = true;
            	sleep = false;
                break;
            case REST:
            	volumeMode = false;
            	break;
            case THUMB_TO_PINKY:
            	if(volumeMode){
            		volumeMode = false;
            	} else {
            		playPauseMusic();
            	}
            	break;
            case FIST:
            	if(volumeMode){
            		volumeMode = false;
            	} else {
            		playPauseMusic();
            	}
            	break;
            case WAVE_IN:
            	back();
            	volumeMode = false;
                break;
            case WAVE_OUT:
            	skip();
            	volumeMode = false;
                break;
	        }
	        try {
	        	if(sleep){
	        		Thread.sleep(500);
	        	}
			} catch (InterruptedException e) {
				Log.d("sleep", "sleep failed wtf");
				// nothing to see here, move along
			}
	    }
	    
	    @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
	    	float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
            
            if(volumeMode){
            	Log.i("rotation", Float.toString(roll));
            	AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            	int volume = Math.min((int)((roll + 60)*(10.0/80.0)), 10);
            	Log.i("volume", Integer.toString(volume));
            	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0); // 15 max, cap at 10/11
            	//-60 to 20
            }
	    }
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Log.e("test", "Could not initialize the Hub.");
            finish();
            return;
        } 
        
    	hub.addListener(mListener);
        
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void playPauseMusic(){
    	Context ctx = getApplicationContext();
        AudioManager mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        Intent mediaIntent = new Intent("com.android.music.musicservicecommand");
        if (mAudioManager.isMusicActive()) {
            mediaIntent.putExtra("command", "pause");
        } else {
        	mediaIntent.putExtra("command", "play");
        }
        ctx.sendBroadcast(mediaIntent);

    }

    
    public void skip(){
    	Context ctx = getApplicationContext();
        AudioManager mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);    
        if (mAudioManager.isMusicActive()){
        	Intent mediaIntent = new Intent("com.android.music.musicservicecommand");
        	mediaIntent.putExtra("command", "next");
        	sendBroadcast(mediaIntent);
        }
    }
    
    public void back(){
    	Context ctx = getApplicationContext();
        AudioManager mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);  
        if(mAudioManager.isMusicActive()){
        	Intent mediaIntent = new Intent("com.android.music.musicservicecommand");
        	mediaIntent.putExtra("command", "previous");
        	sendBroadcast(mediaIntent);
        }
    }
    
    
    
    public void connect(View view){
        Context context = getApplicationContext();
        Intent intent = new Intent(context, ScanActivity.class);
        startActivity(intent);
        Log.d("test",  "done with scan activity");
    }
}
