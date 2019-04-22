package it.openly.core.logging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A class meant to be used for AOP-based logging using Spring and AspectJ.
 * 
 * Methods included in this class allow to ease logging operations and add some useful context
 * to it.<br/>
 * Typical use will involve putting the following in a spring configuration file (say aop-context.xml):<br/>
 * {@code
 *     <beans:bean id="aspectLogger" class="it.openly.core.logging.AspectModule" />
 *     <aop:config>
 *      <aop:aspect id="aspectLogging" ref="aspectLogger">
 *       <aop:pointcut id="pointCutAround" expression="execution(public * it.openly..*(..))" />
 *       <aop:around method="logAround" pointcut-ref="pointCutAround"  />
 *      </aop:aspect>
 *     </aop:config>
 * }
 * Then, writing this in the spring configuration file creating your beans:<br/>
 * {@code
 *     <beans:import resource="aop-context.xml" />
 *     <aop:aspectj-autoproxy/>
 * }
 * 
 * @author Filippo
 *
 */
public class AspectModule {
	/**
	 * Represents the current HTTP session, if any.<br/>
	 * Logback specifier: %X{session}
	 */
	public static final String SESSION = "session";
	/**
	 * Represents the currently logged in user, if any, extracted from Spring's SecurityContextHolder.<br/>
	 * Logback specifier: %X{user}
	 */
	public static final String USER = "user";
	/**
	 * Represents the joinpoint currently being evaluated. This includes the full class and method name.<br/>
	 * Logback specifier: %X{joinpoint}
	 */
	public static final String JOINPOINT = "joinpoint";
	/**
	 * Represents the name of the method currently being evaluated.<br/>
	 * Logback specifier: %X{methodname}
	 */
	public static final String METHODNAME = "methodname";
	/**
	 * Represents the time taken by method's execution.<br/>
	 * Note that when starting method execution an empty string will be specified.<br/>
	 * Logback specifier: %X{elapsedtime}
	 */
	public static final String ELAPSEDTIME = "elapsedtime";
	
	/**
	 * Meant to be consumed by AspectJ as an "around execution" pointcut.<br/>
	 * When used, this method will do the following:<br/>
	 * - Log that the method is starting execution<br/>
	 * - Log that the method is ending execution<br/>
	 * - Log if a method execution raised an error<br/>
	 * <br/>
	 * Note that it rely on Mapped Diagnostic Context features from the
	 *  underlying logging library. Assuming you are using logback for logging,
	 *  you'll be able to access additional useful information. Recommended log pattern is
	 *  the following:<br/>
	 * %d{ISO8601}|${appname}|%thread|%X{session}|%X{user}|%-5level|%logger{60}|%X{methodname}|%X{elapsedtime}|%msg %n<br/>
	 * 
	 * 
	 * @param joinPoint The joinpoint
	 * @return The result value of the function
	 * @throws Throwable The thrown exception
	 */
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
		
		String typeName = joinPoint.getSignature().getDeclaringTypeName();
		String methodName = joinPoint.getSignature().getName();
		
		MDC.put(SESSION, getSessionId());
		MDC.put(USER, getUserName());
		MDC.put(JOINPOINT, typeName + "." + methodName);
		MDC.put(METHODNAME, methodName);
		MDC.put(ELAPSEDTIME, "");
		
		logger.debug("Starting execution of {}.{}.", typeName, methodName);
		logArguments(logger, typeName + "." + methodName, joinPoint.getArgs());
		StopWatch stopwatch = new StopWatch();
		try {
			stopwatch.start();
			Object result = joinPoint.proceed();
			stopwatch.stop();
			logger.debug("Return value of {}.{}: {}", typeName, methodName, result);
			MDC.put(ELAPSEDTIME, stopwatch.getTotalTimeMillis() + " ms");
			logger.debug("Execution succeeded for {}.{}", typeName, methodName);
			return result;
		}
		catch(Throwable t) {
			stopwatch.stop();
			MDC.put(ELAPSEDTIME, stopwatch.getTotalTimeMillis() + " ms");
			logger.error("Execution failed for method {}. Exception hash is {}, stack trace follows.", typeName + "." + methodName, t.hashCode(), t);
			throw t;
		}
		finally {
			MDC.clear();
		}
	}
	
	private static void logArguments(Logger logger, String joinpoint, Object[] args) {
		logger.debug("There are {} arguments for {}.", args.length, joinpoint);
		for(int i = 0; i < args.length; i++) {
			logger.debug("    {}: {}", i, args[i]);
		}
	}
	
	private static String getSessionId() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String result = "no session";
		if(attr != null) {
			HttpServletRequest req = attr.getRequest();
			if(req != null) {
				HttpSession sess = req.getSession(false);
				if(sess != null) {
					result = sess.getId();
				}
			}
		}
		return result;
	}
	
	private static String getUserName() {
		SecurityContext ctx = SecurityContextHolder.getContext();
		String result = "no user";
		if(ctx != null) {
			Authentication auth = ctx.getAuthentication();
			if(auth != null) {
				Object princ = auth.getPrincipal();
				if(princ instanceof User) {
					User user = (User)princ;
					result = user.getUsername();
				}
			}
		}
		return result;
	}
}
