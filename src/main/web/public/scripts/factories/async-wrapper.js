'use strict';

angular.module('OpsBoard')
	.service(
    'AsyncService',
	    function($window) {
    		var test;
    		return {
    			asyncw: $window.async
    		}
    });