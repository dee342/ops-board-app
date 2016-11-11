'use strict';

angular
  .module('OpsBoard')
  .controller(
    'MultiRoutes',
    function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, durations, $rootScope) {
    	
      $scope.boardLocation = OpsBoardRepository.getBoardLocation();
      $scope.showContent = false;

      $scope.cancel = function () {
        $scope.panes.showMultiRoutes.active = false;
        $scope.group.length = 0;
        $scope.showProcessing = false;    
        $scope.showContent = true;  
        $scope.showMessage = true;
        $scope.opData.serverErrors = [];
      }

      $scope.reset = function () {
        $scope.group.length = 0;
        $scope.showProcessing = false;    
        $scope.showContent = true;  
        $scope.showMessage = true;
        $scope.opData.serverErrors = [];
      }

      $scope.addMultiRoutes = function(locationId) {
        //$scope.showMultiRoutes = !$scope.showMultiRoutes;
        $scope.panes.showMultiRoutes.active = !$scope.panes.showMultiRoutes.active;
        $scope.showContent = true;
        $scope.showMessage = true;
        $scope.opData = {
          cancelButtonText : 'Cancel',
          submitButtonText : 'Confirm',
          resetButtonText : 'Reset',
          clientErrors : [],
          serverErrors: [],
          errors : [],
          progress: 100
            // Set to 100 b/c unable to determine intervals
        };
        $scope.durations = durations;
      };

      $scope.isSet = function () {
        var total = 0,
          duration = true;
        for (var i = 0; i < $scope.group.length; i++) {
          total = total + Number($scope.group[i].duration)
          if (!$scope.group[i].duration)
            duration = false;
        }
        if (total === 8 && duration) return true;
        return false;
      }

      $scope.getMessage = function (part) {
        if (!part) return;
        if ($scope.isSet()) {
          if (part === 'head') return ''
          if (part === 'tail') return 'Routes are all set!'
        }
          
        if ($scope.group.length === 0) {
          if (part === 'head') return 'Click on the route to set as the '
          if (part === 'tail') return 'START TRUCK'
        }
        if ($scope.group.length > 0) {
          if (part === 'head') return 'The routes are incomplete.'
          if (part === 'tail') return ''
        }
      }

      $scope.deleteTaskFromGroup = function (index) {
        $scope.group.splice(index, 1); 
        for (var i = 0; i < $scope.group.length; i++) {
          $scope.group[i].sequence = i + 1;
        }
        $scope.opData.serverErrors = [];
        //delete $scope.group[index]
      }

      $scope.confirm = function () {
        $scope.showProcessing = true;
        $scope.showContent = false;
        $scope.showMessage = false;
        OpsBoardRepository.saveGroup($scope.group, function (success) {
          $scope.showProcessing = false;
          $scope.panes.showMultiRoutes.active = false;
          $scope.group.length = 0;
        }, function (error) {
          $scope.showProcessing = false;    
          $scope.showContent = true;  
          $scope.showMessage = false;
          if (error.data.extendedMessages) {
            for (var i = 0; i < error.data.extendedMessages.length; i++) {
              $scope.opData.serverErrors[i] = {
                message: error.data.extendedMessages[i]
              }
            }
          } else {
            $scope.opData.serverErrors = {
              message: 'Generic Server Error'
            }
          }
        });
      }
    });
