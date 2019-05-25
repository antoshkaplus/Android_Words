
define(['jquery', 'knockout', 'require-text!components/score.html'], function($, ko, htmlString) {

    function ScoreViewModel(params, componentInfo) {

        this.statsList = ko.observable([]),
        this.fillStatsList = function() {
                gapi.client.dictionaryApi.getStatsListWhole().execute(function(resp) {
                    if (resp.error != null) {
                        $("#alertErrorGetStatsList").show()
                        return
                    }
                    vm.statsList(resp.list)
                    console.log(resp)
                })
            }

        var vm = this
        externalApis.dictionaryLoaded.subscribe(function(val) {
            if (!val) return;
            vm.fillStatsList()
        })
        if (externalApis.dictionaryLoaded()) vm.fillStatsList()
    }

    return {
        viewModel: {
                createViewModel: function(params, componentInfo) {
                    return new ScoreViewModel(params, componentInfo)
                }
        },
        template: htmlString
    };
})