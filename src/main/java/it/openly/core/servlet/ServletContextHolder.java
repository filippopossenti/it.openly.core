package it.openly.core.servlet;

import javax.servlet.ServletContext;

public class ServletContextHolder {
	
	private static ServletContext servletContext;

	static synchronized void setServletContext(ServletContext value) {
		servletContext = value;
	}
	
	public static synchronized ServletContext getServletContext() {
		return servletContext;
	}
}
