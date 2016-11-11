'use strict';

angular
  .module('dsny-center-modal')
  .factory(
    'positionModal', ['$document', '$window',
      function($document, $window) {
        var _isCurScrOnMultiMonitor = function() {
          //currently checking only two monitors
          var wd = $window.outerWidth ? $window.outerWidth : $document.body.offsetWidth,
            x = $window.screenX || $window.screenLeft,
            left = $window.screen.availLeft || 0,
            pos = x > $window.screen.width ? x - left : x;

          return (pos + wd) > $window.screen.width;
        };
        var _getRelWinPos = function() {

          var wd = $window.outerWidth ? $window.outerWidth : $document.body.offsetWidth,
            x = $window.screenX || $window.screenLeft,
            sWd = $window.screen.width,
            rWd = (x + wd - sWd);
          //100 accounting for scroll bars. Its 65 on u/chrome doesnt have to be accurate. 
          if (rWd > Math.round(wd / 2) + 100) return {
            pos: 'R',
            wd: rWd
          };
          return {
            pos: 'L',
            wd: rWd
          };
        };
        return {
          getPosition: function(win, el) {
            var wd = win.width(), left;
            if (!_isCurScrOnMultiMonitor()) {
              return {
                left: Math.round((wd - el.width()) / 2)
              }
            };
            var relPos = _getRelWinPos();
            if (relPos.pos === 'L') {
              left = Math.round((wd - relPos.wd - el.width()) / 2);
            } else if (relPos.pos === 'R') {
              var lWd = wd - relPos.wd;
              left = Math.round((relPos.wd - el.width()) / 2) + lWd;
            }
            return {
              left: left
            };
          }
        };
    }
  ]);