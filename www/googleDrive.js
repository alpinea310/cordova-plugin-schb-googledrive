function GoogleDrive() {}

GoogleDrive.prototype.downloadFile = function (fileid,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "GoogleDrive", "downloadFile", [fileid]);
};

GoogleDrive.prototype.fileList = function (query ,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "GoogleDrive", "fileList", [query]);
};

// GoogleDrive.prototype.deleteFile = function (fileid,successCallback, errorCallback) {
//     cordova.exec(successCallback, errorCallback, "GoogleDrive", "deleteFile", [fileid]);
// };

GoogleDrive.prototype.requestSync = function(returnFiles,successCallback,errorCallback){
    cordova.exec(successCallback, errorCallback,"GoogleDrive","requestSync",[returnFiles]);
};

GoogleDrive.prototype.createFile = function (newFile,successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "GoogleDrive", "createFile", [newFile]);
};

GoogleDrive.prototype.renameFile = function (newFile,successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "GoogleDrive", "renameFile", [newFile]);
};

GoogleDrive.install = function () {
    if (!window.plugins) {
        window.plugins = {};
    }

    window.plugins.gdrive = new GoogleDrive();
    return window.plugins.gdrive;
};

cordova.addConstructor(GoogleDrive.install);


