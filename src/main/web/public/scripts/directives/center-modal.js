'use strict';

angular
  .module('dsny-center-modal')
  .directive(
    "centeredModal", ['$window', '$timeout', 'positionModal',
      function($window, $timeout, positionModal) {
        return {
          restrict: 'A',
          link: function(scope, el, attr) {

            var w = angular.element($window);
            scope.getWindowState = function() {
              return {
                'h': w.height(),
                'w': w.width(),
                'x': $window.screenX || $window.screenLeft
              };
            };

            scope.reposition = function() {
              var parent = attr.centerClass ? '.' + attr.centerClass : '.modal .modal-dialog';
              var mod = angular.element(el)
                .closest(parent);
              var targetElPos = positionModal.getPosition(w, mod);
              if (targetElPos)
                mod.css('margin-left', targetElPos.left)
            };

            scope.$watch(scope.getWindowState, function(newValue, oldValue) {
              scope.reposition();
            }, true);

            w.bind('resize', function() {
              scope.$apply();
            });
          }
        };
      }
    ]);