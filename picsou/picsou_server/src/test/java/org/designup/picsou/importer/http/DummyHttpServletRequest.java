package org.designup.picsou.importer.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

class DummyHttpServletRequest implements HttpServletRequest {
  private byte[] bytes;
  private Long sessionId;

  public DummyHttpServletRequest(byte[] bytes, long sessionId) {
    this.sessionId = sessionId;
    this.bytes = bytes;
  }

  public String getAuthType() {
    return null;
  }

  public Cookie[] getCookies() {
    return new Cookie[0];
  }

  public long getDateHeader(String string) {
    return 0;
  }

  public String getHeader(String string) {
    return sessionId.toString();
  }

  public Enumeration getHeaders(String string) {
    return null;
  }

  public Enumeration getHeaderNames() {
    return null;
  }

  public int getIntHeader(String string) {
    return 0;
  }

  public String getMethod() {
    return null;
  }

  public String getPathInfo() {
    return null;
  }

  public String getPathTranslated() {
    return null;
  }

  public String getContextPath() {
    return null;
  }

  public String getQueryString() {
    return null;
  }

  public String getRemoteUser() {
    return null;
  }

  public boolean isUserInRole(String string) {
    return false;
  }

  public Principal getUserPrincipal() {
    return null;
  }

  public String getRequestedSessionId() {
    return null;
  }

  public String getRequestURI() {
    return null;
  }

  public StringBuffer getRequestURL() {
    return null;
  }

  public String getServletPath() {
    return null;
  }

  public HttpSession getSession(boolean b) {
    return null;
  }

  public HttpSession getSession() {
    return null;
  }

  public boolean isRequestedSessionIdValid() {
    return false;
  }

  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  public Object getAttribute(String string) {
    return null;
  }

  public Enumeration getAttributeNames() {
    return null;
  }

  public String getCharacterEncoding() {
    return null;
  }

  public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
  }

  public int getContentLength() {
    return 0;
  }

  public String getContentType() {
    return null;
  }

  public ServletInputStream getInputStream() throws IOException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    return new DummyServletInputStream(byteArrayInputStream);
  }

  public String getParameter(String string) {
    return null;
  }

  public Enumeration getParameterNames() {
    return null;
  }

  public String[] getParameterValues(String string) {
    return new String[0];
  }

  public Map getParameterMap() {
    return null;
  }

  public String getProtocol() {
    return null;
  }

  public String getScheme() {
    return null;
  }

  public String getServerName() {
    return null;
  }

  public int getServerPort() {
    return 0;
  }

  public BufferedReader getReader() throws IOException {
    return null;
  }

  public String getRemoteAddr() {
    return null;
  }

  public String getRemoteHost() {
    return null;
  }

  public void setAttribute(String string, Object object) {
  }

  public void removeAttribute(String string) {
  }

  public Locale getLocale() {
    return null;
  }

  public Enumeration getLocales() {
    return null;
  }

  public boolean isSecure() {
    return false;
  }

  public RequestDispatcher getRequestDispatcher(String string) {
    return null;
  }

  public String getRealPath(String string) {
    return null;
  }

  public int getRemotePort() {
    return 0;
  }

  public String getLocalName() {
    return null;
  }

  public String getLocalAddr() {
    return null;
  }

  public int getLocalPort() {
    return 0;
  }

  private static class DummyServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream byteArrayInputStream;

    public DummyServletInputStream(ByteArrayInputStream byteArrayInputStream) {
      this.byteArrayInputStream = byteArrayInputStream;
    }

    public int read() throws IOException {
      return byteArrayInputStream.read();
    }

  }
}
