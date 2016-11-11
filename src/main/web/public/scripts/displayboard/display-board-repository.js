'use strict';

angular.module('OpsBoard')
    .directive('shiftitem', function($timeout) {

      return {
        replace: true,
        restrict: 'E',
        template: '<div class="item"><div class="firstheader">Hello</div></div>',
        link: {
          pre: function PreLinkingFunction($scope, $element, $attributes) {
            $timeout(function() {

              var pathElements = window.location.pathname.split('/displayboard'),
                  pathStart = pathElements[0],
                  pathElements = pathElements[1].split('/'),
                  boardLocation = pathElements[1];

              var snake = '';
              var shifts = $scope.data.taskContainers[boardLocation].locationShifts;

              angular.forEach(shifts, function(shift) {
                snake = snake + '<div class="item" style="height:200px;">' + shift.id + '</div>';
              });
            }, 2000);
          }
        }
      };
    })
    .service('DisplayBoardRepository',
    function($resource, $filter, $timeout, $window, $interval) {

      refreshBoard($resource, $scope);

      /**
       * Section - WebSocket connection for events/messaging - Start *
       */
      var stompClient = null;
      var socket = null;
      var connected = false;


      function setConnection(isConnected) {
        connected = isConnected;
        $scope.$broadcast('connected', connected);
      }

      function openWebSocket() {


        var pathElements = window.location.pathname.split('/displayboard'),
            pathStart = pathElements[0],
            boardLocation = pathElements[1],
            pathElements = pathElements[1].split('/');

        if (!socket) {
          socket = new SockJS(pathStart + '/wsdisplayboard/' + pathElements[1]);


          stompClient = Stomp.over(socket);
          stompClient.connect({}, function(frame) {

            setConnection(true);

            stompClient.subscribe('/topic/displayboard.' + pathElements[1].toUpperCase(), function(message) {
              receivePublishCommand(message,false);

            })
          }, function(error) {
            if (error.headers && error.headers.message) {
              console.log(error.headers.message);
            } else {
              console.log(error);
            }
            setConnection(false);
          });
        }
      }

      function closeWebSocket() {
        stompClient.disconnect(function() {
          setConnection(false);
          console.log('Disconnected');
        });
      }

      function receivePublishCommand (message) {

        var command = JSON.parse(message.body);

        console.log('received command: ' + command);

        refreshBoard($resource, $scope);

        // Disregard error messages
        if (typeof command.error != 'undefined' && command.error > 0) {
          return;
        }
      }

      function reloadBoard() {
        window.location.reload()
      }

      function refreshBoard($resource, $scope) {

        var pathElements = window.location.pathname.split('/displayboard'),
            pathStart = pathElements[0];

        pathElements = pathElements[1].split('/');

        var boardLocation = pathElements[1],
            boardDate = new Date(),
            boardDate =  moment(boardDate).format('YYYYMMDD'),
            boardKey = boardLocation + "_" + boardDate,
            boardData = {
              pathStart: pathStart,
              boardLocation: boardLocation,
              boardDate: boardDate
            };

        var resource = $resource(pathStart + '/:district/:date/load', {
          district : null,
          date : null
        });

        var response = resource.get({
          district : boardLocation,
          date : boardDate
        }, function(data){
        });

        return response.$promise.then(function(data) {

          var dateString = data.date.substring(4, 6) + '/' + data.date.substring(6, 8) + '/' + data.date.substring(0, 4);

          $scope.data = data;
          var shifts = data.taskContainers[boardLocation].locationShifts
          $scope.shifts = shifts;

          var snakes = [];

          angular.forEach(shifts, function(shift) {
            snakes.push('<div class="firstheader">' + shift.shift.name + '</div>');
          });

          $scope.snakes = snakes;
          $scope.data.boardDate = new Date(dateString);

          $timeout(function() {

            var $container = $('.isotope').isotope({
              layoutMode: 'fitColumns',
              itemSelector: '.item'
            });

            $container.isotope('layout');

          }, 200);


          /*  $scope.unavailableFilter = function(persons, filterType) {
           var results = [];

           angular.forEach(persons, function(value, key) {
           if (value.hasOwnProperty('state') && value.state === states.personnel.unavailable) {
           results[key] = value;
           }
           });

           var results = _.uniq(results);

           if (!results || results.length == 0)
           return results;

           var sortedResults = $scope.sortPersonnelPanel(results, filterType);

           return sortedResults;
           };


           makeForm();
           makeBoard(data);
           showpage();

           $('#displayboard').append(headerlogo);

           */




          // Load data

          if (!socket) {
            openWebSocket();
          }
        }, function(error) {
          if (errorFn) {
            return errorFn(error);
          }
          return error;
        });

      }
    });
