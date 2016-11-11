'use strict';
 
App.factory('MonitorCrudService', ['$http', '$q', function($http, $q){
 
    return {
         
    	fetchAllKiosks: function() {
                    return $http.get('/smart-opsboard/admin/monitorcrud/kiosks')
                            .then(
                                    function(response){
                                        return response.data;
                                    }, 
                                    function(errResponse){
                                        console.error('Error while fetching kiosks');
                                        return $q.reject(errResponse);
                                    }
                            );
            },
             
            createKiosk: function(kiosk){
                    return $http.post('/smart-opsboard/admin/monitorcrud/create', kiosk)
                            .then(
                                    function(response){
                                        return response.data;
                                    }, 
                                    function(errResponse){
                                        console.error('Error while creating kiosk');
                                        return $q.reject(errResponse);
                                    }
                            );
            },
             
            updateKiosk: function(kiosk, id){
                    return $http.put('/smart-opsboard/admin/monitorcrud/update/'+id, kiosk)
                            .then(
                                    function(response){
                                        return response.data;
                                    }, 
                                    function(errResponse){
                                        console.error('Error while updating kiosk');
                                        return $q.reject(errResponse);
                                    }
                            );
            },
             
            deleteKiosk: function(id){
                    return $http.delete('/smart-opsboard/admin/monitorcrud/delete/'+id)
                            .then(
                                    function(response){
                                        return response.data;
                                    }, 
                                    function(errResponse){
                                        console.error('Error while deleting kiosk');
                                        return $q.reject(errResponse);
                                    }
                            );
            }
         
    };
 
}]);