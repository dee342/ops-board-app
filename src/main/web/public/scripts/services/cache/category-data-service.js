'use strict';

angular
  .module('OpsBoard')
  .service('CategoryDataService', ['$log', 'ReferenceDataService', 'BoardValueService', 
    function ($log, ReferenceDataService, BoardValueService) {

    var categoryData, allCategoryData;

    var _getCategoryData = function() {
      if (!categoryData) {
        return ReferenceDataService.loadCategories().then(function(data){
          categoryData = data;
          BoardValueService.categoryData = data;
          return data;
        });
      } else {
        return categoryData;
      }
    };



    var _getFormattedCategories = function () {
      //if (formattedCategories) return formattedCategories;
      var formattedCategories = _.object(_.map(categoryData, function(item) {
        return [item.id, item]
      })); 
      return formattedCategories;
    };

      var _getAllFormattedCategories = function () {
        //if (formattedCategories) return formattedCategories;
        var formattedCategories = _.object(_.map(allCategoryData, function(item) {
          return [item.id, item]
        }));
        return formattedCategories;
      };

    var _getAllCategoryData = function () {
      if (!allCategoryData) {
        return ReferenceDataService.loadAllCategories().then(function(data){
          allCategoryData = data;
          BoardValueService.allCategoryData = data;
          return data;
        });
      } else {
        return allCategoryData;
      }
    };

    var _getFormattedSubcategories = function  () {
      //if (formattedSubcategories) return formattedSubcategories;
      var formattedSubcategories = {},
        formattedCategories = _getFormattedCategories();
        
      Object.keys(formattedCategories).forEach(function (key) {  
        var value = formattedCategories[key];
        
        if (value.subcategories) {
          for (var i = 0; i < value.subcategories.length; i++) {
            var subcat = value.subcategories[i];
            formattedSubcategories[subcat.id] = subcat;
          }
        }   
      });
      return formattedSubcategories;
    };

    var _getAllFormattedSubcategories = function  () {
      //if (formattedSubcategories) return formattedSubcategories;
      var formattedSubcategories = {},
        formattedCategories = _getAllFormattedCategories();

      Object.keys(formattedCategories).forEach(function (key) {
        var value = formattedCategories[key];

        if (value.subcategories) {
          for (var i = 0; i < value.subcategories.length; i++) {
            var subcat = value.subcategories[i];
            formattedSubcategories[subcat.id] = subcat;
          }
        }
      });
      return formattedSubcategories;
    };



    var _updateReferenceCategories = function (shiftCategories) {
      var cats = [];
      Object
        .keys(shiftCategories)
        .forEach(
          function (categoryId) {
            var id = shiftCategories[categoryId].category ? shiftCategories[categoryId].category.id : shiftCategories[categoryId].categoryId;
            cats.push(id);
          }
        );
      //var refCats;
      Object.keys(shiftCategories).forEach(function(categoryId) {
        shiftCategories[categoryId].refCats = [];
        for (var k = 0; k < categoryData.length; k++) {
          var found = false
          for (var j = 0; j < cats.length; j++) {
            var id = shiftCategories[categoryId].category ? shiftCategories[categoryId].category.id : shiftCategories[categoryId].categoryId;
            if (cats[j] === categoryData[k].id && cats[j] !== id) {
              found = true;
            }
          }
          if (!found) {
            shiftCategories[categoryId].refCats.push(categoryData[k]);
          }
        }
        shiftCategories[categoryId].refCats = shiftCategories[categoryId].refCats.sort(function(a, b){
          return a.sequence-b.sequence
        }); 
      })
    };

    return {
      getCategoryData: _getCategoryData,
      getFormattedCategories: _getFormattedCategories,
      getAllFormattedCategories: _getAllFormattedCategories,
      getFormattedSubcategories: _getFormattedSubcategories,
      getAllCategoryData: _getAllCategoryData,
      getAllFormattedSubcategories: _getAllFormattedSubcategories,
      updateReferenceCategories: _updateReferenceCategories
    }
  }
]);