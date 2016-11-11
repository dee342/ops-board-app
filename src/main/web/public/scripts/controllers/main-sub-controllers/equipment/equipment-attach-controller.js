'use strict';

angular
    .module('OpsBoard')
    .controller(
        'AttachEquipment', function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states, BoardDataService, BoardValueService) {

            $scope.isEquipmentPendingAttach = function(pieceOfEquipment) {

                if (pieceOfEquipment && pieceOfEquipment.states[BoardValueService.equipmentLocation] === states.equipment.pendingAttach) {
                    return true;
                }
                return false;
            }

            $scope.acceptEquipmentAttachment = function(pieceOfEquipment) {

            	// always the last modified item in detachment history
                var detachmentHistory = pieceOfEquipment.getFormattedDetachments();
                var detachment = pieceOfEquipment.getSortedDetachmentHistory(detachmentHistory);
                detachment = detachment[0];
                
                 // Define attachment data and operation
                $scope.opData = {
                    titleAction : 'Accept Detach',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Attach',
                    clientErrors : [],
                    errors : [],
                    equipmentName : pieceOfEquipment.name,
                    equipmentType : pieceOfEquipment.subType,
                    from : detachment.from,
                    detachDate : $filter('date')(detachment.lastModifiedActual, 'MM/dd/yyyy'),
                    detachTime : $filter('date')(detachment.lastModifiedActual, 'HH:mm'),
                    deliveredBy : detachment.driver,
                    receivedBy : '',
                    receivedRemarks : '',
                    receivedDate : $scope.board.displayDate,
                    receivedTime : new Date(),
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
                                message : 'This Board is not for the current date.  To accept Equipment, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                }

                $scope.submitted = false;
                $scope.operation = function(success, error) {
                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];
                    $scope.opData.serverErrors = [];

                    // Validate received by
                    if (!$scope.opData.receivedBy || !$scope.opData.receivedBy.trim()) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Received By is a required field.'
                        });
                    }

                    // Validate received time
                    if (!$scope.opData.receivedTime) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Received Time is invalid.'
                        });
                    } else {
                        var current = new Date();
                        var rHours = $scope.opData.receivedTime.getHours();
                        var rMinutes = $scope.opData.receivedTime.getMinutes();

                        if (rHours > current.getHours()
                            || (rHours == current.getHours() && rMinutes > current.getMinutes())) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Received Time cannot be later than current time.'
                            });
                        } else if ($scope.opData.receivedDate == $scope.opData.detachDate) {
                            var timeElements = $scope.opData.detachTime.split(':');
                            var detachedTime = new Date(0, 0, 0, timeElements[0], timeElements[1]);
                            var dHours = detachedTime.getHours();
                            var dMinutes = detachedTime.getMinutes();

                            if (rHours < dHours || (rHours == dHours && rMinutes < dMinutes)) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Received Time cannot be prior to Detach Time.'
                                });
                            }
                        }
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                        // return;
                    }

                    // Perform operation
                    OpsBoardRepository.acceptAttachEquipment(pieceOfEquipment, $scope.opData.receivedBy,
                        $scope.opData.receivedDate, $filter('date')($scope.opData.receivedTime, 'HH:mm'),
                        $scope.opData.receivedRemarks, success, error);
                };

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-equipment-attachment',
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