'use strict';

angular
    .module('OpsBoard')
    .controller(
        'CopyBoard',
        function($scope, $modal, $log, OpsBoardRepository, ClientSideError, BoardDataService) {
            $scope.copyBoard = function(person, selectedCode, type) {
                
                var boardDate = BoardDataService.getFormattedBoardDate();
 
                $scope.formats = [ 'dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate', 'MM/dd/yyyy', 'MM/DD/YYYY', "HH:mm"];
                $scope.format = $scope.formats[4];
                $scope.minDate = new Date();
                $scope.minDate.setTime($scope.minDate.getTime() + 1 * 24 * 60 * 60 * 1000);
                $scope.warning = "Please note that any existing tasks for the selected date will be overwritten. Confirm by clicking on 'Copy' again.";
                $scope.showMessage = false;
                $scope.showedMessage = false;
                
                $scope.datepickers = {
                    startDateOpened: false,
                    disabled: function (date) {
                        return moment(date).isSame(boardDate, 'day');
                    }
                };
                
                $scope.open = function($event, opened) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.datepickers[opened] = true;
                };
                
                $scope.passPreValidation = function(){

                    // Validate if board has tasks
                    var containsTasks = OpsBoardRepository.containsTasks();

                    if (!containsTasks) {
                        $scope.opData.errors
                            .push({
                                type : 'danger',
                                message : 'This Board does not have any tasks configured. You can only copy board with at least one task configured.'
                            });
                        return false;
                    }

                    return true;
                }
                 
                var now = new Date();
                var dayOfTheWeek = now.getDay();
                var daysToAdd = 1;
                switch(dayOfTheWeek){
                case 6: daysToAdd = 2;
                }
                var copyBoardTo = new Date($scope.board.displayDate);
                if($scope.board.displayDate<moment(new Date()).format('MM/DD/YYYY')){
                    copyBoardTo=now;
                }
                copyBoardTo.setTime(copyBoardTo.getTime() + daysToAdd * 24 * 60 * 60 * 1000);
                if(copyBoardTo.getDay() == 0){
                    copyBoardTo.setTime(copyBoardTo.getTime() + 1 * 24 * 60 * 60 * 1000);
                }
                
                     $scope.opData = {
                             
                             titleAction : 'Copy Board',
                             titleEntity : '',
                             cancelButtonText : 'Cancel',
                             submitButtonText : 'Copy',
                             clientErrors : [],
                             errors : [],
                             copyBoardTo : moment(copyBoardTo).format($scope.formats[5]),
                             progress : 100,
                             required : []
                         }
               
                $scope.submitted = false;
                
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-copy-board',
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
                    
                    if (!$scope.opData.copyBoardTo) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Copy Board To is a required field.'
                        });
                        $scope.opData.required.push('copyBoardTo');
                    } else {
                        if (!moment($scope.opData.copyBoardTo).isValid()) {
                            $scope.opData.clientErrors.push({
                                type : 'danger',
                                message : 'Copy Board To is invalid.'
                            });
                            $scope.opData.required.push('copyBoardTo');
                        }
                    }
                    
                    if ($scope.opData.clientErrors.length == 0) {
                        $scope.showMessage = true;
                    }
                    
                    if($scope.showMessage && !$scope.showedMessage){
                        $scope.opData.clientErrors
                            .push({
                                type : 'danger',
                                message : $scope.warning
                            });
                        $scope.showMessage = false; 
                        $scope.showedMessage = true;
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                    }
                   OpsBoardRepository.copyBoard($scope.opData.copyBoardTo, success, error);                   
                };  

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });
            }
        })