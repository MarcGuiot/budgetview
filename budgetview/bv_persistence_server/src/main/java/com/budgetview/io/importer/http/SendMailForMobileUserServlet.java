package com.budgetview.io.importer.http;


import com.budgetview.shared.http.HttpBudgetViewConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SendMailForMobileUserServlet extends ExceptionToStatusHttpServlet {
  private String root;

  public SendMailForMobileUserServlet(String root) {
    this.root = root;
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    InputStream inputStream = httpServletRequest.getInputStream();
    OutputStream outputStream = httpServletResponse.getOutputStream();
    String mail = httpServletRequest.getHeader(HttpBudgetViewConstants.HEADER_MAIL);
    String coding = httpServletRequest.getHeader(HttpBudgetViewConstants.HEADER_MAIL);

  }
}
