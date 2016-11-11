'use strict';

/**
 * This file contains all the logic for working with Personnel, including creating commands, processing server commands, formatting and manipulating task data.
 */

angular
  .module('OpsBoard')
  .service('BoardDataService', ['$log', 'CategoryDataService', 'DownCodesService', 'MaterialService', 
    'PersonnelStatusService', 'RepairLocationsService', 'ShiftDataService', 'SpecialPositionService', 
    'UnavailabilityCodeService', 'ReferenceDataService', 'BoardValueService', function ($log, CategoryDataService, DownCodesService, MaterialService, 
    PersonnelStatusService, RepairLocationsService, ShiftDataService, SpecialPositionService, 
    UnavailabilityCodeService, ReferenceDataService, BoardValueService) {

      var pathElements = window.location.pathname.split('/'), 
        pathStart = '/' + pathElements[1];
      var boardLocation = pathElements[2],
        boardDate = pathElements[3],
        boardData = {
          pathStart: pathStart,
          boardLocation: boardLocation,
          boardDate: boardDate
        };

      BoardValueService.boardData = boardData;

      var formattedBoardDate = new Date(boardDate.substring(0, 4), boardDate.substring(4, 6) - 1, boardDate.substring(6, 8), 0, 0, 0);

      var boardRefData;

      function getBoardRefData() {
        if (boardRefData == null) {
          return ReferenceDataService.loadBoardData().then(function(data){
            boardRefData = data;     
            boardData.startDate = data.shiftsStartDate;
            boardData.endDate = data.shiftsEndDate;
            return data;
          }, ReferenceDataService.dataLoadError);
        } else {
          return boardRefData;
        }
      }

      function validateBoardDate(displayDate, errors, action) {
        var current = new Date();
        var currentDate = new Date(current.getFullYear(), current.getMonth(), current.getDate());
        var boardDate = new Date(displayDate);
        if (!moment().subtract(1, 'minutes').isBetween(moment(boardData.startDate), moment(boardData.endDate), 'minutes')) {
          return !errors.push({
            type: 'danger',
            message: 'This Board is not for the current date. To ' + action + ', please load the Board for the current date.'
          });
        }
        return true;
      }

      return {

        validateBoardDate: validateBoardDate,

        dataLoadError: function (err) {
          // should have ui notice
          $log.error(err);
          // for debug - ref data will break app
          document.body.classList.add('data-load-error');
        },

        getBoardData: function ()  {
          return boardData;
        },

        getPathStart: function () {
          return pathStart;
        },

        getFormattedBoardDate: function () {
          return formattedBoardDate;
        },

        getBoardRefData: getBoardRefData,
        getShiftData: ShiftDataService.getShiftData,
        getCategoryData: CategoryDataService.getCategoryData,
        getAllCategoryData: CategoryDataService.getAllCategoryData,
        getEquipmentDownCodes : DownCodesService.getEquipmentDownCodes,
        getMaterials: MaterialService.getMaterials,
        getPersonnelStatusList: PersonnelStatusService.getPersonnelStatusList,
        getPersonnelSpecialPositionsList: SpecialPositionService.getPersonnelSpecialPositionsList,
        getPersonnelUnavailabilityCodes: UnavailabilityCodeService.getPersonnelUnavailabilityCodes,
        getRepairLocations: RepairLocationsService.getRepairLocations,
        
      }
    }
  ])