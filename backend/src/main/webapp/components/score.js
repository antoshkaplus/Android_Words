
(function () {

    var vm = {
        statsList: ko.observable([]),

        fillStatsList: function() {
            gapi.client.dictionaryApi.getStatsListWhole().execute(function(resp) {
                if (resp.error != null) {
                    $("#alertErrorGetStatsList").show()
                    return
                }
                vm.statsList(resp.list)
                console.log(resp)
            })
        }
    }

    externalApis.dictionaryLoaded.subscribe(function(val) {
        if (!val) return;
        vm.fillStatsList()
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

    thatDoc.registerElement('components-score', {prototype: Element});
})()