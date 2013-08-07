/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kollode.redminebrowser.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.kollode.redminebrowser.R;
import de.kollode.redminebrowser.provider.RedmineTables;
import de.kollode.redminebrowser.provider.RedmineTables.Issue;
import de.kollode.redminebrowser.provider.RedmineTables.Project;

public class SyncHelper {

	protected static final String sTag = "RedmineSyncHelper";

	private Context mContext;
	private Boolean canceled = false;

	public SyncHelper(Context context) {
		mContext = context;
	}

	void performSync(SyncResult syncResult, Account account) throws IOException {

		NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
		mBuilder.setContentTitle("Projects")
		    .setContentText("Syncing in progress")
		    .setSmallIcon(R.drawable.ic_launcher);

		
		final ContentResolver resolver = mContext.getContentResolver();
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		AccountManager accMgr = AccountManager.get(this.mContext);
		String serverUrl = accMgr.getUserData(account, "serverUrl");
		
		if (isOnline()) {

			final long startRemote = System.currentTimeMillis();
			
			int syncedProjects = 0;
			Log.i(sTag, "Remote syncing speakers");
			Log.i(sTag, serverUrl);
			
			try {

				int offset = 0;
				int numberOfProjects;
				int limit = 100;
				boolean loadMore = true;
							
				do {
					String restQuery = "sort=updated_on:desc&limit=" + limit + "&offset=" + offset + "&key=" + accMgr.getPassword(account);
					
					Log.d(sTag, "REST URL: " + serverUrl + "/projects.json?" + restQuery); 
					JSONObject projectsJson = getJsonFromUrl(serverUrl + "/projects.json?" + restQuery);
					
					numberOfProjects = projectsJson.getInt("total_count");
					mBuilder.setProgress(numberOfProjects, syncedProjects, false);
					mNotificationManager.notify(0, mBuilder.build());
					
					if(numberOfProjects < limit+offset) {
						Log.d(sTag, "Enough Projects");
						loadMore = false;
					}else {
						Log.d(sTag, "More Projects");
						offset += limit;
					}
					
					JSONArray projects = projectsJson.getJSONArray("projects");
					
					for (int i = 0; i < projects.length(); i++) {
					
						JSONObject project = projects.getJSONObject(i);
						Builder projectBuilder = ContentProviderOperation.newInsert(Project.buildProjectsUri(account.name));
						
						Log.d(sTag, project.toString());
						
						 try { 
							 projectBuilder.withValue(Project.PARENT,project.getJSONObject("parent").getInt("id"));
						 }catch(Exception e) {
						 
						 }
						 
						 batch.add(projectBuilder
								 .withValue(BaseColumns._ID, project.getInt("id")) 
								 .withValue(Project.NAME, project.getString("name"))
								 .withValue(Project.IDENTIFIER, project.getString("identifier"))
								 .withValue(Project.UPDATED, System.currentTimeMillis())
								 .withValue(Project.CREATED, System.currentTimeMillis())
								 .withValue(Project.CREATED_ON, project.getString("created_on"))
								 .withValue(Project.UPDATED_ON, project.getString("updated_on")).build());
						 
						mBuilder.setProgress(numberOfProjects, syncedProjects++, false);
						mNotificationManager.notify(0, mBuilder.build());
					}
				}while(loadMore && !this.isCanceled());
					
				try {
					// Apply all queued up batch operations for local data.
					resolver.applyBatch(RedmineTables.CONTENT_AUTHORITY, batch);
				} catch (RemoteException e) {
					throw new RuntimeException("Problem applying batch operation", e);
				} catch (OperationApplicationException e) {
					throw new RuntimeException("Problem applying batch operation", e);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(this.isCanceled()) {
				mBuilder.setContentText("Sync was canceled").setProgress(0,0,false);
			}else {
				mBuilder.setContentText("Sync complete").setProgress(0,0,false);
			}
			mNotificationManager.notify(0, mBuilder.build());

			syncResult.delayUntil = ((long)60*60);
			
			Log.d(sTag, "Remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");
			Log.d(sTag, "Number of projects: " + syncedProjects);
		}
	}

	public static JSONObject getJsonFromUrl(String url) {
		Log.i(sTag, url);
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			Log.i(sTag, response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {
				return new JSONObject(readInputStream(entity.getContent()));
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private static String readInputStream(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String responseLine;
		StringBuilder responseBuilder = new StringBuilder();
		while ((responseLine = bufferedReader.readLine()) != null) {
			responseBuilder.append(responseLine);
		}
		return responseBuilder.toString();
	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

	public Boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(Boolean canceled) {
		this.canceled = canceled;
	}

}
