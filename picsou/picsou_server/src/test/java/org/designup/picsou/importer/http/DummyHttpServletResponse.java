package org.designup.picsou.importer.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

class DummyHttpServletResponse implements HttpServletResponse {
  private OutputStream out = new ByteArrayOutputStream();
  private PrintWriter printWriter = new PrintWriter(out);

  public void addCookie(Cookie cookie) {
  }

  public boolean containsHeader(String string) {
    return false;
  }

  public String encodeURL(String string) {
    return null;
  }

  public String encodeRedirectURL(String string) {
    return null;
  }

  public String encodeUrl(String string) {
    return null;
  }

  public String encodeRedirectUrl(String string) {
    return null;
  }

  public void sendError(int i, String string) throws IOException {
  }

  public void sendError(int i) throws IOException {
  }

  public void sendRedirect(String string) throws IOException {
  }

  public void setDateHeader(String string, long l) {
  }

  public void addDateHeader(String string, long l) {
  }

  public void setHeader(String string, String string1) {
  }

  public void addHeader(String string, String string1) {
  }

  public void setIntHeader(String string, int i) {
  }

  public void addIntHeader(String string, int i) {
  }

  public void setStatus(int i) {
  }

  public void setStatus(int i, String string) {
  }

  public String getCharacterEncoding() {
    return null;
  }

  public String getContentType() {
    return null;
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStream() {
      public void write(int b) throws IOException {
        out.write(b);
      }
    };
  }

  public PrintWriter getWriter() throws IOException {
    return printWriter;
  }

  public void setCharacterEncoding(String string) {
  }

  public void setContentLength(int i) {
  }

  public void setContentType(String string) {
  }

  public void setBufferSize(int i) {
  }

  public int getBufferSize() {
    return 0;
  }

  public void flushBuffer() throws IOException {
    printWriter.flush();
    out.flush();
  }

  public void resetBuffer() {
  }

  public boolean isCommitted() {
    return false;
  }

  public void reset() {
  }

  public void setLocale(Locale locale) {
  }

  public Locale getLocale() {
    return null;
  }
}
