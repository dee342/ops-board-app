(function() {

		'use strict';

		angular
			.module('OpsBoard')
			.controller('PlowRemoval', ['$scope', '$modalInstance', PlowRemovalController]);

		function PlowRemovalController ($scope, $modalInstance) {

			var data = {
				titleAction: 'Equipment Working Down',
				headerType: 'striped',
				cancelButtonText: 'Cancel',
				submitButtonText: 'Confirm'
			};

			function ok() {
				$modalInstance.close(true);
			}

			function cancel() {
				$modalInstance.dismiss('cancel');
			}

			$scope.ok = ok;
			$scope.cancel = cancel;
			$scope.showContent = true;
			$scope.opData = data;

		};

}());