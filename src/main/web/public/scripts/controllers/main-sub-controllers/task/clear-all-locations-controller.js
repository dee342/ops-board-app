'use strict';

angular
    .module('OpsBoard')
    .controller(
        'ClearAllLocations',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states) {

            $scope.clearMultipleLocations = function() {

                // Define attachment data and operation
                $scope.opData = {
                  titleAction : 'Warning',
                  cancelButtonText : 'Cancel',
                  submitButtonText : 'OK',
                  errors : [],
                  progress: 100,
                  message: 'Are you sure you want to DELETE all tasks created?'
                    // Set to 100 b/c unable to determine intervals
                };
                $scope.passPreValidation = function() {
                    return true;
                  };
                $scope.submitted = false;
                $scope.operation = function(success, error) {
                  $scope.submitted = true;
                  $scope.opData.clientErrors = [];
                  $scope.opData.serverErrors = [];
                  $scope.clearAllLocations();
                  success();
                };

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                  templateUrl: appPathStart + '/views/modals/modal-delete-confirm',
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
                	 $scope.selected = selectedItem;
                }, function() {
                  $log.info('Modal dismissed at: ' + new Date());
                });
              };
              
            });
