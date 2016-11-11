'use strict';

angular
    .module('OpsBoard')
    .controller(
        'DownEquipment',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states, BoardDataService) {
            
            $scope.isEquipmentDown = function(pieceOfEquipment) {
                if (pieceOfEquipment && pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.down) {
                    return true;
                }
                return false;
            }

            function errorMessagesArrayContains(errorArray, error) {
                var found = false;
                for (var i = 0; i < errorArray.length; i++) {
                    var element = errorArray[i];
                    if (element && element.type == error.type && element.message == error.message) {
                        found = true;
                        break;
                    }
                }
                return found;
            }

            $scope.downEquipment =  function(pieceOfEquipment) {

                // Define detachment data and operation
                var conditions = [ 'Down' ];
                var downCodeOptions = [];
                var temp;
                var repairLocations = [];
                var equipmentDesc = [];
                temp = BoardDataService.getEquipmentDownCodes();
                for (var i = 0, len = temp.length; i < len; i ++) {
                    equipmentDesc.push({
                        id : temp[i].code,
                        text : temp[i].description
                    });
                    downCodeOptions.push({
                        label: temp[i].description + ' - ' + temp[i].code,
                        code: temp[i].code
                    });
                }

                temp = BoardDataService.getRepairLocations();
                for (var i = 0; i < temp.length; i++) {
                    repairLocations.push(temp[i].code);
                }

                // Define modal view parameters
                $scope.submitted = false;
                // Marc commented on pull request #695: We should just set the opData property currentLocation to pieceOfEquipment.currentLocation
                $scope.opData = {
                    titleAction : 'Down Equipment',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Down',
                    clientErrors : [],
                    errors : [],
                    equipmentName : pieceOfEquipment.name,
                    equipmentType : pieceOfEquipment.subType,
                    owner : pieceOfEquipment.owner,
                    currentLocation : pieceOfEquipment.currentLocation,
                    license : pieceOfEquipment.licensePlate,
                    conditions : conditions,
                    condition : conditions[0],
                    time1 : new Date(),
                    time2 : new Date(),
                    time3 : new Date(),
                    dt1 : $scope.board.displayDate,
                    dt2 : $scope.board.displayDate,
                    dt3 : $scope.board.displayDate,
                    progress : 100,
                    downCodeOptions : downCodeOptions,
                    repairLocations : repairLocations,
                    selectedDownCode1 : '',
                    selectedDownCode2 : '',
                    selectedDownCode3 : '',
                    selectedRepairLocation1 : pieceOfEquipment.currentLocation,
                    selectedRepairLocation2 : pieceOfEquipment.currentLocation,
                    selectedRepairLocation3 : pieceOfEquipment.currentLocation,
                    required : []
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
                                message : 'This Board is not for the current date.  To down Equipment, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                }

                $scope.isRequired = function(element) {
                    $scope.submitted = false;
                    if ($scope.opData.required.indexOf(element) > -1)
                        return true;
                    return false;
                };

                $scope.operation = function(success, error) {
                    $scope.submitted = true;
                    $scope.opData.clientErrors = []; // clear errors
                    $scope.downData = getDownData($filter, $scope.opData, equipmentDesc, $scope.board.displayDate);
                    $scope.opData.required = [];
                    var time = new Date(), sgHour = signOffTime.getHours(), sgTime = signOffTime.getMinutes();

                    // Perform validation

                    if (!$scope.opData.selectedDownCode1) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Code 1 is a required field.'
                        });
                        $scope.opData.required.push('selectedDownCode1');
                    }

                    if ($scope.opData.selectedDownCode1 && $scope.opData.selectedDownCode2
                        && ($scope.opData.selectedDownCode1 == $scope.opData.selectedDownCode2)) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Code 1 and Code 2 cannot be same.'
                        });
                        $scope.opData.required.push('selectedDownCode1');
                        $scope.opData.required.push('selectedDownCode2');
                    }

                    if ($scope.opData.selectedDownCode2 && $scope.opData.selectedDownCode3
                        && ($scope.opData.selectedDownCode2 == $scope.opData.selectedDownCode3)) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Code 2 and Code 3 cannot be same.'
                        });
                        $scope.opData.required.push('selectedDownCode2');
                        $scope.opData.required.push('selectedDownCode3');
                    }

                    if ($scope.opData.selectedDownCode3 && $scope.opData.selectedDownCode1
                        && ($scope.opData.selectedDownCode3 == $scope.opData.selectedDownCode1)) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Code 3 and Code 1 cannot be same.'
                        });
                        $scope.opData.required.push('selectedDownCode3');
                        $scope.opData.required.push('selectedDownCode1');
                    }

                    if (!$scope.opData.selectedRepairLocation1 || !$scope.opData.selectedRepairLocation1.id.trim()) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Repair Location is a required field.'
                        });
                        $scope.opData.required.push('selectedRepairLocation1');
                    }
                    if (!$scope.opData.time1) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Down Time for Code 1 is invalid.'
                        });
                        $scope.opData.required.push('time1');
                    } else {
                        
                        if (moment(new Date($scope.opData.dt1)).diff(moment(time)) > 0) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Down Time for Code 1 cannot be later than current date and time.'
                            });
                            $scope.opData.required.push('time1');
                        }
                        
                        if (pieceOfEquipment.upDownHistory && pieceOfEquipment.upDownHistory.length > 0) {
                            var upTime1Actual = pieceOfEquipment.upDownHistory[0].lastModifiedActual,
                                upTime1System = pieceOfEquipment.upDownHistory[0].lastModifiedASystem,
                                upTime1 = upTime1Actual ? new Date(upTime1Actual) : new Date(upTime1System),
                                downTime1 = new Date();
                            downTime1.setHours($scope.opData.time1.getHours());
                            downTime1.setMinutes($scope.opData.time1.getMinutes());

                            var df = moment(upTime1).diff(downTime1, 'minutes');  

                            if (df >= 0) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Down Time for Code 1 cannot be prior to latest up time.'
                                });
                            }
                        }
                        
