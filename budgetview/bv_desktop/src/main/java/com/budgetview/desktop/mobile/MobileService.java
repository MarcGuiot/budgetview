package com.budgetview.desktop.mobile;

import com.budgetview.client.ConnectionStatus;
import com.budgetview.shared.encryption.MD5PasswordBasedEncryptor;
import com.budgetview.shared.encryption.PasswordEncryption;
import com.budgetview.shared.http.Http;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import com.budgetview.shared.mobile.model.MobileModel;
import com.budgetview.utils.Lang;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Ref;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MobileService {

  public boolean createMobileAccount(String mail, String password, Ref<String> message, GlobRepository repository) {
    Http.Post postRequest = null;
    try {
      postRequest = createPostMessage(mail, password, MobileConstants.getServerUrl(MobileConstants.SEND_MAIL_TO_CONFIRM_MOBILE));
      HttpResponse response = postRequest.execute();
      ConnectionStatus.setOk(repository);
      if (response.getStatusLine().getStatusCode() != 200) {
        message.set(Lang.get("mobile.user.connection.failed"));
        return false;
      }
      Header isValid = response.getFirstHeader(LicenseConstants.HEADER_IS_VALID);
      if (isValid != null && isValid.getValue().equalsIgnoreCase("true")) {
        message.set(Lang.get("mobile.user.create.mail.sent"));
        return true;
      }
      message.set(Lang.get("mobile.user.create.already.exist"));
      return false;
    }
    catch (Exception e) {
      Log.write("[Mobile] Error creating mobile account", e);
      ConnectionStatus.checkException(repository, e);
    }
    finally {
      if (postRequest != null) {
        postRequest.dispose();
      }
    }
    message.set(Lang.get("mobile.user.connection.failed"));
    return false;
  }

  private Http.Post createPostMessage(String mail, String password, final String url) throws UnsupportedEncodingException {

    MD5PasswordBasedEncryptor encryptor =
      new MD5PasswordBasedEncryptor(MobileConstants.SALT.getBytes(), LicenseConstants.SOME_PASSWORD.toCharArray(), 5);

    byte[] localKey = Base64.encodeBase64(encryptor.encrypt(mail.getBytes("UTF-8")));

    MD5PasswordBasedEncryptor userEncryptor =
      new MD5PasswordBasedEncryptor(MobileConstants.SALT.getBytes(), password.toCharArray(), 5);
    byte[] encryptedMail = userEncryptor.encrypt(mail.getBytes("UTF-8"));
    String sha1Mail = PasswordEncryption.encodeSHA1AndHex(encryptedMail);

    return Http.utf8Post(url)
      .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"))
      .setHeader(LicenseConstants.HEADER_MAIL_FROM, URLEncoder.encode(mail, "UTF-8"))
      .setHeader(LicenseConstants.CODING, URLEncoder.encode(new String(localKey), "UTF-8"))
      .setHeader(MobileConstants.CRYPTED_INFO, URLEncoder.encode(sha1Mail, "UTF-8"));
  }

  public boolean deleteMobileAccount(String mail, String password, Ref<String> message, GlobRepository repository) {
    Http.Post postRequest = null;
    try {
      postRequest = createPostMessage(mail, password, MobileConstants.getServerUrl(MobileConstants.DELETE_MOBILE_ACCOUNT));
      HttpResponse response = postRequest.execute();
      ConnectionStatus.setOk(repository);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpServletResponse.SC_FORBIDDEN) {
        message.set(Lang.get("mobile.user.delete.invalid.password"));
        return false;
      }
      if (statusCode != 200) {
        message.set(Lang.get("mobile.user.connection.failed"));
        return false;
      }
      return true;
    }
    catch (Exception e) {
      Log.write("[Mobile] Error deleting mobile account", e);
      ConnectionStatus.checkException(repository, e);
    }
    finally {
      if (postRequest != null) {
        postRequest.dispose();
      }
    }
    message.set(Lang.get("mobile.user.connection.failed"));
    return false;
  }

  public synchronized boolean sendMobileData(String mail, String password, byte[] bytes, Ref<String> message, boolean pending, GlobRepository repository) {
    Http.Post postRequest = Http.utf8Post(MobileConstants.getServerUrl(MobileConstants.POST_MOBILE_DATA));
    try {
      MD5PasswordBasedEncryptor encryptor =
        new MD5PasswordBasedEncryptor(MobileConstants.SALT.getBytes(), password.toCharArray(), 5);

      byte[] data = encryptor.encrypt(bytes);
      byte[] encryptedMail = encryptor.encrypt(mail.getBytes("UTF-8"));
      String sha1Mail = PasswordEncryption.encodeSHA1AndHex(encryptedMail);
      postRequest
        .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"))
        .setHeader(LicenseConstants.HEADER_MAIL_FROM, URLEncoder.encode(mail, "UTF-8"))
        .setHeader(MobileConstants.CRYPTED_INFO, URLEncoder.encode(sha1Mail, "UTF-8"))
        .setHeader(MobileConstants.MAJOR_VERSION_NAME, Integer.toString(MobileModel.MAJOR_VERSION))
        .setHeader(MobileConstants.MINOR_VERSION_NAME, Integer.toString(MobileModel.MINOR_VERSION));
      if (pending) {
        postRequest.setHeader(LicenseConstants.HEADER_PENDING, "true");
      }
      else {
        postRequest.setHeader(LicenseConstants.HEADER_PENDING, "false");
      }
      postRequest.setEntity(new ByteArrayEntity(data));
      HttpResponse response = postRequest.execute();
      ConnectionStatus.setOk(repository);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpServletResponse.SC_FORBIDDEN) {
        Header configVersionHeader = response.getFirstHeader(MobileConstants.STATUS);
        message.set(configVersionHeader.getValue());
        return false;
      }
      return true;
    }
    catch (Exception e) {
      Log.write("[Mobile] Error while sending data", e);
      message.set(e.getMessage());
      ConnectionStatus.checkException(repository, e);
      return false;
    }
    finally {
      postRequest.dispose();
    }
  }

}
