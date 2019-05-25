

define(['jquery', 'knockout', 'require-text!components/add-usage.html'], function($, ko, htmlString) {
    
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

            var usage = vm.usage()
            gapi.client.dictionaryApi
                .addTranslationUsage({"translationId": translation.id}, new Usage(usage))
                .then(
                    function(resp) {
                        translation.usages.push(usage);
                        console.log("add usage success")
                    },
                    function(reason) {
                        console.log("add usage failure", reason)
                    })
            vm.usage("")
        }
    }
 
    // Return component definition
    return {
        viewModel: {
                createViewModel: function(params, componentInfo) {
                    return new AddUsageViewModel(params, componentInfo)
                }
        },
        template: htmlString
    };
});