//                        if (hours1 < sgHour || (hours1 == sgHour && minutes1 < sgTime)) {
//                        if( $scope.opData.time1.getTime() < signOffTime.getTime()){
//                            $scope.opData.clientErrors.push({
//                                type : 'danger',
//                                message : 'Down Time for Code 1 cannot be prior to sign off time.'
//                            });
//                            $scope.opData.required.push('time1');
//                        }
                    }
                    if (!$scope.opData.reporter1) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Reporter is a required field.'
                        });
                        $scope.opData.required.push('reporter1');
                    }
                    if (!$scope.opData.mechanic1) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Mechanic is a required field.'
                        });
                        $scope.opData.required.push('mechanic1');
                    }
                    
                    if ($scope.opData.selectedDownCode2) {
                        if (!$scope.opData.selectedRepairLocation2 || !$scope.opData.selectedRepairLocation2.id.trim()) {
                            if (!errorMessagesArrayContains($scope.opData.clientErrors, {
                                type : 'danger',
                                message : 'Repair Location is a required field.'
                            })) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Repair Location is a required field.'
                                });
                            }
                            $scope.opData.required.push('selectedRepairLocation2');
                        }
                        if (!$scope.opData.time2) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Down Time for Code 2 is invalid.'
                            });
                            $scope.opData.required.push('time2');
                        } else {
                            var hours2 = $scope.opData.time2.getHours(), minutes2 = $scope.opData.time2.getMinutes();
                            if (hours2 > time.getHours() || (hours2 == time.getHours() && minutes2 > time.getMinutes())) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Down Time for Code 2 cannot be later than current time.'
                                });
                                $scope.opData.required.push('time2');
                            }
                            
                            if (pieceOfEquipment.upDownHistory && pieceOfEquipment.upDownHistory.length > 0) {
                                var upTime2Actual = pieceOfEquipment.upDownHistory[0].lastModifiedActual,
                                    upTime2System = pieceOfEquipment.upDownHistory[0].lastModifiedASystem,
                                    upTime2 = upTime2Actual ? new Date(upTime2Actual) : new Date(upTime2System),
                                downTime2 = new Date();
                                downTime2.setHours($scope.opData.time2.getHours());
                                downTime2.setMinutes($scope.opData.time2.getMinutes());

                                var df = moment(upTime2).diff(downTime2, 'minutes');  

                                if (df >= 0) {
                                    $scope.opData.clientErrors.push({
                                        type : 'danger',
                                        message : 'Down Time for Code 2 cannot be prior to latest up time.'
                                    });
                                }
                            }
                            
