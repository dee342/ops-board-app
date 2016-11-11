'use strict';

angular.module('about-us')
  .controller('NavCtrl', function($scope, $modal, OpsBoardRepository) {
    $scope.isLocked = function() {
      return OpsBoardRepository.isLocked();
    };
    
    $scope.showReleaseNotes = function(event) {
      $scope.aboutModalInstance = $modal.open({
        templateUrl: appPathStart + '/views/modals/modal-release-notes',
        backdrop: 'static',
        controller: 'NavModalCtrl',
        scope: $scope
      });
    };

    $scope.showCredits = function(event) {
      $scope.aboutModalInstance.close();
      var modalInstance = $modal.open({
        templateUrl: appPathStart + '/views/modals/modal-credits',
        backdrop: 'static',
        size: 'lg',
        windowClass: 'modal-credits',
        controller: 'NavModalCtrl',
        scope: $scope
      });
    };

    $scope.isLockedMenu = OpsBoardRepository.isLocked('nav');

  });