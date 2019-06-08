package it.openly.core.logging.logback;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener meant to add a parameter needed to initialise Logback properly in this application.
 * In order for it to work, you have to add a listener in your web.xml file, as follows:
 * {@code
 *     <listener>
 *         <listener-class>it.openly.core.logging.logback.LogbackJNDIListener</listener-class>
 *     </listener>
 * }
 * Note that the listener must be added before the other ones related to Logback. Here follows
 * a more complete example:
 * {@code
 *     <env-entry>
 *         <env-entry-name>logback/context-name</env-entry-name>
 *         <env-entry-type>java.lang.String</env-entry-type>
 *         <env-entry-value>bit</env-entry-value>
 *     </env-entry>
 *     <listener>
 *         <listener-class>it.openly.core.logging.logback.LogbackJNDIListener</listener-class>
 *     </listener>
 *     <listener>
 *         <listener-class>ch.qos.logback.classic.selector.servlet.ContextDetachingSCL</listener-class>
 *     </listener>
 *     <filter>
 *         <filter-name>LoggerContextFilter</filter-name>
 *         <filter-class>ch.qos.logback.classic.selector.servlet.LoggerContextFilter</filter-class>
 *     </filter>
 *     <filter-mapping>
 *         <filter-name>LoggerContextFilter</filter-name>
 *         <url-pattern>/*</url-pattern>
 *     </filter-mapping>
 * }
 * 
 * @author filippo.possenti
 *
 */
public class LogbackJNDIListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	public void contextInitialized(ServletContextEvent arg0) {
		String override = System.getProperty("logbackjndilistener.disable");
		if("true".equalsIgnoreCase(override)) {
			System.out.println("WARNING: logbackjndilistener.disable was set to true. You may need to add -Dlogback.ContextSelector=JNDI to your command line.");
		}
		else {
			System.out.println("INFO: Adding logback.ContextSelector=JNDI system property. You can disable this behaviour by setting the logbackjndilistener.disable system property or by modifying the web application and remove the appropriate listener.");
			System.setProperty("logback.ContextSelector", "JNDI");
		}
	}

}
