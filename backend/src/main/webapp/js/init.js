
//server:client_id:
var CLIENT_ID = "251166830439-2noub1jvf90q79oc87sgbho3up8iurej.apps.googleusercontent.com"
var SCOPES = "https://www.googleapis.com/auth/userinfo.email"


$(function() {

    externalApis = {
        dictionaryLoaded: ko.observable(false),
    }

})


function init() {
    gapi.load('client:auth2', initAuth);
}


// Get authorization from the user to access profile info
function initAuth() {
    gapi.auth2.init({
        client_id: CLIENT_ID,
        scope: SCOPES,
    }).then(
        function () {
            console.log("authorized")
            auth2 = gapi.auth2.getAuthInstance();
            auth2.isSignedIn.listen(updateSigninStatus);
            updateSigninStatus(auth2.isSignedIn.get());
            $('#login').click(auth)
        },
        function () {
            console.log("authorization failure")
        }
    );
}

function updateSigninStatus(isSignedIn) {
    if (isSignedIn) {
        console.log("signed in: ", auth2.currentUser.get().getBasicProfile().getGivenName());
        loadApi();
    } else {
        console.log("signed out")
    }
}

function auth() {
    auth2.signIn();
}

function refreshAuth() {
    gapi.auth2.getAuthInstance().currentUser.get().reloadAuthResponse().then(
        function(resp) {
            console.log("refresh auth success", resp)
        },
        function(reason) {
            console.log("refresh auth failure", reason)
        }
    )
}

function loadApi() {
    var apiName = 'dictionaryApi';
    var apiVersion = 'v3';
    var apiRoot = 'https://' + window.location.host + '/_ah/api';
    if (window.location.hostname == 'localhost'
      || window.location.hostname == '127.0.0.1'
      || ((window.location.port != "") && (window.location.port > 1023))) {
        // We're probably running against the DevAppServer
      apiRoot = 'http://' + window.location.host + '/_ah/api';
    }
    //apiRoot = "https://antoshkaplus-words.appspot.com/_ah/api"

    gapi.client.load(apiName, apiVersion, undefined, apiRoot).then(
        function(response) {
            externalApis.dictionaryLoaded(true)
            console.log("dictionary api loaded")
        },
        function(reason) {
            console.log("dictionary api load failure", reason)
        })
}