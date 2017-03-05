package com.budgetview.server.cloud.commands;

import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class DatabaseCommand extends HttpCommand {

  protected final GlobsDatabase database;

  public DatabaseCommand(Directory directory, HttpServletRequest request, HttpServletResponse response, Logger logger) {
    super(request, response, logger);
    this.database = directory.get(GlobsDatabase.class);
  }
}
