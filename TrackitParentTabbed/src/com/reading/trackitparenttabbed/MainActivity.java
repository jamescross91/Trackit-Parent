package com.reading.trackitparenttabbed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		SharedPreferences auth = getSharedPreferences(
				getString(R.string.authentication), 0);
		boolean authenticated = auth.getBoolean("authenticated", false);

		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, "714729619832");
		} else {
			Log.v("GCM", "Already registered");
		}

		//String reg = GCMRegistrar.getRegistrationId(this);

		if (authenticated) {
			Intent mapIntent = new Intent(this, TabbedActivity.class);
			startActivity(mapIntent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void login(View view) {
		// Intent intent = new Intent(this, ActivityLogin.class);

		EditText editUsername = (EditText) findViewById(R.id.edit_user);
		EditText editPassword = (EditText) findViewById(R.id.edit_pass);

		String username = editUsername.getText().toString();
		String password = editPassword.getText().toString();

		Button loginButton = (Button) findViewById(R.id.button1);
		ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar1);
		
		Login login = new Login(this, username, password, bar, editUsername, editPassword, loginButton);
		String[] array = new String[10];
		
		login.execute(array);
	}
}
