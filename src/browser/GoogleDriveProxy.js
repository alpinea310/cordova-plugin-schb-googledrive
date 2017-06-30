
  var config = {
    'client_id': '88259965068-79s3viahfqe8v5ndg34v4pku3gbv979k.apps.googleusercontent.com',
    'immediate': false,
    'scope': 'https://www.googleapis.com/auth/drive'
  };

  var googleToken = undefined;

  function login() {
    return new Promise(function (resolve, reject) {
      if (googleToken != undefined && googleToken.renew != undefined && new Date().getTime() / 1000 < parseFloat(googleToken.renew)) {
        resolve();
      } else {
        gapi.auth.authorize(config, function (data) {
          if (gapi.auth.getToken()) {
            console.log("found token ");
            googleToken = data.access_token;
            data.renew = parseFloat(new Date().getTime() / 1000) + parseFloat(data.expires_in);
            resolve();
          } else {
            console.log('authorize error' + data.error_subtype);
            reject();
          }
        }, function (reason) {
          console.log("NO TOKEN" + reason);
          reject();
        });
      }
    })
  }

  function fileList(success, error, args) {


    var appFolder = args[0].appFolder ? " 'appDataFolder' in parents" : " not 'appDataFolder' in parents";
    var title = args[0].title ? " title = '" + args[0].title + "'" : "";
    var trashed = "trashed = " + args[0].trashed;

    gapi.client.load('drive', 'v2').then(function () {
      var retrievePageOfFiles = function (request, result) {
        request.execute(function (resp) {

          if (resp.error) {
            console.log("error" + angular.toJson(resp.data));
            error(angular.toJson(resp.data));
          } else {
            var result = {'flist': []};
            for (var i = 0; i < resp.items.length; i++) {
              result.flist.push({
                "title": resp.items[i].title,
                "modifiedTime": resp.items[i].modifiedDate,
                "id": resp.items[i].id,
                "isFolder": resp.items[i].kind != "drive#file",
                "downloadUrl": resp.items[i].downloadUrl

              });

            }
            success(result)
          }
        });
      };
      var query = title + " and " + trashed + " and " + appFolder;
      var initialRequest = gapi.client.drive.files.list({'q': query});
      retrievePageOfFiles(initialRequest, []);
    }, function (reason) {
      console.log("error" + angular.toJson(reason));
      error(angular.toJson(reason))
    });

  }


  function renameFile(success, error, args) {
    var fileId = args[0].fileId;
    var appFolder = args[0].appFolder;
    var folderId = args[0].folderId;
    var title = args[0].title;
    var contentType = args[0].contentType;


    var request = gapi.client.drive.files.patch({'fileId': fileId, 'resource': {'title': title}});

    request.execute(function (resp) {
      if (resp.error) {
        console.log("error" + angular.toJson(resp.data));
        error();
      } else {
        success();
      }
    });
  }

  function createFile(success, error, args) {

    var appFolder = args[0].appFolder;
    var folderId = args[0].folderId;
    var title = args[0].title;
    var contentType = args[0].contentType;
    var data = args[0].data;


    const boundary = '-------314159265358979323846';
    const delimiter = "\r\n--" + boundary + "\r\n";
    const close_delim = "\r\n--" + boundary + "--";

    var contentType = 'application/json';

    var metadata = {'title': title, 'mimeType': contentType};

    var multipartRequestBody = delimiter + 'Content-Type: application/json\r\n\r\n' + JSON.stringify(metadata) + delimiter + 'Content-Type: ' + contentType
      + '\r\n' + 'Content-Transfer-Encoding: chunked\r\n' + '\r\n' + JSON.stringify(data) + close_delim;

    var request = gapi.client.request({
      'path': '/upload/drive/v2/files',
      'method': 'POST',
      'params': {
        'uploadType': 'multipart'
      },
      'headers': {
        'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
      },
      'body': multipartRequestBody
    });


    request.execute(success);

  }

  function downloadFile(success, error, args) {
    var downloadUrl = args[0].downloadUrl;
    var fileId = args[0].fileId;
    var xhr = new XMLHttpRequest();
    xhr.open('GET', downloadUrl);
    xhr.setRequestHeader('Authorization', 'Bearer ' + googleToken);
    xhr.googleId = fileId;
    xhr.onload = function (data) {
      var result = data.currentTarget.responseText;
      success(result);
    };
    xhr.onerror = function (evt) {
      error(evt);
    };
    xhr.send();

  }

  exports.downloadFile = function (success, error, args) {
    login().then(
      function () {
        downloadFile(success, error, args)
      }
    );
  }


  exports.createFile = function (success, error, args) {
    login().then(
      function () {
        createFile(success, error, args)
      }
    );
  }

  exports.renameFile = function (success, error, args) {
    login().then(
      function () {
        renameFile(success, error, args)
      }
    );
  }

  exports.fileList = function (success, error, args) {
    login().then(
      function () {
        fileList(success, error, args);
      }
    );
  };

  require('cordova/exec/proxy').add('GoogleDrive', exports);


