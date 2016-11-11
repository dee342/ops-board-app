'use strict';

angular
  .module('OpsBoard')
  .directive(
  'taskItemDistrict', ['TaskHelperService',
    function(TaskHelperService) {
      return {
        controller: 'TaskController',
        restrict: 'E',

        scope: {
          item: '=',
          layout: '=',
          panes: '=',
          col: '=',
          section: '=',
          locationId: '='
        },

        templateUrl: function(elem,attrs) {
          return TaskHelperService.getPathStart() + '/views/fragments/task-item-district';
        },

        link: function (scope, element, attributes) {
          element.bind("$destroy", function() {
            scope.$destroy();
          });
        }
      };
    }
  ]);