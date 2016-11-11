'use strict';

angular.module('about-us')
  .controller('NavModalCtrl', function($scope, $modalInstance) {

    //AppVersion
    $scope.appVersion = appVersion;

    // Cancel button selected
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.ok = function() {
      $modalInstance.dismiss('cancel');
    };

  });