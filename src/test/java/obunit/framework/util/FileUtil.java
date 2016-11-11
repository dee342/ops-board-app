package obunit.framework.util;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static File getFile (String relativeFileName, Class clazz) throws Exception
	{
		URL file = getURL(relativeFileName, clazz);
		return new File(file.toURI());
	}

	private static URL getURL (String relativePath, Class clazz) throws Exception
	{
		StringBuilder filePath = new StringBuilder(relativePath).append(clazz.getSimpleName()).append(".xlsx");
		String path = filePath.toString();
		return clazz.getResource(path);
	}
	
}
