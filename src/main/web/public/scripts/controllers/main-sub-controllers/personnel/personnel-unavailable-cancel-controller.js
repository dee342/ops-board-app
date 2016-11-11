'use strict';

angular
    .module('OpsBoard')
    .controller(
        'CancelPersonnelUnavailableStatus',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, BoardDataService) {
            $scope.previousModalInstance;
            $scope.cancelUnavailableStatus = function(person, reason) {               
                var name = person.fullName;
                if(name.length>30)
                     name=name.substring(0,30);              
                var endTime=new Date();
                var startTime=new Date();
                /* $scope.minDate = date; */
                $scope.person = person;
                $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY' ];
                $scope.oriStartTime=reason.startTime;
                $scope.minDate=new Date(reason.startDate.substring(6,10), reason.startDate.substring(0,2)-1, reason.startDate.substring(3,5));
                if(reason.endDate=="")
                     $scope.maxDate='';
                else
                	$scope.maxDate=new Date(reason.endDate.substring(6,10), reason.endDate.substring(0,2)-1, reason.endDate.substring(3,5));
                if(startTime<$scope.minDate){
                	startTime=$scope.minDate;
                	endTime=$scope.minDate;
                }
                if($scope.maxDate<$scope.minDate)
                	$scope.maxDate=$scope.minDate;
                $scope.format = $scope.formats[5];
                $scope.remarksMaxLength = 150;                      
                startTime.setHours(0);
                startTime.setMinutes(0);
                    endTime.setHours(23);
                    endTime.setMinutes(59);
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
                                message : 'This Board is not for the current date.  To cancel unavailable status, please load the Board for the current date.'
                            });
                        return false;
                    }
//                    else if(reason==''){
//
//                        $scope.opData.errors
//                            .push({
//                                type : 'danger',
//                                message : 'There is no valid unavailable code to cancel'
//                            });
//                        return false;                       
//                    }
                    return true;
                }

                $scope.opData = {                    
                    titleAction : 'Cancel Unavailable',
                    titleEntity : name,
                    name:name,
                    legend:'Cancel Unavailable Code',
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Confirm',                
                    clientErrors : [],
                    serverErrors:[],
                    errors : [],          
                    unavailableCode : reason ? reason.code : '',
                    startDate :moment(startTime).format($scope.format),
                    startTime : $scope.oriStartTime,
                    endDate :  moment(endTime).format($scope.format),
                    endTime : endTime,
                    id :  reason ? reason.id : '',          
                    remarks : '',
                    progress : 100,
                    required : []
                }
         
           
                $scope.resetcancelTime = function() {
                     if($scope.opData.startDate){
                           $scope.opData.endTime = $scope.opData.startDate;
                           $scope.opData.endTime.setHours(23);
                           $scope.opData.endTime.setMinutes(59);
                           $scope.opData.startDate.setHours(0);
                           $scope.opData.startDate.setMinutes(0);
                     
                     }
                        
                };
                
                
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-personnel-cancel-unavail-status',
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
                    $scope.opData.clientErrors = []; 
                    $scope.opData.serverErrors = [];
                    $scope.opData.required = [];
                    $scope.opData.startDate = $scope.opData.startDate ? (new Date($scope.opData.startDate)) : '';
                    $scope.unavailabilityData = getUnavailabilityData($scope.opData);
   
                    if ($scope.opData.clientErrors.length > 0) {
                    $scope.opData.startDate=moment($scope.opData.startDate).format($scope.format);
                        return error(new ClientSideError('ValidationError'));
                    }
                    
                    if(reason)                         
                     OpsBoardRepository.cancelPersonUnavailabilityCode(person, $scope.unavailabilityData, success, error);
                    $scope.opData.startDate=moment($scope.opData.startDate).format($scope.format);
                };  

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });
                
                function getUnavailabilityData() {
                    return {
                        code : $scope.opData.unavailableCode,
                        action : 'Cancelled',
                        startDate : $scope.opData.startDate,
                        startTime : $scope.opData.startTime,
                        endDate : $scope.opData.endDate,
                        endTime : $scope.opData.endTime,
                        //changed from sorid to id
                        id   : $scope.opData.id,
                        remarks : $scope.opData.remarks
                    }
                };
                
            
            }

        })
