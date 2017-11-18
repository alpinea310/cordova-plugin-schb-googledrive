var argscheck = require('cordova/argscheck');
var exec = require('cordova/exec');

var GoogleDrive = {

  getDateNames: function (successCB, failureCB, options) {
    argscheck.checkArgs('fFO', 'GoogleDrive.getDateNames', arguments);
    exec(successCB, failureCB, "GoogleDrive", "getDateNames", [{"options": options}]);
  },

  downloadFile: function (successCB, failureCB, fileid) {
    argscheck.checkArgs('fFO', 'GoogleDrive.downloadFile', arguments);
    cordova.exec(successCB, failureCB, "GoogleDrive", "downloadFile", [fileid]);
  },

  fileList: function (successCB, failureCB, query) {
    argscheck.checkArgs('fFO', 'GoogleDrive.fileList', arguments);
    cordova.exec(successCB, failureCB, "GoogleDrive", "fileList", [query]);
  },

  deleteFile: function (successCB, failureCB, fileid) {
    argscheck.checkArgs('fFO', 'GoogleDrive.deleteFile', arguments);
    cordova.exec(successCB, failureCB, "GoogleDrive", "deleteFile", [fileid]);
  },

  requestSync: function (successCB, failureCB, returnFiles) {
    argscheck.checkArgs('fFO', 'GoogleDrive.requestSync', arguments);
    cordova.exec(successCB, failureCB, "GoogleDrive", "requestSync", [returnFiles]);
  },

  createFile: function (successCB, failureCB, newFile) {
    argscheck.checkArgs('fFO', 'GoogleDrive.createFile', arguments);
    cordova.exec(successCB, failureCB, "GoogleDrive", "createFile", [newFile]);
  },

  renameFile: function (successCB, failureCB, newFile) {
    argscheck.checkArgs('fFO', 'GoogleDrive.renameFile', arguments);
    cordova.exec(successCB, failureCB, "GoogleDrive", "renameFile", [newFile]);
  }

};

module.exports = GoogleDrive;
