package gov.nyc.dsny.smart.opsboard.services;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.board.Quota;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Series;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.DownCode;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.MaterialType;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.MdaType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.OfficerPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.SpecialPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.UnavailabilityType;
import gov.nyc.dsny.smart.opsboard.domain.reference.BoardType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit;
import gov.nyc.dsny.smart.opsboard.persistence.repos.board.QuotaRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.SeriesRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.BoardTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.CategoryRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.DownCodeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.LocationRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.MaterialTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.MdaTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.OfficerPositionTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.ShiftRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SpecialPositionTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubcategoryRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.UnavailabilityTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.WorkUnitRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbReferenceDataService implements ReferenceDataService {

	@Autowired
	private CategoryRepository categoryRepo;

	@Autowired
	private DownCodeRepository downCodeRepo;

	@Autowired
	private LocationRepository locationRepo;

	@Autowired
	private MaterialTypeRepository materialTypeRepo;

	@Autowired
	private SeriesRepository seriesRepo;
	
	@Autowired
	private BoardTypeRepository boardTypeRepo;

	@Autowired
	private MdaTypeRepository mdaTypeRepo;

	@Autowired
	private SpecialPositionTypeRepository specialPositionTypeRepository;
	

	@Autowired
	private OfficerPositionTypeRepository officerPositionTypeRepository;

	@Autowired
	private UnavailabilityTypeRepository unavailabilityTypeRepository;

	
	@Autowired
	private ShiftRepository shiftRepo;

	@Autowired
	private SubcategoryRepository subcategoryRepo;
	
	@Autowired
	private SubTypeRepository subTypeRepository;

	@Autowired
	private WorkUnitRepository workUnitRepo;
	
	@Autowired
	private QuotaRepository quotaRepo;

	public DbReferenceDataService() {
		super();
	}

	@Override
	public List<Category> getCategories(Date date) {
		return categoryRepo.findByDate(date);
	}

	@Override
	public List<DownCode> getDownCodes() {
		return getDownCodes(new Date());
	}

	@Override
	public List<DownCode> getDownCodes(Date date) {
		List<DownCode> downCodes = downCodeRepo.findByDate(date);

		return downCodes;
	}
	
	@Override
	public List<Location> getLocations() {
		return getLocations (new Date());
	}
	
	@Override
	public List<Location> getLocations(Date date) 
	{
		List<Location> locations =  new ArrayList<Location>(locationRepo.findByDate(date));
		return locations;
	}
	
	@Override
	public Location getLocation(Date date, String code) {
		return locationRepo.findByDateAndCode(date, code);
	}
	
	@Override
	public Location getLocation(Long id)
	{
		return locationRepo.findOne(id);
	}

	@Override
	public List<BoardType> getBoardTypes() {
		return getBoardTypes (new Date());
	}

	@Override
	public List<MaterialType> getMaterialTypes() 
	{
		return getMaterialTypes(new Date());
	}
	
	@Override
	public List<MaterialType> getMaterialTypes(Date boardDate) {
		return materialTypeRepo.findByDate(boardDate);
	}
	
	@Override
	public List<Series> getSeries(Date boardDate)
	{
		return seriesRepo.findByDate(boardDate);
	}

	@Override
	public List<MdaType> getMdaTypes() {
		return getMdaTypes(new Date());
	}

	@Override
	public List<Quota> getQuotas() {
		return quotaRepo.findAll();
	}

	@Override
	public List<Quota> getQuotas(Date boardDate) {
		return quotaRepo.findByDate(boardDate);
	}
	
	@Override
	public List<Shift> getShifts() {
		return shiftRepo.findAll();
	}

	@Override
	public List<Subcategory> getSubcategories() {
		return getSubcategories(new Date());
	}
	
	@Override
	public List<WorkUnit> getWorkUnits() 
	{
		return getWorkUnits(new Date());
	}
	
	@Override
	public List<WorkUnit> getWorkUnits(Date date) 
	{
		List<WorkUnit> workUnits = workUnitRepo.findByDate(date);
		return workUnits;
	}

	@Override
	public List<Subcategory> getSubcategories(Date date) {
		return subcategoryRepo.findByDate(date);
	}

	@Override
	public List<MdaType> getMdaTypes(Date date) {
		List<MdaType> mdaTypes = mdaTypeRepo.findByDate(date);		
		return mdaTypes;
	}
	
	@Override
	public List<SpecialPositionType> getSpecialPositionTypes(Date date)
	{
		List<SpecialPositionType> specialPositionTypes = specialPositionTypeRepository.findByDate(date);
		return specialPositionTypes;
	}
	
	@Override
	public List<OfficerPositionType> getOfficerPositionTypes(Date date)
	{
		List<OfficerPositionType> officerPositionTypes = officerPositionTypeRepository.findByDate(date);		
		return officerPositionTypes;
	}
	
	
	@Override
	public List<UnavailabilityType> getUnavailabilityTypes(Date date)
	{
		List<UnavailabilityType> unavailabilityTypes = unavailabilityTypeRepository.findByDate(date);
		return unavailabilityTypes;
	}

	@Override
	public List<BoardType> getBoardTypes(Date date) {
		List<BoardType> boardTypes = boardTypeRepo.findByDate(date);
		return boardTypes;
	}

	public List<Shift> getShifts(Date date) {
		return shiftRepo.findByDate(date);
	}
	
	@Override
	public List<SubType> getSubType(Date date) {
		return subTypeRepository.findByDate(date);
	}

	@Override
	public Set<Category> getByDateAndBoardType(Date date, String code) {
		return categoryRepo.findByDateAndBoardType(code, date);
	}

	@Override
	public Shift findShiftById(Long shiftId) {
		return shiftRepo.findOne(shiftId);
	}

	@Override
	public Subcategory findSubCategoryById(Long subCagtegoryId) {
		return subcategoryRepo.findOne(subCagtegoryId);
	}
}
