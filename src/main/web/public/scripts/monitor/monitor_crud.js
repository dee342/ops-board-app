'use strict';

App.controller('monitorCrudCtrl', ['$scope','MonitorCrudService', function($scope, MonitorCrudService) {
          var self = this;
          self.kiosk={id:null ,district:'',ipAddress:'',subnetMask:'',defaultGateway:'',hostname:'',username:'',groupName:''};
          self.kiosks=[];
               
          self.fetchAllKiosks = function(){
        	  MonitorCrudService.fetchAllKiosks()
                  .then(
                               function(d) {
                                    self.kiosks = d;
                               },
                                function(errResponse){
                                    console.error('Error while fetching Kiosks');
                                }
                       );
          };
            
          self.createKiosk = function(kiosk){
        	  MonitorCrudService.createKiosk(kiosk)
                      .then(
                      self.fetchAllKiosks, 
                              function(errResponse){
                                   console.error('Error while creating kiosk.');
                              } 
                  );
          };
 
         self.updateKiosk = function(kiosk, id){
        	 MonitorCrudService.updateKiosk(kiosk, id)
                      .then(
                              self.fetchAllKiosks, 
                              function(errResponse){
                                   console.error('Error while updating kiosk.');
                              } 
                  );
          };
 
         self.deleteKiosk = function(id){
        	 MonitorCrudService.deleteKiosk(id)
                      .then(
                              self.fetchAllKiosks, 
                              function(errResponse){
                                   console.error('Error while deleting kiosk.');
                              } 
                  );
          };
 
          self.fetchAllKiosks();
 
          self.submit = function() {
              if(self.kiosk.id==null){
                  console.log('Saving New User', self.kiosk);    
                  self.createKiosk(self.kiosk);
              }else{
                  self.updateKiosk(self.kiosk, self.kiosk.id);
                  console.log('User updated with id ', self.kiosk.id);
              }
              self.reset();
          };
               
          self.edit = function(id){
              console.log('id to be edited', id);
              for(var i = 0; i < self.kiosks.length; i++){
                  if(self.kiosks[i].id == id) {
                     self.kiosk = angular.copy(self.kiosks[i]);
                     break;
                  }
              }
              //self.gotoTop();
          };
               
          self.remove = function(id){
              console.log('id to be deleted', id);
              for(var i = 0; i < self.kiosks.length; i++){//clean form if the user to be deleted is shown there.
                  if(self.kiosks[i].id == id) {
                     self.reset();
                     break;
                  }
              }
              self.deleteKiosk(id);
          };
          
          $scope.groups =
        	    [
        	          "Manhattan" ,
        	         "Bronx",
        	          "Brooklyn North" ,
        	          "Brooklyn South" ,
        	          "Queens West" ,
        	          "Queens East" ,
        	          "Staten Island" ,
        	          "Splinter" 
        	    ];        
           
          self.reset = function(){
              self.kiosk={id:null,district:'',ipAddress:'',subnetMask:'',defaultGateway:'',hostname:'',username:'',groupName:''};
              $scope.myForm.$setPristine(); //reset Form
          };
          
 
      }]);