'use strict';
angular.module('OpsBoard')
  .service(
  'UtilityService',
  function() {
    var mergeRecursive = function (obj1, obj2) {
      for (var p in obj2) {
        try {
          // Property in destination object set; update its value.
          if ( obj2[p].constructor==Object ) {
            obj1[p] = mergeRecursive(obj1[p], obj2[p]);

          } else {
            obj1[p] = obj2[p];
          }
        } catch(e) {
          // Property in destination object not set; create it and set its value.
          obj1[p] = obj2[p];
        }
      }
      return obj1;
    }

    var numberOfObjectsInCollection = function(obj) {
      return Object.keys(obj).length;
    }

    var populateReference = function (type, id, referenceData) {
      var data, msgType, name;
      switch (type) {
        case 'shift':
          if (referenceData.shifts && referenceData.shifts[id] ) {
            name = referenceData.shifts[id].name;
          } else {
            name = '';
          }
          data = name;
          break;
        case 'category':
          if (referenceData.categories && referenceData.categories[id]) {
            name = referenceData.categories[id].name;
          } else {
            name = '';
          }
          data = name;
          break;
        case 'subcategory':
          if (referenceData.subcategories && referenceData.subcategories[id]) {
            name = referenceData.subcategories[id].name;
          } else {
            name = '';
          }
          data = name;
          break;
        case 'equipment':
          data = referenceData.equipments[id] ? referenceData.equipments[id].name : id;
          msgType = 'entity';
          break;
        case 'person':
          data = referenceData.persons[id] ? referenceData.persons[id].fullName : id;
          msgType = 'entity';
          break;
        case 'task':
          if (referenceData.tasks && referenceData.tasks.tasksMap[id] ) {
            data = referenceData.tasks.tasksMap[id].taskName;
          } else {
            data = 'task removed';
            msgType = 'removed';
          }
          break;  
        case 'section':
          if (referenceData.tasks && referenceData.tasks.sectionMap[id] ) {
            data = referenceData.tasks.sectionMap[id].sectionName;
          } else {
            data = 'section removed';
            msgType = 'removed';
          }
          break;  
        case 'errorCode':
          data = data
          msgType = 'error';
          break;
        default:
          data = id;
          if ((/^[,.]$/).test(id)) msgType = 'punctuation';
          break;
      }
      return {
        data: data,
        msgType: msgType
      };
    }

    return {
      numberOfObjectsInCollection: numberOfObjectsInCollection,
      populateReference: populateReference,
      mergeRecursive: mergeRecursive
    }
  })