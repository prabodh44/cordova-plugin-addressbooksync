/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.procit.procitcontacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.CordovaActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class MainActivity extends CordovaActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkAndRequestPermissions();
		// enable Cordova apps to be started in the background
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
			moveTaskToBack(true);
		}

		// Set by <content src="index.html" /> in config.xml
		loadUrl(launchUrl);
	}

	final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 5;

	private boolean checkAndRequestPermissions() {

		try {
			int targetSdkVersion = 0;

			final PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			targetSdkVersion = info.applicationInfo.targetSdkVersion;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (targetSdkVersion >= Build.VERSION_CODES.M) {
					int permissionWriteStorage = ContextCompat.checkSelfPermission(this,
							Manifest.permission.WRITE_EXTERNAL_STORAGE);
					int locationPermission = ContextCompat.checkSelfPermission(this,
							Manifest.permission.ACCESS_FINE_LOCATION);
					int phoneStatePermission = ContextCompat.checkSelfPermission(this,
							Manifest.permission.READ_PHONE_STATE);

					List<String> listPermissionsNeeded = new ArrayList<String>();
					if (locationPermission != PackageManager.PERMISSION_GRANTED) {
						listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
					}
					if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
						listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
					}

					if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
						listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
					}
					if (!listPermissionsNeeded.isEmpty()) {
						ActivityCompat.requestPermissions(this,
								listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
								REQUEST_ID_MULTIPLE_PERMISSIONS);
						return false;
					}
					return true;
				}
			}

		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
		case REQUEST_ID_MULTIPLE_PERMISSIONS: {

			Map<String, Integer> perms = new HashMap<String, Integer>();
			// Initialize the map with permissions
			perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
			perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
			perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
			// Fill with actual results from user
			if (grantResults.length > 0) {
				for (int i = 0; i < permissions.length; i++) {
					perms.put(permissions[i], grantResults[i]);
				}
				// Check for both permissions
				if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
						&& perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
						&& perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "all permissions granted");
					// process the normal flow
					// else any one or both the permissions are not granted
				} else {
					Log.d(TAG, "Some permissions are not granted ask again ");
					// permission is denied (this is the first time, when "never
					// ask again" is not checked) so ask again explaining the
					// usage of permission
					// // shouldShowRequestPermissionRationale will return true
					// show the dialog or snackbar saying its necessary and try
					// again otherwise proceed with setup.
					if (ActivityCompat.shouldShowRequestPermissionRationale(this,
							Manifest.permission.WRITE_EXTERNAL_STORAGE)
							|| ActivityCompat.shouldShowRequestPermissionRationale(this,
									Manifest.permission.ACCESS_FINE_LOCATION)) {
						showDialogOK("All permissions are not provided for the app",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:
											checkAndRequestPermissions();
											break;
										case DialogInterface.BUTTON_NEGATIVE:
											// proceed with logic by disabling
											// the
											// related features or quit the app.
											break;
										}
									}
								});
					}
					// permission is denied (and never ask again is checked)
					// shouldShowRequestPermissionRationale will return false
					else {
						// Toast.makeText(this, "Go to settings and enable
						// permissions", Toast.LENGTH_LONG).show();
						// //proceed with logic by disabling the related
						// features or quit the app.
					}
				}
			}
		}
		}

	}

	private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", okListener)
				.setNegativeButton("Cancel", okListener).create().show();
	}
}
