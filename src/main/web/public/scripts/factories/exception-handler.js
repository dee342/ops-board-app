'use strict';

angular
  .module('exceptionOverride')
  .factory('exceptionHandler', function(ClientSideError) {
    return function(exception, cause) {
      exception.message += ' (caused by "' + cause + '")';
      //alert(new Error(exception.message).stack) //have to wait for Dima. Once env var is set then will enable this for qa env
      throw exception;
    };
  });