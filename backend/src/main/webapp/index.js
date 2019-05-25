
requirejs.config({
    shim: {
        'bootstrap': {
            deps: ['popper']
        },
        'bootstrap-notify': {
            deps: ['bootstrap', 'jquery']
        },
        'gapi': {
            exports: 'gapi'
        }
    },
    paths: {
        'jquery' : 'https://code.jquery.com/jquery-3.3.1.min',
        'bootstrap' : '//netdna.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min',
        'popper' : 'https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min',
        'knockout' : '//knockoutjs.com/downloads/knockout-3.5.0',
        'gapi' : 'https://apis.google.com/js/client',

        'bootstrap-notify' : 'libs/bootstrap-notify-3.1.3/bootstrap-notify.min',
        'require-text': 'libs/require-js-text-2.0.16/text',

        js: 'js',
        components: 'components'
    },
    map: {
        '*': {
            'popper.js': 'popper'
        }
    }
});

requirejs(['bootstrap', 'bootstrap-notify'])

requirejs(['knockout', 'js/init', 'js/util', 'js/index'], (ko) => {

    console.log("scripts loaded")

    ko.components.register('ko-component-add-usage', { require: 'components/add-usage' })
    ko.components.register('ko-component-translation', { require: 'components/translation' })
    ko.components.register('ko-component-score', { require: 'components/score' })
    ko.components.register('ko-component-lookup', { require: 'components/lookup' })
    ko.components.register('ko-component-word-versus', { require: 'components/word-versus' })

    ko.applyBindings()
});