//                            if( $scope.opData.time2.getTime() < signOffTime.getTime()){
//                                $scope.opData.clientErrors.push({
//                                    type : 'danger',
//                                    message : 'Down Time for Code 2 cannot be prior to sign off time.'
//                                });
//                                $scope.opData.required.push('time2');
//                            }
                        }
                        if (!$scope.opData.reporter2) {
                            if (!errorMessagesArrayContains($scope.opData.clientErrors, {
                                type : 'danger',
                                message : 'Reporter is a required field.'
                            })) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Reporter is a required field.'
                                });
                            }
                            $scope.opData.required.push('reporter2');
                        }
                        if (!$scope.opData.mechanic2) {
                            if (!errorMessagesArrayContains($scope.opData.clientErrors, {
                                type : 'danger',
                                message : 'Mechanic is a required field.'
                            })) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Mechanic is a required field.'
                                });
                            }
                            $scope.opData.required.push('mechanic2');
                        }
                    }

                    if ($scope.opData.selectedDownCode3) {
                        if (!$scope.opData.selectedRepairLocation3 || !$scope.opData.selectedRepairLocation3.id.trim()) {
                            if (!errorMessagesArrayContains($scope.opData.clientErrors, {
                                type : 'danger',
                                message : 'Repair Location is a required field.'
                            })) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Repair Location is a required field.'
                                });
                            }
                            $scope.opData.required.push('selectedRepairLocation3');
                        }
                        if (!$scope.opData.time3) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Down Time for Code 3 is invalid.'
                            });
                            $scope.opData.required.push('time3');
                        } else {
                            var hours3 = $scope.opData.time3.getHours(), minutes3 = $scope.opData.time3.getMinutes();
                            if (hours3 > time.getHours() || (hours3 == time.getHours() && minutes3 > time.getMinutes())) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Down Time for Code 3 cannot be later than current time.'
                                });
                                $scope.opData.required.push('time3');
                            }
                            
                            if (pieceOfEquipment.upDownHistory && pieceOfEquipment.upDownHistory.length > 0) {
                                var upTime3Actual = pieceOfEquipment.upDownHistory[0].lastModifiedActual,
                                    upTime3System = pieceOfEquipment.upDownHistory[0].lastModifiedASystem,
                                    upTime3 = upTime3Actual ? new Date(upTime3Actual) : new Date(upTime3System),
                                downTime3 = new Date;
                                downTime3.setHours($scope.opData.time3.getHours());
                                downTime3.setMinutes($scope.opData.time3.getMinutes());

                                var df = moment(upTime3).diff(downTime3, 'minutes');  

                                if (df >= 0) {
                                    $scope.opData.clientErrors.push({
                                        type : 'danger',
                                        message : 'Down Time for Code 2 cannot be prior to latest up time.'
                                    });
                                }
                            }
                            
