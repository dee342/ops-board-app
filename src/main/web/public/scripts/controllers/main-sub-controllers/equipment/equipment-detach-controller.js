'use strict';

angular
    .module('OpsBoard')
    .controller(
        'DetachEquipment',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states, BoardDataService, BoardValueService) {
            $scope.isEquipmentDetached = function(pieceOfEquipment) {
                if (pieceOfEquipment && pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.detached) {
                    return true;
                }
                return false;
            }
           
            $scope.isEquipmentDetachable = function(pieceOfEquipment) {
                if (pieceOfEquipment && !pieceOfEquipment.assigned && (pieceOfEquipment.states[BoardValueService.equipmentLocation] === states.equipment.available || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.down || pieceOfEquipment.states[BoardValueService.equipmentLocation] === states.equipment.pendingAttach || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.pendingDetach || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.pendingLoad)) {
                    return true;
                }
                return false;
            }

            $scope.detachEquipment = function(pieceOfEquipment) {

                var locations = BoardDataService.getRepairLocations();
                var detachLocations = $filter('detachLocationsFilter')(locations, $scope.board.location, pieceOfEquipment.currentLocation);
              
                // Define detachment data and operation
                $scope.opData = {
                    titleAction : 'Initiate Detach',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Detach',
                    clientErrors : [],
                    errors : [],
                    detachDate : $scope.board.displayDate,
                    driver : '',
                    equipmentName : pieceOfEquipment.name,
                    equipmentType : pieceOfEquipment.subType,
                    from : pieceOfEquipment.currentLocation,
                    locations : detachLocations,
                    progress : 100, // Set to 100 b/c unable to determine
                                    // intervals
                    to : "",
                    equipmentGroup : pieceOfEquipment.equipmentGroup,
                    time : new Date()
                };

                $scope.passPreValidation = function() {                	
                    // Validate board date is current date
                    var current = new Date();
                    var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
                    var boardDate = new Date($scope.board.displayDate);
                    var bData = BoardDataService.getBoardData();
                    if (!moment().subtract(1, 'minutes').isBetween(moment(bData.startDate), moment(bData.endDate), 'minutes')) {
                        $scope.opData.errors
                            .push({
                                type : 'danger',
                                message : 'This Board is not for the current date.  To detach Equipment, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                }

                $scope.submitted = false;
                $scope.operation = function(success, error) {
                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];

                    // Validate detach to
                    if (!$scope.opData.to) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Detach To is a required field.'
                        });
                    }

                    // Validate time
                    var time = new Date();
                    if (!$scope.opData.time) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Detach Time is invalid.'
                        });
                    } else {
                        var hours = $scope.opData.time.getHours();
                        var minutes = $scope.opData.time.getMinutes();
                        if (hours > time.getHours() || (hours == time.getHours() && minutes > time.getMinutes())) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Detach Time cannot be later than current time.'
                            });
                        }
                    }

                    // Validate driver
                    if (!$scope.opData.driver || !$scope.opData.driver.trim()) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Driver is a required field.'
                        });
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                        // return;
                    }

                    // Perform operation
                    OpsBoardRepository.detachEquipment(pieceOfEquipment, $scope.opData.to, $filter('date')(
                        $scope.opData.time, 'HH:mm'), $scope.opData.driver, success, error);
                };

                $scope.detachSelect2Options = {
                    allowClear : true
                }
                
                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-equipment-detachment',
                    controller : 'ModalCtrl',
                    backdrop : 'static',
                    resolve : {
                        data : function() {
                            return [ pieceOfEquipment ]
                        }
                    },
                    scope : $scope
                // set scope to current scope of Equipment Controller
                });

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });
            };
        });
