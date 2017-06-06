

var deletedTranslations

// class definitions

function Translation(foreignWord, nativeWord) {
    this.foreignWord = foreignWord;
    this.nativeWord = nativeWord;
}


// viewModel init

// much quicker reation than waiting for google api to load.
// may meed to create viewModel object in the future.
$(function() {
    viewModel = {
        translationList: ko.observable([]),
        translationListCursor: ko.observable(null),
        statsList: ko.observable([]),
        wordTranslation: ko.observable([])
    }
    ko.applyBindings(viewModel)
})


// This is called initially
function init() {
    deletedTranslations = Array()

    var apiName = 'dictionaryApi';
    var apiVersion = 'v2';
    var apiRoot = 'https://' + window.location.host + '/_ah/api';
    if (window.location.hostname == 'localhost'
      || window.location.hostname == '127.0.0.1'
      || ((window.location.port != "") && (window.location.port > 1023))) {
        // We're probably running against the DevAppServer
      apiRoot = 'http://' + window.location.host + '/_ah/api';
    }
    //apiRoot = "https://antoshkaplus-words.appspot.com/_ah/api"

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
            loadMoreTranslations()
            fillStatsList()
            //gapi.client.dictionaryApi.getDictionary().execute(function(resp) {
            //    console.log(resp)
            //})
        } else {
            console.log("user sucks dick")
            signin(false, userAuthed)


        }
    });
}

// we call this function this way because of google api example naming.
function signOut() {
    gapi.auth.setToken(null)
}

// should blink add button until we are not connected
function addTranslation() {
    $("#alertErrorAddTranslation").hide()

    w_0 = $('#foreignWord').val()
    w_1 = $('#nativeWord').val()
    if (!w_0 || !w_1) {
        console.log("one of the words is empty")
        // make red one of the cells
        return
    }
    var translation = new Translation(w_0, w_1);
    // it's now done a lot different
    // have to code very different
    // better go through whole synchronization process
    // but with javascript
    // and only with one element
    // easy to test
    gapi.client.dictionaryApi.addTranslationOnline(translation).execute(function(resp) {
        if (resp.error) {
            $("#alertErrorAddTranslation").show()
            return
        }
        console.log("translation saved", resp)
        $('#foreignWord').focus().select()
        resetTranslationList()
    })

}

function removeTranslation() {
    chs = $(this).closest("tr").children()
    foreignWord = chs.eq(0).text()
    nativeWord = chs.eq(1).text()
    bF = true
    bN = true
    // later on can throw some alert about it to user
    if (typeof foreignWord != 'string') {
        bF = false
    }
    if (typeof nativeWord != 'string') {
        bN = false
    }
    if (!bF || !bN) return
    var translation = new Translation(foreignWord, nativeWord);
    gapi.client.dictionaryApi.removeTranslationOnline(translation).execute(function(resp) {
        if (resp.error) {
            $('#alertErrorRemoveTranslation').show()
            return
        }
        console.log("translation removed", resp)
        resetTranslationList()
    })
}

function fillTranslationList() {
    gapi.client.dictionaryApi.getTranslationListWhole().execute(function(resp) {
        if (resp.error != null) {
            // need to show some kind of sign to reload browser window
            // later on may try to reload by myself
            $("#alertErrorGetTranslationList").show()
            return
        }

        viewModel.translationList(resp.list)
        console.log(resp)
    })
}

function loadMoreTranslations() {
    $("#loadMoreTranslations").prop('disabled', true);

    gapi.client.dictionaryApi
        .getTranslationList_Cursor({pageSize: 10, cursor: viewModel.translationListCursor()})
        .execute(function(resp) {
            if (resp.error != null) {
                // need to show some kind of sign to reload browser window
                // later on may try to reload by myself
                $("#alertErrorGetTranslationList").show()
                return
            }
            if (resp) {
                if (resp.list) {
                    viewModel.translationList(viewModel.translationList().concat(resp.list))
                }
                viewModel.translationListCursor(resp.nextCursor)
            }
            console.log(resp)
            $("#loadMoreTranslations").prop('disabled', false);
        })
}

function resetTranslationList() {
    viewModel.translationList([])
    viewModel.translationListCursor(null)
    loadMoreTranslations()
}

function fillStatsList() {
    gapi.client.dictionaryApi.getStatsListWhole().execute(function(resp) {
        if (resp.error != null) {
            $("#alertErrorGetStatsList").show()
            return
        }
        viewModel.statsList(resp.list)
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
                resetTranslationList()
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
    var foreignWord = $('#foreignWord').val()

    $.get( source, { key: apiKey, source: "en", target: "ru", q: foreignWord } )
        .done(function( data ) {
           var text = data.data.translations[0].translatedText
               console.log(text)
               $('#nativeWord').val(text);
        });

    viewModel.wordTranslation([ new Translation("", "In Progress") ])

    gapi.client.dictionaryApi.getTranslationOnline({foreignWord : foreignWord}).execute(function(resp) {
        if (resp.error != null) {
            // need to show some kind of sign to reload browser window
            // later on may try to reload by myself
            $("#alertErrorGetTranslationList").show()
            return
        }
        if (resp.list === undefined) {
            resp.list = [ new Translation("", "Not Found") ]
        }
        viewModel.wordTranslation(resp.list)
        console.log(resp)
    })
}

