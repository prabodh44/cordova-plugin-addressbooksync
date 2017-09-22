package cordova.plugin.addressbooksync;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class AddressBookSync extends CordovaPlugin {
	public static final String ACTION_SYNC = "sync";
	public static final String ACTION_SENDSMS = "SendSMS";

	public static final int INTENT_CHOOSE_GOOGLE_ACCOUNT = 1000;
	public static final int INTENT_GOOGLE_API_AUTH = 2000;
	CallbackContext mCallbackContext;
	String accountName = null;

	private Drive objDrive;

	@Override
	public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {

		mCallbackContext = callbackContext;

		if (action.equals(ACTION_SYNC)) {

			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {

					try {
						JSONObject arg_object = args.getJSONObject(0);
						final String actionParam = arg_object.getString("message");

						// if (accountName == null ||
						// accountName.equalsIgnoreCase("")) {
						Intent intent = AccountPicker.newChooseAccountIntent(null, null,
								new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null);
						cordova.setActivityResultCallback(AddressBookSync.this);
						cordova.getActivity().startActivityForResult(intent, INTENT_CHOOSE_GOOGLE_ACCOUNT);

						// }

					} catch (Exception ex) {
						ex.printStackTrace();
					}

					PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
					pluginResult.setKeepCallback(true);
					mCallbackContext.sendPluginResult(pluginResult);
					return;

				}
			});

			return true;
		} else if (action.equals(ACTION_SENDSMS)) {
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {

					try {
						JSONObject arg_object = args.getJSONObject(0);
						final String actionParam = arg_object.getString("message");

						// String number = "12346556"; // The number on which
						// you want to send SMS
						cordova.getActivity()
								.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", actionParam, null)));

					} catch (Exception ex) {
						ex.printStackTrace();
					}

					PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
					pluginResult.setKeepCallback(true);
					mCallbackContext.sendPluginResult(pluginResult);
					return;

				}
			});

		}

		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == INTENT_CHOOSE_GOOGLE_ACCOUNT) {
			if (data != null) {
				String account = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

				if (account != null) {
					accountName = account;
					new GoogleDriveAsync().execute();
				}
			}

		} else if (requestCode == INTENT_GOOGLE_API_AUTH && resultCode == Activity.RESULT_OK) {
			new GoogleDriveAsync().execute();
		} else if (resultCode == Activity.RESULT_CANCELED) {

			String errorMsg = "{\"status\":\"error\",\"message\":\"" + "Account not seleced" + "\"}";
			PluginResult result = new PluginResult(PluginResult.Status.ERROR, errorMsg);
			result.setKeepCallback(false);
			mCallbackContext.sendPluginResult(result);
		}
	}

	private class GoogleDriveAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			getDriveService();
			if (objDrive != null) {
				String fileContent = retriveDriveFiles();
				return fileContent;
			
			}
			return "{\"status\":\"error\",\"message\":\"" + "Google drive general error" + "\"}";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (objDrive != null) {
				
				JSONObject resultJson = null;
				try{
					resultJson = new JSONObject(result);
					
					JSONObject driveDetails = (JSONObject)resultJson.get("driveDetails");
					JSONObject  fileContent = (JSONObject)driveDetails.get("fileContent");
					JSONArray contacts = (JSONArray)fileContent.getJSONArray("contacts");
					for (int i = 0; i<contacts.length(); i++){
						JSONObject each = (JSONObject)contacts.get(i);
						each.put("id", i+1);
					}
				}
				catch(Exception ex){
					try{
					resultJson = new JSONObject("{\"status\":\"error\",\"message\":\"" + "Google drive general error" + "\"}");
					}catch(Exception ex1){
						ex1.printStackTrace();
					}
				}
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultJson);
				pluginResult.setKeepCallback(false);
				mCallbackContext.sendPluginResult(pluginResult);
			}
		}
	}

	public void getDriveService() {

		try {
			GoogleAccountCredential credential = GoogleAccountCredential
					.usingOAuth2(cordova.getActivity().getApplicationContext(), DriveScopes.DRIVE);

			if (accountName != null) {
				credential.setSelectedAccountName(accountName);
				try {
					credential.getToken();
					objDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
							.build();
				} catch (UserRecoverableAuthException uraEx) {
					objDrive = null;
					Intent authorizationIntent = uraEx.getIntent();
					cordova.getActivity().startActivityForResult(authorizationIntent, INTENT_GOOGLE_API_AUTH);
				}
			}
		} catch (Exception e) {
      Log.e("ProcitAddressBook", "Check if android version is less and your keystore is resigeted with google");
			objDrive = null;
			e.printStackTrace();

		}

	}

	private String retriveDriveFiles() {
		String result = null;

		String driveFileID = "";
		String role = "";

		try {
			Files.List request = objDrive.files().list()
					.setQ("title contains 'ProcitAddressBookData' and sharedWithMe");
			Map<String, File> textFiles = new HashMap<String, File>();

			do {
				try {
					FileList files = request.execute();

					for (File file : files.getItems()) {
						textFiles.put(file.getId(), file);
						driveFileID = file.getId();
						Permission permission = file.getUserPermission();
						role = permission.getRole();
					}
					request.setPageToken(files.getNextPageToken());
				} catch (Exception e) {
					System.out.println("An error occurred: " + e);
					request.setPageToken(null);

				}
			} while (request.getPageToken() != null && request.getPageToken().length() > 0);

			for (File driveFile : textFiles.values()) {
				if (driveFile != null) {
					result = getFileContent(driveFile, driveFileID, role);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status", "error");
				jsonObj.put("message", ex.getMessage());
				result = jsonObj.toString();
			} catch (Exception ex2) {
				ex2.printStackTrace();
				result = "{\"status\":\"error\",\"message\":\"" + ex.getMessage() + "\"}";
			}
		}
		return result;
	}

	public String getFileContent(File driveFile, String driveFileID, String role) throws Exception {
		String result = null;
		InputStream inputStream = null;

		if (driveFile.getDownloadUrl() != null && driveFile.getDownloadUrl().length() > 0) {
			try {
				GenericUrl downloadUrl = new GenericUrl(driveFile.getDownloadUrl());
				HttpResponse resp = objDrive.getRequestFactory().buildGetRequest(downloadUrl).execute();

				inputStream = resp.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder content = new StringBuilder();
				char[] buffer = new char[1024];
				int num;

				while ((num = reader.read(buffer)) > 0) {
					content.append(buffer, 0, num);
				}
				String fileContentTemp = content.toString();

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status", "success");

				JSONObject jsonDrive = new JSONObject();
				jsonDrive.put("fileId", driveFileID);
				jsonDrive.put("userRole", role);
				jsonDrive.put("fileContent", new JSONObject(fileContentTemp));

				jsonObj.put("driveDetails", jsonDrive);

				result = jsonObj.toString();

			} catch (Exception ex) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception ex1) {
						ex.printStackTrace();
					}
				}
				throw ex;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		return result;
	}
	
	

}
