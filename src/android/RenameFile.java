package ch.schb.cordova.plugin;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;



/**
 * Created by bernd on 28.06.17.
 */

public class RenameFile implements Runnable {
    private static final String TAG = "GoogleDrive.renameFile";

    private GoogleDrive googleDrive;
    private CallbackContext callbackContext;
    private GoogleApiClient googleApiClient;

    final String fileId;
    final boolean appFolder;
    final String folderId;
    final String title;
    final String contentType;

    RenameFile(final CallbackContext callbackContext, String fileId, boolean appFolder, String folderId, String title, String contentType, GoogleDrive googleDrive,GoogleApiClient googleApiClient) {
        this.callbackContext = callbackContext;
        this.googleDrive = googleDrive;
        this.googleApiClient =googleApiClient;

        this.fileId = fileId;
        this.appFolder = appFolder;
        this.folderId = folderId;
        this.title = title;
        this.contentType = contentType;
    }

    public void run() {

        if (googleDrive.connection(this)) {
            renameFile();
        } else {
            Log.i(TAG, " Action free continue");
            renameFile();
        }

    }


    private void renameFile() {
        Log.i(TAG, "renameFile() "+fileId);
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                final DriveContents driveContents = result.getDriveContents();

                if (!result.getStatus().isSuccess()) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Failed to rename"));
                    return;
                }

                DriveId driveId = DriveId.decodeFromString(fileId);

                driveId.asDriveFile().getMetadata(googleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {

                    @Override
                    public void onResult(@NonNull DriveResource.MetadataResult result) {
                        if (!result.getStatus().isSuccess()) {
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Something went wrong to rename"));
                            return;
                        }
                        final Metadata metadata = result.getMetadata();

                        DriveFile f = metadata.getDriveId().asDriveFile();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(title).build();
                        f.updateMetadata(googleApiClient, changeSet);

                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
                    }
                });
            }
        });
    }

}
