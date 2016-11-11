angular
  .module('OpsBoard')
  .controller(
  'maintenanceMode',
  function($scope, $rootScope, $location) {
    $rootScope.$on('MAINTENANCE-MODE', function(event, data) {
      $scope.maintenancemode = data.maintenancemode;
      if(data.maintenancemode === 'entering_maintenance') {
        var exiturl = '../maintenance#/maintenance';
        window.location = exiturl;
      }
    });

  });
