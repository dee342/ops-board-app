package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
@EnableEntityLinks
@EnableJpaRepositories(basePackages = {"gov.nyc.dsny.smart.opsboard.persistence.repos", "gov.nyc.dsny.smart.opsboard.integration.repo", "gov.nyc.dsny.smart.opsboard.refloader.repos"})
public class JpaConfiguration {
	
	@Autowired
	private EntityLinks entityLinks; 	
	
	@Bean 
	public ResourceProcessor<Resource<BoardEquipment>> equipmentProcessor() {
        return new ResourceProcessor<Resource<BoardEquipment>>() {
            @Override public Resource<BoardEquipment> process(Resource<BoardEquipment> resource) {
            	LinkBuilder lb = entityLinks.linkFor(gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment.class);
            	StringBuilder sb = new StringBuilder();
            	sb.append(lb.toString());
            	sb.append("/search/findByEquipmentId?equipment=");
            	sb.append(resource.getContent().getId());
            	resource.add(new Link(sb.toString()).withRel("detachments"));
                return resource;
            }
        };
    }
	
}