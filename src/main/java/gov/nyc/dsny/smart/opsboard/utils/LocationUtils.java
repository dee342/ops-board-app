package gov.nyc.dsny.smart.opsboard.utils;

import gov.nyc.dsny.smart.opsboard.util.Utils;

import java.net.InetAddress;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.catalina.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationUtils {

	public synchronized static String GetAppServerName() {
		if (appServerName != null && !appServerName.isEmpty()) {
			return appServerName;
		}
		;
		// TODO: Refactor this code when we will find solution how to get Tomcat host & port
		String host = "";
		try {
			host = InetAddress.getLocalHost().getHostName() + "_";
			MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
			ObjectName name = new ObjectName("Tomcat", "type", "Server");
			Server server = (Server) mBeanServer.getAttribute(name, "managedResource");
			//appServerName = System.getProperty("user.dir");
			//DP:
			appServerName = host;
			log.debug(appServerName);
			return appServerName;
		} catch (Exception e) {
			appServerName = host + System.getProperty("catalina.base");
			log.debug(appServerName);
			return appServerName;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	private static String appServerName;
}
