package com.budgetview.functests.checkers.mobile;

import com.budgetview.shared.model.MobileModel;
import com.budgetview.shared.utils.MobileConstants;
import com.budgetview.shared.utils.Crypt;
import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.globsframework.model.GlobList;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.xml.XmlGlobParser;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class MobileAppChecker {
  private final int httpPort;

  public MobileAppChecker(int httpPort) {
    this.httpPort = httpPort;
  }

  public void checkLogin(String email, String password) throws Exception {

    Crypt encryptedPassword = new Crypt(password.toCharArray(), MobileConstants.SALT);
    HttpResponse response = getHttpResponse(email, encryptedPassword);
    Assert.assertEquals(200, response.getStatusLine().getStatusCode());

    InputStream content = response.getEntity().getContent();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Files.copyStream(content, stream);
    String fileAsText = encryptedPassword.decodeAndUnzipData(stream.toByteArray());

    DefaultGlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    XmlGlobParser.parse(MobileModel.get(), repository, new StringReader(fileAsText), "globs");
    GlobList all = repository.getAll();
    Assert.assertFalse(all.isEmpty());
    content.close();
  }

  public void checkLoginFails(String mail, String newPassword) throws Exception {
    Crypt encryptedPassword = new Crypt(newPassword.toCharArray(), MobileConstants.SALT);
    HttpResponse response = getHttpResponse(mail, encryptedPassword);
    Assert.assertEquals(403, response.getStatusLine().getStatusCode());
  }

  private HttpResponse getHttpResponse(String email, Crypt crypt) throws URISyntaxException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
    StringBuilder params = new StringBuilder();
    params.append(MobileConstants.MAIL).append("=").append(URLEncoder.encode(email, "UTF-8"));
    String data = Crypt.encodeSHA1AndHex(crypt.encodeData(email.getBytes("UTF-8")));
    params.append("&").append(MobileConstants.CRYPTED_INFO).append("=").append(URLEncoder.encode(data, "UTF-8"));

    HttpGet method = new HttpGet("http://localhost:" + httpPort + MobileConstants.GET_MOBILE_DATA + "?" + params);
    HttpClient httpClient = new DefaultHttpClient();
    return httpClient.execute(method);
  }

  public void sendEmail(String emailAddress) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    URIBuilder builder = new URIBuilder("http://localhost:" + httpPort + MobileConstants.SEND_MAIL_REMINDER_FROM_MOBILE);
    builder.addParameter("mail", URLEncoder.encode(emailAddress, "UTF-8"));
    HttpPost method = new HttpPost(builder.build());
    HttpResponse response = httpClient.execute(method);
    Assert.assertEquals(200, response.getStatusLine().getStatusCode());
  }
}
