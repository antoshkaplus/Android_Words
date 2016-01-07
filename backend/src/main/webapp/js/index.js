

// A function that attaches a "Say Hello" button click handler
function enableClick() {
  document.getElementById('helloButton').onclick = function() {
    var name = document.getElementById('nameInput').value;
    gapi.client.myApi.sayHi({'name': name}).execute(
      function(response) {
        var outputAlertDiv = document.getElementById('outputAlert');
        outputAlertDiv.style.visibility = 'visible';

        if (!response.error) {
          outputAlertDiv.className = 'alert alert-success';
          outputAlertDiv.innerHTML = '<h2>' + response.result.data + '</h2>';
        }
        else if (response.error) {
          outputAlertDiv.className = 'alert alert-danger';
          outputAlertDiv.innerHTML = '<b>Error Code: </b>' + response.error.code + ' [' + response.error.message + ']';
        }
      }
    );
    return false;
  }
}

// This is called initially
function init() {
  var apiName = 'dictionaryApi';
  var apiVersion = 'v1';
  var apiRoot = 'https://' + window.location.host + '/_ah/api';
  if (window.location.hostname == 'localhost'
      || window.location.hostname == '127.0.0.1'
      || ((window.location.port != "") && (window.location.port > 1023))) {
        // We're probably running against the DevAppServer
      apiRoot = 'http://' + window.location.host + '/_ah/api';
  }
  apiRoot = "https://antoshkaplus-words.appspot.com/_ah/api"

  var apisToLoad = 2
  var callback = function() {
      if (--apisToLoad == 0) {
          signin(true, userAuthed);
      }
  }

  gapi.client.load(apiName, apiVersion, callback, apiRoot);
  gapi.client.load('oauth2', 'v2', callback)
}
//server:client_id:
var CLIENT_ID = "251166830439-2noub1jvf90q79oc87sgbho3up8iurej.apps.googleusercontent.com"
var SCOPES = "https://www.googleapis.com/auth/userinfo.email"

function signin(mode, authorizeCallback) {
    gapi.auth.authorize({
        client_id: CLIENT_ID,
        scope: SCOPES,
        immediate: mode},
        authorizeCallback);
}

function userAuthed() {
    var request = gapi.client.oauth2.userinfo.get().execute(function(resp) {
        if (!resp.code) {
            // user is signed in, call my endpoint
            console.log("user is signed in, continue with your bullshit")
            gapi.client.dictionaryApi.getDictionary().execute(function(resp) {
                console.log(resp)
            })
        } else {
            console.log("user sucks dick")
            signin(false, userAuthed)


        }
    });
}
