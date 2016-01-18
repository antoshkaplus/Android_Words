
function Translation(foreignWord, nativeWord) {
    this.foreignWord = foreignWord;
    this.nativeWord = nativeWord;
}




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

    var ENTER_KEY_CODE = 13
    $('#foreignWord').keyup(function(e){
        if(e.keyCode == ENTER_KEY_CODE)
        {
            autoTranslate()
        }
    });

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
            fillTranslationList()
            //gapi.client.dictionaryApi.getDictionary().execute(function(resp) {
            //    console.log(resp)
            //})
        } else {
            console.log("user sucks dick")
            signin(false, userAuthed)


        }
    });
}

// should blink add button until we are not connected
function addTranslation() {
    w_0 = $('#foreignWord').val()
    w_1 = $('#nativeWord').val()
    if (!w_0 || !w_1) {
        console.log("one of the words is empty")
        // make red one of the cells
        return
    }
    var translation = new Translation(w_0, w_1);
    gapi.client.dictionaryApi.addTranslation(translation).execute(function(resp) {
        console.log("translation saved", resp)
        $('#foreignWord').focus().select()
    })

}

function fillTranslationList() {
    gapi.client.dictionaryApi.getTranslationList().execute(function(resp) {
        ko.applyBindings({translationList: resp.list});
        console.log(resp)
    })
}

function addFileTranslationList() {
    f = $("#translationListFile")[0].files[0];
    if (f) {
        var r = new FileReader();
        r.onload = function(e) {
            var contents = e.target.result;
            var list = []
            var translationList = {list: list}
            translations = contents.split("\n")
            for (t of translations) {
                var foreign, native
                t = t.split(";")
                foreign = t[0]
                native = t[1]
                if (!foreign || !native) continue
                list.push(new Translation(foreign, native));
            }
            gapi.client.dictionaryApi.addTranslationList(translationList).execute(function(resp) {
                console.log(resp)
            })
        }
        r.readAsText(f)
    } else {
        alert("Failed to load file")
    }

}


function autoTranslate() {
    var apiKey = 'AIzaSyCpNJPGA_zTpriCby8-z4XyAwEllC9wRlM'
    var source = 'https://www.googleapis.com/language/translate/v2';
    $.get( source, { key: apiKey, source: "en", target: "ru", q: $('#foreignWord').val() } )
        .done(function( data ) {
           var text = data.data.translations[0].translatedText
               console.log(text)
               $('#nativeWord').val(text);
        });
}

