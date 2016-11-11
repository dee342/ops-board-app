package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.ActiveUserSession;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.SectionTask;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.ShiftCategory;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.SubcategoryTask;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.Task;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BoroTasksBoard{

	private OpsBoard board;
	
	private List<String> personIdList=new ArrayList<String>();
	
	private List<String> equipmentIdList=new ArrayList<String>();
	

	public List<String> getPersonIdList() {
		return personIdList;
	}

	public void setPersonIdList(List<String> personIdList) {
		this.personIdList = personIdList;
	}

	public BoroTasksBoard(OpsBoard board) {
		this.board = board;
		this.getTaskContainers();
	}

/*	public List<CommandMessage> getCommandMessagesHistory() {
		return board.getCommandMessagesHistory();
	}*/

	public String getDate() {
		return board.getDate();		
	}

	public String getId() {
		return board.getId();
	}

	public String getLocation() {
		return board.getLocation();
	}

	public String getLocationType() {
		return board.getLocationType();
	}

	public Map<String, ActiveUserSession> getOnlineSessions() {
		return board.getOnlineSessions();
	}
	
	

	public Map<String, TaskContainer> getTaskContainers(){
		
		Map<String, TaskContainer> map = new HashMap<String, TaskContainer>();
		
		for (Map.Entry<String, TaskContainer> tcEntry : board.getTaskContainers().entrySet()){
			
			TaskContainer tc = tcEntry.getValue();
			List<String> deleteShiftL=new ArrayList<String>();
			for (Map.Entry<String, LocationShift> lsEntry : tc.getLocationShifts().entrySet()){
			   
				LocationShift ls = lsEntry.getValue();
				List<String> deleteCateL=new ArrayList<String>();

				for (Map.Entry<String, ShiftCategory> scEntry : ls.getShiftCategories().entrySet()){
					
					ShiftCategory sc = scEntry.getValue();
				
					List<String> deleteSubCateL=new ArrayList<String>();
					for (Map.Entry<String, SubcategoryTask> scTaskEntry : sc.getSubcategoryTasks().entrySet()){
				
						SubcategoryTask scTask = scTaskEntry.getValue();
						
						Boolean isSuperTask=sc.getCategory().getName().equals("Supervision");	
							
				
						List<String> deleteTaskL=new ArrayList<String>();
						for(Entry<String, Task> taskEntry:scTask.getTasks().entrySet()){
							
							Boolean isNonLMad=false;
							Task task=taskEntry.getValue();
							if(task.getAssignedPerson1()!=null&&task.getAssignedPerson1().getPerson()!=null&&task.getAssignedPerson1().getPerson().getActiveMdaCodes()!=null){
								List<MdaStatus> personOneMda=task.getAssignedPerson1().getPerson().getActiveMdaCodes();
								for(MdaStatus mda:personOneMda){
									if(mda.getType().equals("MDA")&& (!mda.getSubType().equals("1L"))&&DateUtils.onOrBetween(new Date(), mda.getStartDate(), mda.getEndDate())){
										isNonLMad=true;
									}
								}
							}
							if(task.getAssignedPerson2()!=null&&task.getAssignedPerson2().getPerson()!=null&&task.getAssignedPerson2().getPerson().getActiveMdaCodes()!=null){
								List<MdaStatus> personTwoMda=task.getAssignedPerson2().getPerson().getActiveMdaCodes();
								for(MdaStatus mda:personTwoMda){
									if(mda.getType().equals("MDA")&& (!mda.getSubType().equals("1L"))&&DateUtils.onOrBetween(new Date(), mda.getStartDate(), mda.getEndDate())){
										isNonLMad=true;
									}
								}
							}
							//all equipment will show if in supervision category
							if(isSuperTask){
								if(task.getAssignedEquipment()!=null && task.getAssignedEquipment().getEquipment()!=null)
									this.equipmentIdList.add(task.getAssignedEquipment().getEquipment().getId());
							}
							//delete task does not meet need
							if(!((task.getAssignedPerson1()!=null && task.getAssignedPerson1().getPerson()!=null&&task.getAssignedPerson1().getPerson().isOfficer())||
							(task.getAssignedPerson2()!=null &&task.getAssignedPerson2().getPerson()!=null&& task.getAssignedPerson2().getPerson().isOfficer())||
							((task.getAssignedPerson1()!=null&&task.getAssignedPerson1().getPerson()!=null||task.getAssignedPerson2()!=null&&task.getAssignedPerson2().getPerson()!=null)&&isSuperTask)||
							((task.getAssignedPerson1()!=null&&task.getAssignedPerson1().getPerson()!=null||task.getAssignedPerson2()!=null&&task.getAssignedPerson2().getPerson()!=null)&&isNonLMad))){
								//sc.getSubcategoryTasks().remove(scTaskEntry.getKey());
								if(!isSuperTask)
								deleteTaskL.add(taskEntry.getKey());
								
								
							}
							//collect personId and equipmentId meet need
							else{
								if(task.getAssignedPerson1()!=null && task.getAssignedPerson1().getPerson()!=null)
									this.personIdList.add(task.getAssignedPerson1().getPerson().getId());
								if(task.getAssignedPerson2()!=null && task.getAssignedPerson2().getPerson()!=null)
									this.personIdList.add(task.getAssignedPerson2().getPerson().getId());
								if(task.getAssignedEquipment()!=null && task.getAssignedEquipment().getEquipment()!=null)
									this.equipmentIdList.add(task.getAssignedEquipment().getEquipment().getId());
							}
					
						}
						//actually delete un wanted task from subCate
						if(deleteTaskL.size()>0){
							for(String key:deleteTaskL)
								scTask.getTasks().remove(key);
						}
						deleteTaskL.clear();
							
					
						List<String> removeSecL=new ArrayList<>();
						for(Entry<String, SectionTask> sectionEntry:scTask.getSections().entrySet()){
							
							SectionTask sectionTask=sectionEntry.getValue();
							List<String> removeSecTaskL=new ArrayList<>();
							for(Entry<String, Task> sTaskEntry:sectionTask.getTasks().entrySet()){
								Boolean isNonLMad=false;
								Task sTask=sTaskEntry.getValue();
								if(sTask.getAssignedPerson1()!=null&&sTask.getAssignedPerson1().getPerson()!=null&&sTask.getAssignedPerson1().getPerson().getActiveMdaCodes()!=null){
									List<MdaStatus> personOneMda=sTask.getAssignedPerson1().getPerson().getActiveMdaCodes();
									for(MdaStatus mda:personOneMda){
										if(mda.getType().equals("MDA")&& (!mda.getSubType().equals("1L"))&&DateUtils.onOrBetween(new Date(), mda.getStartDate(), mda.getEndDate())){
											isNonLMad=true;
										}
									}
								}
								if(sTask.getAssignedPerson2()!=null&&sTask.getAssignedPerson2().getPerson()!=null&&sTask.getAssignedPerson2().getPerson().getActiveMdaCodes()!=null){
									List<MdaStatus> personTwoMda=sTask.getAssignedPerson2().getPerson().getActiveMdaCodes();
									for(MdaStatus mda:personTwoMda){
										if(mda.getType().equals("MDA")&& (!mda.getSubType().equals("1L"))&&DateUtils.onOrBetween(new Date(), mda.getStartDate(), mda.getEndDate())){
											isNonLMad=true;
										}
									}
								}
								
								if(isSuperTask){
									if(sTask.getAssignedEquipment()!=null && sTask.getAssignedEquipment().getEquipment()!=null)
										this.equipmentIdList.add(sTask.getAssignedEquipment().getEquipment().getId());
								}
								//delete section task does not meet need from section
								if(!((sTask.getAssignedPerson1()!=null && sTask.getAssignedPerson1().getPerson()!=null&&sTask.getAssignedPerson1().getPerson().isOfficer())||
								(sTask.getAssignedPerson2()!=null && sTask.getAssignedPerson2().getPerson()!=null&&sTask.getAssignedPerson2().getPerson().isOfficer())||
								((sTask.getAssignedPerson1()!=null&&sTask.getAssignedPerson1().getPerson()!=null||sTask.getAssignedPerson2()!=null&&sTask.getAssignedPerson2().getPerson()!=null)&&isSuperTask)||
								((sTask.getAssignedPerson1()!=null&&sTask.getAssignedPerson1().getPerson()!=null||sTask.getAssignedPerson2()!=null&&sTask.getAssignedPerson2().getPerson()!=null)&&isNonLMad))){
									//collect section task that need to be deleted
									if(!isSuperTask)
									removeSecTaskL.add(sTaskEntry.getKey());													
									
								}
								//collect personId and equipmentId meet need
								else{
									if(sTask.getAssignedPerson1()!=null && sTask.getAssignedPerson1().getPerson()!=null)
										this.personIdList.add(sTask.getAssignedPerson1().getPerson().getId());
									if(sTask.getAssignedPerson2()!=null && sTask.getAssignedPerson2().getPerson()!=null)
										this.personIdList.add(sTask.getAssignedPerson2().getPerson().getId());
									if(sTask.getAssignedEquipment()!=null && sTask.getAssignedEquipment().getEquipment()!=null)
										this.equipmentIdList.add(sTask.getAssignedEquipment().getEquipment().getId());
								}							
								
							}
							//actual delete un needed section task
							if(removeSecTaskL.size()>0){
								for(String key:removeSecTaskL){
									sectionTask.getTasks().remove(key);	
								}
							}
							removeSecTaskL.clear();
							//collect empty section from subcategories
							if(sectionTask.getTasks().size()==0)
								removeSecL.add(sectionEntry.getKey());
							
						}		
						//actual delete un wanted section from subcate
						if(removeSecL.size()>0){
							for(String key:removeSecL)
							    scTask.getSections().remove(key);
						}
						removeSecL.clear();
						//if both section task and task are empty, delete subCate from Cate
						if(scTask.getSections().size()==0 && scTask.getTasks().size()==0)
							deleteSubCateL.add(scTaskEntry.getKey());
				
					}
					//actully delete subCate
					if(deleteSubCateL.size()>0){
						for(String key:deleteSubCateL){
							sc.getSubcategoryTasks().remove(key);
						}
					}
					deleteSubCateL.clear();
					//collect Cate need to be deleted
					if(sc.getSubcategoryTasks().size()==0)
						deleteCateL.add(scEntry.getKey());

					
			}
				//actually delelte cate
				if(deleteCateL.size()>0){
					for(String key: deleteCateL){
						ls.getShiftCategories().remove(key);
					}
				}
				deleteCateL.clear();
				//collect shift need to be deleted
				if(ls.getShiftCategories().size()==0)
					 deleteShiftL.add(lsEntry.getKey());				
			}
			if(deleteShiftL.size()>0){
				for(String key:deleteShiftL){
					 tc.getLocationShifts().remove(key);
				}
			}
		
			map.put(tcEntry.getKey(), tc);
		}	
		return map;
	}
	
	public Map<String, OpsBoardEquipment> getEquipment() {
		Map<String, OpsBoardEquipment> oldEquipment = board.getEquipment();
		Map<String, OpsBoardEquipment> newEquipment = new HashMap<String, OpsBoardEquipment>();
		for(String  equipmentId:this.equipmentIdList){
			newEquipment.put(equipmentId,oldEquipment.get(equipmentId));
		}	
		return newEquipment;
	}

	public Map<String, OpsBoardPerson> getPersonnel(){			
		Map<String, OpsBoardPerson> oldPersonnel=board.getPersonnel();
		Map<String, OpsBoardPerson> newPersonnel=new HashMap<String, OpsBoardPerson>();
		for(String  personId:this.personIdList){
			newPersonnel.put(personId,oldPersonnel.get(personId));
		}
		
		//looking for all supervisors 
		for(Map.Entry<String, OpsBoardPerson> superPerson:oldPersonnel.entrySet()){		
                if(superPerson.getValue().isOfficer() && superPerson.getValue().getCurrentLocation().equals(getLocation()))
                	newPersonnel.put(superPerson.getKey(),superPerson.getValue());
		}
		//look for MDA person except person with 1L MDA code
		for(Map.Entry<String, OpsBoardPerson> superPerson:oldPersonnel.entrySet()){
			Boolean isNonL=false;
			List<MdaStatus>  mdaReson=superPerson.getValue().getActiveMdaCodes();
			for(MdaStatus reason:mdaReson){
				if(reason.getType().equals("MDA")&& (!reason.getSubType().equals("1L"))&&DateUtils.onOrBetween(new Date(), reason.getStartDate(), reason.getEndDate()) 
						&& superPerson.getValue().getCurrentLocation().equals(getLocation())){
					isNonL=true;
				}
			}
                if(isNonL)
                	newPersonnel.put(superPerson.getKey(),superPerson.getValue());
		}
		
		return newPersonnel;
	}

	public Date getShiftsEndDate() {
		return board.getShiftsEndDate();
	}

	public Date getShiftsStartDate() {
		return board.getShiftsStartDate();
	}
		
}
