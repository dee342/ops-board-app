'use strict';

angular
    .module('OpsBoard')
    .controller(
        'CancelDetachEquipment',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states, BoardDataService, BoardValueService) {
            
            $scope.isEquipmentPendingDetach = function(pieceOfEquipment) {
                if (pieceOfEquipment && pieceOfEquipment.states[BoardValueService.equipmentLocation] === states.equipment.pendingDetach) {
                    return true;
                }
                return false;
            }

            $scope.cancelEquipmentDetachment = function(pieceOfEquipment) { 

                // Define attachment data and operation
                $scope.opData = {
                    titleAction : 'Cancel Detach',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'No',
                    submitButtonText : 'Yes',
                    errors : [],
                    equipmentName : pieceOfEquipment.name,
                    equipmentType : pieceOfEquipment.subType,
                    progress : 100
                // Set to 100 b/c unable to determine intervals
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
                                message : 'This Board is not for the current date.  To cancel a Pending Equipment Detachment, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                }

                $scope.operation = function(success, error) {
                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];
                    $scope.opData.serverErrors = [];
                    $scope.cancelData = getCancelData($scope.opData);

                    // Perform operation
                    OpsBoardRepository.cancelEquipmentDetachment(pieceOfEquipment, success, error);
                };

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-equipment-cancel-detachment',
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

                function getCancelData(opData) {
                    return {
                        equipmentName : pieceOfEquipment.name,
                        equipmentType : pieceOfEquipment.subType
                    };
                }

            };
        })
