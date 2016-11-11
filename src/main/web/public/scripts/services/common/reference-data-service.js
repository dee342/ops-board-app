(function () {

	'use strict';

	// shared with display board, do not modify without checking both

	angular
		.module('OpsBoard')
		.service('ReferenceDataService', ['$q', '$resource', '$log', '$window', 'BoardValueService', referenceDataService]);

		function referenceDataService($q, $resource, $log, $window, BoardValueService) {

			var pathElements = window.location.pathname.split('/'), 
        		pathStart = '/' + pathElements[1];
			var boardLocation = pathElements[2],
		        boardDate = pathElements[3];
	        var params = {
				district: boardLocation,
				boardDate: boardDate
			};

			var boardRefData;

	        // when a load fails or promise is rejected
			var dataLoadError = function (res) {
				var errorData = {
					message: res.data.message,
					date: res.data.exceptionTimestamp
				};
				$window.localStorage.setItem('errorData', angular.toJson(errorData));
				$window.location.href = appPathStart + '/error';
		    };

		    var setParams = function (newParams) {
		    	params = newParams;
		    };

			var loadDownCodes = function (boardLoc) {
				var resource = $resource(pathStart + '/referencedata/downcodes');
				var response = resource.query(params);
				return response.$promise.then(function (list) {
					return list;
				}, dataLoadError);
			};

			var loadBoardData = function () {
				var def = $q.defer();
				$resource(pathStart + '/:district/:date/load').get({
					district: boardLocation,
					date: boardDate
				}).$promise.then(function (res) {
					def.resolve(res);
				}, dataLoadError);
				return def.promise;
			};

			var loadShifts = function () {
				var resource = $resource(pathStart + '/referencedata/shifts');
				var response = resource.get(params);
				return response.$promise.then(function (list) {
					BoardValueService.shiftData = list;
					return list;
				}, dataLoadError);
			};

			var loadCategories = function (boardLoc, asObject) {
				// boardLocation is different for display board
				var boardLoc = boardLoc || boardLocation;
				var resource = $resource(pathStart + '/referencedata/categories/' + boardLoc);
				var response = resource.get(params);
				return response.$promise.then(function (map) {
					BoardValueService.categoryData = map.toJSON();
					return asObject ? map : _.toArray(map.toJSON());
				}, dataLoadError);
			};

			var loadAllCategories = function (boardLoc, asObject) {
				var resource = $resource(pathStart + '/referencedata/categories/');
				var response = resource.get(params);
				return response.$promise.then(function (map) {

					BoardValueService.allCategoryData = map.toJSON();
					return asObject ? map : _.toArray(map.toJSON());
				}, dataLoadError);
			};

			var loadMaterialList = function () {
				var resource = $resource(pathStart + '/referencedata/materialtypes');
				var response = resource.query(params);
				return response.$promise.then(function (list) {
					list.forEach(function (item, index) {
						var fItem = {
							uniqueId: item.id,
							materialType: item.code,
							descr: item.description,
							group: item.group,
							subGroup: item.category,
							color: item.color
						}
						list[index] = fItem;
					});
					return list;
				}, dataLoadError);
			};

			var loadRepairLocations = function () {
				var resource = $resource(pathStart + '/referencedata/locations');
				var response = resource.query(params);
				return response.$promise.then(function (data) {
					return data;
				}, dataLoadError);
			};

			var loadPersonnelStatusList = function () {
				var resource = $resource(pathStart + '/referencedata/personnelmdatypes');
				var response = resource.query(params);
				return response.$promise.then(function (list) {
					list.forEach(function (item, index) {
						var fItem = {
							type: 'MDA',
							subType: item.code,
							descr: item.description
						};
						list[index] = fItem;
					})
					return list;
				}, dataLoadError);
			};			

			var loadPersonnelUnavailabilityCodes = function () {
				var resource = $resource(pathStart + '/referencedata/personnelunavailabilitytypes');
				var response = resource.query(params);
				return response.$promise.then(function (list) {
					list.forEach(function (item, index) {
						var fItem = {
							code: item.code,
							id: item.id,						
							longDescription: item.description,
							sortSequence: item.sortSequence
						};
						list[index] = fItem;
					})
					return list;
				}, dataLoadError);
			};

			var loadPersonnelSpecialPositionsList = function () {
				var resource = $resource(pathStart + '/referencedata/personnelspecialpositiontypes');
				var response = resource.query(params);
				return response.$promise.then(function (list) {
					list.forEach(function (item, index) {
						var fItem = {
							code: item.code,
							id: item.id,						
							longDescription: item.description
						};
						list[index] = fItem;
					})
					return list;
				}, dataLoadError);
			};

			var getBoardRefData = function () {
			  if (boardRefData == null) {
			    return loadBoardData().then(function (data) {
			      boardRefData = data;
			      return data;
			    }, dataLoadError);
			  } else {
			    return boardRefData;
			  }
			};

			return {

				// only core data needed to initialize board
				loadReferenceData: function () {
					var def = $q.defer();
					$q.all([
						loadDownCodes(),
						loadShifts(),
						loadCategories(),
						loadMaterialList(),
						loadPersonnelStatusList(),
						loadRepairLocations()
					]).then(function (res) {
						def.resolve(res);
					});
					return def.promise;
				},

				dataLoadError: dataLoadError,
				getBoardRefData: getBoardRefData,
				loadBoardData: loadBoardData,
				loadEquipmentDownCodes: loadDownCodes,
				loadShifts: loadShifts,
				loadCategories: loadCategories,
				loadAllCategories: loadAllCategories,
				loadMaterialList: loadMaterialList,
				loadPersonnelStatusList: loadPersonnelStatusList,
				loadRepairLocations: loadRepairLocations,
				loadPersonnelUnavailabilityCodes: loadPersonnelUnavailabilityCodes,
				loadPersonnelSpecialPositionsList: loadPersonnelSpecialPositionsList,
				setParams: setParams

			}
		

		}

}());