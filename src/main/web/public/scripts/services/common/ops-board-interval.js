'use strict';

//will extend this service later
//currently it will just redigest if specified condition is met

angular.module('OpsBoard')
  .service(
    'OpsBoardInterval',
    function($rootScope, $interval) {
      var toDoList = [];

      function execute() {
        var check = false;
        if (toDoList.length) {
          for (var i = 0; i < toDoList.length; i++) {
            if (Object.prototype.toString.call(toDoList[i]) === '[object Function]') {
              if (toDoList[i]()) check = true;
            }
          }
          if (check && $rootScope.$$phase !== '$digest') {
            $rootScope.$apply();
          }
        }
      }


      $interval(execute, 60000);

      function addToDoList(fn) {
        toDoList.push(fn);
      }

      return {
        addToDoList: addToDoList
      }

    });