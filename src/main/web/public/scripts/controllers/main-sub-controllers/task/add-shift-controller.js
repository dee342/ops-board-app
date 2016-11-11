'use strict';

angular
  .module('OpsBoard')
  .controller(
    'AddShift',
    function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, $rootScope) {
    	
      $scope.boardLocation = OpsBoardRepository.getBoardLocation();

      //$scope.locationId = $scope.boardLocation//temporary logic untill multiple locations are hooken up

      var locationId;

      $scope.getRemainingShifts = function (locationId) {
        var shifts = angular.copy($scope.shiftData);
        Object.keys(shifts).forEach(function (shiftId) {
        	if(isNaN(shiftId)){
        		delete shifts[shiftId];
        	}
        })
        if (!$scope.tasks.locations[locationId])  return shifts;
        var locationShifts = $scope.tasks.locations[locationId].locationShifts;
          if (!locationShifts) return shifts;
        Object.keys(shifts).forEach(function (shiftId) {
          Object.keys(locationShifts).forEach(function (locationShiftId) {
            if(locationShifts[locationShiftId].shift.id == shiftId) delete shifts[shiftId]
          })
        })
        return shifts;
      }

      $scope.isOpen = true;
      
      $scope.select2Options = {
        allowClear : false
      }

      $scope.addShift = function(locationId) {
        angular.forEach($scope.tasks.locations, function (val) {
          if (val.active && val.locationCode) {
            locationId = val.locationCode;
          }
        });
    	  if(!Object.keys($scope.getRemainingShifts(locationId)).length) return;
        // Define attachment data and operation
        $scope.opData = {
          titleAction : 'Select Your Shift',
          cancelButtonText : 'Cancel',
          submitButtonText : 'OK',
          clientErrors : [],
          errors : [],
          shiftId: '',
          shifts: $scope.getRemainingShifts(locationId) ,
          progress: 100
            // Set to 100 b/c unable to determine intervals
        };

        $scope.passPreValidation = function() {
          return true;
        };
        
        $scope.showCategoryContainer = false;
        $scope.submitted = false;
        $scope.operation = function(success, error) {
          $scope.submitted = true;
          $scope.opData.clientErrors = [];
          $scope.opData.serverErrors = [];

          // Validate received by
          if (!$scope.opData.shiftId) {
            $scope.opData.clientErrors.push({
              type: 'danger',
              message: 'Please select your shift.'
            });
          }

          if ($scope.opData.clientErrors.length > 0) {
            return error(new ClientSideError('ValidationError'));
          }
          success();
        };

        // Configure modal controller and create instance
        var modalInstance = $modal.open({
          templateUrl: appPathStart + '/views/modals/modal-add-shift',
          controller: 'ModalCtrl',
          backdrop: 'static',
          size: 'sm',
          windowClass: 'shift-modal',
          resolve: {
            data: function() {
              return [$scope.opData]
            }
          },
          scope: $scope
            // set scope to current scope of Task Controller
        });

        modalInstance.result.then(function(selectedItem) {
          $scope.pushShift(locationId, selectedItem)
        }, function() {
          $log.info('Modal dismissed at: ' + new Date());
        });
      };
      
    });
