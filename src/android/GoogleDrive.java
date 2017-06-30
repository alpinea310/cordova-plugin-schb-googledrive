package ch.schb.cordova.plugin;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.Phaser;

import static android.app.Activity.RESULT_OK;

public class GoogleDrive extends CordovaPlugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleDrive";
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private GoogleApiClient googleApiClient;
    private CallbackContext callbackContext;
    private final Phaser phaser = new Phaser(1);


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(cordova.getActivity())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        Log.i(TAG, "Plugin initialized");
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        Log.i(TAG, " execute " + action);

        if ("downloadFile".equals(action)) {

            String fileId = args.getJSONObject(0).getString("fileId");
            DownloadFile df = new DownloadFile(callbackContext, fileId, this, phaser, googleApiClient);
            cordova.getThreadPool().execute(df);

            return true;

        } else if ("createFile".equals(action)) {

            boolean appFolder = args.getJSONObject(0).getBoolean("appFolder");
            String folderId = args.getJSONObject(0).getString("folderId");
            String title = args.getJSONObject(0).getString("title");
            String contentType = args.getJSONObject(0).getString("contentType");
            String data = args.getJSONObject(0).getString("data");

            CreateFile cf = new CreateFile(callbackContext,appFolder,folderId,title, contentType, data, this, phaser, googleApiClient);
            cordova.getThreadPool().execute(cf);

            return true;

        } else if ("renameFile".equals(action)) {

            String fileId = args.getJSONObject(0).getString("fileId");
            boolean appFolder = args.getJSONObject(0).getBoolean("appFolder");
            String folderId = args.getJSONObject(0).getString("folderId");
            String title = args.getJSONObject(0).getString("title");
            String contentType = args.getJSONObject(0).getString("contentType");

            RenameFile rf = new RenameFile(callbackContext, fileId, appFolder, folderId, title, contentType, this, phaser, googleApiClient);
            cordova.getThreadPool().execute(rf);

            return true;

        } else if ("fileList".equals(action)) {

            boolean appFolder = args.getJSONObject(0).getBoolean("appFolder");
            String title = args.getJSONObject(0).getString("title");
            boolean trashed = args.getJSONObject(0).getBoolean("trashed");
            boolean withContent = args.getJSONObject(0).getBoolean("withContent");

            FileList fl = new FileList(callbackContext, appFolder, title, trashed, withContent, this, phaser, googleApiClient);
            new Thread(fl).start();

            return true;

        } else if ("deleteFile".equals(action)) {

        }
        return true;

    }

    protected boolean connection(Object actionthread) {
        if (googleApiClient.isConnected()) {
            return true;
        } else {
            googleApiClient.connect();
            return false;
        }
    }


    private void requestSync(final boolean listOfFiles) {
        Drive.DriveApi.requestSync(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (!status.isSuccess()) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, status + ""));
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            googleApiClient.connect();
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "user cancelled authorization"));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(cordova.getActivity(), result.getErrorCode(), 0).show();
            return;
        }
        try {
            Log.i(TAG, "trying to resolve issue...");
            cordova.setActivityResultCallback(this);//
            result.startResolutionForResult(cordova.getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        phaser.arriveAndAwaitAdvance();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }


}
