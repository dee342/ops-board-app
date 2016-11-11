var fs = require('fs'),
    path = require('path'),
    util = require('util'),
    _ = require('../node_modules/underscore'),
    moment = require('../node_modules/moment'),
    //xml2js = require('xml2js'),
    soap = require('../node_modules/soap'),
    util = require('util');

describe('SMART Operations Board', function() {

    // Add global spec helpers in this file
    var getDateStr = function () {
        var d = (new Date() + '').replace(new RegExp(':', 'g'), '-').split(' ');
        // "2013-Sep-03-21:58:03"
        return [d[3], d[1], d[2], d[4]].join('-');
    };

    var locations = [];

    var errorCallback = function (err) {
        console.log(err);
    };

    // create a new javascript Date object based on the timestamp
    var timestampToDate = function (unix_timestamp) {
        var date = new Date(unix_timestamp);
        // hours part from the timestamp
        var hours = date.getHours();
        // minutes part from the timestamp
        var minutes = date.getMinutes();
        // seconds part from the timestamp
        var seconds = date.getSeconds();

        var timeValues = [hours, minutes, seconds];
        timeValues.forEach(function (val) {
            if (val.length < 2) {
                // padding
                val = '0' + val;
            }
        });
        // will display time in 10:30:23 format
        return hours + ':' + minutes + ':' + seconds;
    };

    // Take a screenshot automatically after each failing test.
    afterEach(function () {
        var passed = jasmine.getEnv().currentSpec.results().passed();
        // Replace all space characters in spec name with dashes
        var specName = jasmine.getEnv().currentSpec.description.replace(new RegExp(' ', 'g'), '-'),
            baseFileName = specName + '-' + getDateStr(),
            reportDir = path.resolve(__dirname + '/../report/'),
            consoleLogsDir = path.resolve(reportDir + '/logs/'),
            screenshotsDir = path.resolve(reportDir + '/screenshots/');

        if (!fs.existsSync(reportDir)) {
            fs.mkdirSync(reportDir);
        }

        if (!passed) {
            // Create screenshots dir if doesn't exist
            //console.log('screenshotsDir = [' + screenshotsDir + ']');
            if (!fs.existsSync(screenshotsDir)) {
                fs.mkdirSync(screenshotsDir);
            }

            var pngFileName = path.resolve(screenshotsDir + '/' + baseFileName + '.png');
            browser.takeScreenshot().then(function (png) {
                // Do something with the png...
                //console.log('Writing file ' + pngFileName);

                fs.writeFileSync(pngFileName, png, {encoding: 'base64'}, function (err) {
                    console.log(err);
                });
            }, errorCallback);
        }

        // Flush browser console to file
        var logs = browser.driver.manage().logs(),
            logType = 'browser'; // browser
        logs.getAvailableLogTypes().then(function (logTypes) {
            if (logTypes.indexOf(logType) > -1) {
                var logFileName = path.resolve(consoleLogsDir + '/' + baseFileName + '.txt');
                browser.driver.manage().logs().get(logType).then(function (logsEntries) {
                    if (!fs.existsSync(consoleLogsDir)) {
                        fs.mkdirSync(consoleLogsDir);
                    }
                    // Write the browser logs to file
                    //console.log('Writing file ' + logFileName);
                    var len = logsEntries.length;
                    for (var i = 0; i < len; ++i) {

                        var logEntry = logsEntries[i];

                        var msg = timestampToDate(logEntry.timestamp) + ' ' + logEntry.type + ' ' + logEntry.message;
                        fs.appendFileSync(logFileName, msg + '\r\n', {encoding: 'utf8'}, errorCallback);
                    }
                }, errorCallback);
            }
        });

    });


    var driver = browser.driver;
    driver.manage().deleteAllCookies();

    beforeEach(function() {
        browser.get('http://127.0.0.1/smart-opsboard/');
        //driver.manage().addCookie('JSESSIONID', 'E295F7C64E3DFD476380978321F4EE89', '/smart-opsboard/', '127.0.0.1');
    });

    it('should have a title', function() {
        expect(browser.getTitle()).toEqual('SMART Operations Board - Login');
    });

    it ('should go to home page', function() {
        element(by.id('username')).sendKeys('admin');
        element(by.id('password')).sendKeys('admin');
        element(by.id('Login')).click();
        expect(browser.getTitle()).toEqual('SMART Operations Board - Board Selection');  

        browser.debugger();    
    });

    it ('Verifying workunits screen data', function(done) {
        driver.executeAsyncScript(function() {
            var callback = arguments[arguments.length - 1];
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    callback(xhr.responseText);
                }
            }
            xhr.open("GET", "/smart-opsboard/referencedata/workunits", true);
            xhr.send();
          
        }).then(function(res) {
            var data = JSON.parse(res);
            var flag = false;


            var url = 'http://msdlva-dsnysmt3.csc.nycnet:12000/PSIGW/PeopleSoftServiceListeningConnector/DS_WORK_UNIT.1.wsdl';
            var args = {WorkUnitRequestPsft: ''};
            soap.createClient(url, function(err, client) {
                client.DS_GET_WORK_UNIT(args, function(err, result) {
                    var unit = result.WorkUnit;
                    var psVsJava = false;
                    for (var j = 0; j < unit.length; j++) {
                        for (var i = 0; i < data.length; i++) {
                            var index = i;
                            if (unit[j].Name === data[index].workunitDescription) {
                                locations = _.union(locations, unit[j].LocationID);
                                psVsJava = true;
                            }
                        }
                        //expect(psVsJava).toEqual(true);  
                        if (!psVsJava)  console.log('PS WS data not matching with Java - ' + unit[j].Name)
                        psVsJava = false;  
                    }
                    var javaVsJS = false;

                    for (var i = 0; i < data.length; i++) {
                        var index = i;
                        element(by.model('workUnit')).all(by.css('option')).each(function(element){
                            element.getText().then(function(test) {
                               if (data[index].workunitDescription.trim() === test.trim()) {
                                    javaVsJS = true;
                                }
                            })

                        }).then(function(){
                            expect(javaVsJS).toEqual(true);
                            if (!javaVsJS)  console.log('Java data not matching with JS - ' + data[index].workunitDescription)   
                            javaVsJS = false;  
                        })
                        
                    }
                    element(by.model('workUnit')).sendKeys('Brooklyn North Boro Office');
                    element(by.model('location')).sendKeys('BKN01');
                    element(by.css('.button')).click(); 
                    done()
                    
                })      
            });            
        })
    });

    it ('Verifying equipment data', function(done) {
        var locs, 
            i = 0;
        locs = 'BKN01';//locations[i]
        driver.executeAsyncScript(function(locs) {
            var callback = arguments[arguments.length - 1];
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    callback(xhr.responseText);
                }
            }
            xhr.open("GET", "/smart-opsboard/BKN01/20140829/load", true);
            xhr.send();
          
        }).then(function(res) {
                var flag = false,
                    equipment = JSON.parse(res).equipment,
                    equipments = [];
                 console.log('1')
                var url = 'http://msdlva-dsnysmt3.csc.nycnet:12000/PSIGW/PeopleSoftServiceListeningConnector/DS_EQUIP_DATA.1.wsdl';
                var d = new Date();
                d.setDate(d.getDate() - 1);
                d.setHours(22,0,0,0);

                var h = new Date();
                h.setDate(d.getDate() - 1);
                h.setHours(5,0,0,0);
                soap.createClient(url, function(err, client) {  
                    console.log('2')
                    var args = {EquipmentRequestPsft: {
                        Location: locs,
                        EndDateTime: d.toISOString(),
                        HistoryStartDateTime: h.toISOString()
                    }};
                    client.DS_GET_EQUIP_DATA(args, function(err, result) {
                        console.log('5')
                        var equips = result.EquipmentPS;
                        var equipFlag = false;
                        Object.keys(equipment).forEach(function(key){
                            for (var k = 0; k < equips.length; k++) {
                                if (equips[k].EquipmentID === key.split('_')[0])
                                    equipFlag = true;
                            }
                            expect(equipFlag).toEqual(true);  
                            if (!equipFlag)  console.log('PS WS data not matching with Java - ' + key + ' location - ' + locs)
                            equipFlag = false;  
                        })
                        done()
                    })
                });
        })
    })

});