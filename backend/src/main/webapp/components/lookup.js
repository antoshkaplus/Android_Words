
define(['knockout', 'require-text!components/lookup.html'], function(ko, htmlString) {

    function LookupViewModel(params, componentInfo) {

        this.statsList = ko.observable([]),
        this.fillStatsList = function() {
                gapi.client.dictionaryApi.getStatsListWhole().execute(function(resp) {
                    if (resp.error != null) {
                        $("#alertErrorGetStatsList").show()
                        return
                    }
                    if (!resp.list) resp.list = [];
                    resp.list = resp.list.filter(stat => stat.lookupCount > 0)
                    resp.list.sort((left, right) => left.lookupCount - right.lookupCount)

                    vm.statsList(resp.list)
                    console.log(resp)
                })
            }

        var vm = this
        externalApis.dictionaryLoaded.subscribe(function(val) {
            if (!val) return;
            vm.fillStatsList()
        })
    }

    return {
        viewModel: {
               createViewModel: function(params, componentInfo) {
                   return new LookupViewModel(params, componentInfo)
               }
        },
        template: htmlString
    };
})