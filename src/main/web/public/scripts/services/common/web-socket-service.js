(function () {

	angular
		.module('OpsBoard')
		.service('WebSocketService', ['$rootScope', '$log', '$q', '$window', 'BoardDataService', webSocketService]);

	function webSocketService($rootScope, $log, $q, $window, BoardDataService) {

		var stompClient,
			socket,
			frame,
			connected = false;

		var setConnection = function (status) {
			connected = status;
			$rootScope.$broadcast('connected', connected);
		};

		var isConnected = function () {
			return connected;
		};
		
		var openWebSocket = function (boardData) {
			var deferred = $q.defer();
			var boardData = BoardDataService.getBoardData();
			var socketURL = window.location.origin.replace('http', 'ws') + boardData.pathStart + '/wsboard/'
			socketURL +=  boardData.boardLocation + '/' + boardData.boardDate + '/websocket';
			if (!socket) {
			  socket = new WebSocket(socketURL);
			  stompClient = Stomp.over(socket);
			  stompClient.connect({persistant: false}, function (slidingFrame) {
			    $log.info('stomp connected %s', $window.performance.now());
			    frame = slidingFrame;
			    setConnection(true);
			    deferred.resolve();
			  }, function (error) {
			    if (error.headers && error.headers.message) {
			      $log.error(error.headers.message);
			    } else {
			      $log.error(error);
			    }
			    setConnection(false);
			    deferred.reject(error);
			  });
			}
			return deferred.promise;
		};

		var closeWebSocket = function () {
			stompClient.disconnect(function () {
				setConnection(false);
				$log.warn('socket disconnected');
			});
		};

		var subscribe = function (boardData, user, callback) {
			var deferred = $q.defer();
			var locationFormatted = boardData.boardLocation.toUpperCase();

			stompClient.subscribe('/topic/broadcast', function (message) {
				callback(message, false, frame);
			});

			stompClient.subscribe('/topic/commands.' + locationFormatted + '.' + boardData.boardDate, function (message) {
			  callback(message, false, frame);
			});

			stompClient.subscribe('/topic/user-queue-notifications.' + user.username + '.'+ locationFormatted + '.' + boardData.boardDate, function (message) {
				deferred.resolve();
			  callback(message, false, frame);
			});
		    $log.info('subscribed %s', $window.performance.now());
		    return deferred.promise;
		};

		var send = function (url, options, command) {
			stompClient.send(url, options, command);
		};

		return {
			isConnected: isConnected,
			closeWebSocket: closeWebSocket,
			subscribe: subscribe,
			openWebSocket: openWebSocket,
			send: send
		};

	}

	// message is initial load data enum for equipment and personnel
	// from refactor when we had separate channels
	// function receiveMessage (model, message) {
	// 	var command = JSON.parse(message.body);
	// 	if (message.command === 'MESSAGE' && typeof model === 'object') {
	// 		angular.extend(model[command.id], command);
	// 	}
	// }

}());