'use strict';

describe('EquipmentCtrl', function(){
    var $scope,
        ctrl,
        OpsBoardRepository,
        constants,
        $rootScope;

    beforeEach(function() {
        module('OpsBoard')
    });

    beforeEach(function() {
        OpsBoardRepository = {
            getEquipment: function(){return {}},
            getMaterials: function(){return {}}
        }
    })

    beforeEach(inject(function(_$rootScope_, $controller){        
        $rootScope = _$rootScope_;
        $scope = $rootScope.$new();
        ctrl = $controller('EquipmentCtrl', {
            $scope: $scope,
            $modal: {},
            $log: {},
            OpsBoardRepository: OpsBoardRepository,
            states: {}, 
            binStatus: {}, 
            materialGroups: {}, 
            materialSubGroups: {},
            groups: {
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
                }
            } 
        });
    }));

    it('numDetached should be 0', function(){
        expect($scope.numDetached).toBe(0);
    })

    it('alleyTrucks should be expanded', function(){
        expect($scope.alleyTrucks.open).toBe(true);
    })

    it('alleyTrucks should be expanded', function(){
        OpsBoardRepository.getEquipment = function(){return {
            "10AA-103_null": {
                "id": "10AA-103_null",
                "group": "Miscellaneous"
            }
        }};
        var equipment = OpsBoardRepository.getEquipment();
        var results = $scope.miscellaneousFilter(equipment)
        console.log(results)
        //expect($scope.alleyTrucks.open).toBe(true);
    })
});
