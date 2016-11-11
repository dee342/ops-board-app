'use strict';

angular
    .module('OpsBoard')
    .controller(
        'UpdateSnowReadinessEquipment',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, groups, states, plowTypes, plowDirections, loads, InfoService, BoardDataService) {

        	$scope.loads = loads;
			$scope.plowTypes = plowTypes;
			$scope.plowDirections = plowDirections;	
        	
            function confirmPlowRemoval() {
                return $modal.open({
                    templateUrl: appPathStart + '/views/modals/modal-plow-removal',
                    backdrop: 'static',
                    controller: 'PlowRemoval'
                }).result;
            }

            $scope.isEquipmentDressable = function (pieceOfEquipment) {
            	if (!pieceOfEquipment) return false;
            	return (pieceOfEquipment.subTypeObj.canBeChained || 
                pieceOfEquipment.subTypeObj.plowTypes.length > 0 ||
                pieceOfEquipment.subTypeObj.loads.length > 0) ||
                pieceOfEquipment.subTypeObj.code === '2200' ||
                pieceOfEquipment.subTypeObj.code === '5000';
            };
            
            $scope.isEquipmentDressed = function(pieceOfEquipment){
            	if( (pieceOfEquipment.snowReadiness.plowType != null && pieceOfEquipment.snowReadiness.plowType != "NO_PLOW" ) ||
            			(pieceOfEquipment.snowReadiness.load != null && pieceOfEquipment.snowReadiness.load != "NONE" ))
            		return true;
            	
            	return false;
            };
            
            $scope.UpdateSnowReadinessEquipment = function(pieceOfEquipment) {
            	$scope.plowType = null;
            	$scope.loadedWith = null;
            	$scope.hasSnowAssignment = null;
            	$scope.equippedChain = null;
            	
            	$scope.canHavePlow = pieceOfEquipment.subTypeObj.plowTypes.length > 0;            	
            	$scope.canHaveLoad = pieceOfEquipment.subTypeObj.loads.length > 0;
            	$scope.canBeChained =  pieceOfEquipment.subTypeObj.canBeChained;
            	
            	var tPlowTypes = [];
            	var aPlowTypes = ['NO_PLOW'].concat( pieceOfEquipment.subTypeObj.plowTypes);
            	
            	angular.forEach(plowTypes, function(v, k){
            		if(aPlowTypes.indexOf(k) != -1)
            			tPlowTypes.push(k);
            	});

            	$scope.plowDirection = null;
            	$scope.isDown = pieceOfEquipment.getUpDownCondition().condition === 'Down';
            	
            	if($scope.canHavePlow) {
            		if(pieceOfEquipment.snowReadiness.plowType) {
            			if(pieceOfEquipment.snowReadiness.plowType == 'REGULAR_PLOW') {
                			$scope.plowDirection= pieceOfEquipment.snowReadiness.plowDirection ? pieceOfEquipment.snowReadiness.plowDirection:$scope.plowDirections[0];
            			}
            			$scope.equippedPlow = pieceOfEquipment.snowReadiness.plowType;    	            	  	
                }
            	} 
            	
            	if($scope.canBeChained) {
            		if(pieceOfEquipment.snowReadiness.chained) {
                  $scope.equippedChain='Chained';
                } else {
                  $scope.equippedChain='Not Chained';
                }
            	}
            
              if($scope.canHaveLoad) {
            		if(pieceOfEquipment.snowReadiness.load){
            			$scope.loadedWith = pieceOfEquipment.snowReadiness.load        		
                	}
            	}

            	if(pieceOfEquipment.snowReadiness.workingDown) {
                $scope.isWorkingDown='Yes';
              } else {
                $scope.isWorkingDown='No';
              }

            	if(pieceOfEquipment.snowReadiness.snowAssignment) {
                $scope.hasSnowAssignment='Yes';
              } else {
                $scope.hasSnowAssignment='No';
              }
            	
            	if((!$scope.equippedPlow || $scope.equippedPlow=='NO_PLOW') && (!(pieceOfEquipment.subTypeObj.code === '5000' || pieceOfEquipment.subTypeObj.code === '2200'))){
                	$scope.snowAssignmentEnabled = false;
                }else{
                	$scope.snowAssignmentEnabled = true;
                }

              var workingDownEnabled = false;

              if(pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Available" && pieceOfEquipment.snowReadiness.workingDown || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Down") {
                workingDownEnabled = true;
              }
            	/*test data for story 561*/
            	$scope.allowedPlowTypes = tPlowTypes;  //['None','Regular Plow','Mini V-Plow','Large V-Plow'];

            	$scope.allowedLoads= ['NONE'].concat( pieceOfEquipment.subTypeObj.loads); //['Not Loaded','Salt','Sand'];
            	$scope.chains=['Chained','Not Chained'];
            	$scope.workingDown=['No','Yes'];
            	$scope.snowAssignment=['No','Yes'];

                // Define snow readiness data and operation
                $scope.opData = {
                    titleAction : 'Update Snow Readiness',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Update',
                    clientErrors : [],
                    serverErrors : [],
                    errors : [],
                    equipmentName : pieceOfEquipment.name,
                    plowTypeOptions:$scope.allowedPlowTypes,
                    plowType: $scope.equippedPlow,
                    plowDirectionOptions:$scope.plowDirections,
                    plowDirection: $scope.plowDirection,
                    loadsOptions:$scope.allowedLoads,
                    loads:$scope.loadedWith,
                    workingDownOptions:$scope.workingDown,
                    workingDownEnabled: workingDownEnabled,
                    workingDown:$scope.isWorkingDown,
                    chainsOptions:$scope.chains,
                    chains:$scope.equippedChain,
                    snowAssignmentOptions:$scope.snowAssignment,
                    snowAssignment:$scope.hasSnowAssignment,
                    snowAssignmentEnabled:$scope.snowAssignmentEnabled,
                    progress : 100, // Set to 100 b/c unable to determine
                                    // intervals
                };

                $scope.changePlowOrLoad = function() {
                  if(pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Available" && pieceOfEquipment.snowReadiness.workingDown || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Down") {
                    $scope.opData.workingDownEnabled = true;
                  }else{
                    $scope.opData.workingDownEnabled = false;
                  }

                };
                
                
                $scope.changeSnowAssignment = function(snowAssignment) {
                	if($scope.opData.workingDown == 'No'
                		&&(pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Down" || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Available")
                		&&  $scope.opData.workingDownEnabled) {
                		$scope.opData.snowAssignment = 'No';
                	}else{
                		$scope.opData.snowAssignment = snowAssignment;
                	}

                };

                $scope.changeSnowAssignmentByWorkingDown = function(workingDown) {
                	if($scope.opData.workingDown == 'No' 
                		&& (pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Down" || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === "Available")
                		&&  $scope.opData.workingDownEnabled){
                		$scope.opData.snowAssignment = 'No';
                	}
                };
                
                function formSubmitSuccess(selectedItem) {
                    $scope.selected = selectedItem;
                }

                function submitForm(success, error) {

                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];
                    $scope.opData.serverErrors = [];
                    var snowData={
                            chained:false,
                            load: null,              
                            plowType:null,
                            plowDirection:null,
                            workingDown:false,
                            snowAssignment:false
                            
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                    }

                    if ($scope.opData.loads) {
                        snowData.load = $scope.opData.loads ? $scope.opData.loads : null;
                        // server must get null type instead of string from ui
                        if (snowData.load === 'NONE') {
                            snowData.load = null;
                        }
                    }
                    
                    if($scope.opData.plowType)
                    {                       
                        snowData.plowType = ($scope.opData.plowType) ? $scope.opData.plowType : 'NO_PLOW';
                        if(snowData.plowType == 'REGULAR_PLOW')
                            snowData.plowDirection = $scope.opData.plowDirection ? $scope.opData.plowDirection : 'Right';
                    }
                    
                    if($scope.opData.workingDown=='Yes')
                         snowData.workingDown=true;
                   
                    if($scope.opData.chains=='Chained')
                        snowData.chained=true;
                  
                    if($scope.opData.snowAssignment=='Yes')
                        snowData.snowAssignment=true;
                    
                    // Perform operation
                    OpsBoardRepository.updateSnowEquipment(pieceOfEquipment, snowData, success, error);

                };

                function changePlowType() {
                    
                    if (pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.available &&
                        pieceOfEquipment.snowReadiness.workingDown ||
                        pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.down) {
                      $scope.opData.workingDownEnabled = true;
                    } else {
                      $scope.opData.workingDownEnabled = false;
                    }
                    
                    // default and no plow
                    if (!$scope.opData.plowType || $scope.opData.plowType === 'NO_PLOW') {
                        // ignore the dialog when the user has already selected No in working down
                        if (pieceOfEquipment.snowReadiness.workingDown === true && $scope.opData.workingDown !== 'No') {
                            confirmPlowRemoval()
                                .then(function (removalConfirmed) {
                                    if (removalConfirmed) {
                                        // user has confirmed the removal modal
                                        submitForm(formSubmitSuccess);
                                        modalInstance.dismiss();
                                    }
                                });
                        }
                        $scope.opData.snowAssignment = 'No';
                        $scope.opData.snowAssignmentEnabled = false;
                    } else {
                        $scope.opData.snowAssignmentEnabled = true;
                    }
                    
                    // regular
                    if ($scope.opData.plowType === 'REGULAR_PLOW') {
                        $scope.opData.plowDirection = $scope.opData.plowDirectionOptions[0];
                    } else {
                        $scope.opData.plowDirection = null;
                    }

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
                                message : 'This Board is not for the current date.  To update snow readiness for a Equipment, please load the Board for the current date.'
                            });
                        return false;
                    }

                    return true;
                }

                $scope.changePlowType = changePlowType;
                $scope.submitted = false;
                $scope.operation = submitForm;
                $scope.snowSelect2Options = {
                    allowClear : true
                }

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-equipment-update-readiness',
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

                modalInstance.result.then(formSubmitSuccess);

            };
        });
