'use strict';

angular
    .module('OpsBoard')
    .controller(
        'AddSpecialPosition',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, groups, BoardDataService) {
           
            $scope.addPersonSpecialPosition = function(person) {
                
            var name = person.fullName;
            if(name.length>30)
                name=name.substring(0,30);
            var personnelSpecialPositionsList = BoardDataService.getPersonnelSpecialPositionsList();
            
            var startDate = new Date($scope.board.displayDate);
            $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY', "HH:mm"];
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
     
            $scope.remarksMaxLength = 150;
            var d = new Date();
            d.setHours(0);
            d.setMinutes(0);
            
            var endDate = new Date();
            endDate.setHours(23);
            endDate.setMinutes(59);
            
            
                var sd, ed, et;
                
                $scope.person = person;
                $scope.opData = {
                    titleAction : 'Add Special Position',
                    titleEntity : name,
                    name : name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Add',
                    legend: 'Add Special Position',
                    clientErrors : [],
                    errors : [],
                    personnelSpecialPositionsList : formattedpersonnelSpecialPositionsList,
                    startDate : startDate ? moment(new Date(startDate)).format($scope.formats[5]) : '',
                    endDate : ed ? moment(ed).format($scope.formats[5]): '',
                    progress : 100,
                    specialPositionCode : '',
                    displaySpecialPositionCode : '',
                    required : [],
                };

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
                                message : 'This Board is not for the current date.  To set special position, please load the Board for the current date.'
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
                     
                     var validStartDate = false;
                     var validEndDate = false;
                    
                    
                     
                     if ($scope.opData.endDate) {
                         if (moment($scope.opData.endDate).isValid()) {
                             $scope.opData.endDate.setHours(23);
                             $scope.opData.endDate.setMinutes(59);
                         }
                     }
                     
                     if ((!$scope.opData.displaySpecialPositionCode) || $scope.opData.displaySpecialPositionCode == "") {
                         $scope.opData.clientErrors.push({
                             type : 'danger',
                             message : 'Special Position is a required field.'
                         });
                         $scope.opData.required.push('specialPositionCode');
                     }
                     
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
                         $scope.opData.startDate.setHours(0);
                         $scope.opData.startDate.setMinutes(0);
                     }
                     
                     //var specialPositionDescr = opData.displaySpecialPositionCode.substring(0, $scope.opData.specialPositionCode.indexOf("-"));
                    //var specialPositionCode = opData.displaySpecialPositionCode.substring($scope.opData.specialPositionCode.indexOf("-")+1);

                     
                     
                   /*  if($scope.editMode && !$scope.changeOccurred){
                         
                             $scope.opData.clientErrors.push({
                                 type : 'danger',
                                 message : 'No changes were made.'
                             });
                             $scope.opData.required.push('subType');
                     }*/
                                      
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

                     
                     if (validStartDate && validEndDate && 
                             moment(moment($scope.opData.endDate).format('MM/DD/YYYY')).diff(moment(moment($scope.opData.startDate).format('MM/DD/YYYY'))) < 0) {
                             $scope.opData.clientErrors.push({
                                 type : 'danger',
                                 message : 'End Date cannot be less than Start Date.'
                             });
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
                     
                     $scope.specialPositionData = getSpecialPositionData();
                     
                     if ($scope.opData.clientErrors.length > 0) {
                         return error(new ClientSideError('ValidationError'));
                     }
                     OpsBoardRepository.addSpecialPosition(person, $scope.specialPositionData, success, error);
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

                function getSpecialPositionData() {
                    //var startDate = moment($scope.opData.startDate).isValid() ? moment($scope.opData.startDate) : '', 
                    //    endDate = moment($scope.opData.endDate).isValid() ? moment($scope.opData.endDate) : '';

                    var specialPositionDescr = $scope.opData.displaySpecialPositionCode?$scope.opData.displaySpecialPositionCode.substring(0, $scope.opData.displaySpecialPositionCode.indexOf("--")):'';
                    var specialPositionCode = $scope.opData.displaySpecialPositionCode?$scope.opData.displaySpecialPositionCode.substring($scope.opData.displaySpecialPositionCode.indexOf("--")+2):'';
                    
                    return {
                        startDate : $scope.opData.startDate,
                        id : $scope.opData.id,
                        endDate : $scope.opData.endDate,
                        code : specialPositionCode,
                        description : specialPositionDescr,
                        comments : $scope.opData.remarks
                    }
                }
            };
        })