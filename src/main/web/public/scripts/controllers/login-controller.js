'use strict';

angular.module('about-us')
  .controller('LoginCtrl', function ($window, $scope, $log, $controller, $rootScope, BrowserService) {
	  
	  // checks for a board hash, display board uses this after login
	  function checkHash() {
	  	if ((/#\/\d+/g).test($window.location.hash)) {
	  		$window.localStorage.setItem('boardDate', $window.location.hash.split('/')[1].replace(/\D/g, ''));
	  	}
	  }

	  checkHash();

	  $scope.browserType = BrowserService.getBrowser();
	  
	  $scope.isSupportedBrowser = function(){
		  var supported = false;
		  if ($scope.browserType === "chrome" || $scope.browserType === "firefox"){
			  supported = true;
		  }  
		  return supported;
	  }

});