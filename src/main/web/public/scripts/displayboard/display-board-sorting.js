'use strict';

angular.module('displayBoardSorting', [])




    .factory('sortPersonnelPanel',function(){

        return function(list, filterType) {

            if (!list) return list;
            if (!filterType) filterType = 'Location Seniority';

            return list.sort(function(me, that) {

                if (filterType === 'Last Name') {

                    if (me.lastName > that.lastName)
                        return 1;

                    if (me.lastName < that.lastName)
                        return -1;

                    return 0;
                }

                if (!me.activeDetachment && that.activeDetachment && me.state === 'Available')
                    return -1;

                if (me.activeDetachment && !that.activeDetachment && me.state === 'Available')
                    return 1;

                if ((!me.activeDetachment && !that.activeDetachment) || me.state !== 'Available') {

                    if (titleHierarchy.indexOf(me.civilServiceTitle) > titleHierarchy.indexOf(that.civilServiceTitle))
                        return 1;

                    if (titleHierarchy.indexOf(me.civilServiceTitle) < titleHierarchy.indexOf(that.civilServiceTitle))
                        return -1;

                    if (me.civilServiceTitle === titles.civilian && that.civilServiceTitle === titles.civilian) {

                        if (me.lastName > that.lastName)
                            return 1;

                        if (me.lastName < that.lastName)
                            return -1;
                    }

                    if(filterType === 'Location Seniority' || filterType === 'Location Reverse Seniority') {



                        /*                        if (me.homeLocation !== $scope.board.location && that.homeLocation === $scope.board.location)
                         return 1;

                         if (me.homeLocation === $scope.board.location && that.homeLocation !== $scope.board.location)
                         return -1;*/

                        if (me.homeLocation > that.homeLocation)
                            return 1;

                        if (me.homeLocation < that.homeLocation)
                            return -1;

                        if (me.payrollLocationId > that.payrollLocationId)
                            return 1;

                        if (me.payrollLocationId < that.payrollLocationId)
                            return -1;
                    }
                }


                if (filterType === 'Seniority' || filterType === 'Location Seniority') {

                    if(me.seniorityDate > that.seniorityDate)
                        return 1;

                    if(me.seniorityDate < that.seniorityDate)
                        return -1;

                    if(me.listNumber && !that.listNumber)
                        return 1;

                    if(!me.listNumber && that.listNumber)
                        return -1;

                    if(!me.listNumber && !that.listNumber)
                        return 0;

                    if(Number(me.listNumber.replace(/[^0-9]+/g, '')) > Number(that.listNumber.replace(/[^0-9]+/g, '')))
                        return 1;

                    if(Number(me.listNumber.replace(/[^0-9]+/g, '')) < Number(that.listNumber.replace(/[^0-9]+/g, '')))
                        return -1;

                    return 0;
                }

                if (filterType === 'Reverse Seniority' || filterType === 'Location Reverse Seniority') {


                    if(me.seniorityDate < that.seniorityDate)
                        return 1;

                    if(me.seniorityDate > that.seniorityDate)
                        return -1;

                    if(me.listNumber && !that.listNumber)
                        return -1;

                    if(!me.listNumber && that.listNumber)
                        return 1;

                    if(!me.listNumber && !that.listNumber)
                        return 0;

                    if(Number(me.listNumber.replace(/[^0-9]+/g, '')) < Number(that.listNumber.replace(/[^0-9]+/g, '')))
                        return 1;

                    if(Number(me.listNumber.replace(/[^0-9]+/g, '')) > Number(that.listNumber.replace(/[^0-9]+/g, '')))
                        return -1;

                    return 0;
                }
            });
        };



    });