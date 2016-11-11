'use strict';

angular.module('OpsBoard')
  .service(
    'UUIDGenerator',
    function () {
      //need to remove all global libs
      return {
        uuid: function () {
          return uuid.v4();
        }
      }
    });