'use strict';

angular.module('OpsBoard', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'bgDirectives',
  'lvl.directives.dragdrop',
  'context-menu',
  'ui.bootstrap',
  'ui.bootstrap.transition',
  'OpsBoardFilters'
])
  .directive('modalBackdrop', ['$timeout',
    function($timeout) {
      return {
        restrict: 'EA',
        replace: true,
        templateUrl: '/template/modal/backdrop.html',
        link: function(scope) {

          scope.animate = false;

          //trigger CSS transitions
          $timeout(function() {
            scope.animate = true;
          });
        }
      };
   }])

.directive('modalWindow', ['$modalStack', '$timeout',
  function($modalStack, $timeout) {
    return {
      restrict: 'EA',
      scope: {
        index: '@',
        animate: '='
      },
      replace: true,
      transclude: true,
      templateUrl: function(tElement, tAttrs) {
        return tAttrs.templateUrl || '/template/modal/window.html';
      },
      link: function(scope, element, attrs) {
        element.addClass(attrs.windowClass || '');
        scope.size = attrs.size;

        $timeout(function() {
          // trigger CSS transitions
          scope.animate = true;
          // focus a freshly-opened modal
          element[0].focus();
        });

        scope.close = function(evt) {
          var modal = $modalStack.getTop();
          if (modal && modal.value.backdrop && modal.value.backdrop != 'static' && (evt.target === evt.currentTarget)) {
            evt.preventDefault();
            evt.stopPropagation();
            $modalStack.dismiss(modal.key, 'backdrop click');
            scope.$destroy();
          }
        };
      }
    };
   }])

//william: add directive to loop, focus, and delete the interval.
.directive('downEquipmentFocus', ["$interval", function ($interval) {
  return {
    restrict: 'A',
    link: function postLink(scope, element, attr) {

      var stop = $interval(function () {
        if (element[0].length) {
          element.select2('focus');
          $interval.cancel(stop);
          stop = undefined;
        }
      }, 100);
    }
  }
}]);