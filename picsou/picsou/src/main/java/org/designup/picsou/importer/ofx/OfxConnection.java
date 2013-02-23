package org.designup.picsou.importer.ofx;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.globsframework.model.FieldValue.value;

public class OfxConnection {
  static OfxConnection connection = new OfxConnection();

  static final DateFormat format = new SimpleDateFormat("yyyyMMdd");


  public static String current() {
    final DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss.000");
    return format.format(new Date());
  }

  public static class AccountInfo {
    public String bankId;
    public String number;
    public String accType;

    public AccountInfo() {
    }

    public AccountInfo(String bankId, String number, String accType) {
      this.bankId = bankId;
      this.number = number;
      this.accType = accType;
    }

    public String toString() {
      return bankId + "/" + number + "/" + accType;
    }
  }

  public static void register(OfxConnection connection) {
    OfxConnection.connection = connection;
  }

  // for test
  public static OfxConnection getInstance() {
    return connection;
  }

  public List<AccountInfo> getAccounts(String user, String password, String date, final String url,
                                       final String org, final String fid, String uuid, final boolean v2) {
    String accountInfo = getAccountInfo(user, password, date, url, org, fid, uuid, v2);
    OfxParser parser = new OfxParser();
    AccountInfoOfxFunctor accountInfoOfxFunctor = new AccountInfoOfxFunctor();
    try {
      parser.parse(new StringReader(accountInfo), accountInfoOfxFunctor);
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    return accountInfoOfxFunctor.getAccounts();
  }

  public void loadOperation(Glob realAccount, String fromDate, String user, String password,
                            final String url, final String org, final String fid,
                            final File outputFile, final String uuid, final boolean v2) throws IOException {
    StringWriter stringWriter = new StringWriter();
    OfxWriter writer = new OfxWriter(stringWriter, v2);
    writer.writeLoadOp(fromDate, OfxConnection.current(), user, password, org, fid, realAccount.get(RealAccount.BANK_ID),
                       realAccount.get(RealAccount.NUMBER), realAccount.get(RealAccount.ACC_TYPE), uuid);
    String request = stringWriter.toString();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    buffer.write(request.getBytes("UTF-8"));
    InputStream inputStream = sendBuffer(new URL(url), buffer);
    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
    Files.copyStream(inputStream, fileOutputStream);
  }

  public String getAccountInfo(String user, String password, String date, final String url, final String org, final String fid, String uuid, final boolean v2) {
    try {
      StringWriter stringWriter = new StringWriter();
      OfxWriter writer = new OfxWriter(stringWriter, v2);
      writer.writeQuery(user, password, date, org, fid, uuid);
      String request = stringWriter.toString();
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      buffer.write(request.getBytes("UTF-8"));
      InputStream inputStream = sendBuffer(new URL(url), buffer);
      String tmp = Files.loadStreamToString(inputStream, "UTF-8");
      if (tmp.contains("<html>")) {
        throw new RuntimeException(Lang.get("synchro.ofx.content.invalid") + " : " + tmp);
      }
      return tmp;
    }
    catch (IOException e) {
      throw new RuntimeException(Lang.get("synchro.ofx.bad.url", url));
    }
  }

  static public InputStream sendBuffer(URL url, ByteArrayOutputStream outBuffer) {

    if (System.getProperty("ofx.use.httpClient", "true").equalsIgnoreCase("true")) {
      return sendWithHttpClient(url, outBuffer);
    }

    InputStream in;
    try {
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-ofx");
      connection.setRequestProperty("Content-Length", String.valueOf(outBuffer.size()));
      connection.setRequestProperty("Accept", "*/*, application/x-ofx");
      connection.setDoOutput(true);
      connection.setConnectTimeout(60000);
      connection.setReadTimeout(60000);
      connection.connect();

      OutputStream out = connection.getOutputStream();
      out.write(outBuffer.toByteArray());

      int responseCode = connection.getResponseCode();
      if (responseCode >= 200 && responseCode < 300) {
        in = connection.getInputStream();
      }
      else if (responseCode >= 400 && responseCode < 500) {
        String message = Lang.get("synchro.ofx.response.error") + connection.getResponseMessage();
        throw new RuntimeException(message);
      }
      else {
        String message = Lang.get("synchro.ofx.response.error") + connection.getResponseMessage();
        throw new RuntimeException(message);
      }
    }
    catch (IOException e) {
      Log.write("ofx time out", e);
      throw new RuntimeException(Lang.get("synchro.ofx.timeout"));
    }
    return in;
  }

  private static InputStream sendWithHttpClient(URL url, ByteArrayOutputStream outBuffer) {
    HttpPost postMethod = null;
    HttpClient client = null;
    try {
      client = new DefaultHttpClient();
      client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
      postMethod = new HttpPost(url.toURI());
      postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
      postMethod.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
      postMethod.setHeader("Content-Type", "application/x-ofx");
//      postMethod.setHeader("Content-Length", String.valueOf(outBuffer.size()));
      postMethod.setHeader("Accept", "*/*, application/x-ofx");
      postMethod.setEntity(new ByteArrayEntity(outBuffer.toByteArray()));
      HttpResponse response = client.execute(postMethod);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= 200 && statusCode < 300) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Files.copyStream(response.getEntity().getContent(), stream);
        return new ByteArrayInputStream(stream.toByteArray());
      }
      else {
        String msg = Lang.get("synchro.ofx.response.error", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        Log.write("for " + url + "  " + msg);
        throw new RuntimeException(msg);
      }
    }
    catch (URISyntaxException e) {
      Log.write("for " + url, e);
      throw new RuntimeException("Unexpected url " + url);
    }
    catch (IOException e) {
      Log.write("for " + url, e);
      throw new RuntimeException("socket closed");
    }
    finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
      if (client != null) {
        client.getConnectionManager().closeExpiredConnections();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    Reader reader = new BufferedReader(new InputStreamReader(System.in));
//    char[] chars = new char[255];
//    int count = reader.read(chars);
//    String password = new String(chars, 0, count - 1);
    String fromDate = previousDate(120);
    System.out.println("OfxConnection.main ");
    String account = "0350763423L";
    String password = "441146";
//    String url = "https://online.americanexpress.com/myca/ofxdl/desktop/desktopDownload.do?request_type=nl_ofxdownload";
//    String org = "AMEX";
//    String fid = "3101";
//    String account = "XXXX";
//    String password = "XXXX";
    String url = "https://ofx.videoposte.com";
    String org = "0";
    String fid = "0";
    String uuid = UUID.randomUUID().toString();
    DefaultGlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    List<AccountInfo> globList = OfxConnection.getInstance().getAccounts(account, password, current(),
                                                                         url, org, fid, uuid, false);
    System.out.println("OfxConnection.main " + globList);
    for (AccountInfo accountInfo : globList) {
      System.out.println("OfxConnection.main " + accountInfo.number + " " + accountInfo.accType);
      File outputFile = new File("/tmp/operation.ofx");
      Glob glob = repository.create(RealAccount.TYPE,
                                    value(RealAccount.ACC_TYPE, accountInfo.accType),
                                    value(RealAccount.BANK_ID, accountInfo.bankId),
                                    value(RealAccount.NUMBER, accountInfo.number),
                                    value(RealAccount.FROM_SYNCHRO, true));
      OfxConnection.getInstance().loadOperation(glob, fromDate, account, password,
                                                url, org, fid, outputFile, uuid, false);
      OfxImporter importer = new OfxImporter();
      GlobList list = importer.loadTransactions(new FileReader(outputFile), repository, repository, null);
      System.out.println("OfxConnection.main " + list.size());
    }
  }

  public static String previousDate(final int dayBefore) {
    String fromDate;
    synchronized (format) {
      Calendar bankCalendar = Calendar.getInstance();
      bankCalendar.add(Calendar.DAY_OF_YEAR, -dayBefore);
      fromDate = format.format(bankCalendar.getTime());
    }
    return fromDate;
  }

  private static class InstitutionNode extends DefaultXmlNode {
    String fid;
    String id;
    String org;
    String name;
    String url;

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
      if (childName.equals("financialInstitutionId")) {
        return new DefaultXmlNode() {
          public void setValue(String value) {
            fid = value;
          }
        };
      }
      if (childName.equals("id")) {
        return new DefaultXmlNode() {
          public void setValue(String value) {
            id = value;
          }
        };
      }
      if (childName.equals("name")) {
        return new DefaultXmlNode() {
          public void setValue(String value) {
            name = value;
          }
        };
      }
      if (childName.equals("OFXURL")) {
        return new DefaultXmlNode() {
          public void setValue(String value) {
            url = value;
          }
        };
      }
      if (childName.equals("organization")) {
        return new DefaultXmlNode() {
          public void setValue(String value) {
            org = value;
          }
        };
      }
      return super.getSubNode(childName, xmlAttrs);
    }
  }
}
