'use strict';

angular
    .module('OpsBoard')
    .controller(
        'SetPersonnelMDAStatus',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, officerMdaCodes, unsupportedMdaCodes, BoardDataService) {
           
            $scope.setMdaStatus = function(person, selectedMdaType) {
                
            	$scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY' , "HH:mm"];
                $scope.format = $scope.formats[4];
            	
                var mdaStatus = null;
                if(selectedMdaType && selectedMdaType === 'UPDATE_ACTIVE'){
                	if(person.activeMdaCodes && (person.activeMdaCodes.length > 0))           		
                	    mdaStatus = person.activeMdaCodes[0];
                }else
                    mdaStatus = selectedMdaType;
                
            	var personnelStatusList = $scope.personnelStatusList, 
                persStatusListFrm = getFormattedList(personnelStatusList), 
                startDate = mdaStatus && mdaStatus.startDate ? new Date(mdaStatus.startDate) : new Date($scope.board.displayDate), 
                subType = mdaStatus ? mdaStatus.subType : '', 
                appointmentDate = mdaStatus && mdaStatus.appointmentDate ? new Date(mdaStatus.appointmentDate) : '', 
                remarks = mdaStatus ? mdaStatus.remarks: '', 
                name = person.fullName;
                if(name.length>30)
                	name=name.substring(0,30);

            $scope.datepickers = {
                startDateOpened : false,
                healthApptDateOpened : false,
                endDateOpened:false
            }

            $scope.open = function($event, opened) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.datepickers[opened] = true;
                if($scope.datepickers.healthApptDateOpened==true ){
                	$scope.datepickers.endDateOpened=false;
                }
                if($scope.datepickers.startDateOpened==true){
                	$scope.datepickers.healthApptDateOpened=false;
                	$scope.datepickers.endDateOpened=false;
                }
            };
            
            $scope.updated = false;
            $scope.onChange = function(isEndDate){
            	$scope.updated = true;
            	if(isEndDate){
            		$scope.resetEndTime();
            	}
            }

            $scope.remarksMaxLength = 150;
            var d = new Date();
            d.setHours(0);
            d.setMinutes(0);
            
            var endTime = new Date();
            endTime.setHours(23);
           
            
            
                var sd, ed, et;
                $scope.update = false;
                if (selectedMdaType) {
                    $scope.update = true;
                    sd = new Date(selectedMdaType.startDate);
                    ed = selectedMdaType.endDate ? new Date(selectedMdaType.endDate + " " + selectedMdaType.endTime) : '';
                    endTime=ed;
                           }           
                else{
                    endTime.setHours(23);
                    endTime.setMinutes(59);
                }

                $scope.opData = {
                    titleAction : selectedMdaType ? 'Update MDA' : 'Set MDA',
                    titleEntity : name,
                    name : name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : selectedMdaType ? 'Update' : 'Add',
                    legend: selectedMdaType ? 'Update MDA' : 'Add New MDA',
                    clientErrors : [],
                    errors : [],
                    personnelStatusList : persStatusListFrm,
                    startDate : startDate ? moment(startDate).format($scope.formats[5]) : '',
                    endDate : ed ? moment(ed).format($scope.formats[5]): '',
                    endTime : ed ? ed : d,
                    subType : subType,
                    appointmentDate : appointmentDate ? moment(appointmentDate).format($scope.formats[5]) : '',
                    //changed to id from sorid
                    id :  selectedMdaType ? selectedMdaType.id : '',
                    reasonForChange : selectedMdaType ? selectedMdaType.reasonForChange : '',
                    remarks : selectedMdaType ? selectedMdaType.comments : '',
                    progress : 100,
                    required : []
                    
                }

                $scope.resetEndTime = function() {
                    if(!$scope.opData.endDate)
                        $scope.opData.endTime = d;
                    else 
                        $scope.opData.endTime = endTime;
                }
                
                $scope.master= angular.copy(getMDAData($scope.opData));
                $scope.changeOccurred = function() {
                    return OpsBoardRepository.isSame($scope.master, $scope.mdaData);
                }

                $scope.passPreValidation = function(){

                    // Validate board date is current date
                    var current = new Date();
                    var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
                    var boardDate = new Date($scope.board.displayDate);
                    var bData = BoardDataService.getBoardData();
                    if (!moment().subtract(1, 'minutes').isBetween(moment(bData.startDate), moment(bData.endDate), 'minutes')) {
                        $scope.opData.errors
                            .push({
                                type : 'danger',
                                message : 'This Board is not for the current date.  To set MDA status, please load the Board for the current date.'
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
                
               $scope.submitted = false;
                
               var modalInstance = $modal.open({
                   templateUrl : appPathStart + '/views/modals/modal-mda-status',
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
                     $scope.mdaData = getMDAData($scope.opData);
                     var validStartDate = false;
                     var validEndDate = false;
                     var systemDate=new Date();

                     // Validate received by
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
                         }else{
                         	validStartDate = true;
                         }
                     }

                     if (!$scope.opData.subType) {
                         $scope.opData.clientErrors.push({
                             type : 'danger',
                             message : 'Type is a required field.'
                         });
                         $scope.opData.required.push('subType');
                     }
                     
                     if(!$scope.updated && $scope.update)
                	 {
                     $scope.opData.clientErrors.push({
                         type : 'danger',
                         message : 'No changes were made'
                     });
                 }
                     
                     if (!$scope.opData.endTime) {
                         $scope.opData.clientErrors.push({
                             type : 'danger',
                             message : 'Invalid End Time.'
                         });
                         $scope.opData.required.push('endTime');
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
                     		$scope.opData.clientErrors.push({type: 'danger', message : 'End date should be entered to set end time.'});
                     		$scope.opData.required.push('endTime');
                     	}
                     	
                     }

                     if (validStartDate && validEndDate
                         && moment($scope.opData.endDate).diff(moment($scope.opData.startDate), 'days') < 0) {
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
                     
                     // Validation for another code at the same time
                     //if(!$scope.update && !$scope.remove){
                         var activeMdaStatus = person.mdaStatusHistory;
                         if (activeMdaStatus.length) {
                             for (var i = 0; i < activeMdaStatus.length; i++) {
                            	 
                                 if (((activeMdaStatus[i].subType === $scope.opData.subType &&  !$scope.update && !$scope.remove) || activeMdaStatus[i].sorId !== $scope.opData.sorId)  && (activeMdaStatus[i].status === 'A'))  {

                                     var sd = moment($scope.opData.startDate).format('MM/DD/YYYY') + '/'
                                         + moment($scope.opData.startTime).format('HH:mm');
                                     var ed;
                                     if($scope.opData.endDate === "")
                                    	 ed = moment(new Date()).format('MM/DD/YYYY') + '/'
                                         + moment($scope.opData.endTime).format('HH:mm');
                                     else
                                    	 ed = moment($scope.opData.endDate).format('MM/DD/YYYY') + '/'
                                         + moment($scope.opData.endTime).format('HH:mm');
                                     console.log(sd);
                                     console.log(ed);
                                     if (moment(sd).isValid() && moment(activeMdaStatus[i].startDate).isValid()
                                         && moment(activeMdaStatus[i].startDate).diff(moment(sd), 'minutes') <= 0) {
                                         if (!activeMdaStatus[i].endDate && !$scope.opData.clientErrors.length) {
                                             $scope.opData.clientErrors.push({
                                                 type : 'danger',
                                                 message : 'MDA Status Code for this date range already exists'
                                             });
                                             $scope.opData.required.push('startDate');
                                         } else {
                                             if (moment(activeMdaStatus[i].endDate).diff(moment(sd), 'minutes') >= 0
                                                 && !$scope.opData.clientErrors.length) {
                                                 $scope.opData.clientErrors.push({
                                                     type : 'danger',
                                                     message : 'MDA Status Code for this date range already exists'
                                                 });
                                                 $scope.opData.required.push('startDate');
                                             }
                                         }
                                     }

                                     if (moment(sd).isValid() && moment(activeMdaStatus[i].start).isValid()
                                         && moment(activeMdaStatus[i].startDate).diff(moment(sd), 'minutes') > 0) {
                                         if (!$scope.opData.endDate && !$scope.opData.clientErrors.length) {
                                             $scope.opData.clientErrors.push({
                                                 type : 'danger',
                                                 message : 'MDA Status Code for this date range already exists'
                                             });
                                             $scope.opData.required.push('startDate');
                                         } else {
                                             if (moment(activeMdaStatus[i].startDate).diff(moment(ed), 'minutes') < 0
                                                 && !$scope.opData.clientErrors.length) {
                                                 $scope.opData.clientErrors.push({
                                                     type : 'danger',
                                                     message : 'MDA Status Code for this date range already exists'
                                                 });
                                                 $scope.opData.required.push('startDate');
                                             }
                                         }
                                     }
                                 }
                             }
                         }    
                    // }
                     // Validation for another code at the same time

                     
                     if (validStartDate && validEndDate && 
                    		 ($scope.opData.endDate.getFullYear()==systemDate.getFullYear()&&
                    				 $scope.opData.endDate.getMonth() == systemDate.getMonth()&&
                    				 $scope.opData.endDate.getDate() == systemDate.getDate())&&
                             ($scope.opData.endDate.getFullYear() == $scope.opData.startDate.getFullYear() &&
                                     $scope.opData.endDate.getMonth() == $scope.opData.startDate.getMonth() &&
                                     $scope.opData.endDate.getDate() == $scope.opData.startDate.getDate()) && 
                                     moment($scope.opData.endTime).format('HH:mm') < moment($scope.opData.startTime).format('HH:mm')) {
                             $scope.opData.clientErrors.push({
                                 type : 'danger',
                                 message : 'End Time cannot be less than Start time.'
                             });
                             $scope.opData.required.push('startTime'); 
                         }

                         if (validStartDate && validEndDate && 
                             ($scope.opData.endDate.getFullYear() == $scope.opData.startDate.getFullYear() &&
                                     $scope.opData.endDate.getMonth() == $scope.opData.startDate.getMonth() &&
                                     $scope.opData.endDate.getDate() == $scope.opData.startDate.getDate()) && 
                                     moment($scope.opData.endTime).format('HH:mm') == moment($scope.opData.startTime).format('HH:mm')) {
                             $scope.opData.clientErrors.push({
                                 type : 'danger',
                                 message : 'End Time cannot be same as Start time.'
                             });
                             $scope.opData.required.push('startTime'); 
                         }

                   if ($scope.changeOccurred()) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'No changes were made'
                        });
                    }
                     if ($scope.opData.clientErrors.length > 0) {
                         return error(new ClientSideError('ValidationError'));
                     }
                    if(selectedMdaType)
                    	OpsBoardRepository.updatePersonMdaStatus(person, $scope.mdaData, success, error);
                    else
                    	OpsBoardRepository.createPersonMdaStatus(person, $scope.mdaData, success, error);
                };  

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
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

                function getFormattedList(list) {
                    var sup = false;
                    if (person.civilServiceTitle)
                        if (groups.personnel.chiefs.indexOf(person.civilServiceTitle) >= 0
                            || groups.personnel.superintendents.indexOf(person.civilServiceTitle) >= 0
                            || groups.personnel.supervisors.indexOf(person.civilServiceTitle) >= 0)
                            sup = true;
                        else
                            sup = false;

                    var formttedList = [];
                    list.forEach(function(value, key) {
                        if (value.type === 'MDA') {
                        	if(unsupportedMdaCodes.indexOf(value.subType) == -1){
                    			if ((sup && officerMdaCodes.indexOf(value.subType) > -1) || (!sup && officerMdaCodes.indexOf(value.subType) ==-1))
	                                formttedList.push({
	                                    code : value.subType,
	                                    descr : value.descr
	                                })
                            }
                        }
                    })
                    return formttedList;
                }         
                function getMDAData(opData) {
                    var startDate = moment($scope.opData.startDate).isValid() ? moment($scope.opData.startDate).format($scope.formats[4].toUpperCase()) : '', 
                        appointmentDate = moment($scope.opData.appointmentDate).isValid() ? moment($scope.opData.appointmentDate).format($scope.formats[4].toUpperCase()) : '';
                    
                    return {
                        startDate : $scope.opData.startDate ? moment($scope.opData.startDate).format($scope.formats[5]) : '',
                        //changed from sorid to id
                        id : $scope.opData.id,
                        endDate : $scope.opData.endDate ? moment($scope.opData.endDate).format($scope.formats[5]) : '',

                        endTime : $scope.opData.endTime,
                        type : 'MDA',
                        subType : $scope.opData.subType,
                        appointmentDate : $scope.opData.appointmentDate ? moment($scope.opData.appointmentDate).format($scope.formats[5]) : '',
                        remarks : $scope.opData.remarks
                    }
                }
            };
        })