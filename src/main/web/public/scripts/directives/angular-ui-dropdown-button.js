/**
 * Created by cvolk on 9/15/14.
 *
 * THis was lifted right from the angular-ui site.
 */

'use strict';

function DropdownCtrl($scope) {

    $scope.status = {
        isopen: false
    };

    $scope.toggled = function(open) {
        console.log('Dropdown is now: ', open);
    };

    $scope.toggleDropdown = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.status.isopen = !$scope.status.isopen;
    };
}

