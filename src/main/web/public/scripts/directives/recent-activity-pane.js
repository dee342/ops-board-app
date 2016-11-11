'use strict';

angular
  .module('OpsBoard')
  .directive(
    'recentActivityPane', ['$window', '$modal', 'durations', '$sce', '$compile', 'OpsBoardRepository',
      function($window, $modal, durations, $sce, $compile, OpsBoardRepository) {
        return {
          controller: 'ActionPanelCtrl',
          restrict: 'A',
          scope: {
            panes: '=',
            showDetails: '&'
          },
          templateUrl: OpsBoardRepository.getPathStart() + '/views/fragments/recent-activity-slide-out-pane',
          link: function (scope, element, attributes) {
            scope.actions = OpsBoardRepository.getActions();
            scope.onlineUsers = OpsBoardRepository.getOnlineUsers();
          }
        };
      }
    ]);