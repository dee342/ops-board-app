(function () {
	
	angular.module('OpsBoard', [
		'ngCookies',
		'ngResource',
		'ngSanitize',
		'ngRoute',
		'angular-flex-splitter',
		'lvl.directives.dragdrop',
		'ui.bootstrap',
		'ui.select2',
		'dsny-center-modal',
		'WorkUnitsInformation',
		'multi-select',
		'about-us',
		'OpsBoardFilters',
		'personnelFilters',
		'boroBoardFilters',
		'equipmentFilters',
		'locationFilters',
		'taskFilters',
		'exceptionOverride',
		'perfect_scrollbar',
		'ngOrderObjectBy',
		'OpsBoard.directives'
	])
	.config(['$routeProvider', 'serverConfig',
		function ($routeProvider, serverConfig) {
			$routeProvider
				.when('/', {
					templateUrl: serverConfig.templateUrl,
					controller: 'MainCtrl',
					resolve: {
						initBoard: function (OpsBoardRepository, DistrictService, WebSocketService, BoardHelperService) {
							return WebSocketService.openWebSocket()
							.then(BoardHelperService.loadCoreData)
							.then(OpsBoardRepository.init)
							.then(DistrictService.renderDistricts)
							.then(OpsBoardRepository.subscribe)
						}
					}
				})
				.otherwise({
					redirectTo: '/'
				});
			}
	])
	.config(['$compileProvider',
			function ($compileProvider) {
				$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|blob):/);
			}
	]);

}());