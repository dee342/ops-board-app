'use strict';

angular
  .module('errorStack')
  .factory(
    'ClientSideError',
    function() {

      var ClientSideErr = function(message) {
        var err = new Error(message);
        err.name = 'ClientSideError';
        this.name = err.name;
        this.message = err.message;
        //check if there is a stack property supported in browser
        if (err.stack) {
          this.stack = err.stack;
        }
        //we should define how our toString function works as this will be used internally
        //by the browser's stack trace generation function
        this.toString = function() {
          return this.name + ': ' + this.message;
        };
      };
      ClientSideErr.prototype = new Error();
      ClientSideErr.prototype.name = 'ClientSideError';

      return ClientSideErr;

    })