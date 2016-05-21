package com.budgetview.importer.http;

import com.budgetview.client.exceptions.RemoteException;
import org.globsframework.utils.exceptions.Rethrowable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;

public abstract class ExceptionToStatusHttpServlet extends HttpServlet {

  protected void doPost(HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
    throws ServletException, IOException {
    httpServletRequest.setCharacterEncoding("UTF-8");
    httpServletResponse.setCharacterEncoding("UTF-8");
    try {
      action(httpServletRequest, httpServletResponse);
    }
    catch (RemoteException e) {
      processError(httpServletResponse, e.getId(), e.getMessage());
    }
    catch (Rethrowable e) {
      throw e.<IOException>getException();
    }
  }

  private void processError(HttpServletResponse httpServletResponse, int action, String message) {
    httpServletResponse.setStatus(400);
    try {
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(httpServletResponse.getOutputStream());
      objectOutputStream.writeInt(action);
      objectOutputStream.writeObject(message);
      objectOutputStream.close();
    }
    catch (IOException e) {
      throw new Rethrowable(e);
    }
  }

  protected abstract void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    throws IOException;

}
