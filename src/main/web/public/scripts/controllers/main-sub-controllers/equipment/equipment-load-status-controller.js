'use strict';

angular
    .module('OpsBoard')
    .controller(
        'LoadStatusEquipment',
        function($scope, $filter, $modal, $log, OpsBoardRepository, ClientSideError, equipmentStatuses, groups, states, BoardDataService) {

            var formattedBins;

            $scope.isLoadStatusUpdatable = function(pieceOfEquipment) {
                if (pieceOfEquipment && states.equipment.onLocation.indexOf(pieceOfEquipment.states[pieceOfEquipment.currentLocation]) >= 0 && pieceOfEquipment.bins && pieceOfEquipment.bins.length >= 1) {
                     return true;
                }
                return false;
            };
            
            $scope.isPendingLoad = function(pieceOfEquipment){
                if(pieceOfEquipment && pieceOfEquipment.states[pieceOfEquipment.currentLocation] == states.equipment.pendingLoad) {
                return true;
                }
                return false;
            };

            $scope.updateLoadStatus = function(pieceOfEquipment) {

                var loadStatuses = equipmentStatuses,
                    materials = $scope.materialList,
                    bins = pieceOfEquipment.bins,
                    materialDetails = {},
                    upDown = pieceOfEquipment.getUpDownCondition(),
                    condition = upDown ? upDown.condition : '';

                formattedBins = pieceOfEquipment.getFormattedBins(materials);

                // Define updateLoadStatus data and operation
                $scope.opData = {
                    titleAction : 'Update Load',
                    titleEntity : pieceOfEquipment.name,
                    cancelButtonText : 'Cancel',
                    submitButtonText : 'Update',
                    clientErrors : [],
                    errors : [],
                    equipmentName : pieceOfEquipment.name,
                    equipmentType : pieceOfEquipment.subType,
                    bins : bins,
                    condition : condition,
                    loadStatuses : loadStatuses,
                    materials : materials,
                    bin1Status : formattedBins[0] ? formattedBins[0].status : '',
                    bin2Status : formattedBins[1] ? formattedBins[1].status : '',
                    bin1MaterialType : formattedBins[0] ? formattedBins[0].descr : '',
                    bin2MaterialType : formattedBins[1] ? formattedBins[1].descr : '',
                    bin1LastUpdated : formattedBins[0] ? formattedBins[0].lastUpdated : '',
                    bin2LastUpdated : formattedBins[1] ? formattedBins[1].lastUpdated : '',
                    bin1LastUpdatedBy : formattedBins[0] ? formattedBins[0].lastUpdatedBy : '',
                    bin2LastUpdatedBy : formattedBins[1] ? formattedBins[1].lastUpdatedBy : '',
                    selectedBin1Status : '',
                    selectedBin2Status : '',
                    selectedBin1MaterialType : '',
                    selectedBin2MaterialType : '',
                    progress : 100, // Set to 100 b/c unable to determine
                                    // intervals
                    required : []
                }

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
                                message : 'This Board is not for the current date.  To update load status, please load the Board for the current date.'
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
                $scope.operation = function(success, error) {

                    // see modal-load-status.html ng-show
                    var binOrSide = $scope.opData.bins.length === 1 ? true : false;

                    $scope.submitted = true;
                    $scope.opData.clientErrors = [];
                    $scope.opData.serverErrors = [];
                    $scope.opData.required = [];
                    $scope.loadData = getLoadData($scope.opData);

                    // Validate received by
                    if (!$scope.opData.selectedBin1Status) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : (binOrSide ? 'Bin 1' : 'Large Side') + ' New Status is a required field.'
                        });
                        $scope.opData.required.push('selectedBin1Status');
                    }

                    if (!$scope.opData.selectedBin1MaterialType && $scope.opData.selectedBin1Status !== 'Empty') {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : (binOrSide ? 'Bin 1' : 'Large Side') + ' Material is a required field.'
                        });
                        $scope.opData.required.push('selectedBin1MaterialType');
                    }

                    if (!$scope.opData.selectedBin2Status && $scope.opData.bins.length > 1) {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Small Side New Status is a required field.'
                        });
                        $scope.opData.required.push('selectedBin2Status');
                    }

                    if (!$scope.opData.selectedBin2MaterialType && $scope.opData.bins.length > 1
                        && $scope.opData.selectedBin2Status !== 'Empty') {
                        $scope.opData.clientErrors.push({
                            type : 'danger',
                            message : 'Small Side Material is a required field.'
                        });
                        $scope.opData.required.push('selectedBin2MaterialType');
                    }

                    if ($scope.opData.clientErrors.length > 0) {
                        return error(new ClientSideError('ValidationError'));
                    }


                    // Perform operation
                    OpsBoardRepository.updateEquipmentLoadStatus(pieceOfEquipment, $scope.loadData, success, error);
                    //after update load, the equipment card move to another group, this is for refresh the details pane after pane loose focus,
                    //create fake data to show instance result, this result equals to the data after back end command
                    $scope.panes.equipmentDetails.active=false;
                    
                    if ($scope.loadData && $scope.loadData.length > 0) {

                        // set formatted bins from local scope if not existent (details weren't opened)
                        pieceOfEquipment.formattedBins = pieceOfEquipment.formattedBins || formattedBins;

                        for (var i = 0; i < $scope.loadData.length; i++) {
                          var bin = $scope.loadData[i];
                          if (bin.name === "BIN 1") {
                              pieceOfEquipment.formattedBins[i].status = bin.status;     
                              for(var j=0;j<materials.length;j++){
                                  if(bin.materialType==materials[j].materialType){
                                      pieceOfEquipment.formattedBins[i].materialType=materials[j].materialType;
                                      pieceOfEquipment.formattedBins[i].descr=materials[j].descr;
                                  }
                              }
                              pieceOfEquipment.formattedBins[i].lastUpdated=moment(new Date()).format('MM/DD/YYYY HH:mm:ss');
                              pieceOfEquipment.formattedBins[i].lastUpdatedBy=$scope.user.username;                           
                          } else if (bin.name === "BIN 2") {
                              pieceOfEquipment.formattedBins[i].status = bin.status;     
                              for(var j=0;j<materials.length;j++){
                                  if(bin.materialType==materials[j].materialType){
                                      pieceOfEquipment.formattedBins[i].materialType=materials[j].materialType;
                                      pieceOfEquipment.formattedBins[i].descr=materials[j].descr;
                                  }
                              }
                              pieceOfEquipment.formattedBins[i].lastUpdated=moment(new Date()).format('MM/DD/YYYY HH:mm:ss');
                              pieceOfEquipment.formattedBins[i].lastUpdatedBy=$scope.user.username;                           
                          
                          }
                        }
                      }
                    
                    $scope.$emit('SHOW-EQUIPMENT-DETAILS',{equipmentId: pieceOfEquipment.id});
                };

                // Configure modal controller and create instance
                var modalInstance = $modal.open({
                    templateUrl : appPathStart + '/views/modals/modal-load-status',
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

                modalInstance.result.then(function(selectedItem) {
                    $scope.selected = selectedItem;
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });

                $scope.loadStatusSelect2Options = {
                    allowClear : true
                }

                $scope.materialSelect2Options = {
                    allowClear : true
                }

                $scope.checkMaterial = function(status) {
                    if (status === 'loadStatus1'
                        && ($scope.opData.selectedBin1Status === 'Empty' || !$scope.opData.selectedBin1Status))
                        $scope.opData.selectedBin1MaterialType = ''
                    if (status === 'loadStatus2'
                        && ($scope.opData.selectedBin2Status === 'Empty' || !$scope.opData.selectedBin2Status))
                        $scope.opData.selectedBin2MaterialType = ''
                }

                function getLoadData(opData) {
                    var bins = [ {
                        name : "BIN 1",
                        displayName : "Large Side",
                        status : $scope.opData.selectedBin1Status,
                        materialType : $scope.opData.selectedBin1MaterialType
             
                    } ]
                    if ($scope.opData.selectedBin2Status)
                        bins.push({
                            name : "BIN 2",
                            displayName : "Small Side",
                            status : $scope.opData.selectedBin2Status,
                            materialType : $scope.opData.selectedBin2MaterialType
                        })
                    return bins;
                }

            };
        })
