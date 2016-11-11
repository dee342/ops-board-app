'use strict';

angular
    .module('OpsBoard')
    .controller(
        'UpEquipment',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states, BoardDataService) {
            $scope.upEquipment = function(pieceOfEquipment) {

                var conditions = [ 'Up', 'Down' ];
                $scope.submitted = false;
                var time = new Date();
                $scope.opData = {
                    titleAction : 'Up Equipment',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Up',
                    clientErrors : [],
                    errors : [],
                    equipmentName : pieceOfEquipment.name,
                    equipmentType : pieceOfEquipment.subType,
                    owner : pieceOfEquipment.owner,
                    currentLocation : pieceOfEquipment.currentLocation,
                    license : pieceOfEquipment.licensePlate,
                    dt1 : $scope.board.displayDate,
                    time1 : new Date(),
                    progress : 100,
                    reporter : $scope.user.username,
                    checkoutId : pieceOfEquipment.checkoutId
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
                                message : 'This Board is not for the current date.  To up Equipment, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                };

                $scope.operation = function(success, error) {
                    $scope.opData.clientErrors = []; // clear errors
                    $scope.upData = getUpData($scope.opData); // get Data to be passed to application server from opData which has much more than that
                    $scope.submitted = true;
                    var time = new Date(), sgHour = signOffTime.getHours(), sgTime = signOffTime.getMinutes();

                    // Perform validation
                    if (!$scope.opData.mechanic) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Mechanic is a required field.'
                        });
                    }

                    if (!$scope.opData.reporter) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Reporter is a required field.'
                        });
                    }

                    if (!$scope.opData.time1) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Up Time is invalid.'
                        });
                    } else {
                        var hours4 = $scope.opData.time1.getHours(), minutes4 = $scope.opData.time1.getMinutes();
                        if (hours4 > time.getHours() || (hours4 == time.getHours() && minutes4 > time.getMinutes())) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Up Time cannot be later than current time.'
                            });
                        }
                        
                        var downTimeActual = pieceOfEquipment.upDownHistory[0].conditions[0].lastModifiedActual,
                            downTimeSystem = pieceOfEquipment.upDownHistory[0].conditions[0].lastModifiedASystem,
                            downTime = downTimeActual ? new Date(downTimeActual) : new Date(lastModifiedASystem),
                            upTime = new Date();
                        upTime.setHours($scope.opData.time1.getHours());
                        upTime.setMinutes($scope.opData.time1.getMinutes());
                        var df = moment(downTime).diff(upTime, 'minutes');
                        if (df >= 0) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Up Time cannot be prior to latest down condition time.'
                            });
                        }
//                        if( $scope.opData.time1.getTime() < signOffTime.getTime()){
//                            $scope.opData.clientErrors.push({
//                                type : 'danger',
//                                message : 'Up Time cannot be prior to sign off time.'
//                            });
//                        }   
                    }
                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                    }

                    // Perform operation
                    OpsBoardRepository.upEquipment(pieceOfEquipment, $scope.upData, success, error);
                };

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-equipment-up',
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

                $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate' ];
                $scope.format = $scope.formats[0];

                // get Data to be passed to backend from opData which has much more than that
                function getUpData(opData) {
                    return {
                        mechanic : opData.mechanic,
                        reporter : opData.reporter,
                        dt1 : $scope.board.date,
                        time1 : $filter('date')(opData.time1, 'HH:mm')
                    };
                }
            };
        })