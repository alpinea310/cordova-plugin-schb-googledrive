package ch.schb.cordova.plugin;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by bernd on 28.06.17.
 */

public class DownloadFile implements Runnable {
  private static final String TAG = "GoogleDrive.downloadFil";

  private GoogleDrive googleDrive;
  private CallbackContext callbackContext;
  private GoogleApiClient googleApiClient;

  final String fileId;


  DownloadFile(final CallbackContext callbackContext, String fileId, GoogleDrive googleDrive, GoogleApiClient googleApiClient) {
    this.callbackContext = callbackContext;
    this.googleDrive = googleDrive;
    this.googleApiClient = googleApiClient;

    this.fileId = fileId;
  }

  public void run() {

    if (googleDrive.connection(this)) {
      downloadFile();
    } else {
      Log.i(TAG, " Action free continue");
      downloadFile();
    }

  }


  private void downloadFile() {
    Log.i(TAG, "download() " + fileId);

    DriveFile.DownloadProgressListener listener = new DriveFile.DownloadProgressListener() {
      @Override
      public void onProgress(long bytesDownloaded, long bytesExpected) {
        int progress = (int) (bytesDownloaded * 100 / bytesExpected);
        Log.d(TAG, String.format("Loading progress: %d percent", progress));

      }
    };

    final DriveFile file = DriveId.decodeFromString(fileId).asDriveFile();
    file.open(googleApiClient, DriveFile.MODE_READ_ONLY, listener)
      .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull final DriveApi.DriveContentsResult result) {
          final DriveContents driveContents = result.getDriveContents();

          if (!result.getStatus().isSuccess()) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Something went wrong with file download"));
            return;
          }

          try {

            InputStream inputStream = driveContents.getInputStream();
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            String dataString = new String(data);

            try {
              JSONObject result1 = new JSONObject(dataString.toString());
              callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result1));
            } catch (JSONException ex) {
              Log.i(TAG, ex.getMessage());
            }


          } catch (IOException e) {
            Log.e(TAG, e.getMessage());
          }

        }
      });
  }
}
