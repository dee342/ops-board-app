'use strict';

angular
    .module('OpsBoard')
    .controller(
        'RemovePersonnelRecord',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, BoardDataService) {
            $scope.removeStatus = function(person, selectedCode, type) {
            
                var name = person.fullName;
                $scope.remarksMaxLength = 150;

                $scope.passPreValidation = function(){

                	if (type == 'DETACH') {
                		return true;
                	}
                    // Validate board date is current date
                    var current = new Date();
                    var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
                    var boardDate = new Date($scope.board.displayDate);
                    var messageMDA='This Board is not for the current date.  To remove MDA code, please load the Board for the current date.';
                    var messageUna='This Board is not for the current date.  To remove unavailable code, please load the Board for the current date.';
                    var messageSpe='This Board is not for the current date.  To remove special position code, please load the Board for the current date.';
                    var warnMessage='';
                    if (type == 'MDA') {
                        warnMessage = messageMDA;
                    } else if (type == 'UNAVAILABLE') {
                        warnMessage = messageUna;
                    } else if (type == 'SWSPECIALPOSITION') {
                        warnMessage = messageSpe;
                    }
                    
                    var bData = BoardDataService.getBoardData();
                    if (!moment().subtract(1, 'minutes').isBetween(moment(bData.startDate), moment(bData.endDate), 'minutes')) {
                        $scope.opData.errors
                            .push({
                                type : 'danger',
                                message : warnMessage
                            });
                        return false;
                    }

                    return true;
                }
                
                if(type == 'MDA'){
                	 $scope.opData = {
                             
                             titleAction : 'Remove',
                             titleEntity : name,
                             cancelButtonText : 'Cancel',
                             submitButtonText : 'Remove',
                             clientErrors : [],
                             errors : [],
                             subType: selectedCode.subType,
                             id :  selectedCode.id,
                             startDate: selectedCode.startDate,
                             reasonForChange : selectedCode ? selectedCode.reasonForChange : '',
                             progress : 100
                         }

                }else if (type == 'UNAVAILABLE'){
                	 $scope.opData = {
                             
                             titleAction : 'Remove',
                             titleEntity : name,
                             cancelButtonText : 'Cancel',
                             submitButtonText : 'Remove',
                             clientErrors : [],
                             errors : [],
                             code: selectedCode.code,
                             startDate: selectedCode.startDate,
                             //changed from sorid to id
                             id :  selectedCode.id,
                             reasonForChange :'',
                             progress : 100
                         }

                }else if (type == 'SWSPECIALPOSITION'){
                	 $scope.opData = {
                             
                             titleAction : 'Remove',
                             titleEntity : name,
                             cancelButtonText : 'Cancel',
                             submitButtonText : 'Remove',
                             clientErrors : [],
                             errors : [],
                             specialPositionCode:selectedCode.code,
                             id :  selectedCode.id,
                             startDate: selectedCode.startDate,
                             reasonForChange : selectedCode ? selectedCode.reasonForChange : '',
                             progress : 100
                         }

                }else if (type == 'DETACH'){
               	 $scope.opData = {
                         
                         titleAction : 'Remove',
                         titleEntity : name,
                         cancelButtonText : 'Cancel',
                         submitButtonText : 'Remove',
                         clientErrors : [],
                         errors : [],
                         id :  selectedCode.id,
                         startDate: selectedCode.startDate,
                         reasonForChange : selectedCode ? selectedCode.reasonForChange : '',
                         progress : 100
                     }

            }
                
                $scope.opData.btnType = "Remove";
                
                
                $scope.submitted = false;
                
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-personnel-remove-unavail-status',
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
                    $scope.personnelRecordData = getPersonnelRecordData($scope.opData);

                    var validStartDate = false, validEndDate = false;

                    if (!$scope.opData.reasonForChange) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Reason For Change is a required field.'
                        });
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                    }
                   
                    if(type == 'MDA'){
                    	OpsBoardRepository.removePersonMdaStatus(person, $scope.personnelRecordData, success, error);
                    }else if (type == 'UNAVAILABLE'){
                    	 OpsBoardRepository.removePersonUnavailabilityCode(person, $scope.personnelRecordData, success, error);
                    }else if (type == 'SWSPECIALPOSITION'){
                    	 OpsBoardRepository.removeSpecialPosition(person, $scope.personnelRecordData, success, error);
                    }else if (type == 'DETACH'){
                    	 OpsBoardRepository.cancelDetach(person, $scope.personnelRecordData, success, error);
                    }
                   
                };  

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });

                function getPersonnelRecordData() {
                	   if(type == 'MDA'){
                		   return {
                               subType: $scope.opData.subType,
                               //changed from sorid to id
                			   id   : $scope.opData.id,
                               startDate: $scope.opData.startDate,
                               reasonForChange : $scope.opData.reasonForChange
                           }
                       }else if (type == 'UNAVAILABLE'){
                    	   return {
                               code: $scope.opData.code,
                    		   id : $scope.opData.id,
                    		   startDate: $scope.opData.startDate,
                               reasonForChange : $scope.opData.reasonForChange
                           }
                       }else if (type == 'SWSPECIALPOSITION'){
                    	   return {
                    		   code:$scope.opData.specialPositionCode,
                    		   id   : $scope.opData.id,
                               startDate: $scope.opData.startDate,
                    		   reasonForChange : $scope.opData.reasonForChange
                           }
                       }else if (type == 'DETACH'){
                    	   return {
                    		   id   : $scope.opData.id,
                               startDate: $scope.opData.startDate,
                    		   reasonForChange : $scope.opData.reasonForChange
                           }
                       }
                	
                	
                	
                }
            }
        })