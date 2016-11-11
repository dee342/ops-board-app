(function () {

  'use strict';

  angular.module('WorkUnitsInformation')
    .service('WorkUnits', ['$rootScope', '$resource', '$q', function ($rootScope, $resource, $q) {

        // Data arrays
        var workUnits,
          locations;

        var pathElements = window.location.pathname.split('/'),
        pathStart = '/' + pathElements[1],
        boardDate = pathElements[3];

        return {

          getWorkUnits: function(forDate) {
            var resource = $resource(pathStart + '/referencedata/workunits');
            var forDate = forDate ? moment(forDate) : moment();
            var def = $q.defer();
            resource.query({
              boardDate: forDate.format('YYYYMMDD')
            }).$promise.then(function (data) {
              def.resolve(data);
            }, function (err) {
              def.reject(err);
            });
            return def.promise;
          },

          getLocations: function() {
            if (locations == null || !locations.length) {
              var resource = $resource(pathStart + '/referencedata/locations'),
                response = resource.query({ boardDate: boardDate });
              return response.$promise.then(function(data) {
                var sortedResults = [];
                for (var i = 0, len = data.length; i < len; i ++) {
                  if (data[i].hasGarage || data[i].servicesEquipmentLocations === true) {
                    sortedResults.push(data[i].code);
                  }
                }

                locations = _.unique(sortedResults);
                return sortedResults;
              }, function(error) {
                console.log(error);
              });
            } else {
              return locations;
            }
          }
        }
      }
    ]);

}());