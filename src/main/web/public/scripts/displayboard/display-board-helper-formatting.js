'use strict';

angular.module('OpsBoard')
  .service(
  'DisplayBoardHelperFormatting',
  function($filter, BoardValueService, $window, DisplayBoardHelperCommon) {

      var _checkHeight = function(blockHeight, blockType, refData) {
        refData.lastBlockAddedType = blockType;
        refData.blockCount = refData.blockCount + 1;
        var maxColHeight = $window.innerHeight + 25;
        refData.targetColHeight = refData.currentColHeight + blockHeight;

        if (refData.targetColHeight < maxColHeight) {
          refData.currentColHeight = refData.currentColHeight + blockHeight;
        } else {

          // COUMN SWITCH
          refData.columnShift = true;
          refData.previousColLastBlock = refData.lastBlockType;
          var prevCol = refData.cols['col' + refData.col];

          DisplayBoardHelperCommon.orphanPop(prevCol, refData);
          DisplayBoardHelperCommon.orphanPop(prevCol, refData);
          DisplayBoardHelperCommon.orphanPop(prevCol, refData);
          DisplayBoardHelperCommon.orphanPop(prevCol, refData);

          refData.blockCount = refData.blockCount + 1;
          refData.colcount = refData.colcount + 1;
          refData.col = refData.col + .1;
          refData.currentColHeight = 0;
          refData.cols['col' + refData.col] = [];

          if ((refData.colcount - 1) % 3 === 0) {
            /*refData.currentColHeight = refData.currentColHeight + 174;
            refData.cols['col' + refData.col].push({type: 'lastDateTimeBlock', html: refData.lastDateTimeBlock, lastshiftblock: refData.lastShiftBlockType});*/

            if (refData.locationcount === 1) {
              refData.currentColHeight = refData.currentColHeight + 80;
              refData.cols['col' + refData.col].push({type: 'lastheaderLocation', html: refData.lastheaderLocation, lastshiftblock: refData.lastShiftBlockType});
            }
          }

          if (refData.locationcount > 1 && refData.lastheaderLocation !== '') {
            refData.currentColHeight = refData.currentColHeight + 80;
            refData.cols['col' + refData.col].push({type: 'lastheaderLocation', html: refData.lastheaderLocation, lastshiftblock: refData.lastShiftBlockType});
          }

          refData.currentColHeight = refData.currentColHeight + 82;
          refData.cols['col' + refData.col].push({type: 'lastShiftBlock', html: refData.lastShiftBlock, lastshiftblock: refData.lastShiftBlockType});

          if (blockType === 'task') {
            refData.currentColHeight = refData.currentColHeight + 74;
            refData.cols['col' + refData.col].push({type: 'lastSubcategoryBlock', html: refData.lastSubcategoryBlock, lastshiftblock: refData.lastShiftBlockType});
            if (refData.lastSectionBlock !== '') {
              refData.currentColHeight = refData.currentColHeight + 54;
              refData.cols['col' + refData.col].push({type: 'lastSectionBlock', html: refData.lastSectionBlock, lastshiftblock: refData.lastShiftBlockType});
            }
          }

          if (blockType === 'section') {
            refData.currentColHeight = refData.currentColHeight + 74;
            refData.cols['col' + refData.col].push({type: 'lastSubcategoryBlock', html: refData.lastSubcategoryBlock, lastshiftblock: refData.lastShiftBlockType});
          }
        }

        return refData;
    };

    return {
      formatSubcategoryName: function (shiftcategory2, categorytask2, refData) {
        refData.lastSectionBlock = '';
        var id = Math.random();
        var shiftCategory = refData.categories.filter(function (val) {
          return val && val.id === shiftcategory2.categoryId;
        })[0];
        var shiftSubCategory = refData.allSubcategories.filter(function (val) {
          return val && val.id === categorytask2.subcategoryId;
        })[0];

        var html = '<div id="cl' + id + '" class="categoriesline ' + shiftCategory.name + '"><span class="categories ' + shiftCategory.name + '">' + shiftSubCategory.name + '</span></div><div  id="rd' + categorytask2.id + '"  style="clear:both;"></div>';
        refData.lastSubcategoryBlock =  '<div id="cl' + id + '" class="categoriesline ' + shiftCategory.name + '"><span class="categories ' + shiftCategory.name + '">' + shiftSubCategory.name + '</span></div><div  id="rd' + categorytask2.id + '"  style="clear:both;"></div>';
        refData.lastBlockType = 'subcategory';
        refData.cols['col' + refData.col].push({type: 'subcategory', html: html});
        return refData;
      },
      formatSectionName: function(section2, refData) {
          var id = Math.random();
          refData.lastBlockType = 'section';
          var blockHTML = '<div id="sn' + id + section2.id + '" class="tab tab' + section2.sectionName + '"><span class="name section' + section2.sectionName + '">Section ' + section2.sectionName + '</span></div>';
          refData.cols['col' + refData.col].push({type: 'section', html: blockHTML});
          refData.lastSectionBlock = '<div id="sn' + id + section2.id + '" class="tab tab' + section2.sectionName + '"><span class="name section' + section2.sectionName + '">Section ' + section2.sectionName + '</span></div>';
          return refData;
      },
      formatShiftName: function (shift2, refData) {
        var id = Math.random();
        var shitftclass = 'shift1';
        var shiftModel = refData.shifts[shift2.shiftId];

        if (shiftModel.sequence > 6 && shiftModel.sequence < 15) {
          shitftclass = 'shift2';
        }

        if(refData.cols['col' + refData.col].last().type === 'lastShiftBlock') {
          refData.currentColHeight = refData.currentColHeight - 77;
          refData.cols['col' + refData.col].pop();
        }

        var shiftBlock = '<div id="s' + id + '" class="firstheader ' + shitftclass+'">' + shiftModel.name + '</div>'

        refData.lastBlockType = 'shift';
        refData.cols['col' + refData.col].push({type: 'shift', html: '<div id="s' + id + '" class="firstheader ' + shitftclass+'">' + shiftModel.name + '</div>'});
        refData.lastShiftBlockType = 'shift';
        refData.lastShiftBlock = shiftBlock;
        refData.columnShift = false;
        return refData;
      },
      checkHeight:  function(blockHeight, blockType, refData) {
        return _checkHeight(blockHeight, blockType, refData);
      },
      footerBlock: function(refData) {
        var now = moment().format('MMMM Do YYYY, h:mm:ss a');
        refData = _checkHeight(74, 'footer', refData);
        refData.lastBlockType = 'footer';
        refData.cols['col' + refData.col].push({type: 'footer', html: '<div class="footerBlock">Board Published ' + now + '</div><div style="clear: both;"></div>'});
        return refData;
      },
      headerLocation: function(location, refData) {
        var id = Math.random();
        var headerLocationHtml = '<div id="hl' + id + '" class="headerlogo form"><div id="hl' + id + '" class="logomulti ' + location.locationCode + '">' + location.locationCode +  '</div></div>';
        refData.lastheaderLocation = '<div id="hl' + id + '" class="headerlogo form"><div id="hl' + id + '" class="logomulti ' + location.locationCode + '">' + location.locationCode +  '</div></div>';

        if(!refData.firstHeaderLocation) {
          refData.firstHeaderLocation = '<div id="hl' + id + '" class="headerlogo form"><div id="hl' + id + '" class="logomulti ' + location.locationCode + '">' + location.locationCode +  '</div></div>';
        }

        refData.lastBlockType = 'location';
        refData.lastShiftBlockType = 'location';
        refData.cols['col' + refData.col].push({type: 'location', html: headerLocationHtml});
        refData.columnShift = false;

        return refData;

      },
      dateTime: function(data, refData) {
        var dateString = data.date.substring(4, 6) + '/' + data.date.substring(6, 8) + '/' + data.date.substring(0, 4);
        var logoday = moment(dateString).format('dddd') ;
        var logodate = moment(dateString).format('MMMM D, YYYY');
        var dateTimeHtml = '';
        refData.lastBlockType = 'datetime';
        dateTimeHtml = '<div class="dateTime"><img class="logo" src="../images/logo.png" /><div class="logoText"><p class="logoday">' + logoday + '</p><p class="logodate">' + logodate + '</p></div><div style="clear: both;"></div>';
        refData.cols['col' + refData.col].push({type: 'datetime', html: dateTimeHtml});
        refData.lastDateTimeBlock = dateTimeHtml;
        return refData;
      }
    }
  });