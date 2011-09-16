package org.designup.picsou.importer.ofx;

import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.FieldValue;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class OfxConnection {
  static OfxConnection connection = new OfxConnection();

  static final DateFormat format = new SimpleDateFormat("yyyyMMdd");

  public static class AccountInfo{
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

  public static void register(OfxConnection connection){
    OfxConnection.connection = connection;
  }
  // for test
  public static OfxConnection getInstance(){
    return connection;
  }


  public List<AccountInfo> getAccounts(String user, String password, final String url,
                                              final String org, final String fid) {
      String accountInfo = getAccountInfo(user, password, url, org, fid);
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
                                   final File outputFile) throws IOException {
    StringWriter stringWriter = new StringWriter();
    OfxWriter writer = new OfxWriter(stringWriter);
    writer.writeLoadOp(fromDate, user, password, org, fid, realAccount.get(RealAccount.BANK_ID),
                       realAccount.get(RealAccount.NUMBER), realAccount.get(RealAccount.ACC_TYPE));
    String request = stringWriter.toString();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    buffer.write(request.getBytes("UTF-8"));
    InputStream inputStream = sendBuffer(new URL(url), buffer);
    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
    Files.copyStream(inputStream, fileOutputStream);
  }

  public String getAccountInfo(String user, String password, final String url, final String org, final String fid) {
    try {
      StringWriter stringWriter = new StringWriter();
      OfxWriter writer = new OfxWriter(stringWriter);
      writer.writeQuery(user, password, org, fid);
      String request = stringWriter.toString();
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      buffer.write(request.getBytes("UTF-8"));
      InputStream inputStream = sendBuffer(new URL(url), buffer);
      String tmp = Files.loadStreamToString(inputStream, "UTF-8");
      if (tmp.contains("<html>")){
        throw new RuntimeException(Lang.get("synchro.ofx.content.invalid"));
      }
      return tmp;
    }
    catch (IOException e) {
      throw new RuntimeException(Lang.get("synchro.ofx.bad.url", url));
    }
  }

  static public InputStream sendBuffer(URL url, ByteArrayOutputStream outBuffer) {
    InputStream in;
    try {
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-ofx");
      connection.setRequestProperty("Content-Length", String.valueOf(outBuffer.size()));
      connection.setRequestProperty("Accept", "*/*, application/x-ofx");
      connection.setDoOutput(true);
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(10000);
      connection.connect();

      OutputStream out = connection.getOutputStream();
      out.write(outBuffer.toByteArray());

      int responseCode = connection.getResponseCode();
      if (responseCode >= 200 && responseCode < 300) {
        in = connection.getInputStream();
      }
      else if (responseCode >= 400 && responseCode < 500) {
        throw new RuntimeException(Lang.get("synchro.ofx.response.error"));
      }
      else {
        throw new RuntimeException(Lang.get("synchro.ofx.response.error"));
      }
    }
    catch (IOException e) {
      Log.write("ofx time out", e);
      throw new RuntimeException(Lang.get("synchro.ofx.timeout"));
    }

    return in;
  }

  public static void main(String[] args) throws IOException {
    Reader reader = new BufferedReader(new InputStreamReader(System.in));
    char[] chars = new char[255];
    int count = reader.read(chars);
    String password = new String(chars, 0, count - 1);
    System.out.println("OfxConnection.main '" + password + "'");
    DefaultGlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    List<AccountInfo> globList = OfxConnection.getInstance().getAccounts("0350763423L", password, "https://ofx.videoposte.com",
                                                  "0", "0");
    System.out.println("OfxConnection.main " + globList);
    for (AccountInfo accountInfo : globList) {
      System.out.println("OfxConnection.main " + accountInfo.number + " " + accountInfo.accType);
      String fromDate;
      fromDate = previousDate(120);
      File outputFile = new File("/tmp/operation.ofx");
      Glob glob = repository.create(RealAccount.TYPE,
                                     FieldValue.value(RealAccount.ACC_TYPE, accountInfo.accType),
                                     FieldValue.value(RealAccount.BANK_ID, accountInfo.bankId),
                                     FieldValue.value(RealAccount.NUMBER, accountInfo.number));
      OfxConnection.getInstance().loadOperation(glob, fromDate, "0350763423L", password,
                                  "https://ofx.videoposte.com", "0", "0", outputFile);
      OfxImporter importer = new OfxImporter();
      GlobList list = importer.loadTransactions(new FileReader(outputFile), repository, repository);
      System.out.println("OfxConnection.main " + list.size());
    }
  }


//  public static void main(String[] args) throws IOException {
//    XMLReader xmlReader = XmlUtils.getXmlReader();
//    final List<InstitutionNode> institutions = new ArrayList<InstitutionNode>();
//    SaxStackParser.parse(xmlReader, new DefaultXmlNode(){
//      public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
//        if (childName.equals("institution")){
//          InstitutionNode node = new InstitutionNode();
//          institutions.add(node);
//          return node;
//        }
//        return this;
//      }
//    }, new File(args[0]));
//    OutputStreamWriter writer = new OutputStreamWriter(System.out);
//    XmlTag tag = XmlWriter.startTag(writer, "globs");
//    XmlTag childTag = tag.createChildTag("bankFormat");
//    childTag.addAttribute("id", 100);
//    for (InstitutionNode institution : institutions) {
//      childTag.createChildTag("bank")
//        .addAttribute("name", institution.name)
//        .addAttribute("downloadUrl", institution.url)
//        .addAttribute("id", institution.id)
//        .addAttribute("fid", institution.fid)
//        .addAttribute("org", institution.org)
//        .addAttribute("ofxDownload", "true")
//        .end();
//    }
//    childTag.end();
//    tag.end();
//    writer.flush();
//  }

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
      if (childName.equals("financialInstitutionId")){
        return new DefaultXmlNode(){
          public void setValue(String value) {
            fid = value;
          }
        };
      }
      if (childName.equals("id")){
        return new DefaultXmlNode(){
          public void setValue(String value) {
            id = value;
          }
        };
      }
      if (childName.equals("name")){
        return new DefaultXmlNode(){
          public void setValue(String value) {
            name = value;
          }
        };
      }
      if (childName.equals("OFXURL")){
        return new DefaultXmlNode(){
          public void setValue(String value) {
            url = value;
          }
        };
      }
      if (childName.equals("organization")){
        return new DefaultXmlNode(){
          public void setValue(String value) {
            org = value;
          }
        };
      }
      return super.getSubNode(childName, xmlAttrs);
    }
  }
}
