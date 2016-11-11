'use strict';

angular.module('OpsBoard')
    .controller('ModalCtrl', function ($scope, $modalInstance, data, OpsBoardRepository, $timeout) {

        $scope.data = data;
        $scope.selected = {
            item: $scope.data[0]
        };

        if ($scope.passPreValidation()) {
            // Display content
            $scope.showContent = true;
            $scope.showProcessing = false;
            $scope.showError = false;
        } else {
            // Display error
            $scope.showContent = false;
            $scope.showProcessing = false;
            $scope.showError = true;
        }

        // client side handle from server response
        function responseError(error) {

            if (error instanceof Error && error.name === 'ClientSideError') {
                $scope.showProcessing = false;
                $scope.showContent = true;
                return;
            }

            // supported error codes to display extended message from server - see ErrorMessage.java
            var errorCodes = [3901, 3907];
            var message = 'Generic Server Error';
            $scope.opData.serverErrors = [];

            // server side error handle
            if (error.data && errorCodes.indexOf(error.data.code) !== -1 || error.data.status === 500) {
                // server side validation error
                $scope.showProcessing = false;
                $scope.showContent = true;
                if (error.data.extendedMessages) {
                    for (var i = 0; i < error.data.extendedMessages.length; i++) {
                        $scope.opData.serverErrors[i] = {
                            message: error.data.extendedMessages[i]
                        };
                    }
                } else {
                    $scope.opData.serverErrors[0] = {
                        message: error.data.message || message
                    };
                }
                return;
            }

            $scope.showContent = true;
            $scope.showProcessing = false;

            if (error.status == 412 || error.status == 409) {
                $scope.opData.serverErrors.push({
                    type: 'danger',
                    message: error.data
                });
            } else {
                var data;
                try {
                    data = JSON.parse(error.data);
                    if (data.extendedMessages && data.extendedMessages.length > 0) {
                        for (var j = 0; j < data.extendedMessages.length; j++) {
                            $scope.opData.serverErrors.push({
                                message: data.extendedMessages[j]
                            });
                        }
                    } else {
                        $scope.opData.serverErrors.push({
                            message: data.message ? data.message : message
                        });
                    }
                } catch (e) {
                    $scope.opData.serverErrors.push({
                        type: 'danger',
                        message: error.statusText ? error.statusText : message
                    });
                }
            }

        }

        // Ok button selected
        $scope.ok = function () {
            $scope.showContent = false;
            $scope.showProcessing = true;
            $scope.showError = false;
            $scope.operation(function () {
                $modalInstance.close($scope.selected.item);
            }, responseError);

            if ($scope.opData && !!$scope.opData.clientErrors) {
                var $element_focus = angular.element('form').find('.ng-invalid:first');
                if ($element_focus.length) {
                    $timeout(function () {
                        if ($element_focus.is('div')) { //select2
                            $element_focus.select2('focus');
                        }
                        else { //input
                            $element_focus.focus();
                        } //what else type? checkbox,radio,textarea,button
                    });
                }
            }
        };

        // Cancel button selected
        $scope.cancel = function (fromUpdate) {
            if (fromUpdate) {
                $scope.cancelOperation(function () {
                    $modalInstance.close($scope.selected.item);
                }, function (error) {
                    //client side error handle
                    if (error instanceof Error && error.name === 'ClientSideError') {
                        $scope.showProcessing = false;
                        $scope.showContent = true;
                        return;
                    }
                    console.log(error);
                });
            } else {
                $modalInstance.dismiss('cancel');
            }
        };

        // Exit button selected
        $scope.exit = function () {
            $modalInstance.dismiss('cancel');
        };

        // Remove button selected
        $scope.remove = function () {
            $scope.showContent = false;
            $scope.showProcessing = true;

            $scope.removeOperation(function () {
                $modalInstance.close($scope.selected.item);
            }, function (error) {
                //client side error handle
                if (error instanceof Error && error.name === 'ClientSideError') {
                    $scope.showProcessing = false;
                    $scope.showContent = true;
                    return;
                }

                //server side error handle
                $scope.opData.errors = [];

                $scope.showContent = false;
                $scope.showProcessing = false;
                $scope.showError = true;

                if (error.status === 412 || error.status === 409) {
                    $scope.opData.errors.push({
                        type: 'danger',
                        message: error.data
                    });
                } else {
                    $scope.opData.errors.push({
                        type: 'danger',
                        message: OpsBoardRepository.getErrorMessage(error.data)
                    });
                }
                console.log(error);
            });
        };

    });