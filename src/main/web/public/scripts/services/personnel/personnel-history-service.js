'use strict';

/**
 * This file contains all the logic for working with Personnel, including creating commands, processing server commands, formatting and manipulating task data.
 */

angular.module('OpsBoard')
  .service(
    'PersonnelHistoryService',
    function($rootScope, InfoService) {

      var _getPersonKey = function (id) {
        if (id == null || typeof id == undefined || id == '') return;
        if (id.indexOf('_') < 0) return id;
        return id.split('_')[0];
      }

      return {
        getDetachmentData: function (page, size)  {
          
        }
      }
    })