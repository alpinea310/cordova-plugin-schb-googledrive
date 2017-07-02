package ch.schb.cordova.plugin;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by bernd on 28.06.17.
 */

public class CreateFile implements Runnable {
    private static final String TAG = "GoogleDrive.createFile";

    private GoogleDrive googleDrive;
    private CallbackContext callbackContext;
    private GoogleApiClient googleApiClient;


    final boolean appFolder;
    final String folderId;
    final String title;
    final String contentType;
    final String data;

    CreateFile(final CallbackContext callbackContext, boolean appFolder, String folderId, String title, String contentType, String data, GoogleDrive googleDrive,GoogleApiClient googleApiClient) {

        this.callbackContext = callbackContext;
        this.googleDrive = googleDrive;

        this.googleApiClient =googleApiClient;

        this.appFolder = appFolder;
        this.folderId = folderId;
        this.title = title;
        this.contentType = contentType;
        this.data = data;

    }

    public void run() {

        if (googleDrive.connection(this)) {
            createFile();
        } else {
            Log.i(TAG, " Action free continue");
            createFile();
        }

    }

    private void createFile() {
        Log.i(TAG, "createFile");
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                final DriveContents driveContents = result.getDriveContents();


                if (!result.getStatus().isSuccess()) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Failed to create new contents"));
                    return;
                }


                OutputStream outputStream = driveContents.getOutputStream();
                try {
                    outputStream.write(data.getBytes());

                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }


                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType(contentType)
                        .setTitle(title)
                        .build();
                DriveFolder uploadFolder = Drive.DriveApi.getRootFolder(googleApiClient);
                if (appFolder)
                    uploadFolder = Drive.DriveApi.getAppFolder(googleApiClient);

                uploadFolder
                        .createFile(googleApiClient, metadataChangeSet, driveContents)
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult result) {
                                if (result.getStatus().isSuccess()) {
                                    try {

                                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject().put("fileId", result.getDriveFile().getDriveId())));
                                    } catch (JSONException ex) {
                                        Log.i(TAG, ex.getMessage());
                                    }
                                    Log.i(TAG, result.getDriveFile().getDriveId() + "");
                                }
                            }
                        });

            }
        });
    }


}
