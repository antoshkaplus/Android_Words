

define(['jquery', 'knockout', 'require-text!components/word-versus.html'], function($, ko, htmlString) {

    function WordVersusViewModel(params, componentInfo) {

        this.wordVersusList = ko.observable([]),
        this.fillWordVersusList = function() {
            gapi.client.dictionaryApi.word.getWordVersusListWhole().then(
                function(resp) {
                    vm.wordVersusList(resp.result.list)
                    console.log("get word versus list", resp.list)
                },
                function(reason) {
                   var msg = reason.result.error.message
                   msg =  `get word versus failure ${msg}`
                   $.notify({
                       message: msg
                   },{
                       type: 'danger'
                   });
                   console.log(msg)
                })
        }
        this.words = [
            ko.observable(''),
            ko.observable(''),
            ko.observable('')
        ]
        this.description = ko.observable('')

        this.addWordVersus = () => {
            words = vm.words.map(w => w()).filter(w => w)
            var msg = ''
            if (!words.length) {
                msg = 'no words found. '
            }
            description = vm.description()
            if (!description) {
                msg += 'no description found.'
            }
            if (msg) {
                $.notify({
                    message: msg
                },{
                    type: 'danger'
                });
                return
            }

            params = {
                words: words,
                description: description
            }

            gapi.client.dictionaryApi.word.addWordVersus(params).then(
                function(resp) {
                    $.notify({
                        message: "word versus added"
                    },{
                        type: 'success'
                    });
                },
                function(reason) {
                    var msg = reason.result.error.message
                    msg =  `add word versus failure ${msg}`
                    $.notify({
                        message: msg
                    },{
                        type: 'danger'
                    });
                    console.log(msg)
                })
        }

        var vm = this
        externalApis.dictionaryLoaded.subscribe(function(val) {
              if (!val) return;
              vm.fillWordVersusList()
        })
        if (externalApis.dictionaryLoaded()) vm.fillWordVersusList()
    }

    return {
         viewModel: {
             createViewModel: function(params, componentInfo) {
                 return new WordVersusViewModel(params, componentInfo)
             }
         },
         template: htmlString
    };
})