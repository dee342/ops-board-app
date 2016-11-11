/*
DO NOT DELETE!!
DO NOT DELETE!!
DO NOT DELETE!!
DO NOT DELETE!!
DO NOT DELETE!!

THIS IS NOT NEEDED UNTIL DISTRIBUTED CACHING IS ENABLED.

DO NOT DELETE!!
DO NOT DELETE!!
DO NOT DELETE!!
DO NOT DELETE!! 

package gov.nyc.dsny.smart.opsboard.misc;

import gov.nyc.dsny.smart.opsboard.cache.gf.EquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.PersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GemfireTransientAspect {

	@Autowired
	private EquipmentCacheService equipmentCache;
	
	@Autowired
	private PersonnelCacheService personnelCache;	
		
	@SuppressWarnings("unchecked")
	@AfterReturning(pointcut = "execution(* gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardEquipmentGFRepository.find*(..)) || execution(* gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardEquipmentGFRepository.get*(..))", returning = "result")
	public void populateEquipment(JoinPoint jp, Object result) throws Throwable{
		
		if(result == null)
			return;
		
		if(!List.class.isInstance(result)){
			if(BoardEquipment.class.isInstance(result))
				result = Stream.of((BoardEquipment) result).collect(Collectors.toList());
		}

		((List) result).forEach(be -> {
			if(BoardEquipment.class.isInstance(be)){
				BoardEquipment temp = (BoardEquipment) be;
				temp.setEquipment(equipmentCache.get(BoardEquipment.EXTRACT_EQUIPMENT_ID(temp.getId())));
			}
		});
			

	}
	
	@SuppressWarnings("unchecked")
	@AfterReturning(pointcut = "execution(* gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardPersonGFRepository.find*(..)) || execution(* gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardPersonGFRepository.get*(..))", returning = "result")
	public void populatePersonnel(JoinPoint jp, Object result) throws Throwable{
		
		if(result == null)
			return;
		
		if(!List.class.isInstance(result)){
			if(BoardPerson.class.isInstance(result))
				result = Stream.of((BoardPerson) result).collect(Collectors.toList());
		}
		
		((List) result).forEach(bp -> {
			if(BoardPerson.class.isInstance(bp)){
				BoardPerson temp = (BoardPerson) bp;
				temp.setPerson(personnelCache.get(BoardPerson.EXTRACT_PERSON_ID(temp.getId())));
			}
		});
			

	}
	
	@SuppressWarnings("unchecked")
	@AfterReturning(pointcut = "execution(* gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardGFRepository.find*(..)) || execution(* gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardGFRepository.get*(..))", returning = "result")
	public void populateBoard(JoinPoint jp, Object result) throws Throwable{
		
		if(result == null)
			return;
		
		if(!List.class.isInstance(result)){
			if(BoardContainer.class.isInstance(result))
				result = Stream.of((BoardContainer) result).collect(Collectors.toList());
		}
		
		((List) result).forEach(b -> {
			if(BoardContainer.class.isInstance(b)){
				Board temp = ((BoardContainer) b).getBoard();
				temp.getEquipment().values().forEach(be -> {
					be.setEquipment(equipmentCache.get(BoardEquipment.EXTRACT_EQUIPMENT_ID(be.getId())));
				});
				
				temp.getPersonnel().values().forEach(bp -> {
					bp.setPerson(personnelCache.get(BoardPerson.EXTRACT_PERSON_ID(bp.getId())));
				});
				
			}
		});
			

	}
	
}
*/