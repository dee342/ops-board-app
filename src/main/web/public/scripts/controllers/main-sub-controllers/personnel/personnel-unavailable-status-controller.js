'use strict';

angular
    .module('OpsBoard')
    .controller(
        'SetPersonnelUnavailableStatus',
        function($rootScope, $scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, BoardDataService) {
            $scope.previousModalInstance;
            $scope.setUnavailableStatus = function(person, selectedCode) {
                
                var name = person.fullName;
                if(name.length>30)
                	name=name.substring(0,30);
                var address = person.getHomeAddress();
                var homePhone = person.getPhone('home');

                var date = new Date();
                $scope.minDate = date;
                $scope.person = person;

                $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY' ];
                $scope.format = $scope.formats[4];
                
                var d = new Date();
                d.setHours(0);
                d.setMinutes(0);
                
                var startTime = moment(new Date($scope.board.displayDate)).diff(moment(new Date()), 'days') !== 0 ? d
                    : date;

                var endTime = new Date();
                endTime.setHours(23);
                endTime.setMinutes(59);
             

                var pageEdited = false;
                $scope.changeOccurred = function(){
                	pageEdited = true;
                };

                $scope.remarksMaxLength = 150;

                $scope.isRequired = function(element) {
                    $scope.submitted = false;
                    if ($scope.opData.required.indexOf(element) > -1) {
                        return true;
                    }
                    return false;
                };
                
                var sd, ed;
                $scope.update = false;

                if (selectedCode) {
                    if(selectedCode.code === 'JURY DUTY' || selectedCode.code === 'CHART' || selectedCode.code === 'VACATION') {
                        $scope.update = true;
                    }

                    sd = new Date(selectedCode.startDate + " " + selectedCode.startTime);
                    ed = selectedCode.endDate ? new Date(selectedCode.endDate + " " + selectedCode.endTime) : '';
                    endTime=ed;
                }

                 $scope.passPreValidation = function(){

                    // Validate board date is current date
                    var current = new Date();
                    var action = $scope.update ? 'update' : 'set';
                    var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
                    var boardDate = new Date($scope.board.displayDate);
                    var bData = BoardDataService.getBoardData();
                    if (!moment().subtract(1, 'minutes').isBetween(moment(bData.startDate), moment(bData.endDate), 'minutes')) {
                        $scope.opData.errors
                            .push({
                                type : 'danger',
                                message : 'This Board is not for the current date.  To ' + action + ' unavailable status, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                }

                $scope.opData = {
                    
                    titleAction : selectedCode ? 'Update Unavailable' : 'Set Unavailable',
                    titleEntity : name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : selectedCode ? 'Update' : 'Add',
                    legend: selectedCode ? 'Update Unavailable Code' : 'Add New Unavailable Code',
                    clientErrors : [],
                    errors : [],
                    name : name,
                    address : address,
                    homePhone : homePhone,
                    civilServiceTitle : person.civilServiceTitle,
                    chartInfo : getChartInfo(person),
                    referenceNum : person.referenceNum,
                    workLocation : person.workLocation,
                    unavailableCode : selectedCode ? selectedCode.code : '',
                    unavailabilityCodeList : $scope.unavailabilityCodeList,
                    startDate : sd ? moment(sd).format($scope.formats[5]) : moment(new Date($scope.board.displayDate)).format($scope.formats[5]),
                    startTime : sd ? sd : startTime,
                    endDate : ed ? moment(ed).format($scope.formats[5]): '',
                    endTime : ed ? ed : d,
                    //changed from sorid to id
                    id :  selectedCode ? selectedCode.id : '',
                    reasonForChange :'',
                    remarks : selectedCode ? selectedCode.comments : '',
                    progress : 100,
                    required : []
                }

                $scope.resetEndTime = function() {
                	pageEdited = true;
                	if(!$scope.opData.endDate)
                        $scope.opData.endTime = d;
                    else 
                        $scope.opData.endTime = endTime;
                }
                
                $scope.master= angular.copy(getUnavailabilityData($scope.opData));
                $scope.changesOccurred = function() {
                    return OpsBoardRepository.isSame($scope.master, $scope.unavailabilityData);
                }
                $scope.resetEffectiveTime = function() {
                    $scope.opData.startTime = d;
                    pageEdited = true;
                };          

                $scope.submitted = false;
                
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-personnel-unavail-status',
                    controller : 'ModalCtrl',
                    backdrop : 'static',
                    resolve : {
                        data : function() {
                            return [ person ]
                        }
                    },
                    scope : $scope
                });

                $scope.operation = function(success, error) {
                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];
                    $scope.opData.serverErrors = [];
                    $scope.opData.required = [];
                    $scope.opData.startDate = $scope.opData.startDate ? (new Date($scope.opData.startDate)) : '';
                    $scope.unavailabilityData = getUnavailabilityData($scope.opData);

                    var validStartDate = false, validEndDate = false;

                    // Validate received by
                    if (!$scope.opData.unavailableCode) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Unavailable Code is a required field.'
                        });
                        $scope.opData.required.push('unavailableCode');
                    }

                    if (!$scope.opData.reasonForChange && $scope.update) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Reason For Change is a required field.'
                        });
                        $scope.opData.required.push('reasonForChange');
                    }

                    if (!$scope.opData.startTime) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Invalid Start Time.'
                        });
                        $scope.opData.required.push('startTime');
                    }

                    if (!$scope.opData.endTime) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Invalid End Time.'
                        });
                        $scope.opData.required.push('endTime');
                    }

                    if (!$scope.opData.startDate) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Start Date is a required field.'
                        });
                        $scope.opData.required.push('startDate');
                    } else {
                        if (!moment($scope.opData.startDate).isValid()) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Start Date is invalid.'
                            });
                            $scope.opData.required.push('startDate');
                        } else {
                            validStartDate = true;
                        }
                    }
                    
                    if ($scope.opData.endDate) {
                        if (!moment($scope.opData.endDate).isValid()) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'End Date is invalid.'
                            });
                            $scope.opData.required.push('endDate');
                        } else {
                            validEndDate = true;
                        }
                    }
                    
                    if (!$scope.opData.endDate && $scope.opData.endTime) {
                        if($scope.opData.endTime.getHours() != 0 || $scope.opData.endTime.getMinutes() != 0)
                        {
                            $scope.opData.clientErrors.push({type: 'danger', message : 'Cannot change end time if end date is not specified.'});
                            $scope.opData.required.push('endTime');
                        }
                        
                    }

                    if (validStartDate && validEndDate
                        && (moment(moment($scope.opData.endDate).format('MM/DD/YYYY')).diff(moment(moment($scope.opData.startDate).format('MM/DD/YYYY'))) < 0)) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Start Date cannot be greater than End Date.'
                        });
                        $scope.opData.required.push('startDate');
                    }
                    
                    if (validStartDate && validEndDate
                            && (moment(moment($scope.opData.startDate).format('MM/DD/YYYY')).diff(moment(moment().format('MM/DD/YYYY'))) < 0) && !$scope.opData.remarks) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Start Date is in past. Please enter comments'
                            });
                            $scope.opData.required.push('comments');
                        }
                    if (validStartDate
                            && (moment(moment($scope.opData.startDate).format('MM/DD/YYYY')).diff(moment(moment().format('MM/DD/YYYY'))) < 0) && !$scope.opData.endDate && !$scope.opData.remarks) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Start Date is in past. Please enter comments'
                            });
                            $scope.opData.required.push('comments');
                        }
                    
                    if (validStartDate && validEndDate &&
                        ((moment($scope.opData.startDate).diff(moment($scope.opData.endDate)) === 0)
                        && (moment($scope.opData.endTime).format('HH:mm') < moment($scope.opData.startTime).format('HH:mm'))
                        )) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'End Time cannot be less than Start time.'
                        });
                        $scope.opData.required.push('startTime'); 
                    }

                    if (validStartDate && validEndDate &&
                        (moment($scope.opData.startDate).format() === moment($scope.opData.endDate).format()
                        && moment($scope.opData.startTime).format('HH:mm') === moment($scope.opData.endTime).format('HH:mm')
                        )) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'End Time cannot be equal to Start time.'
                        });
                        $scope.opData.required.push('startTime'); 
                    }
                    
                    if(($scope.opData.unavailableCode === 'JURY DUTY' || $scope.opData.unavailableCode === 'CHART' || $scope.opData.unavailableCode === 'VACATION') && !$scope.opData.endDate) {
                    	 $scope.opData.clientErrors.push({
                             type : 'danger',
                             message : 'Cannot create '+ $scope.opData.unavailableCode +' as end date cannot be empty.'
                         });
                         $scope.opData.required.push('endDate');
                    }
                    
                    if ($scope.opData.unavailableCode === 'CHART' && (($scope.opData.startDate && $scope.opData.startDate.getDay() === 0) || ($scope.opData.endDate && $scope.opData.endDate.getDay() === 0))) {
                    	$scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Cannot create '+ $scope.opData.unavailableCode +' on Sunday.'
                        });
                    	$scope.opData.required.push('unavailableCode');
                    }
                    
                    var newStartDay = moment(moment($scope.opData.startDate).format('MM/DD/YYYY'));
                    var newEndDay = moment(moment($scope.opData.endDate).format('MM/DD/YYYY'));
                    var unavailHist = person.unavailabilityHistory;

                    var isUnavailableInRange = false;
                    
                    if (!$scope.opData.clientErrors.length && unavailHist.length && moment(newStartDay).isValid()) {
                        for (var i = 0; i < unavailHist.length; i++) {
                            if (unavailHist[i].id !== $scope.opData.id) {
                                if (unavailHist[i].code === $scope.opData.unavailableCode && unavailHist[i].status === 'A') {
                                  var hStartDay = moment(moment(unavailHist[i].start).format('MM/DD/YYYY'));
                                  if (hStartDay.isValid()) {
                                    var hEndDay = !unavailHist[i].end ? null : moment(moment(unavailHist[i].end).format('MM/DD/YYYY'));
                                      if (hEndDay.diff(newStartDay, 'days') < 0) {
                                        // history record is prior to new record
                                        continue;
                                    }

                                    if (hStartDay.diff(newEndDay, 'days') > 0) {
                                        // history record is after new record
                                        continue;
                                    }

                                    isUnavailableInRange = true;
                                    $scope.opData.required.push('startDate');
                                  }
                                }
                            }
                        }
                        
                        if(isUnavailableInRange){
                        	$scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Unavailable Code for this calendar date range already exists.'
                              });
                        }
                    }
                    
                    if ($scope.changesOccurred()) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'No changes were made'
                        });
                    }
                    if(!pageEdited && selectedCode){
                    	$scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'There are no changes to save'
                        });
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                    	if($scope.opData.startDate!="")
                    	$scope.opData.startDate=moment($scope.opData.startDate).format($scope.formats[5]);
                    	if($scope.opData.endDate)
                    	$scope.opData.endDate=moment($scope.opData.endDate).format($scope.formats[5]);
                        return error(new ClientSideError('ValidationError'));
                    }
                    
                    if(selectedCode)
                    	OpsBoardRepository.updatePersonUnavailabilityCode(person, $scope.unavailabilityData, success, error);
                    else
                    	OpsBoardRepository.createPersonUnavailabilityCode(person, $scope.unavailabilityData, success, error);
                    $scope.opData.startDate=moment($scope.opData.startDate).format($scope.formats[5]);
                };  

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                    $rootScope.$broadcast('UPDATE-PERSONNEL-PANE');

                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });

                $scope.unavailableSelect2Options = {
                    allowClear : false
                }

                $scope.unavailablePersonDateOpts = {
                    formatYear : 'yyyy',
                    startingDay : 0
                }

                function getChartInfo(person) {
                    var chartName = person.chartName, chartNumber = person.chartNumber;
                    if (!chartName && !chartNumber)
                        return '';
                    if (!chartNumber)
                        return chartName;
                    if (!chartName)
                        return chartNumber;
                    return chartName + ' / ' + chartNumber;
                }

                function getUnavailabilityData() {
                    return {
                        code : $scope.opData.unavailableCode,
                        action : 'add',
                        startDate : $scope.opData.startDate ? moment($scope.opData.startDate).format($scope.formats[5]) : '',
                        startTime : $scope.opData.startTime,
                        endDate : $scope.opData.endDate ? moment($scope.opData.endDate).format($scope.formats[5]) : '',
                        endTime : $scope.opData.endTime,
                        //changed from sorid to id
                        id   : $scope.opData.id,
                        reasonForChange : $scope.opData.reasonForChange,
                        remarks : $scope.opData.remarks
                    }
                }
            }
        })