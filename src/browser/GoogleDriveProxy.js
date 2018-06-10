cordova.define("cordova-plugin-schb-googledrive.GoogleDriveProxy", function (require, exports, module) {


    var googleToken = undefined;
    var renew = undefined;

    function login(action, success, fail, args) {
        setTimeout(function () {
            if (googleToken != undefined && renew != undefined && new Date().getTime() / 1000 < parseFloat(renew)) {
                action(success, fail, args);
            } else {
                gapi.auth.authorize(GOOGLE_ACCOUNT, function (data) {
                    if (gapi.auth.getToken()) {
                        console.log("found token");
                        googleToken = data.access_token;
                        renew = parseFloat(new Date().getTime() / 1000) + parseFloat(data.expires_in);
                        action(success, fail, args);
                    } else {
                        console.log("error_authorize");
                        alert('error_authorize' + data.error);
                        fail();
                    }
                }, function (resp) {
                    console.log("error_on_token" + angular.toJson(resp.data));
                    alert("error_on_token" + reason);
                    fail();
                });
            }
        }, 100);
    }

    function fileList(success, fail, args) {

        var appFolder = args[0].appFolder ? " 'appDataFolder' in parents" : " not 'appDataFolder' in parents";
        var title = args[0].title ? " title = '" + args[0].title + "'" : "";
        var trashed = "trashed = " + args[0].trashed;

        gapi.client.load('drive', 'v2').then(function () {
            var retrievePageOfFiles = function (request, result) {
                request.execute(function (resp) {

                    if (resp.error) {
                        alert("error_filelist" + resp.data);
                        fail();
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
        }, function (resp) {
            console.log("error_filelist " + angular.toJson(resp.data));
            alert("error_filelist" + resp.data);
            fail();
        });

    }


    function renameFile(success, fail, args) {
        var fileId = args[0].fileId;
        var appFolder = args[0].appFolder;
        var folderId = args[0].folderId;
        var title = args[0].title;
        var contentType = args[0].contentType;


        var request = gapi.client.drive.files.patch({'fileId': fileId, 'resource': {'title': title}});

        request.execute(function (resp) {
            if (resp.error) {
                console.log("error" + angular.toJson(resp.data));
                fail();
            } else {
                success();
            }
        });
    }

    function createFile(success, fail, args) {

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

        request.onerror = function (evt) {
            console.log("error_createFile " + angular.toJson(resp.data));
            alert("error_createFile" + resp.data);
            fail();
        };

        request.execute(success);

    }

    function downloadFile(success, fail, args) {
        var downloadUrl = args[0].downloadUrl;
        var fileId = args[0].fileId;
        var xhr = new XMLHttpRequest();
        xhr.open('GET', downloadUrl);
        xhr.setRequestHeader('Authorization', 'Bearer ' + googleToken);
        xhr.googleId = fileId;
        xhr.onload = function (data) {
            var result = data.currentTarget.responseText;
            success(JSON.parse(result));
        };
        xhr.onerror = function (evt) {
            console.log("error_downloadFile" + angular.toJson(resp.data));
            alert("error_downloadFile" + resp.data);
            fail();
        };
        xhr.send();

    }


    var GoogleDrive = {

        downloadFile: function (success, fail, args) {
            login(downloadFile, success, fail, args);
        },
        createFile: function (success, fail, args) {
            login(createFile, success, fail, args);
        },
        renameFile: function (success, fail, args) {
            login(renameFile, success, fail, args);
        },
        fileList: function (success, fail, args) {
            login(fileList, success, fail, args);
        }

    };

    module.exports = GoogleDrive;

    require('cordova/exec/proxy').add('GoogleDrive', module.exports);

});
