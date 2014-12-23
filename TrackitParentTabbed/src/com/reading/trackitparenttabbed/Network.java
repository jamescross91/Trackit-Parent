package com.reading.trackitparenttabbed;

import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public abstract class Network extends AsyncTask<String, Void, String> {
	protected Context thisContext;

	public Network(Context context) {
		thisContext = context;
	}

	// Check if the device is connection to the network - either WiFi or
	// ceulluar (3G/Edge/GPRS)
	protected boolean isConnected() {
		ConnectivityManager connManager = (ConnectivityManager) thisContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}

	public HttpResponse networkExec(String URL, List<NameValuePair> pairs) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HttpResponse response = null;

		if (!isConnected()) {
			result.put("Connection status", false);
			result.put("Error cause", "Unable to connect to the internet");
		}

		HttpClient client = new MyHttpClient(thisContext);
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
		HttpConnectionParams.setSoTimeout(params, 15 * 1000);

		HttpPost post = new HttpPost(URL);
		post.setHeader("User-Agent", "Custom Header");

		try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
			response = client.execute(post);

		} catch (Exception e) {
			Log.e("Login", "Error executing HTTP Request: " + e.toString());
			e.printStackTrace();
		}
		return response;
	}
	
	public HttpResponse networkExec(String URL, JSONObject object) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HttpResponse response = null;

		if (!isConnected()) {
			result.put("Connection status", false);
			result.put("Error cause", "Unable to connect to the internet");
		}

		HttpClient client = new MyHttpClient(thisContext);
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
		HttpConnectionParams.setSoTimeout(params, 15 * 1000);

		HttpPost post = new HttpPost(URL);
		post.setHeader("User-Agent", "Custom Header");

		try {
			StringEntity entity = new StringEntity(object.toString(), "UTF8");
			post.setEntity(entity);
			response = client.execute(post);

		} catch (Exception e) {
			Log.e("Login", "Error executing HTTP Request: " + e.toString());
			e.printStackTrace();
		}
		return response;
	}

	@Override
	protected String doInBackground(String... params) {
		return null;
	}
	
}
