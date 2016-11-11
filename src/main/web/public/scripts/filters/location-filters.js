'use strict';

angular.module('locationFilters', [])
  .filter('removeInvalidLocations',
    function() {
      return function(locations, filterType) {
        var locationResults = [];

        if(filterType && filterType === 'personnel') {
          angular.forEach(locations, function(location,key) {
            if (location && (location.hasGarage === true || location.servicesItself === true || location.servicesEquipmentLocations === true) && location.type !== 'district-garage') {
              locationResults.push(location);
            }
          });
        } else {
          angular.forEach(locations, function(location,key) {
            if (location && (location.hasGarage === true || location.servicesItself === true)) {
              locationResults.push(location);
            }
          });
        }

        return locationResults;
      }
  }).filter('sortLocationsBySequence', function() {
    // sorts array of locations keys based on ref data sequence number
    return function (boardLocations, allLocations) {
      return boardLocations.sort(function (a, b) {
        return allLocations[a].sortSequence > allLocations[b].sortSequence;
      });
    }
  });