//                            if( $scope.opData.time3.getTime() < signOffTime.getTime()){
//                                $scope.opData.clientErrors.push({
//                                    type : 'danger',
//                                    message : 'Down Time for Code 3 cannot be prior to sign off time.'
//                                });
//                                $scope.opData.required.push('time3');
//                            }
                        }
                        if (!$scope.opData.reporter3) {
                            if (!errorMessagesArrayContains($scope.opData.clientErrors, {
                                type : 'danger',
                                message : 'Reporter is a required field.'
                            })) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Reporter is a required field.'
                                });
                            }
                            $scope.opData.required.push('reporter3');
                        }
                        if (!$scope.opData.mechanic3) {
                            if (!errorMessagesArrayContains($scope.opData.clientErrors, {
                                type : 'danger',
                                message : 'Mechanic is a required field.'
                            })) {
                                $scope.opData.clientErrors.push({
                                    type : 'danger',
                                    message : 'Mechanic is a required field.'
                                });
                            }
                            $scope.opData.required.push('mechanic3');
                        }
                    }
                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                    }
                    // Perform operation
                    OpsBoardRepository.downEquipment(pieceOfEquipment, $scope.downData, success, error);
                };

                $scope.downCodeSelect2Options = {
                    allowClear : true
                }

                $scope.repairLocationSelect2Options = {
                    allowClear : true,
                    createSearchChoice : function(value, data) {
                        if ($(data).filter(function() {
                            if (/\W/g.test(value))
                                return 'error';
                            return this.text.localeCompare(value) === 0;
                        }).length === 0) {
                            return {
                                id : value,
                                text : value
                            };
                        } else {
                            return ''
                        }
                    },
                    initSelection : function(element, callback) {
                        var value = $(element).val();
                        if (value)
                            callback({
                                id : value,
                                text : value
                            });
                    },
                    query : function(query) {
                        // match
                        var items = $.grep($scope.opData.repairLocations, function(item) {
                            if (!query.term)
                                return true;
                            return query.matcher(query.term, item);
                        });

                        items = items.sort();
                        if (query.term)
                            items.push(query.term);

                        // map to {id: '', text: ''}
                        var data = {};
                        data.results = $.map(items, function(item) {
                            return {
                                id : item,
                                text : item
                            };
                        })

                        if (query.term.length && /\W/g.test(query.term))
                            data = {
                                results : [ {
                                    id : query.term.replace(/\W/g, ''),
                                    text : query.term.replace(/\W/g, '')
                                } ]
                            };
                        query.callback(data);
                    }
                };

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-equipment-down',
                    controller : 'ModalCtrl',
                    backdrop : 'static',
                    resolve : {
                        data : function() {
                            return [ pieceOfEquipment ]
                        }
                    },
                    windowClass : 'ng-Class: {{test()}}',
                    scope : $scope
                // set scope to current scope of Equipment Controller
                });

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });
            }

            /** Section - Helper methods - Start * */

            // Get data to be passed to backend (less than is contained in opData)
            function getDownData($filter, opData, equipmentDesc, displayDate) {
                var descr1, descr2, descr3; // better than creating new model and watching it for changes. That will involve more
                                            // loops.
                for (var j = 0; j < equipmentDesc.length; j++) {
                    if (equipmentDesc[j].id === opData.selectedDownCode1) {
                        descr1 = equipmentDesc[j].text;
                    }
                    if (equipmentDesc[j].id === opData.selectedDownCode2) {
                        descr2 = equipmentDesc[j].text;
                    }
                    if (equipmentDesc[j].id === opData.selectedDownCode3) {
                        descr3 = equipmentDesc[j].text;
                    }
                }
                return {
                    selectedDownCode1 : opData.selectedDownCode1,
                    selectedDownCode2 : opData.selectedDownCode2,
                    selectedDownCode3 : opData.selectedDownCode3,
                    selectedRepairLocation1 : opData.selectedRepairLocation1 ? opData.selectedRepairLocation1.id.substring(0, 30)
                        : '',
                    selectedRepairLocation2 : opData.selectedRepairLocation2 ? opData.selectedRepairLocation2.id.substring(0, 30)
                        : '',
                    selectedRepairLocation3 : opData.selectedRepairLocation3 ? opData.selectedRepairLocation3.id.substring(0, 30)
                        : '',
                    reporter1 : opData.reporter1,
                    reporter2 : opData.reporter2,
                    reporter3 : opData.reporter3,
                    mechanic1 : opData.mechanic1,
                    mechanic2 : opData.mechanic2,
                    mechanic3 : opData.mechanic3,
                    descr1 : descr1,
                    descr2 : descr2,
                    descr3 : descr3,
                    remarks1 : opData.remarks1,
                    remarks2 : opData.remarks2,
                    remarks3 : opData.remarks3,
                    dt1 : displayDate,
                    dt2 : displayDate,
                    dt3 : displayDate,
                    time1 : $filter('date')(opData.time1, 'HH:mm'),
                    time2 : $filter('date')(opData.time2, 'HH:mm'),
                    time3 : $filter('date')(opData.time3, 'HH:mm')
                };
            }

            /** Section - Helper methods - End **/

        })