var monitorApp = angular.module('monitorApp', []);

monitorApp.controller('monitorCtrl', ['$scope', '$http', '$interval', '$timeout', function ($scope, $http, $interval, $timeout) {

    $http.get('/smart-opsboard/admin/monitor/loadDashboard').success(function (data) {
        $scope.kiosks = data;


        $scope.myFilter = function (kiosks) {
            return kiosks.groupName === "Manhattan";
        };

        $scope.bxFilter = function (kiosks) {
            return kiosks.groupName === "Bronx";
        };
        $scope.bnFilter = function (kiosks) {
            return kiosks.groupName === "Brooklyn North";
        };
        $scope.bsFilter = function (kiosks) {
            return kiosks.groupName === "Brooklyn South";
        };
        $scope.qwFilter = function (kiosks) {
            return kiosks.groupName === "Queens West";
        };
        $scope.qeFilter = function (kiosks) {
            return kiosks.groupName === "Queens East";
        };
        $scope.siFilter = function (kiosks) {
            return kiosks.groupName === "Staten Island";
        };
        $scope.spFilter = function (kiosks) {
            return kiosks.groupName === "Splinter";
        };
        
        $scope.lastBoardDate = null;
        $scope.lastPublishedDate = null

    });

    var stompClient = null;
    var socket = null;
    $scope.connected = false;

    if (!socket) {
        openWebSocket();
    }

    function setConnection(isConnected) {
        $scope.$apply( function(){
            $scope.connected = isConnected;
        });
    }

    function openWebSocket() {
        var pathElements = window.location.pathname.split('/admin'),
            pathStart = pathElements[0];
        
        if (!socket) {
            socket = new SockJS(pathStart + '/wskioskDashboard/');
            stompClient = Stomp.over(socket);
            stompClient.connect({}, function () {
                setConnection(true);
                stompClient.subscribe('/topic/kioskDashboard', function (message) {
                    receiveCommand(message, false);
                });

            }, function (error) {
                if (error.headers && error.headers.message) {
                    console.log(error.headers.message);
                } else {
                    $scope.bodyerror = true;
                    $scope.$apply();
                    console.log(error);
                    $timeout(function () {
                        $interval(function () {
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


    function receiveCommand(message) {
        var command = JSON.parse(message.body);
        console.log(command);
        switch (command.commandName) {
            case 'UpdateDashboard':
                processUpdateDashboardCommand(command);

                break;
        }
        // Disregard error messages
        if (command.error !== 'undefined' && command.error > 0) {
            return;
        }
    }

    var processUpdateDashboardCommand = function (command) {
        var cmd = command;

        angular.forEach(command.commandContent.kioskMap, function(kioskMapValue, kioskKey) {
        	var now = moment();
        	var publishedDate = moment(kioskMapValue.lastUpdatedTimestamp);

            angular.forEach($scope.kiosks, function(kiosk, key) {
                if (kiosk.district === kioskKey){
                	if (now.diff(publishedDate, 'minutes') === 0 && now.diff(publishedDate, 'seconds') < 20){
		            	 $scope.$apply(function(){
		                     kiosk.status = kioskMapValue.status;
		                     kiosk.lastPublishedDate = kioskMapValue.lastPublishedDate;
		                     kiosk.boardDate = kioskMapValue.boardDate;
		                 });
            		}else{
            			$scope.$apply(function(){
		                     kiosk.status = false;
		                     kiosk.lastPublishedDate = null;
		                     kiosk.boardDate = null;
		                });
            		}
                }
            });
        });
    }



}]);