(function () {

    angular
        .module('OpsBoard')
        .directive('contextMenu', ['$document', contextMenu]);

    function contextMenu($document) {

        var cachedElement,
            bound = false,
            opened = false;

        function openMenu(event, element) {
            if (cachedElement) closeMenu(); // close all other open menus
            element = cachedElement = angular.element(element).addClass('open');
            var doc = window.document.documentElement;
            var docLeft = (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0),
                docTop = (window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0),
                elementWidth = element[0].scrollWidth,
                elementHeight = element[0].scrollHeight;
            var docWidth = doc.clientWidth + docLeft,
                docHeight = doc.clientHeight + docTop,
                totalWidth = elementWidth + event.pageX,
                totalHeight = elementHeight + event.pageY,
                left = Math.max(event.pageX - docLeft, 0),
                top = Math.max(event.pageY - docTop, 0);
            if (totalWidth > docWidth) {
                left = left - (totalWidth - docWidth);
            }
            if (totalHeight > docHeight) {
                top = top - (totalHeight - docHeight);
            }
            return !!element
                .css('top', top + 'px')
                .css('left', left + 'px');
        }

        function closeMenu() {
            return !cachedElement.removeClass('open');
        }

        function onContextMenu(event) {
            event.preventDefault();
            var isAssigned = event.target.dataset.target;
            // latter is for cards with indicators
            var targetMenuId = isAssigned ? $(event.target).find('.context-menu') : $(event.target).parent('[data-target]').find('.context-menu');
            if (!targetMenuId.length || event.target.classList.contains('empty')) return;
            opened = openMenu(event, targetMenuId);
        }

        function onClick() {
            if (opened) {
               opened = closeMenu();
           }
        }

        function init() {
            if (!bound) { // ensure link is exected only once
                bound = !!$document
                    .bind('contextmenu', onContextMenu)
                    .bind('click', onClick);
            }
        }

        return {
            restrict: 'A',
            link: init
        };

    }

}());