(function () {
	
	'use strict';

	angular
		.module('SelectBoard', ['ui.bootstrap', 'ui.select2', 'ngResource', 'WorkUnitsInformation', 'dsny-center-modal'])
		.controller('SelectBoardController', ['$scope', '$window', '$location', '$http', '$log', 'WorkUnits', 'serverConfig', selectBoardController]);

		function selectBoardController($scope, $window, $location, $http, $log, WorkUnits, serverConfig) {

			var dateNow = new Date();

			$scope.year = dateNow.getFullYear();
			$scope.workUnits = [];
			$scope.dt = moment(dateNow).format('MM/DD/YYYY');
			$scope.dateNow = dateNow; // we default to this date if scope date is invalid
			$scope.location = '';
			$scope.submitted = false;
			$scope.changeDate = updateWorkUnits;
			$scope.select2Options = {
                allowClear: false
            };

			function updateWorkUnits(forDate) {
				$scope.loading = true;
				WorkUnits.getWorkUnits(forDate)
					.then(function (workUnits) {
						// reset form
						$scope.submitted = false;
						$scope.workUnits = workUnits;
						$scope.loading = false;
						_getLocations();
					}, function (err) {
						$scope.loading = false;
						$log.error(err);
					});
			};


			$scope.open = function ($event) {
				$event.preventDefault();
				$event.stopPropagation();
				$scope.opened = true;
			};

			function _getLocations()  {
				var unitlocations = [];
				for (var i = 0; i < $scope.workUnits.length; i++) {
					if ($scope.workUnits[i].workunitCode === $scope.workUnit) {
						_.each($scope.workUnits[i].locations, function(location) {
							if(location.type !== 'district-garage') {
								unitlocations.push(location);
							}
						});

						unitlocations.sort(function (a, b) {
							return (a.sortSequence > b.sortSequence ? 1 : -1);
						});

						$scope.unitlocations = unitlocations;
					}
				}
			};

		$scope.getLocations = function() {
			_getLocations();
		};

			$scope.submit = function (invalid) {
				$scope.submitted = true;
				if (!invalid) {
					var local = $scope.dt;
					local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
					var dateElements = local.toJSON().slice(0, 10).split('-');
					var date = dateElements[0] + dateElements[1] + dateElements[2];	
					$window.location = serverConfig.formActionUrl + $scope.location + '/' + date;
				};
			};
			
			$scope.invalidAfterSubmit = function (exp) {
				return $scope.submitted && exp;
			};

		}

}());