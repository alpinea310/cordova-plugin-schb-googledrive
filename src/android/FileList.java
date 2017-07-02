package ch.schb.cordova.plugin;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Phaser;

/**
 * Created by bernd on 28.06.17.
 */

public class FileList implements Runnable {
    private static final String TAG = "GoogleDrive.fileList";

    private GoogleDrive googleDrive;
    private CallbackContext callbackContext;
    private GoogleApiClient googleApiClient;

    private boolean appFolder;
    private String title;
    private boolean trashed;
    private boolean withContent;


    FileList(final CallbackContext callbackContext, boolean appFolder, String title, boolean trashed, boolean withContent, GoogleDrive googleDrive,GoogleApiClient googleApiClient) {
        this.callbackContext = callbackContext;
        this.googleDrive = googleDrive;
        this.googleApiClient =googleApiClient;
        this.appFolder = appFolder;
        this.title = title;
        this.trashed = trashed;
        this.withContent = withContent;

    }

    public void run() {
        if (googleDrive.connection(this)) {
            fileList();
        } else {
            Log.i(TAG, " Action free continue");
            fileList();
        }
    }

    private void fileList() {
        Log.i(TAG, "fileList() ");

        Drive.DriveApi.requestSync(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.i(TAG, "fileList > requestSync ");
                if (status.isSuccess()) {
                    Query.Builder qb = new Query.Builder();
                    qb.addFilter(Filters.and(
                            Filters.eq(SearchableField.TITLE, title),
                            Filters.eq(SearchableField.TRASHED, trashed)));

                    if (appFolder) {
                        DriveId appFolderId = Drive.DriveApi.getAppFolder(googleApiClient).getDriveId();
                        qb.addFilter(Filters.in(SearchableField.PARENTS, appFolderId));
                    }

                    Query query = qb.build();

                    Drive.DriveApi.query(googleApiClient, query)
                            .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                                @Override
                                public void onResult(DriveApi.MetadataBufferResult result) {
                                    if (!result.getStatus().isSuccess()) {
                                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "failed to retrieve file list"));
                                        return;
                                    }
                                    MetadataBuffer flist = result.getMetadataBuffer();
                                    JSONArray response = new JSONArray();
                                    for (Metadata file : flist
                                            ) {
                                        try {
                                            response.put(new JSONObject().put("title", file.getTitle())
                                                    .put("modifiedTime", file.getCreatedDate().toString())
                                                    .put("id", file.getDriveId())
                                                    .put("isFolder", file.isFolder())
                                            );
                                            if (withContent && !file.isFolder()) {
                                                DriveFile driveFile = file.getDriveId().asDriveFile();
                                            }

                                        } catch (JSONException ex) {
                                        }
                                    }
                                    JSONObject flistJSON = new JSONObject();
                                    try {
                                        flistJSON.put("flist", response);
                                    } catch (JSONException ex) {
                                    }
                                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, flistJSON));
                                    flist.release();
                                    return;
                                }
                            });
                } else {
                    Log.i(TAG, "fileList > requestSync No success" + status.getStatusMessage());
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, status.getStatusMessage()));
                    return;
                }
            }
        });
    }
}
