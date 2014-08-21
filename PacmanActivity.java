package com.pacman.android;
// TEST FUER GIT

import com.airhockey.android.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class PacmanActivity extends Activity {
	
	private GLSurfaceView glSurfaceView;
	private boolean rendererSet = false;
	// TEST
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		glSurfaceView = new GLSurfaceView(this);
		
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
				
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
		
		if (supportsEs2) 
		{
			// Request an OpenGL ES 2.0 compatible context.
			glSurfaceView.setEGLContextClientVersion(2);
			// Assign our renderer.
			glSurfaceView.setRenderer(new PacmanRenderer(this));
			Toast.makeText(this, "This device supports OpenGL ES 2.0.",
					Toast.LENGTH_LONG).show();
			rendererSet = true;
			
		} 
		else 
		{
			Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
			Toast.LENGTH_LONG).show();
			return;
		}
		
		setContentView(glSurfaceView);
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
	
	@Override
	protected void onPause() {
		super.onPause();

		if (rendererSet)
		{
				glSurfaceView.onPause();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (rendererSet)
		{
			glSurfaceView.onResume();
		}
	}
}
