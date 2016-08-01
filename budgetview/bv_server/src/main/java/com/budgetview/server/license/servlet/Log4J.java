package com.budgetview.server.license.servlet;

import org.apache.log4j.PropertyConfigurator;

public class Log4J {
  public static void init() {
    try {
      PropertyConfigurator.configure(Log4J.class.getResourceAsStream("/log4j.properties"));
    }
    catch (Exception e) {

      System.err.println("Failed to init log4j - /log4j.properties file not found in JAR");
    }
  }
}
