package gov.nyc.dsny.smart.opsboard.controllers;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nyc.dsny.smart.opsboard.domain.Kiosk;
import gov.nyc.dsny.smart.opsboard.persistence.repos.board.KioskRepository;
import gov.nyc.dsny.smart.opsboard.services.executors.KioskExecutor;


@Controller
@RequestMapping("/admin/monitorcrud")
public class KioskCrudController  {
	
	@Autowired
	private KioskRepository kioskRepository;
	
	@Autowired
	private KioskExecutor kioskExecutor;
	
	private static final Logger logger = LoggerFactory.getLogger(KioskCrudController.class);

  
	 @RequestMapping(method = RequestMethod.GET)
     public String getIndexPage() {
         return "monitorcrud";
     }
    @RequestMapping(value = "/kiosks", method = RequestMethod.GET)
    public ResponseEntity<List<Kiosk>> listAllKiosks() throws Exception {
        List<Kiosk> kiosks = kioskExecutor.loadAllKiosks();
        if(kiosks.isEmpty()){
            return new ResponseEntity<List<Kiosk>>(HttpStatus.NO_CONTENT);//You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<List<Kiosk>>(kiosks, HttpStatus.OK);
    }
  
    
    //-------------------Retrieve Single Kiosk--------------------------------------------------------
    
    @RequestMapping(value = "/kiosk/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Kiosk> getKiosk(@PathVariable("id") long id) {
    	logger.info("Fetching kiosk with id " + id);
        Kiosk kiosk = kioskRepository.getKioskById(id);
        if (kiosk == null) {
            logger.info("kiosk with id " + id + " not found");
            return new ResponseEntity<Kiosk>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Kiosk>(kiosk, HttpStatus.OK);
    }
  
  
    //-------------------Create a Kiosk--------------------------------------------------------
    
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Void> createKiosk(@RequestBody Kiosk kiosk, UriComponentsBuilder ucBuilder) throws Exception {
        logger.info("Creating Kiosk " + kiosk.getIpAddress());
  
        if (kioskExecutor.isKioskExist(kiosk)) {
            logger.info("A Kiosk with name " + kiosk.getIpAddress() + " already exist");
            return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }
  
        Kiosk newKiosk = kioskExecutor.save(kiosk);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/kiosk/{id}").buildAndExpand(newKiosk.getId()).toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }
    
  //------------------- Update a Kiosk --------------------------------------------------------
    
    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity<Kiosk> updateKiosk(@PathVariable("id") long id, @RequestBody Kiosk kiosk) throws Exception {
        logger.info("Updating Kiosk " + id);
          
        Kiosk existingKiosk = kioskRepository.getKioskById(id);
          
        if (existingKiosk==null) {
            logger.info("Kiosk with id " + id + " not found");
            return new ResponseEntity<Kiosk>(HttpStatus.NOT_FOUND);
        }
  
        existingKiosk.setIpAddress(kiosk.getIpAddress());
        existingKiosk.setDistrict(kiosk.getDistrict());
        existingKiosk.setDefaultGateway(kiosk.getDefaultGateway());
        existingKiosk.setHostname(kiosk.getHostname());
        existingKiosk.setSubnetMask(kiosk.getSubnetMask());
        existingKiosk.setUsername(kiosk.getUsername());
        existingKiosk.setGroupName(kiosk.getGroupName());
          
        kioskExecutor.save(existingKiosk);
        return new ResponseEntity<Kiosk>(existingKiosk, HttpStatus.OK);
    }
    
    //------------------- Delete a Kiosk --------------------------------------------------------
    
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Kiosk> deleteKiosk(@PathVariable("id") long id) {
        logger.info("Fetching & Deleting Kiosk with id " + id);
  
        Kiosk kiosk = kioskRepository.getKioskById(id);
        if (kiosk == null) {
            logger.info("Unable to delete. Kiosk with id " + id + " not found");
            return new ResponseEntity<Kiosk>(HttpStatus.NOT_FOUND);
        }
  
        kioskExecutor.deleteKioskById(kiosk);
        return new ResponseEntity<Kiosk>(HttpStatus.NO_CONTENT);
    }
  
}