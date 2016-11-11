/** As we go along we will mock this beast out completely **/


var signOffTime = new Date();
	signOffTime.setHours(6,00);

angular.module('dsny-center-modal', [])
angular.module('angular-loading-bar', [])
angular.module('about-us', [])
angular.module('exceptionOverride', [])
angular.module('OpsBoard', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'angular-flex-splitter',
  'lvl.directives.dragdrop',
  'ng-context-menu',
  'ui.bootstrap',
  'ui.select2',
  'angular-loading-bar',
  'dsny-center-modal',
  'about-us',
  'OpsBoardFilters',
  'exceptionOverride'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '',
        controller: 'MainCtrl',
        resolve: {
        	// Wait till board is loaded from server before instantiating the controller
    		opsBoard: function(OpsBoardRepository) {
    			return OpsBoardRepository.getOpsBoard();
    		},
            equipmentDownData: function(OpsBoardRepository) {
                return OpsBoardRepository.getEquipmentDownData();
            },
    		repairLocations: function(OpsBoardRepository) {
    			return OpsBoardRepository.getRepairLocations();
    		},
    		equipmentDownCodes: function(OpsBoardRepository) {
    			return OpsBoardRepository.getEquipmentDownCodes();
    		}
    	}
      })
      .otherwise({
        redirectTo: '/'
      });
  })
  .constant('states', {
        equipment : {
                      available: 'Available',
                      down: 'Down',
                      hidden: 'Hidden',
                      pendingLoad:'Pending Load',
                      pendingDetach: 'Pending Detach',
                      pendingAttach: 'Pending Attach',
                      detached: 'Detached',
                      onLocation: ['Available', 'Down']
        },
        personnel : {
                      assigned: 'Assigned',
                      available: 'Available',
                      hidden: 'Hidden',
                      unavailable: 'Unavailable'
        }
      })
      .constant('groups', {
        equipment : {
                      rearLoader: 'Rear Loaders',
                      dualBins: 'Dual Bins',
                      alleyTruck: 'Alley Trucks',
                      hoistFittedChassis: 'Hoist-Fitted Chassis',
                      mechanicalBrooms: 'Mechanical Brooms',
                      roRo: 'RO-ROs',
                      ezPacks: 'E-Z Packs',
                      miscellaneous: 'Miscellaneous',
                      snow: 'Snow'
        },        
        personnel : {
              chiefs : ['GS II', 'GS III'],
              superintendents : 'GS I',
              supervisors : 'SUP',
              sanitationWorkers : 'SW',
              civilians : 'Civilian'  
      }
      })
      .constant('binStatus', {
        equipment : {
          relay: 'Relay',
          rollover: 'Rollover'
        }
      })
      .constant('materialGroups', {
        equipment : {
          collection: 'Collection',
          recycling: 'Recycling',
          miscellaneous: 'Miscellaneous'
        }
      })
      .constant('materialSubGroups', {
        equipment : {
          organics: 'Organics',
          mgp: 'MGP',
          paper: 'Paper',
          recyclingMisc: 'Recycling Misc'
        }
      })
      .constant('signOffTime', signOffTime)
      .constant('equipmentStatuses', ['Empty', 'Relay', 'Rollover'])
      .constant('user', {"username" : 'admin'});