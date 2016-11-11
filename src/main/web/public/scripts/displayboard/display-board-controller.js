(function () {
    'use strict';

    Array.prototype.last = function () {
      return this[this.length - 1];
    };

    angular
      .module('OpsBoard')
      .controller('DisplayBoardCtrl',
      function ($resource, $scope, $filter, $log, $timeout, $interval, $window, ReferenceDataService, durations, BoardValueService, $q, $sce, $http, DisplayBoardHelperService, DisplayBoardHelperCommon, DisplayBoardHelperFormatting, DisplayBoardHelperFormattingLarge) {

        $scope.to_trusted = function(html_code) {
          return $sce.trustAsHtml(html_code);
        };

        // Section - WebSocket connection for events/messaging - Start
        var stompClient = null;
        var socket = null;
        var connected = false;

        // core ref data used for lookups
        var boardLocation = _.last(location.pathname.split('/'));

        // location href is different for display boards, set proper date id
        ReferenceDataService.setParams({
          boardDate: $window.location.hash.split('/')[1]
        });

        var boardDate = $window.location.hash.split('/')[1];
        var finalBoardDate = boardDate.substring(4, 6)+ '-' + boardDate.substring(6, 8) + '-' + boardDate.substring(0, 4);

        var refData = {
          downCodes: ReferenceDataService.loadEquipmentDownCodes(),
          shifts: ReferenceDataService.loadShifts(),
          categories: ReferenceDataService.loadCategories(boardLocation),
          categoriesObject: ReferenceDataService.loadCategories(boardLocation, true),
          materials: ReferenceDataService.loadMaterialList(),
          personnelStatus: ReferenceDataService.loadPersonnelStatusList(),
          locations: ReferenceDataService.loadRepairLocations(),
          unavailableCodes: ReferenceDataService.loadPersonnelUnavailabilityCodes(),
          specialPositions: ReferenceDataService.loadPersonnelSpecialPositionsList(),
          boardDate: finalBoardDate,
          boardLocation: boardLocation
        };

        BoardValueService.refData = refData;

        // Converting location data to an Object/Key reference for the filters - for color display purposes
        var displayLocationsRefData = {};
        refData.locations.then(function(displaylocation){
          var tempLocationsRefData = displaylocation;

          tempLocationsRefData.forEach(function(i) {
              displayLocationsRefData[i.code] = i;
          });

          // Adding location reference data to the board values service
          BoardValueService.displaylocationRefsData = displayLocationsRefData;

        });

        var refDataPromises = [];

        // array of promises to all
        Object.keys(refData).forEach(function (val) {
          refDataPromises.push(refData[val]);
        });

        $q.all(refDataPromises).then(function (data) {
          // assuming data is same index as ruest, angular doesnt support spreading. FIX: add real Q or bluebird to support this

          Object.keys(refData).forEach(function (val, index) {
              refData[val] = data[index];
          });

          refData.allSubcategories = DisplayBoardHelperService.getAllSubcategories(refData.categories);
          refData.allSubcategoriesObject = DisplayBoardHelperService.getAllSubcategoriesObject(refData.categories);
          $scope.refData = refData;
          refData.shiftsObject = {};

          _.each(refData.shifts, function(shift) {
            refData.shiftsObject[shift.name] = {name: shift.name, sequence: shift.sequence};
          });

          BoardValueService.refData = refData;

          refreshBoard($resource, $scope, refData);
        });

        function setConnection(isConnected) {
          connected = isConnected;
          $scope.$broadcast('connected', connected);
        }

        function openWebSocket() {
          var pathElements = window.location.pathname.split('/displayboard'),
            pathStart = pathElements[0];

          pathElements = pathElements[1].split('/');

          if (!socket) {
                  socket = new SockJS(pathStart + '/wsdisplayboard/' + pathElements[1]);
                  stompClient = Stomp.over(socket);
                  stompClient.connect({}, function() {
                    setConnection(true);
                    stompClient.subscribe('/topic/displayboard.' + pathElements[1].toUpperCase(), function(message) {
                      receiveDisplayBoardCommand(message,false);
                  });
              }, function(error) {
                  if (error.headers && error.headers.message) {
                      console.log(error.headers.message);
                  } else {
                      $scope.bodyerror = true;
                      $scope.$apply();
                      console.log(error);
                      $timeout(function() {
                          $interval(function() {
                              $http.get(window.location)
                                .success(function () {
                                    window.location.reload();
                                }
                              ).error(function () {
                                    $scope.bodyerror = true;
                                });
                          }, 60000);

                      }, 60000);
                  }
                  setConnection(false);
              });
          }
        }

        var timerset = false;

        if (!timerset) {
          timerset = true;
          $interval( function() {
              var pathElements = window.location.pathname.split('/displayboard'),
                pathStart = pathElements[0],
                boardLocation2 = pathElements[1];
              var resource = $resource(pathStart + '/' + boardLocation2 + '/heartbeat'),
                response = resource.get();
              return response.$promise.then(function() {
                  $log.log('heartbeat for ' + pathStart + '/' + boardLocation2 + '/heartbeat');
              });
          },5*1000*60);
        }

        function receiveDisplayBoardCommand (message) {
          var command = JSON.parse(message.body);
          console.log(command);
          switch (command.commandName) {
          case 'PublishBoard':
        	  reloadBoard($resource, $scope, command.date);
              break;
          case 'CheckStatus':
            processCheckStatusCommand($resource, $scope, command);
            break;  
          }
          // Disregard error messages
          if (command.error !== 'undefined' && command.error > 0) {
              return;
          }
        }

        function reloadBoard($resource, $scope, commanddate) {
            window.location.hash = '#/' + commanddate + '/screen1';
            window.location.reload();
        }
        
        function processCheckStatusCommand($resource, $scope, command){
        	command.status = true;
        	command.commandContent.remoteAddress = getRemoteAddress();
        	sendCommand(command);
        }
        
        function sendCommand(command) {
        	stompClient.send('/app/kioskDashboard', {}, JSON.stringify(command));
        }

        function refreshBoard($resource, $scope, refData) {
          var params  = window.location.hash.split('/');
          var pathElements = window.location.pathname.split('/displayboard'),
            pathStart = pathElements[0];

          pathElements = pathElements[1].split('/');

          var boardLocation3 = pathElements[1],
            boardDate3 = params[1];

          var resource = $resource(pathStart + '/:district/:date/load', {
              district: null,
              date: null
          });

          var response = resource.get({
              district: boardLocation3,
              date: boardDate3
          });

          return response.$promise.then(function (data) {
            var orderByLocation = $filter('orderByLocation')(data.taskContainers, $scope.refData.locations);

            refData.locationcount = orderByLocation.length;

            if(data.locationType === 'borough') {
                  var newTaskContainers = {};
                  newTaskContainers[data.location] = {};
                  newTaskContainers[data.location] = data.taskContainers[data.location];
                  data.taskContainers = newTaskContainers;
              }

              $scope.data = data;
              $scope.remoteAddress = data.remoteAddress;
              

              var formattedNames = {};


              angular.forEach($scope.data.personnel, function(person, key) {
                  person = $filter('extendDisplayPerson')(person);
                  person.formattedName = $filter('getFormattedName')(person);
                  if(!formattedNames[person.formattedName]) {
                      formattedNames[person.formattedName] = [];
                  }
                  formattedNames[person.formattedName].push(person.id);
              });

              angular.forEach(formattedNames, function(name, key) {
                  if(name.length > 1) {
                      angular.forEach(name, function(id, key) {
                          $scope.data.personnel[id].formattedName = $filter('getFormattedNameMI')($scope.data.personnel[id]);
                      });
                  }
              });

              var equipment = data.equipment;

              BoardValueService.equipment = data.equipment;
              BoardValueService.personnel = data.personnel;
              refData.sections = {};
              refData.shiftCategories = {};
              refData.subcategorytasks = {};
              refData.tasksObject = {};
              refData.person2= {};
              refData.currentColHeight = 0;
              refData.cols = {};
              refData.col = .1;
              refData.colcount = 1;
              refData.cols['col' + refData.col] = [];
              refData.lastShiftBlock = '';
              refData.lastSubcategoryBlock = '';
              refData.lastSectionBlock = '';
              refData.blockCount = 0;
              refData.lastBlockType = '';
              refData.previousColLastBlock = '';
              refData.columnShift = false;
              refData.lastheaderLocation = '';
              refData.lastDateTimeBlock = '';
              refData.lastBlockAddedType = '';
              refData.targetColHeight = 0;
              refData.mapgroupobject = {};

              // BUILD SNAKING ARRAY

              refData = DisplayBoardHelperFormattingLarge.makeBlocks(orderByLocation, data, refData);

              refData.lastSubcategoryBlock = '';
              refData.lastSectionBlock = '';
              
              var location = refData.mapgroupobject.orderByLocation[0];
              refData = DisplayBoardHelperFormatting.headerLocation(location, refData);
              refData = DisplayBoardHelperFormattingLarge.unavailable(data, refData);
              refData = DisplayBoardHelperFormattingLarge.detached(data, refData);
              refData = DisplayBoardHelperFormattingLarge.multiroute(refData.mapgroupobject.multiroutes, refData);
              refData = DisplayBoardHelperFormatting.footerBlock(refData);

              var newstext = [];

              newstext.forEach(function(newstext) {
                //refData = DisplayBoardHelperFormatting.checkHeight(74, 'dsny', refData);
                refData.colcount = refData.colcount + 1;
                refData.col = refData.col + .1;
                refData.cols['col' + refData.col] = [];
                refData.currentColHeight = refData.currentColHeight + 168;
                refData.cols['col' + refData.col].push({type: 'lastDateTimeBlock', html: refData.lastDateTimeBlock});
                refData.cols['col' + refData.col].push({type: 'padding', html: '<div class="firstheader greyback">DSNY Message</div>'});
                refData.cols['col' + refData.col].push({type: 'padding', html: '<div class="newstext">' + newstext+ '</div>'});
              });

              var promotext = [];

              promotext.forEach(function(promo) {
                //refData = DisplayBoardHelperFormatting.checkHeight(74, 'dsny', refData);
                refData.colcount = refData.colcount + 1;
                refData.col = refData.col + .1;
                refData.cols['col' + refData.col] = [];
                refData.currentColHeight = refData.currentColHeight + 168;
                refData.cols['col' + refData.col].push({type: 'lastDateTimeBlock', html: refData.lastDateTimeBlock});
                refData.cols['col' + refData.col].push({type: 'padding', html: '<div class="firstheader greyback">DSNY Promotions</div>'});
                refData.cols['col' + refData.col].push({type: 'padding', html: '<div class="newstext">' + promo+ '</div>'});
              });

              var monitorWidth = 1920;
              var colsPerScreen = Math.floor(monitorWidth / monitorWidth) * 3;
              var fullscreens = Math.floor(refData.colcount / colsPerScreen) * colsPerScreen;
              var monitors = Math.floor($window.outerWidth / $window.devicePixelRatio / monitorWidth);
              var screensTotal = Math.ceil($scope.boardWidth / monitorWidth);

              var screensNeeded = Math.ceil(refData.colcount / colsPerScreen);
              var screensMax = screensNeeded - monitors;
              var colsNeeded = screensNeeded * colsPerScreen;
              var extraCols = colsNeeded - refData.colcount;
              var screenOffset = screensTotal - screensNeeded - 1;
              var startScreen = 1;
              var screen = 1;
              var hashArray = window.location.hash.split('/');

              for(var extraloop = 0; extraloop < extraCols; extraloop = extraloop + 1 ) {
                  refData.colcount = refData.colcount + 1;
                  refData.col = refData.col + .1;
                  refData.cols['col' + refData.col] = [];

                  /*refData.currentColHeight = refData.currentColHeight + 168;
                  refData.cols['col' + refData.col].push({type: 'lastDateTimeBlock', html: refData.lastDateTimeBlock});
                  refData.cols['col' + refData.col].push({type: 'padding', html: '<div class="firstheader greyback">&nbsp;</div>'});*/
              }

                $scope.boardWidth = (refData.colcount + extraCols) * 657;
                $scope.maxColHeight = $window.innerHeight - 67;
                $scope.innerWidthClss = $window.innerWidth;

                window.location.hash = '#/' + hashArray[1] + '/screen1';

                //PASS COLS TO VIEW
                $scope.cols = refData.cols;

                $interval(function () {
                  var scrolltarget = 0;
                  var offset = 0;

                  if(screen > screensMax) {
                    screen = 0;
                  }

                  screen = screen + 1;

                  if(screen == 1) {
                    scrolltarget = 0;
                  } else {
                    //offset = 25 * (screen - 1);
                    scrolltarget = ((screen - 1) * monitorWidth) - offset;
                  }

                  $(window).scrollLeft(scrolltarget);
                  window.location.hash = '#/' + hashArray[1] + '/screen' + screen;
                }, (1000 * 12) );

                if (!socket) {
                    openWebSocket();
                }
            }, function (error) {
                $scope.bodyerror = true;
                $timeout(function() {
                    window.location.reload();
                }, 60000);

                return error;
            });
          }
        
        function getRemoteAddress(){
        	return $scope.remoteAddress;
        }
        
        }
      );
}());