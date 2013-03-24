package com.budgetview.android.datasync;

import android.content.Context;
import android.content.SharedPreferences;
import org.globsframework.utils.Strings;

public class LoginInfo {

  private static String PREFS_ID = "com.budgetview";
  private static String EMAIL_KEY = "email";
  private static String PASSWORD_KEY = "password";

  public final String email;
  public final String password;

  public static LoginInfo load(Context context) {
    SharedPreferences settings = context.getSharedPreferences(PREFS_ID,  Context.MODE_PRIVATE);
    String email = settings.getString(EMAIL_KEY, null);
    String password = settings.getString(PASSWORD_KEY, null);
    return new LoginInfo(email, password);
  }

  public static void save(LoginInfo loginInfo, Context context) {
    SharedPreferences settings = context.getSharedPreferences(PREFS_ID,  Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(EMAIL_KEY, loginInfo.email);
    editor.putString(PASSWORD_KEY, loginInfo.password);
    editor.commit();
  }

  public static void clear(Context context) {
    SharedPreferences settings = context.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.clear();
    editor.commit();
  }

  public LoginInfo(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public boolean isSet() {
    return Strings.isNotEmpty(email) && Strings.isNotEmpty(password);
  }
}
