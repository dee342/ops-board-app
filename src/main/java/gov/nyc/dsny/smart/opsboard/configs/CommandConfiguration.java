package gov.nyc.dsny.smart.opsboard.configs;

import java.util.HashMap;
import java.util.Map;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

@Configuration
public class CommandConfiguration {

	public static final String COMMAND_PACKAGE = "gov.nyc.dsny.smart.opsboard.commands";
	private static final Logger log = LoggerFactory.getLogger(CommandConfiguration.class);

	@Bean
	public Map<String, Class<?>> commandClasses() {
		log.info("Creating commandClasses bean");
		Map<String, Class<?>> commandClasses = new HashMap<String, Class<?>>();

		try {
			ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
			MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils
							.resolvePlaceholders(COMMAND_PACKAGE)) + "/" + "**/*.class";
			Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
					Class<?> clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
					if (StringUtils.startsWith(clazz.getSimpleName(), "Command")
							&& !StringUtils.contains(clazz.getSimpleName(), "Test")) {
						commandClasses.put(StringUtils.replace(clazz.getSimpleName(), "Command", ""), clazz);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return commandClasses;
	}
}
