// Created by Justin Klemm ... modified by Dan
'use strict';

(
  function(angular) {
    return angular
      .module('ngOrderObjectBy', [])
      .filter('orderObjectBy', function() {
        return function (items, field, reverse, key) { //modified parameters
          var filtered = [];
          angular.forEach(items, function(item) {
        	  if (!field.then) { // Dan - add check to make sure added object is the one interested (i.e. not promise)
              //modified - not very good check but fast and avoids error if field is a.b.c
        		  filtered.push(item);
        	  }
          });
          function index(obj, i) {
            return obj[i];
          }
          filtered.sort(function (a, b) {
            var comparator;
            var reducedA = field.split('.').reduce(index, a);
            var reducedB = field.split('.').reduce(index, b);
            if (reducedA === reducedB) {
              comparator = 0;
            } else {
              comparator = (reducedA > reducedB ? 1 : -1);
            }
            return comparator;
          });
          if (reverse) {
            filtered.reverse();
          }/*
          if (typeof items === 'object' && key) { //if I give you object gimme object back :)
            var filtered = {};
            angular.forEach(items, function(item) {
              filtered[item[key]] = item;
            });
          }*/
          return filtered;
        };
      }).filter('orderObjectBy2', function() {

          return function(items, field, reverse) {
            var filtered = [];
            angular.forEach(items, function(item) {
              filtered.push(item);
            });
            filtered.sort(function (a, b) {
              return (a[field] > b[field] ? 1 : -1);
            });
            if(reverse) filtered.reverse();


            return filtered;
          };
        });
  }
)(angular);