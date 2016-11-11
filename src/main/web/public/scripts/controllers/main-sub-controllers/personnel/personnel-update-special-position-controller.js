'use strict';

angular
    .module('OpsBoard')
    .controller(
        'UpdateSpecialPosition',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, groups, BoardDataService) {
           
            $scope.updateSpecialPosition = function(person, specialPosition) {
                
            var name = person.fullName;
            var personnelSpecialPositionsList = BoardDataService.getPersonnelSpecialPositionsList();
            
            var startDate = new Date($scope.board.displayDate);
            $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY' , "HH:mm"];
            $scope.format = $scope.formats[4];
            
            function getFormattedSpecialPositionsList(list) {
                var code;
            	var formttedList = [];
                list.forEach(function(value, key) {
                            formttedList.push({
                                descr : value.longDescription,
                                code : value.code
                            })
                   
                })
                return formttedList;
            }
            
            var formattedpersonnelSpecialPositionsList = getFormattedSpecialPositionsList(personnelSpecialPositionsList);	
                
            $scope.datepickers = {
                startDateOpened : false,
                appointmentDateOpened : false
            }

            $scope.open = function($event, opened) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.datepickers[opened] = true;
            };

            $scope.remarksMaxLength = 150;
            var d = new Date();
            d.setHours(0);
            d.setMinutes(0);
            
            var endTime = new Date();
            endTime.setHours(23);
            endTime.setMinutes(59);
            
            
                var sd, ed, et;
                var remarks = specialPosition.comments != "null" ? specialPosition.comments : '';
                var displaySpecialPositionCode = specialPosition.description+"-"+specialPosition.code;
                
                $scope.opData = {
                    titleAction : 'Update Special Position',
                    titleEntity : name,
                    name : name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Update',
                    legend: 'Update Special Position',
                    clientErrors : [],
                    errors : [],
                    id : specialPosition.id,
                    personnelSpecialPositionsList : formattedpersonnelSpecialPositionsList,
                    startDate : specialPosition.startDate ? moment(new Date(specialPosition.startDate)).format($scope.formats[5]) : '',
                    endDate : specialPosition.endDate != "" ? moment(new Date(specialPosition.endDate)).format($scope.formats[5]): '',
                    progress : 100,
                    displaySpecialPositionCode : specialPosition.description+"--"+specialPosition.code,
                    required : [],
                    remarks : remarks
                   
                    
                }

                $scope.resetEndTime = function() {
                    if(!$scope.opData.endDate)
                        $scope.opData.endTime = d;
                    else 
                        $scope.opData.endTime = endTime;
                }
                $scope.updated = false;
                $scope.onChange = function(){
                	$scope.updated = true;
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
                                message : 'This Board is not for the current date.  To update Special Position, please load the Board for the current date.'
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
                
                $scope.master= angular.copy(getSpecialPositionData($scope.opData));
                $scope.changeOccurred = function() {
                    return OpsBoardRepository.isSame($scope.master, $scope.specialPositionData);
                }

               $scope.submitted = false;
                
               var modalInstance = $modal.open({
                   templateUrl : appPathStart + '/views/modals/modal-add-special-positions',
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
                     $scope.specialPositionData = getSpecialPositionData($scope.opData);
                     var validStartDate = false;
                     var validEndDate = false;
                    
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
                     
                     if(validStartDate){
                    	 $scope.opData.startDate.setHours(new Date().getHours());
                         $scope.opData.startDate.setMinutes(new Date().getMinutes());
                     }

                     if (!$scope.opData.specialPositionCode && $scope.opData.specialPositionCode == "") {
                         $scope.opData.clientErrors.push({
                             type : 'danger',
                             message : 'Special Position is a required field.'
                         });
                         $scope.opData.required.push('specialPositionCode');
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
                     
                     if (validStartDate && validEndDate && 
                    		 moment(moment($scope.opData.endDate).format('MM/DD/YYYY')).diff(moment(moment($scope.opData.startDate).format('MM/DD/YYYY'))) < 0) {
                             $scope.opData.clientErrors.push({
                                 type : 'danger',
                                 message : 'End Date cannot be less than Start Date.'
                             });
                         }
                     
                     if(!$scope.updated)
                    	 {
                         $scope.opData.clientErrors.push({
                             type : 'danger',
                             message : 'No changes were made'
                         });
                     }
                     
                  // Validation for another code at the same time
                     var activeSpecialPositions = person.specialPositionsHistory;
                     if (activeSpecialPositions.length) {

                    	 var sd = moment($scope.opData.startDate).format('MM/DD/YYYY');
                         var ed;
                         if($scope.opData.endDate == "")
                             ed = moment(new Date()).format('MM/DD/YYYY');
                         else
                             ed = moment($scope.opData.endDate).format('MM/DD/YYYY');
                         console.log(sd);
                         console.log(ed);
                        
                         for (var i = 0; i < activeSpecialPositions.length; i++) {
                              if (activeSpecialPositions[i].status === 'A') {
                                
                            	 var activeStartDate =  null;
                            	 var activeEndDate =  null;
                            	 
                            	 if($scope.opData.id == activeSpecialPositions[i].id)
                            		 continue;
                            	 
                            	 if(activeSpecialPositions[i].startDate && (activeSpecialPositions[i].startDate != "")){
                            		 activeStartDate = moment(activeSpecialPositions[i].startDate).format('MM/DD/YYYY');
                            	 }
                            	 
                            	 if(activeSpecialPositions[i].endDate && (activeSpecialPositions[i].endDate != "")){
                            		 activeEndDate =   moment(activeSpecialPositions[i].endDate).format('MM/DD/YYYY');
                            	 }
                            	 
                                 if (moment(sd).isValid() && moment(activeStartDate).isValid()
                                     && moment(activeStartDate).diff(moment(sd), 'days', true) <= 0) {
                                     if (!moment(activeEndDate).isValid() && !$scope.opData.clientErrors.length) {
                                         $scope.opData.clientErrors.push({
                                             type : 'danger',
                                             message : 'Special Position for this date range already exists'
                                         });
                                         break;
                                        
                                     } else {
                                         if (moment(activeEndDate).diff(moment(sd), 'days', true) >= 0
                                             && !$scope.opData.clientErrors.length) {
                                             $scope.opData.clientErrors.push({
                                                 type : 'danger',
                                                 message : 'Special Position for this date range already exists'
                                             });
                                             break;
                                         }
                                     }
                                 }

                                 if (moment(sd).isValid() && moment(activeStartDate).isValid()
                                     && moment(activeStartDate).diff(moment(sd), 'days', true) > 0) {
                                     if (!$scope.opData.endDate && !$scope.opData.clientErrors.length) {
                                         $scope.opData.clientErrors.push({
                                             type : 'danger',
                                             message : 'Special Position for this date range already exists'
                                         });
                                         break;
                                     } else {
                                    	 
                                         if (moment(activeStartDate).diff(moment(ed), 'days', true) <= 0
                                             && !$scope.opData.clientErrors.length) {
                                             $scope.opData.clientErrors.push({
                                                 type : 'danger',
                                                 message : 'Special Position for this date range already exists'
                                             });
                                             break;
                                         }
                                     }
                                 }
                             }
                         }
                     } 
					// Validation for another code at the same time

                    if ($scope.changeOccurred()) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'No changes were made'
                        });
                    }
					
                     if ($scope.opData.clientErrors.length > 0) {


                         return error(new ClientSideError('ValidationError'));
                     }
                     OpsBoardRepository.updateSpecialPosition(person, $scope.specialPositionData, success, error);
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

                function getSpecialPositionData(opData) {
                	var specialPositionDescr = opData.displaySpecialPositionCode.substring(0, $scope.opData.displaySpecialPositionCode.indexOf("--"));
                	var specialPositionCode = opData.displaySpecialPositionCode.substring($scope.opData.displaySpecialPositionCode.indexOf("--")+2);
                    
                    return {
                        startDate : $scope.opData.startDate ? moment($scope.opData.startDate).format($scope.formats[5]) : '',
                        id : $scope.opData.id,
                        endDate : $scope.opData.endDate ? moment($scope.opData.endDate).format($scope.formats[5]) : '',
                        code : specialPositionCode,
                        description : specialPositionDescr,
                        comments : $scope.opData.remarks
                    }
                }
            };
        })