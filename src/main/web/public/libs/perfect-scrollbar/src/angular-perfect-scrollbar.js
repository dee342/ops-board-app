angular.module('perfect_scrollbar', []).directive('perfectScrollbar',
  ['$parse', '$window', '$timeout', function($parse, $window, $timeout) {
  var psOptions = [
    'wheelSpeed', 'wheelPropagation', 'minScrollbarLength', 'useBothWheelAxes',
    'useKeyboard', 'suppressScrollX', 'suppressScrollY', 'scrollXMarginOffset',
    'scrollYMarginOffset', 'includePadding', 'wideScrollbar'//, 'onScroll', 'scrollDown'
  ];

  return {
    restrict: 'EA',
    transclude: true,
    template: '<div><div ng-transclude></div></div>',
    replace: true,
    link: function($scope, $elem, $attr) {
      var jqWindow = angular.element($window);
      var options = {};

      for (var i=0, l=psOptions.length; i<l; i++) {
        var opt = psOptions[i];
        if ($attr[opt] !== undefined) {
          options[opt] = $parse($attr[opt])();
        }
      }
      $scope.$evalAsync(function() {
        $elem.perfectScrollbar(options);

        setTimeout(function () {
          $elem.perfectScrollbar('update');
        }, 100);

        var onScrollHandler = $parse($attr.onScroll);

        $elem.scroll(function(){
          var scrollTop = $elem.scrollTop();
          var scrollHeight = $elem.prop('scrollHeight') - $elem.height();
          $scope.$apply(function() {
            onScrollHandler($scope, {
              scrollTop: scrollTop,
              scrollHeight: scrollHeight
            })
          })
        });
      });

      function update(event) {
        $scope.$evalAsync(function() {
          if ($attr.scrollDown == 'true' && event != 'mouseenter') {
            setTimeout(function () {
              $($elem).scrollTop($($elem).prop("scrollHeight"));
            }, 100);
          }
          $elem.perfectScrollbar('update');
        });

        // This is necessary if you aren't watching anything for refreshes
        if(!$scope.$$phase) {
          setTimeout(function(){
            $scope.$apply()
          }, 0)
        }

      }

      // This is necessary when you don't watch anything with the scrollbar
      $elem.bind('mouseenter', update('mouseenter'));

      // Possible future improvement - check the type here and use the appropriate watch for non-arrays
      if ($attr.refreshOnChange) {
        $scope.$watchCollection($attr.refreshOnChange, function() {
          update();
        });
      }

      // this is from a pull request - I am not totally sure what the original issue is but seems harmless
      if ($attr.refreshOnResize) {
        jqWindow.on('resize', update);
      }

      $elem.bind('$destroy', function() {
        jqWindow.off('resize', update);
	$scope.$destroy();
        $elem.perfectScrollbar('destroy');
      });

    }
  };
}]).directive('psMouseOver', function () {
    return {
      link: function(scope, element) {

        setTimeout(function () {
          element.perfectScrollbar('update');
        }, 100);

        element.bind("mouseover", function(e){
          setTimeout(function () {
            element.perfectScrollbar('update');
          }, 100);
          e.stopPropagation();
          e.preventDefault();
        });
      }
    }
  });