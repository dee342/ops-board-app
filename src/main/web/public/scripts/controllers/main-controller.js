'use strict';

angular
  .module('OpsBoard')
  .controller(
  'MainCtrl',
  function ($scope, $filter, $modal, $log, $controller, $timeout, $window, $rootScope, states, groups, links,
            plowTypes, plowDirections, loads, equipmentStatuses, user, signOffTime, OpsBoardRepository, ClientSideError,
            EquipmentHelperService, PersonnelHelperService, WorkUnits, urls, itemsPerPage, TaskHelperService, BoardValueService, BoardDataService, BoardHelperService) {
    $scope.user = user;

    var w = angular.element($window);

    w.bind('resize', function () {
      adjustLayout();
      $rootScope.$broadcast('UPDATE-TASKS', {repaint: true});
      $scope.$apply();
    });

    // Board details
    $scope.board = {
      location: OpsBoardRepository.getBoardLocation(),
      date: OpsBoardRepository.getBoardDate(),
      realDate: new Date(OpsBoardRepository.getBoardDate()
          .substring(4, 6)
        + '/'
        + OpsBoardRepository.getBoardDate().substring(
          6, 8)
        + '/'
        + OpsBoardRepository.getBoardDate().substring(
          0, 4)),
      displayDate: moment(
        new Date(OpsBoardRepository.getBoardDate()
            .substring(4, 6)
          + '/'
          + OpsBoardRepository.getBoardDate()
            .substring(6, 8)
          + '/'
          + OpsBoardRepository.getBoardDate()
            .substring(0, 4))).format(
        'MM/DD/YYYY'),
      version: 'Active Board',
      boroBoard: OpsBoardRepository.isBoroBoard()
    };

    $scope.materialList = BoardDataService.getMaterials();
    $scope.personnelStatusList = BoardDataService.getPersonnelStatusList();
    $scope.personnelSpecialPositionsList = BoardDataService.getPersonnelSpecialPositionsList();
    $scope.unsortedUnavailabilityCodeList = BoardDataService.getPersonnelUnavailabilityCodes();
    $scope.shifts = BoardDataService.getShiftData();
    $scope.shiftData = BoardDataService.getShiftData();
    $scope.tasks = OpsBoardRepository.getTasks();
    $scope.group = [];
    $scope.personnelpane = {};

    $scope.equipments = OpsBoardRepository.getEquipment();
    $scope.persons = OpsBoardRepository.getPersonnel();
    $scope.locations = WorkUnits.getLocations(BoardDataService.getRepairLocations());

    $scope.getSortedUnavailabilityCodeList = function () {
      return this.unsortedUnavailabilityCodeList.sort(function (me, that) {

          if (that == null)
            return 1;

          // Now sort by sortSequence
          if (me.sortSequence == null && that.sortSequence != null)
            return 1;
          else if (me.sortSequence != null && that.sortSequence == null)
            return -1;

          if (me.sortSequence != null && that.sortSequence != null) {
            if (me.sortSequence < that.sortSequence)
              return -1;
            else if (me.sortSequence > that.sortSequence)
              return 1;
          }
        }
      )
    };
    $scope.unavailabilityCodeList = $scope.getSortedUnavailabilityCodeList();

    // will change later
    if (Object.prototype.toString.call($scope.materialList) !== '[object Array]')
      $scope.materialList = [];
    if (Object.prototype.toString
        .call($scope.personnelStatusList) !== '[object Array]')
      $scope.personnelStatusList = [];
    if (Object.prototype.toString
        .call($scope.unavailabilityCodeList) !== '[object Array]')
      $scope.unavailabilityCodeList = [];
    if (Object.prototype.toString.call($scope.locations) !== '[object Array]')
      $scope.locations = [];

    /* Server connection */
    $scope.connected = OpsBoardRepository.isConnected();

    $scope.$on('connected', function (event, connected) {
      $scope.$apply(function () {
        $scope.connected = connected;
      });
    });

    // SMARTOB-8122: not use jQuery.width(), use screen.width to avoid 2 monitors case.
    // jQuery.width(): current computed width
    // var windowWidth = angular.element($window).width();
    var windowWidth = window.screen.width;
    var boardWidth = windowWidth;
    var splitterlWidth = 4;
    var leftScreenWidth = parseInt(windowWidth / 2);
    var rightScreenWidth = parseInt(windowWidth / 2);
    var leftPanelsWidth = leftScreenWidth - 193;
    var leftPanel = parseInt(leftPanelsWidth / 2);
    var rightPanel = parseInt(leftPanelsWidth / 2);
    var screenHeight = angular.element($window).height();

    $scope.taskWidthCurrent = rightScreenWidth;

    if (screenHeight < 625) {
      screenHeight = 625;
    }

    // Window Layout

    $scope.layout = {
      splitter: {
        width: splitterlWidth + 'px'
      },

      board: {
        width: boardWidth + 'px',
        height: screenHeight + 'px'

      },

      scrollpanel: {
        height: (screenHeight - 39) + 'px'
      },

      chart: {
        height: '660px'
      },

      taskpanel: {
        height: (screenHeight - 84) + 'px'
      },


      screen1: {
        pane1: {
          visible: true,
          width: leftPanel + 'px',
        },
        pane2: {
          visible: true,
          width: rightPanel + 'px'
        },
        visible: true,
        width: leftScreenWidth + 'px',
        taskWidth: rightScreenWidth + 'px',
        height: screenHeight + 'px',
        boardWidth: boardWidth + 'px'

      },
      screen2: {
        pane1: {
          visible: true,
          width: rightScreenWidth + 'px'
        },
        visible: true,
        width: rightScreenWidth + 'px'
      }
    };

    $scope.start = 0;
    $scope.size = 5;

    $scope.panes = {
      equipment: {
        active: true
      },
      equipmentDetails: {
        active: false
      },
      personnel: {
        active: true
      },
      boro: {
        active: false
      },
      personDetails: {
        active: false
      },
      recentActivity: {
        active: false,
        visible: false
      },
      tasks: {
        active: true
      },
      taskSettings: {
        active: false
      },
      reports: {
        active: false
      },
      charts: {
        active: false
      },
      showMultiRoutes: {
        active: false
      }
    };

    $scope.toggleEquipmentPane = function () {

      if (BoardValueService.EquipmentSummary) {
        $rootScope.$broadcast('SHOW-EQUIPMENT-SUMMARY');
        return;
      }

      $scope.panes.equipmentDetails.active = false;

      if ($scope.panes.taskSettings.active)
        return;
      $scope.panes.equipment.active = !$scope.panes.equipment.active;
      $scope.layout.screen1.pane1.visible = $scope.panes.equipment.active
        || $scope.panes.personDetails.active;
      $scope.layout.screen1.pane2.visible = $scope.panes.personnel.active
        || $scope.panes.equipmentDetails.active;
      adjustLayout();
    };

    $scope.togglePersonnelPane = function () {

      if (BoardValueService.PersonnelSummary) {
        $rootScope.$broadcast('SHOW-PERSONNEL-SUMMARY');
        return;
      }

      $scope.panes.personDetails.active = false;

      if ($scope.panes.taskSettings.active)
        return;
      $scope.panes.personnel.active = !$scope.panes.personnel.active;
      $scope.layout.screen1.pane1.visible = $scope.panes.equipment.active
        || $scope.panes.personDetails.active;
      $scope.layout.screen1.pane2.visible = $scope.panes.personnel.active
        || $scope.panes.equipmentDetails.active;
      adjustLayout();
    };

    $scope.toggleRecentActivityPane = function () {
      $scope.panes.recentActivity.visible = !$scope.panes.recentActivity.visible;
      $timeout(
        function () {
          $scope.panes.recentActivity.active = !$scope.panes.recentActivity.active;
        }, 100);
    };

    $scope.logout = function () {
      var pathElements = window.location.pathname.split('/'),
        pathStart = '/' + pathElements[1];
      $window.location = pathStart + urls.logout;
    };

    $scope.displayBoard = function (copyBoardTo) {
      var destinationUrl = "/displayboard/" + $scope.board.location + "#/" + $scope.board.date + "/screen1";

      if (window.location.pathname.indexOf('smart-opsboard') > -1) {
        destinationUrl = "/smart-opsboard/displayboard/" + $scope.board.location + "#/" + $scope.board.date + "/screen1";
      }

      window.open(destinationUrl);

    };

    $scope.districtBoard = function (location) {
      var destinationUrl = "/displayboard/" + $scope.board.location + "/" + $scope.board.date;

      if (window.location.pathname.indexOf('smart-opsboard') > -1) {
        destinationUrl = "/smart-opsboard/" + location + "/" + $scope.board.date;
      }

      window.open(destinationUrl);
    };

    $scope.publishBoard = function () {
      OpsBoardRepository.publishBoard();
    };

    $scope.helpPage = function () {
      window.open(encodeURI(links.manuals))
    };

    $scope.toggleTasksPane = function () {
      $scope.panes.tasks.active = !$scope.panes.tasks.active;
      $scope.layout.screen2.pane1.visible = $scope.panes.tasks.active ? true : false;
      $scope.layout.screen2.visible = $scope.panes.charts.active || $scope.panes.tasks.active;
      adjustLayout();
    };

    $scope.toggleTaskSettingsPane = function ($event) {

      $scope.panes.taskSettings.active = !$scope.panes.taskSettings.active;
      $scope.panes.reports.active = false;
      $scope.layout.screen1.visible = $scope.panes.taskSettings.active;


      $scope.layout.screen1.pane1.visible = $scope.panes.taskSettings.active ? false
        : $scope.panes.equipment.active
      || $scope.panes.personDetails.active;
      $scope.layout.screen1.pane2.visible = $scope.panes.taskSettings.active ? false
        : $scope.panes.personnel.active
      || $scope.panes.equipmentDetails.active;
      var tasks = {
        locations: []
      };

      $rootScope.$broadcast('task-setting-changed', tasks);
      adjustLayout();
    };

    $scope.massUpdateOptions = [{name: 'Chart'}];

    $scope.toggleBoroPane = function ($event) {
      if (!$scope.panes.boro.active) {
        $scope.panes.boro.active = true;
      } else {
        $scope.panes.boro.active = false;
      }
      adjustLayout();
    };

    $scope.toggleReportsPane = function ($event) {
      $scope.panes.reports.active = !$scope.panes.reports.active;
      $scope.panes.taskSettings.active = false;
      $scope.layout.screen1.visible = $scope.panes.reports.active;
      $scope.layout.screen1.pane1.visible = $scope.panes.reports.active ? false : $scope.panes.equipment.active || $scope.panes.personDetails.active;
      $scope.layout.screen1.pane2.visible = $scope.panes.reports.active ? false : $scope.panes.personnel.active || $scope.panes.equipmentDetails.active;
      adjustLayout();
    };

    $scope.toggleChartsPane = function ($event) {
      $rootScope.$broadcast('reset-charts-pane', $scope.panes.charts.active)
    };

    $rootScope.$on('adjust-layout', adjustLayout);

    $scope.closeEquipmentDetailsPane = function () {
      $scope.pieceOfEquipment = null;
      $scope.panes.equipmentDetails.active = false;
      $scope.layout.screen1.pane2.visible = $scope.panes.personnel.active || $scope.panes.equipmentDetails.active;
      $rootScope.$broadcast('UPDATE-PERSONNEL-PANE');
      adjustLayout();
    };

    /* shoeDetails of person or equipment         */

    $scope.ifEntityExistsAndHasLinkedDetails = function (type, id) {
      if (type != 'equipment' && type != 'person')
        return false; //

      if (type == 'equipment'
        && $scope.equipments[id])
        return true;
      if (type == 'person'
        && $scope.persons[id])
        return true;
      return false;
    };

    $scope.ifEntityRemoved = function (type, id) {
      if (type == 'equipment'
        && !$scope.equipments[id])
        return true;
      if (type == 'person'
        && !$scope.persons[id])
        return true;

      return false;
    };

    $scope.ifExist = function (type, id) {
      if (type != 'equipment' || type != 'person')
        return true;

      if (type == 'equipment'
        && $scope.equipments[id])
        return true;
      if (type == 'person'
        && $scope.persons[id])
        return true;
      return false;
    };

    $scope.showDetails = function (event, type, id) {
      if (!$scope.ifEntityExistsAndHasLinkedDetails(type, id))
        return;
      if (type == 'equipment') {
        _showEquipmentDetails(event, id);
      } else if (type == 'person') {
        _showPersonDetails(id, true);
      }
    };

    var _updateEquipmentDetails = function (event, equipmentId) {
      var currentEquipment = $scope.equipments[equipmentId];
      if (!currentEquipment) return $scope.closeEquipmentDetailsPane();
      $scope.pieceOfEquipment = currentEquipment;
    }

    var _showEquipmentDetails = function (event, equipmentId) {
      if (!equipmentId) {
        return;
      }

      // Disregard actions on menu items
      if (event && event !== 'loose focus show' && event.target.className.indexOf("menu-item") > -1) {
        return;
      }

      // Format equipment values  Amanda

      $scope.pieceOfEquipment = $scope.equipments[equipmentId];

      if ($scope.pieceOfEquipment.lastUpdated != null && $scope.pieceOfEquipment.lastUpdated != '') {
        $scope.pieceOfEquipment.lastUpdated = $scope.pieceOfEquipment.lastUpdated.substring(0, 16);
        $scope.pieceOfEquipment.formattedDetachments = $scope.pieceOfEquipment.getFormattedDetachments();
      }

      if (event == 'loose focus show')
        $scope.pieceOfEquipment.formattedBins = pieceOfEquipment.formattedBins;
      else
        $scope.pieceOfEquipment.formattedBins = $scope.pieceOfEquipment.getFormattedBins($scope.materialList);
      // Show equipment details
      $scope.panes.equipmentDetails.active = true;
      $scope.$broadcast('reset-details-pane'); // informed details controller to reset

      // Adjust layout
      $scope.layout.screen1.pane2.visible = true;
      adjustLayout();

      //pagination for detachment hisotry
      $scope.detachmentPerPage = 5;
      $scope.pieceOfEquipment.detachmentCurrentPage = 1;
      $scope.pieceOfEquipment.formattedDetachments = $scope.pieceOfEquipment.getFormattedDetachments();
      $scope.pieceOfEquipment.showPaginationLinksForDetachment = $scope.pieceOfEquipment.formattedDetachments.length > $scope.detachmentPerPage;
      var detachmentBegin = (($scope.pieceOfEquipment.detachmentCurrentPage - 1) * $scope.detachmentPerPage);
      var detachmentEnd = detachmentBegin + $scope.detachmentPerPage;
      $scope.pieceOfEquipment.equipmentDetachmentPaginationModel = $scope.pieceOfEquipment.formattedDetachments.slice(detachmentBegin, detachmentEnd);
      $scope.pieceOfEquipment.loadedDetachmentPages = [1, 2];
      $scope.personnelpane = {};

      //pagination for up/down hisotry
      $scope.pieceOfEquipment.FormattedUpDownHistory = $scope.pieceOfEquipment.getFormattedUpDownHistory();
      $scope.upDownPerPage = 5;
      $scope.pieceOfEquipment.upDownCurrentPage = 1;
      $scope.pieceOfEquipment.showPaginationLinksForUpDown = $scope.pieceOfEquipment.getFormattedUpDownHistory().length > $scope.upDownPerPage;
      var upDownBegin = (($scope.pieceOfEquipment.upDownCurrentPage - 1) * $scope.upDownPerPage);
      var upDownEnd = upDownBegin + $scope.upDownPerPage;
      $scope.pieceOfEquipment.upDownPaginationModel = $scope.pieceOfEquipment.getFormattedUpDownHistory().slice(upDownBegin, upDownEnd);
      $scope.pieceOfEquipment.loadedUpDownPages = [1, 2];
    };

    $scope.closePersonDetailsPane = function () {
      $scope.panes.personDetails.active = false;
      $scope.layout.screen1.pane1.visible = $scope.panes.equipment.active || $scope.panes.personDetails.active;
      adjustLayout();
    };

    $scope.person = { // required for personnel-details-pane.html to render
      fullName: ''
    };

    $scope.titleMap = function (title) {
      var results = OpsBoardRepository.getMappedTitle(title);
      return results;
    };

    var _showPersonDetails = function (personId, showPanel) {
      if (!personId) {
        return;
      }

      // Person display logic
      $scope.showQualification = false;

      if (typeof personId === 'object') {
        $scope.person = personId;
      } else {
        $scope.person = $scope.persons[personId];
      }

      if ($scope.person === undefined) {
        $scope.person = BoardValueService.districtPersons[personId];
      }

      if ($scope.person.civilServiceTitle.toLowerCase() === 'sw')
        $scope.showQualification = true;

      $scope.person.formattedUnavailableReasons = $scope.person.getFormattedUnavailableReasons();
      $scope.person.unavailableReasonsCount = $scope.person.formattedUnavailableReasons.length;
      $scope.person.formattedSpecialPositions = $scope.person.getFormattedSpecialPositions();
      $scope.person.formattedMdaStatus = $scope.person.getFormattedMdaStatus();
      $scope.person.formattedDetachments = $scope.person.getFormattedDetachmentHistory();
      $scope.person.formattedGroundingHistory = $scope.person.getFormattedGroundedHistory();

      $scope.person.personDetachPaginationModel = $scope.person.getFormattedDetachmentHistory().slice($scope.start, $scope.size);
      $scope.person.personMDAPaginationModel = $scope.person.getFormattedMdaStatus().slice($scope.start, $scope.size);
      $scope.person.specialPositionsPaginationModel = $scope.person.getFormattedSpecialPositions().slice($scope.start, $scope.size);
      $scope.person.groundingHistoryPaginationModel = $scope.person.getFormattedGroundedHistory().slice($scope.start, $scope.size);
      $scope.person.unavailabilityPaginationModel = $scope.person.getFormattedUnavailableReasons().slice($scope.start, $scope.size);
      $scope.person.personDetachCurrentPage = 1;
      $scope.person.personMDACurrentPage = 1;
      $scope.person.unavailabilityCurrentPage = 1;
      $scope.person.specialPositionsCurrentPage = 1;
      $scope.person.groundingHistoryCurrentPage = 1;

      // Show person details if showPanel is true
      if (showPanel === true) {
        $scope.panes.personDetails.active = true;
        $scope.$broadcast('reset-details-pane'); // informed details controller to reset
        // Adjust layout
        $scope.layout.screen1.pane1.visible = true;
        adjustLayout();
      }
      /*
       //detachment history pagination
       $scope.person.personDetachCurrentPage = 1;
       $rootScope.$broadcast('DETACH-PERSON', $scope.person);

       // MDA history
       $scope.person.personMDACurrentPage = 1;
       $rootScope.$broadcast('MDA-PERSON', $scope.person);

       // Unavailability History - start
       $scope.person.unavailabilityCurrentPage = 1;
       $rootScope.$broadcast('UNAVAILABILITY-PERSON', $scope.person);

       // Unavailability History - end

       // Special Positions - start
       $scope.person.specialPositionsCurrentPage = 1;
       $rootScope.$broadcast('SPECIAL-POSITION-PERSON', $scope.person);

       // Special Positions - end

       // grounding - start
       $scope.person.groundingHistoryCurrentPage = 1;
       $rootScope.$broadcast('GROUND-PERSON', $scope.person);
       */

    };


    $scope.showEntityDetails = function (action) {
      if (action.entity.type === 'equipment' && $scope.equipments) {
        _showEquipmentDetails(null, action.id);
      }

      if (action.entity.type === 'personnel' && $scope.persons) {
        _showPersonDetails(action.id, true);
      }
    };

    function adjustLayout(tasksWidth) {
      $scope.layout.screen1.visible = $scope.layout.screen1.pane1.visible || $scope.layout.screen1.pane2.visible || $scope.panes.taskSettings.active || $scope.panes.reports.active;
      $scope.layout.screen2.visible = $scope.layout.screen2.pane1.visible || $scope.panes.charts.active;

      var windowWidth = angular.element($window).width();
      var boardWidth = windowWidth;
      var splitterlWidth = 4;

      var leftScreenWidth = parseInt(windowWidth / 2);
      var rightScreenWidth = parseInt(windowWidth / 2);

      var leftPanelsWidth = leftScreenWidth - 193;
      var leftPanel = parseInt(leftPanelsWidth / 2);
      var rightPanel = parseInt(leftPanelsWidth / 2);

      if (tasksWidth) {
        rightScreenWidth = tasksWidth;
        leftScreenWidth = windowWidth - rightScreenWidth;
      }

      var screenHeight = angular.element($window).height();

      if (screenHeight < 625) {
        screenHeight = 625;
      }

      // #adjustLayout

      $scope.layout.board.height = screenHeight + 'px';
      $scope.layout.screen1.height = screenHeight + 'px';
      $scope.layout.scrollpanel.height = (screenHeight - 37) + 'px';
      $scope.layout.chart.height = '660px';

      if ($scope.layout.screen1.visible && $scope.layout.screen2.visible) {
        $scope.layout.screen1.width = leftScreenWidth + 'px';
        $scope.layout.screen2.width = rightScreenWidth + 'px'
      } else if ($scope.layout.screen1.visible) {
        $scope.layout.screen1.width = windowWidth + 'px';
      } else if ($scope.layout.screen2.visible) {

        $scope.layout.screen2.width = windowWidth + 'px';
        $scope.layout.screen1.taskWidth = windowWidth + 'px';
      }

      //if($scope.panes.taskSettings.active || $scope.panes.boro.active)
      if ($scope.panes.taskSettings.active || $scope.panes.boro.active) {

        if (leftScreenWidth > 962) {
          $scope.layout.screen1.width = 962 + 'px';
          $scope.layout.screen2.width = windowWidth - 962 + 'px';
          $scope.layout.screen1.taskWidth = windowWidth - 965 + 'px';
        } else {
          $scope.layout.screen1.width = leftScreenWidth + 'px';
          $scope.layout.screen2.width = rightScreenWidth + 'px';
          $scope.layout.screen1.taskWidth = rightScreenWidth + 'px';
        }
      }

      if ($scope.panes.reports.active) {
        $scope.layout.screen1.width = leftScreenWidth + 'px';
        $scope.layout.screen2.width = rightScreenWidth + 'px';
        $scope.layout.screen1.taskWidth = rightScreenWidth + 'px';
      }

      // Adjust panes

      if ($scope.layout.screen1.visible && $scope.layout.screen2.visible) {

        // Left and right screens visible

        if ($scope.layout.screen1.pane1.visible && $scope.layout.screen1.pane2.visible) {

          // Both panels visible

          $scope.layout.screen1.pane1.width = leftPanel + 'px';
          $scope.layout.screen1.pane2.width = rightPanel + 'px';
          $scope.layout.screen1.taskWidth = rightScreenWidth + 'px';

        } else if ($scope.layout.screen1.pane1.visible && !$scope.layout.screen1.pane2.visible) {

          $scope.layout.screen1.width = leftScreenWidth + 'px';
          $scope.layout.screen1.pane1.width = leftScreenWidth + 'px';
          $scope.layout.screen1.taskWidth = rightScreenWidth + 'px';

        } else if (!$scope.layout.screen1.pane1.visible && $scope.layout.screen1.pane2.visible) {
          $scope.layout.screen1.taskWidth = rightScreenWidth + 'px';
          $scope.layout.screen1.pane2.width = leftScreenWidth + 'px';
        }

      } else if ($scope.layout.screen1.visible) {

        // Right screen off, left screen on
        $scope.layout.screen1.width = windowWidth + 'px';

        if ($scope.layout.screen1.pane1.visible && $scope.layout.screen1.pane2.visible) {

          windowWidth = windowWidth - 193;

          leftPanel = parseInt(windowWidth / 2)
          rightPanel = parseInt(windowWidth / 2);

          $scope.layout.screen1.width = windowWidth + 'px';
          $scope.layout.screen1.pane1.width = leftPanel + 'px';
          $scope.layout.screen1.pane2.width = rightPanel + 'px';

        } else if ($scope.layout.screen1.pane1.visible && !$scope.layout.screen1.pane2.visible) {
          $scope.layout.screen1.width = windowWidth + 'px';
          $scope.layout.screen1.pane1.width = windowWidth + 'px';
        } else if (!$scope.layout.screen1.pane1.visible && $scope.layout.screen1.pane2.visible) {
          $scope.layout.screen1.width = windowWidth + 'px';
          $scope.layout.screen1.pane2.width = windowWidth + 'px';
        }

      } else if ($scope.layout.screen2.visible) {
        windowWidth = windowWidth - 193;
        $scope.layout.screen2.width = windowWidth + 'px';
        $scope.layout.screen1.taskWidth = windowWidth + 'px';
      }
    }

    // Always show date the user chooses
    var lengthOfURL = window.location.pathname.length;
    var dd = parseInt(window.location.pathname.substring(lengthOfURL - 2, lengthOfURL));
    var mm = parseInt(window.location.pathname.substring(lengthOfURL - 4, lengthOfURL - 2)) - 1;
    var yy = parseInt(window.location.pathname.substring(lengthOfURL - 8, lengthOfURL - 4));
    var ddtomorrow = dd + 1;

    $scope.dt = new Date(yy, mm, ddtomorrow);

    //for function navigation to another date board.

    $scope.status = {
      isopen: false
    };

    $scope.changeDate = function (dtd) {
      var lengthOfU = window.location.pathname.length;
      var changeD = window.location.pathname.substring(0, lengthOfU - 8);
      var formatttedDate = moment(dtd).format('YYYYMMDD');
      /*$window.location.pathname=changeD+formatttedDate;*/
      $scope.status.isopen = !$scope.status.isopen;
      $window.open(changeD + formatttedDate);
    };

    // Control for date picker
    $scope.datepickers = {
      startDateOpened: false,
      endDateOpened: false
    };

    $scope.open = function ($event, opened) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope.datepickers[opened] = !$scope.datepickers[opened];
      if ($scope.datepickers.startDateOpened == true && $scope.datepickers.endDateOpened == true)
        $scope.datepickers.endDateOpened = false;
    };

    var _decideAssignmentType = function (event, data) {
      var task = data.task,
        type = data.type,
        position = data.position,
        elementId = data.elementId,
        locationId = data.locationId,
        shiftId = data.shiftId;

      if (task.assignedPerson1.person != null && task.assignedPerson2.person != null) {
        return;
      }

      var otherPos = (position == 1) ? 2 : 1;

      if (task["assignedPerson" + otherPos].person && task["assignedPerson" + otherPos].person.id == elementId) {
        return;
      }

      var nextDayOnly = [{id: 'NEXT_DAY', checked: false, name: 'The Next Day'}],
        all = [{id: 'OVERTIME', checked: false, name: 'Overtime'}, {
          id: 'NEXT_DAY',
          checked: false,
          name: 'The Next Day'
        }, {id: 'OTHER', checked: false, name: 'Diversion'}],
        noNextDay = [{id: 'OVERTIME', checked: false, name: 'Overtime'}, {
          id: 'OTHER',
          checked: false,
          name: 'Diversion'
        }];

      $scope.person = $scope.persons[elementId];

      if (!$scope.person.availableNextDay && (!$scope.person.assignedNextDayShifts || !$scope.person.assignedNextDayShifts.length) && BoardHelperService.isCurrentOrPastBoard()) {
        $scope.assignmentTypes = all;
        if ($scope.person.state == 'Unavailable' && !$scope.person.assigned) {
          $scope.assignmentTypes = nextDayOnly;
        } else if ($scope.person.assigned) {
          var shifts = TaskHelperService.getPersonAssignedShifts(elementId);
          if (shifts.indexOf(shiftId) >= 0) {
            $scope.assignmentTypes = noNextDay;
          }
        }
      } else {
        $scope.assignmentTypes = noNextDay;
      }

      $scope.passPreValidation = function () {
        return true;
      };


      var activeUnavailable = $scope.person.getFormattedUnavailableReasons();
      var formattedT = moment(BoardValueService.boardData.boardDate, 'YYYYMMDD').add(1, 'days').format('MM/DD/YYYY');
      var toNotAvailable = false;

      for (var i = 0; i < activeUnavailable.length; i++) {
        if (activeUnavailable[i].action != 'Cancelled' && activeUnavailable[i].action != 'Removed' && (activeUnavailable[i].dispStatus == 'Active' || activeUnavailable[i].dispStatus == 'Future')
          && (activeUnavailable[i].startDate == formattedT || activeUnavailable[i].endDate == formattedT || (activeUnavailable[i].startDate < formattedT && (activeUnavailable[i].endDate > formattedT || activeUnavailable[i].endDate == ""))))
          toNotAvailable = true;
      };

      $scope.opData = {
        errors: [],
        titleAction: 'Select Assignment Type',
        titleEntity: '',
        cancelButtonText: 'Cancel',
        submitButtonText: 'Submit',
        assignmentType: '',
        clientErrors: [],
        required: []
      };

      $scope.changeType = function (aType) {

        angular.forEach($scope.assignmentTypes, function (item) {
          item.checked = false;
        });
        aType.checked = true;
      };

      var modalInstance = $modal.open({
        templateUrl: appPathStart + '/views/modals/modal-task-assignment-type',
        controller: 'ModalCtrl',
        backdrop: 'static',
        size: 'tiny',
        resolve: {
          data: function () {
            return ({});
          }
        },
        scope: $scope
      });

      var _assginmentTypeToTask = function (task, type, position, elementId, locationId) {
        assignToTaskByDragAndDrop($scope, OpsBoardRepository, task, type, position, elementId, locationId, null, null, null, null);
      };

      $scope.operation = function (success, error) {

        $scope.opData.clientErrors = [];

        // something must be selected
        if (!$scope.opData.assignmentType) {
          $scope.opData.clientErrors.push({
            type: 'danger',
            message: 'You must select an assignment type.'
          });
        }

        // check that person is available for next day assignment
        // Commented out by VG. Per Bruce, we are currently removing this message.
        /* if ($scope.opData.assignmentType === 'NEXT_DAY' && toNotAvailable === true) {
         $scope.opData.clientErrors.push({
         type: 'danger',
         message: 'Person is not available for next day assignment.'
         });
         }*/

        if ($scope.opData.clientErrors.length > 0) {
          return error(new ClientSideError('ValidationError'));
        }

        // update task model with assignment type for position
        if ($scope.opData.assignmentType !== '') {
          task['assignedPerson' + position].type = $scope.opData.assignmentType;
          _assginmentTypeToTask(task, type, position, elementId, locationId);
        }

        modalInstance.close();

      };

      modalInstance.result.then(function () {
      }, function () {
        /* $log.info('Modal dismissed at: ' + new Date());*/
      });
    };
    /* Show Details */

    var locals = {
      $scope: $scope,
      $filter: $filter,
      $modal: $modal,
      $log: $log,
      OpsBoardRepository: OpsBoardRepository,
      ClientSideError: ClientSideError,
      equipmentStatuses: equipmentStatuses,
      groups: groups,
      states: states,
      plowTypes: plowTypes,
      plowDirections: plowDirections,
      loads: loads
    };
    /* personnel logic */
    /* personnel Modals - Start */

    /** Detach Person * */
    $controller('DetachPerson', locals);

    /** Set Unavailable * */
    $controller('SetPersonnelUnavailableStatus', locals);

    /** Set Unavailable - End * */
    $controller('SetPersonnelMDAStatus', locals);
    $controller('RemovePersonnelRecord', locals);
    $controller('CopyBoard', locals);
    $controller('ReloadBoard', locals);

    $controller('AddSpecialPosition', locals);
    $controller('UpdateSpecialPosition', locals);

    /*Cancel Unavailable */
    $controller('CancelPersonnelUnavailableStatus', locals);
    /*Reverse Cancel Unavailable*/
    $controller('ReverseCancelPersonnelUnavailableStatus', locals);

    /*  $controller('PersonAssignmentType', locals);*/

    /* personnel Modals - End */

    /* Equipment Logic */
    /* Equipment Modals - Start */

    /** Update Load Status * */
    $controller('LoadStatusEquipment', locals);

    /** Cancel Detach * */
    $controller('CancelDetachEquipment', locals);

    /** Attach Equipment * */
    $controller('AttachEquipment', locals);

    /** Detach Equipment * */
    $controller('DetachEquipment', locals);

    /** Down Equipment * */
    $controller('DownEquipment', locals);

    /** Up Equipment **/
    $controller('UpEquipment', locals);

    /** Update Snow Equipment **/
    $controller('UpdateSnowReadinessEquipment', locals);

    /** isEquipmentSelectViewable * */
    //$controller('isEquipmentSelectViewable', locals);

    // Up Equipment end
    /* Equipment Modals - End */

    adjustLayout();

    $scope.$on('SHOW-PERSON-DETAILS', function (event, data) {
      _showPersonDetails(data.personId, true);
    });

    $scope.$on('UPDATE-PERSON-DETAILS', function (event, data) {
      // update personnel details only when already open and viewing same person
      if ($scope.panes.personDetails.active && ($scope.person.id === data.personId)) {
        _showPersonDetails(data.personId);
      }
    });

    $scope.$on('SHOW-EQUIPMENT-DETAILS', function (event, data) {
      _showEquipmentDetails(null, data.equipmentId);
    });

    $scope.$on('UPDATE-EQUIPMENT-DETAILS', function (event, data) {
      if ($scope.panes.equipmentDetails.active && ($scope.pieceOfEquipment.id === data.equipmentId)) {
        _updateEquipmentDetails(null, data.equipmentId);
      }
    });

    $scope.$on('DECIDE-ASSIGNMENT-TYPE', function (event, data) {
      _decideAssignmentType(event, data);
    });

    $rootScope.$broadcast('cfpLoadingBar:started');
    $scope.$on('$viewContentLoaded', function () {
      $rootScope.$broadcast('cfpLoadingBar:completed');
      $rootScope.hidespinner = true;
      $('#loading-bar-spinner').hide();
    });

    $rootScope.$on('RESIZE-TASK-PANE', function (event, data) {

      if (!data.width > -1) {
        data.width = $scope.taskWidthCurrent;
      }

      if (data.width > 0) {

        if (data.taskSettingFlag) {
          return;
        }

        $scope.taskWidthCurrent = data.width;
        $scope.layout.screen1.taskWidth = data.width + 'px';
        $scope.$apply();
      }

    });
  });