window.screensize = 1920;
window.form = 1;

// set hash if none exists
(function () {
    var boardDate = localStorage.getItem('boardDate') || moment().format('YYYYMMDD');
    if (!window.location.hash) {
        window.location.hash = '/' +  boardDate + '/screen1';
    }
}());

function getScreenId() {
    var screenId = (/screen\d/gi).exec(window.location.hash);
    return (screenId && screenId.length) ? screenId[0].replace(/\D/g, '') : 1;
}

function showpage() {
    var screen = getScreenId();
    var scrolltarget = 0;



    if(screen === 2) {
        scrolltarget = ((screen - 1) * window.screensize) - 42;
    } else {
        scrolltarget = ((screen - 1) * window.screensize) + 42;
    }

    $(window).scrollLeft(scrolltarget);
};

function slideshow() {
    /*var screen = parseInt(window.location.hash.replace(/^#page/,''));
    if(isNaN(screen)) {
        window.location.hash = '#page' + 1;
    } else {
        screen = parseInt(screen);
    }
    var winwidth = $('#displayboard').width();
    var screens = parseInt(Math.ceil(winwidth / window.screensize));

    screen = screen + 1;

    if(screen > screens) {
        screen = 1;
    }

    window.location.hash = '#page' + screen;*/
}

$(document).ready(function() {
    var screen = getScreenId();

    if (screen > 0) {
        $('#displayboard').show();


        if(!isNaN(screen)) {
            var doStuff = function () {
                slideshow();
                setTimeout(doStuff, 15000);
            };

            setTimeout(doStuff, 15000);
        }
    }

    if(!isNaN(screen)) {
        var doStuff = function () {
            slideshow();
            setTimeout(doStuff, 15000);
        };

        setTimeout(doStuff, 15000);
    }



    $(window).bind('hashchange', function() {

        if (window.form === 1) {
            window.form = 0;
        }

        //showpage();
    });

});
