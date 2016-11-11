(function () {

	angular
		.module('OpsBoard')
		.service('BoardHelperService', ['$resource', '$q', '$timeout', 'BoardDataService', 'ReferenceDataService', helperService]);

	function helperService($resource, $q, $timeout, BoardDataService, ReferenceDataService) {

		var boardData = BoardDataService.getBoardData();

		// loads board and ref data in parallel
		var loadCoreData = function () {
			var def = $q.defer();
			$q.all([
				BoardDataService.getBoardRefData(),
				BoardDataService.getShiftData(),
				BoardDataService.getCategoryData(),
				BoardDataService.getAllCategoryData(),
				BoardDataService.getEquipmentDownCodes(),
				BoardDataService.getMaterials(),
				BoardDataService.getPersonnelStatusList(),
				BoardDataService.getPersonnelSpecialPositionsList(),
				BoardDataService.getPersonnelUnavailabilityCodes(),
				BoardDataService.getRepairLocations()
			]).then(function (res) {
				def.resolve(res);
			});
			return def.promise;
		};

		var copyBoardOnServer = function ($resource, $window, newDate, boardData, successFn, errorFn) {
			var newDateStr =  moment(newDate).format('YYYYMMDD');
			console.log(boardData.pathStart+ '/:district/:date/copy/' + newDateStr);
			var resource = $resource(boardData.pathStart
					+ '/:district/:date/copy/'+newDateStr
					, {
				district : boardData.boardLocation,
				date : boardData.boardDate
			});
			var response = resource.get({
				district : boardData.boardLocation,
				date : boardData.boardDate
			}, function(data) {
				console.log('success with CopyBoard, got data: ', data);
				var url = boardData.pathStart + "/"+ boardData.boardLocation+"/"+newDateStr;
				$window.open(url, "_blank");
				successFn(data);
			}, function(error) {
				errorFn(error);
			});
		};
		
		var isCurrentDayBoard = function(){
			 var current = new Date();
             var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
             var boardDate = moment(BoardDataService.getBoardData().boardDate, 'YYYYMMDD');
             if(moment(currentDate).diff(moment(boardDate), 'days') != 0){
           	  return false;
             }
             return true;
		};
		
		var isCurrentOrPastBoard = function(){
			var current = new Date();
            var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
            var boardDate = moment(BoardDataService.getBoardData().boardDate, 'YYYYMMDD');
            if(moment(currentDate).diff(moment(boardDate), 'days') < 0){
          	  return false;
            }
            return true;
		};

		return {
			copyBoardOnServer: copyBoardOnServer,
			loadCoreData: loadCoreData,
			isCurrentDayBoard: isCurrentDayBoard,
			isCurrentOrPastBoard: isCurrentOrPastBoard
		};

	}

}());