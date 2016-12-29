package com.budgetview.server.cloud.commands;

import javax.servlet.ServletException;
import java.io.IOException;

public interface Command {
  void run() throws ServletException, IOException;
}
