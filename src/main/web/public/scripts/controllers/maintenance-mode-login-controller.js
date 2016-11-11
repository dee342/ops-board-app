'use strict';

angular.module('about-us')
  .controller('LoginCtrl', function ($window, $scope, $log, $controller, $rootScope, BrowserService ,$interval, $location, $http) {

    var maint = true;

    $http.get('http://localhost:8080/smart-opsboard/admin/application/status')
      .success(function (e) {
        if(e === 'maintenance') {
          $rootScope.$emit('MAINTENANCE-MODE',{maintenancemode: 'entering_maintenance'});
        }
      }
    ).error(function () {

      });




    // checks for a board hash, display board uses this after login
    function checkHash() {
      if ((/#\/\d+/g).test($window.location.hash)) {
        $window.localStorage.setItem('boardDate', $window.location.hash.split('/')[1].replace(/\D/g, ''));
      }
    }

    var stompClient,
      socket,
      frame,
      connected = false;






    function setConnection(isConnected) {
      connected = isConnected;
      $scope.$broadcast('connected', connected);
    }

    function openWebSocket() {

      var pathElements = window.location.pathname.split('/'),
        pathStart = '/' + pathElements[1];
      var boardLocation = pathElements[2],
        boardDate = pathElements[3],
        boardData = {
          pathStart: pathStart,
          boardLocation: boardLocation,
          boardDate: boardDate
        };


      var socketURL = window.location.origin.replace('http', 'ws') + boardData.pathStart + '/wsboard/';
      socketURL +=  boardData.boardLocation + '/' + boardData.boardDate + '/websocket';

      if (!socket) {

        socket = new WebSocket(socketURL);

        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {

          setConnection(true);

          stompClient.subscribe('/topic/broadcast', function(message) {
            receivePublishCommand(message,false);

          })
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

    function closeWebSocket() {
      stompClient.disconnect(function() {
        setConnection(false);
        console.log('Disconnected');
      });
    }

    function receivePublishCommand (message) {
      var command = JSON.parse(message.body);
      $rootScope.$emit('MAINTENANCE-MODE',{maintenancemode: command.key});
    };

    if($location.path() ===  '/maintenance') {
      $rootScope.$emit('MAINTENANCE-MODE',{maintenancemode: 'entering_maintenance'});
    };

    openWebSocket();
    checkHash();

    $scope.browserType = BrowserService.getBrowser();

    $scope.isSupportedBrowser = function(){
      var supported = false;
      if ($scope.browserType === "chrome" || $scope.browserType === "firefox"){
        supported = true;
      }
      return supported;
    }

  }).controller(
  'maintenanceMode',
  function($scope, $rootScope, $location) {
    $rootScope.$on('MAINTENANCE-MODE', function(event, data) {
      $scope.maintenancemode = data.maintenancemode;
      if(data.maintenancemode === 'entering_maintenance') {
        $scope.maintenancemessage1 = 'Maintenance Mode';
        $scope.maintenancemessage2 = 'Please stand by...';
        $location.path('/maintenance');
      } else {

        var exiturl = 'login';
        window.location = exiturl;

        $scope.maintenancemessage = '';
        $scope.maintenancemessage2 = '';
        if($location.path() ===  '/maintenance') {
          $location.path('');
        };
      }

      if(!$scope.$$phase) {
        $scope.$apply();
      }
    });



  });
;/**
 * Created by jengstrom on 7/9/2015.
 */
