
(function () {

    var thatDoc = document;
    var thisDoc = document.currentScript.ownerDocument

    var tmpl = thisDoc.querySelector('template')

    function AddUsageViewModel(params, componentInfo) {
        var vm = this

        vm.componentUuid = guid()

        $(componentInfo.element).find('.modal').attr('id', vm.componentUuid)

        vm.usage = ko.observable()
        vm.translation = ko.observable()

        params.translation.subscribe((translation) => {
            if (!translation) return
            this.translation(translation)
            $('#' + this.componentUuid).modal()
        })

        vm.addUsage = () => {
            $('#' + this.componentUuid).modal('hide')

            var translation = vm.translation();
            vm.translation(null)

            gapi.client.dictionaryApi
                .addTranslationUsage({"translationId": translation.id}, new Usage(vm.usage()))
                .then(
                    function(resp) {
                        translation.usages.push(vm.usage());
                        console.log("add usage success")
                    },
                    function(reason) {
                        console.log("add usage failure", reason)
                    })
        }
    }

    ko.components.register('ko-component-add-usage', {
        viewModel: {
            createViewModel: function(params, componentInfo){
                return new AddUsageViewModel(params, componentInfo)
            }
        },
        template: tmpl.innerHTML
    })

})()