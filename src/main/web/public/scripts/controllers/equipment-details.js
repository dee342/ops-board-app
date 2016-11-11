'use strict';

angular
		.module('OpsBoard')
		.controller(
				'EquipmentDetailsCtrl',
				function($scope, $filter, OpsBoardRepository, EquipmentModel, $http, BoardDataService, states, BoardValueService) {
					$scope.states = states;
					$scope.toInteger = function(length) {
						return length + 1;
					}

					/* Detail sub-panels */
					$scope.equipmentInfo = {
						open : true,
						show : true
					}
					$scope.equipmentUpDownHist = {
						open : true,
						show : true
					}
					$scope.loadStatusDetails = {
						open : true,
						show : true
					}
					$scope.devicesDetails = {
						open : true,
						show : true
					}
					$scope.snowDetails = {
						open : true,
						show : true
					}
					$scope.detachHist = {
						open : true,
						show : true
					}
					$scope.showInfo = false;
					$scope.showDetachments = false;
					$scope.materials = BoardDataService.getMaterials();

					$scope.hideInfo = function() {
						$scope.showInfo = true;
						$scope.showDetachments = false;
					}

					$scope.hideDetachments = function() {
						$scope.showDetachments = true;
						$scope.showInfo = false;
					}

					$scope.toggleOne = function() {
						if ($scope.showInfo)
							$scope.showInfo = !$scope.showInfo;
						if ($scope.showDetachments)
							$scope.showDetachments = !$scope.showDetachments;
					}

					$scope.close = function() {
						$scope.showInfo = false;
						$scope.showDetachments = false;
						$scope.closeEquipmentDetailsPane();
					}

					$scope.$on('reset-details-pane', function(event, args) {
						$scope.showInfo = false;
						$scope.showDetachments = false;
					});

					$scope.showBinStatus = function(pieceOfEquipment) {
						if (pieceOfEquipment && pieceOfEquipment.bins
								&& pieceOfEquipment.bins.length > 0)
							return true;
						return false;
					}

					$scope.showDownCodes = function(pieceOfEquipment, showInfo) {
						if (pieceOfEquipment
								&& pieceOfEquipment.upDownHistory
								&& pieceOfEquipment.upDownHistory.length > 0
								&& pieceOfEquipment.upDownHistory[0].down)
							if (!showInfo)
								return true;
						return false;
					}

					$scope.showDeviceConditions = function(pieceOfEquipment) {
						if (pieceOfEquipment
								&& pieceOfEquipment.deviceConditions
								&& pieceOfEquipment.deviceConditions.length > 0)
							return true;
						return false;
					}

					$scope.showInfoTable = function() {
						if (!$scope.showDetachments && $scope.showInfo)
							return true;
						return false;
					}

					$scope.showDetachmentsTable = function() {
						if ($scope.showDetachments && !$scope.showInfo)
							return true;
						return false;
					}

					//TODO - commented out by VG. This function was not being used, and would currently return incorrect values.
					//TODO - dressable is no longer a field and is now inferred based on data from the piece of equipment.
/*					$scope.showSnow = function(pieceOfEquipment) {
						if (pieceOfEquipment
								&& (pieceOfEquipment.snowReadiness.dressable == 'D' || pieceOfEquipment.snowReadiness.dressable == 'E'))
							return true;
						return false;
					}*/

					$scope.getEquipmentUpDownCondition = function(
							pieceOfEquipment) {
						var results = OpsBoardRepository
								.getEquipmentUpDownCondition(pieceOfEquipment);
						return results;
					}

					// Toggles for new re-skinned panels
					$scope.toggleEquipmentInfoSubpanel = function() {
						$scope.equipmentInfo.show = !$scope.equipmentInfo.show;
					}
					$scope.toggleEquipmentUpDownHistSubpanel = function() {
						$scope.equipmentUpDownHist.show = !$scope.equipmentUpDownHist.show;
					}
					$scope.toggleLoadStatusDetailsSubpanel = function() {
						$scope.loadStatusDetails.show = !$scope.loadStatusDetails.show;
					}
					$scope.toggleDevicesDetailsSubpanel = function() {
						$scope.devicesDetails.show = !$scope.devicesDetails.show;
					}
					$scope.toggleSnowDetailsSubpanel = function() {
						$scope.snowDetails.show = !$scope.snowDetails.show;
					}
					$scope.toggleDetachHistSubpanel = function() {
						$scope.detachHist.show = !$scope.detachHist.show;
					}

					$scope.upDownPageChanged = function(upDownCurrentPage) {
			            $http.get(BoardDataService.getPathStart() + '/view/conditions/' + $scope.pieceOfEquipment.id + '?page=' + (upDownCurrentPage - 1) + '&size=5&sortDir=desc&sortColumn=id&upToDate=' + new Date(BoardDataService.getBoardData().endDate)).
			              success(function(result, status, headers, config) {
			                $scope.pieceOfEquipment.conditionsCount = result.count;
			                $scope.pieceOfEquipment.upDownPaginationModel = $scope.pieceOfEquipment.getFormattedUpDownConditions(result.items);
			              }).
			              error(function(data, status, headers, config) {
			              });

			          };

			          //paginatino for detachment hisotry

			        $scope.detachmentPageChanged = function(detachmentCurrentPage) {
			            $http.get(BoardDataService.getPathStart() + '/view/equipmentDetachments/' + $scope.pieceOfEquipment.id + '?page=' + (detachmentCurrentPage - 1) + '&size=5&sortDir=desc&sortColumn=id&upToDate=' + new Date(BoardDataService.getBoardData().endDate)).
			              success(function(result, status, headers, config) {
			                $scope.pieceOfEquipment.detachmentHistory = result.items;
			                $scope.pieceOfEquipment.detachmentCount = result.count;
			                $scope.pieceOfEquipment.equipmentDetachmentPaginationModel = $scope.pieceOfEquipment.getFormattedDetachments();
			              }).
			              error(function(data, status, headers, config) {
			              });
			        };

				  $scope.isEquipmentDetachable = function(pieceOfEquipment) {
					/**
					 * remove to fix SMARTOB-8534: !pieceOfEquipment.assigned?
					 *  pieceOfEquipment && !pieceOfEquipment.assigned
					 */
					if (pieceOfEquipment && (pieceOfEquipment.states[BoardValueService.equipmentLocation] === states.equipment.available || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.down || pieceOfEquipment.states[BoardValueService.equipmentLocation] === states.equipment.pendingAttach || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.pendingDetach || pieceOfEquipment.states[pieceOfEquipment.currentLocation] === states.equipment.pendingLoad)) {
					  return true;
					}
					return false;
				  };

				  $scope.actionSwitch = function(pieceOfEquipment) {
					pieceOfEquipment = pieceOfEquipment || $scope.pieceOfEquipment;
					if(!pieceOfEquipment) {
					  return false; //ng-hide: false; always make 'Action' show by default.
					}
					return pieceOfEquipment.states[$scope.equipmentLocation] === states.equipment.hidden
					  || !$scope.isEquipmentDetachable(pieceOfEquipment)
					  || (pieceOfEquipment.assigned && ! pieceOfEquipment.isLoadStatusUpdatable);
				  };


				  $scope.detachSwitch = function(pieceOfEquipment) {
					pieceOfEquipment = pieceOfEquipment || $scope.pieceOfEquipment;
					if(!pieceOfEquipment) {
					  return true; //ng-hide: false; always make 'Action' show by default.
					}
					if(pieceOfEquipment.assigned) {
					  return false;
					}
					else {
					  var hideFlag = $scope.isEquipmentPendingDetach(pieceOfEquipment) || $scope.isEquipmentPendingAttach(pieceOfEquipment) || $scope.isPendingLoad(pieceOfEquipment);
					  return $scope.isEquipmentDetachable(pieceOfEquipment) && !hideFlag;
					}
				  }

				});

angular.module('OpsBoard').filter('toInteger',
		[ '$filter', '$locale', function(filter, locale) {
			var currencyFilter = filter('currency');
			var formats = locale.NUMBER_FORMATS;
			return function(amount, currencySymbol) {
				var value = currencyFilter(amount, currencySymbol);
				var sep = value.indexOf(formats.DECIMAL_SEP);
				if (amount >= 0) {
					return value.substring(1, sep);
				}
				return value.substring(1, sep) + ')';
			};
		} ]);
