'use strict';

angular
    .module('OpsBoard')
    .controller(
        'ReloadBoard',
        function($scope, $modal, $log, OpsBoardRepository, ClientSideError) {
        	
        	$scope.$on('reload-board', function(event, message) {
        		$scope.reloadBoard(message);
        	});
        	
        	$scope.reloadBoard = function(message) {
            
            	$scope.passPreValidation = function(){
                    return true;
                }                
                	 $scope.opData = {
                             
                             titleAction : 'Reload Board',
                             cancelButtonText : 'Cancel',
                             submitButtonText : 'Reload',
                             clientErrors : [],
                             errors : [],
                             progress : 100,
                             message: message,
                             required : []
                         }
               
                $scope.submitted = false;
                
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-reload-board',
                    controller : 'ModalCtrl',
                    backdrop : 'static',
                    resolve : {
                        data : function() {
                            return [];
                        }
                    },
                    scope : $scope
                });

                $scope.operation = function(success, error) {
                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];
                    $scope.opData.serverErrors = [];

                    location.reload();             
                };  

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });
            }
        })