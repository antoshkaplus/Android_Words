
(function () {

    var vm = {
        translationList: ko.observableArray(),
        translationListCursor: ko.observable(null),
        wordTranslation: ko.observable([]),
        translationKindOptions: ko.observable(["Word", "Idiom", "Phrase", "Pronun", "Name", "Abbr"]),
        translationKindSelected: ko.observable(),
        addUsageTranslation: ko.observable(),

        gapiTranslations: {},

        loadMoreTranslations: function() {
            $("#loadMoreTranslations").prop('disabled', true);

            gapi.client.dictionaryApi
                .getTranslationList_Cursor({pageSize: 10, cursor: vm.translationListCursor()})
                .then(
                    function(resp) {
                        if (resp) {
                            if (resp.result.list) {
                                ko.utils.arrayPushAll(vm.translationList, resp.result.list)
                            }
                            vm.translationListCursor(resp.result.nextCursor)
                        }
                        console.log(resp)
                        $("#loadMoreTranslations").prop('disabled', false);
                    },
                    function(reason) {
                        $("#alertErrorGetTranslationList").show()
                    })
        },
        resetTranslationList: function() {
            vm.translationList([])
            vm.translationListCursor(null)
            loadMoreTranslations()
        },
        addFileTranslationList: function() {
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
        },
        autoTranslate: function() {
            var apiKey = 'AIzaSyCpNJPGA_zTpriCby8-z4XyAwEllC9wRlM'
            var source = 'https://www.googleapis.com/language/translate/v2';
            var foreignWord = $('#foreignWord').val()

            translatedText = vm.gapiTranslations[foreignWord];
            if (translatedText === undefined) {
                vm.gapiTranslations[foreignWord] = null;
                $.get( source, { key: apiKey, source: "en", target: "ru", q: foreignWord } )
                    .done(function( data ) {
                        text = vm.gapiTranslations[foreignWord] = data.data.translations[0].translatedText

                        console.log(text)
                        $('#nativeWord').val(text);
                    });
            } else if (translatedText === null) {
                // nothing to do wait for it
            } else {
                $('#nativeWord').val(translatedText);
            }


            vm.wordTranslation([ new Translation("", "In Progress") ])

            gapi.client.dictionaryApi.getTranslationOnline({foreignWord : foreignWord}).then(
                function(resp) {
                    if (resp.result.list === undefined) {
                        resp.result.list = [ new Translation("", "Not Found") ]
                    }
                    vm.wordTranslation( resp.result.list.map(ConvertWebTranslation) )
                    console.log(resp)
                },
                function(reason) {
                    $("#alertErrorGetTranslationList").show()
                })
        },
        // should blink add button until we are not connected
        addTranslation: function() {
            $("#alertErrorAddTranslation").hide()

            w_0 = $('#foreignWord').val()
            w_1 = $('#nativeWord').val()
            kind = $('#translationKind').val()
            if (!w_0 || !w_1) {
                console.log("one of the words is empty")
                // make red one of the cells
                return
            }
            var translation = new Translation(w_0, w_1, kind);
            // it's now done a lot different
            // have to code very different
            // better go through whole synchronization process
            // but with javascript
            // and only with one element
            // easy to test
            gapi.client.dictionaryApi.addTranslationOnline(translation).then(
                function(resp) {
                    console.log("translation saved", resp)
                    $('#foreignWord').focus().select()
                    vm.translationList.unshift(resp.result)
                },
                function(reason){
                    $("#alertErrorAddTranslation").show()
                });
        },
        removeTranslation: function(translation) {
            var bF = true;
            var bN = true;
            // later on can throw some alert about it to user

            if (typeof translation.foreignWord != 'string') {
                bF = false
            }
            if (typeof translation.nativeWord != 'string') {
                bN = false
            }
            if (!bF || !bN) return
            gapi.client.dictionaryApi.removeTranslationOnline(translation).then(
                function(resp) {
                    console.log("translation removed", resp)
                    vm.translationList.remove(translation)
                },
                function(reason) {
                    $('#alertErrorRemoveTranslation').show()
                })
        },
        addUsage: function(translation) {
            vm.addUsageTranslation(translation);
        },
        fillTranslationList: function() {
            gapi.client.dictionaryApi.getTranslationListWhole().execute(function(resp) {
                if (resp.error != null) {
                    // need to show some kind of sign to reload browser window
                    // later on may try to reload by myself
                    $("#alertErrorGetTranslationList").show()
                    return
                }

                vm.translationList(resp.list)
                console.log(resp)
            })
        },
        onKeyUp: function(data, e) {
            const ENTER_KEY_CODE = 13
            if(e.keyCode == ENTER_KEY_CODE)
            {
                vm.autoTranslate();
                var foreignWord = $('#foreignWord').val();
                utterThis = new SpeechSynthesisUtterance(foreignWord);
                utterThis.voice = window.speechSynthesis.getVoices().find(voice => voice.lang == "en-US");
                window.speechSynthesis.speak(utterThis);
            }
        }
    }

    externalApis.dictionaryLoaded.subscribe(function(val) {
        if (!val) return;
        vm.loadMoreTranslations()
    })

    vm.translationKindSelected.subscribe(function(newKind) {
        form = $("#addTranslationForm")
        if (newKind == "Word" || newKind == "Pronun") {
            form.addClass("form-inline")
        } else {
            form.removeClass("form-inline")
        }
    })

    var thatDoc = document;
    var thisDoc = document.currentScript.ownerDocument

    var tmpl = thisDoc.querySelector('template')
    var Element = Object.create(HTMLElement.prototype)

    var shadowRoot;

    Element.createdCallback = function () {
        var clone = thatDoc.importNode(tmpl.content, true);
        this.appendChild(clone);

        ko.applyBindings(vm, this)
    }

    thatDoc.registerElement('components-translation', {prototype: Element});
})()