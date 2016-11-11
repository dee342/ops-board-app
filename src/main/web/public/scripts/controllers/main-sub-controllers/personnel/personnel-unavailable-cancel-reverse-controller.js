'use strict';

angular
    .module('OpsBoard')
    .controller(
        'ReverseCancelPersonnelUnavailableStatus',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, BoardDataService) {
            $scope.previousModalInstance;
            $scope.undoCancelUnavailableStatus = function(person, reason) {               
                var name = person.fullName;
                if(name.length>30)
                     name=name.substring(0,30);              
                
                /* $scope.minDate = date; */
                $scope.person = person;
                $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY' ];
                $scope.oriStartTime=reason.startDate;
                var endTime=$scope.oriStartTime;
                var startTime=$scope.oriStartTime;
                $scope.format = $scope.formats[5];
                $scope.remarksMaxLength = 150;                      
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
                                message : 'This Board is not for the current date.  To reverse cancel unavailable status, please load the Board for the current date.'
                            });
                        return false;
                    }
                    return true;
                }

                $scope.opData = {                    
                    titleAction : 'Return to Unavailable',
                    titleEntity : name,
                    name:name,
                    legend:'Reverse Unavailable Code',
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Confirm',                
                    clientErrors : [],
                    serverErrors:[],
                    errors : [],          
                    unavailableCode : reason ? reason.code : '',
                    startDate :startTime,
                    startTime : $scope.oriStartTime,
                    endDate :  endTime,
                    endTime : endTime,
                    id :  reason ? reason.id : '',          
                    remarks : '',
                    progress : 100,
                    required : []
                }

                
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-personnel-reverse-cancel-unavail-status',
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
                     OpsBoardRepository.undoCancelPersonUnavailabilityCode(person, $scope.unavailabilityData, success, error);
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
                        action : 'Reinstated',
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
