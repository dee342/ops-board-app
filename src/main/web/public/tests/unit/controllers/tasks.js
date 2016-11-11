'use strict';

describe('TasksCtrl', function(){
    var $scope,
        ctrl,
        OpsBoardRepository,
        $rootScope;

    beforeEach(function() {
        module('OpsBoard');
    });

    beforeEach(function() {
        OpsBoardRepository = {
            getEquipment: function(){return {}},
            getPersonnel: function(){return {}}
        }
    })

    beforeEach(inject(function(_$rootScope_, $controller){        
        $rootScope = _$rootScope_;
        $scope = $rootScope.$new();
        ctrl = $controller('TasksCtrl', {
            $scope: $scope,
            OpsBoardRepository: OpsBoardRepository,            
            $rootScope: $rootScope,
            $document: {}
        });
    }));

    it('should have a menu item - Reset', function() {
        expect($scope.items[1]).toBe('Reset');
    })

    it('should register recentlyPushedId', function() {
        $rootScope.recentlyPushedId = 'old';
        $rootScope.pushedId = 'Test';
        $rootScope.$apply();
        expect($scope.recentlyPushedId).toEqual('Test');
    })

    it('should not register recentlyPushedId', function() {
        $rootScope.recentlyPushedId = 'old';
        $rootScope.pushedId = undefined;
        $rootScope.$apply();
        expect($scope.recentlyPushedId).toEqual('old');
    })

    it('should toggle', function() {
        $scope.status.isopen = true;
        $scope.toggleDropdown({preventDefault: function(){}, stopPropagation: function(){}})
        expect($scope.status.isopen).toBe(false)
    })

});