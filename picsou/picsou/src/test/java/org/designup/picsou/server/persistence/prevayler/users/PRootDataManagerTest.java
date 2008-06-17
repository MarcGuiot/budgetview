package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.Encoder;

import java.util.Arrays;

public class PRootDataManagerTest extends FunctionalTestCase {

  public void test() throws Exception {
    PRootDataManager pRootDataManager = new PRootDataManager(url, ServerDirectory.getNewDirectory(url), true);

    Persistence.UserInfo user =
      pRootDataManager.createUserAndHiddenUser("name", false, "pass".getBytes(), "linkgInfo".getBytes(), "crypted".getBytes());

    Glob userGlob = pRootDataManager.getUser("name");
    assertEquals("name", userGlob.get(User.NAME));
    assertTrue(Arrays.equals("pass".getBytes(), userGlob.get(User.ENCRYPTED_PASSWORD)));
    assertTrue(Arrays.equals("linkgInfo".getBytes(), userGlob.get(User.LINK_INFO)));

    Glob hiddenUserGlob = pRootDataManager.getHiddenUser(Encoder.b64Decode("crypted".getBytes()));
    assertEquals(user.userId, hiddenUserGlob.get(HiddenUser.USER_ID));
  }

  public void testReload() throws Exception {
    Directory directory = ServerDirectory.getNewDirectory(url);
    PRootDataManager pRootDataManager = new PRootDataManager(url, directory, false);

    Persistence.UserInfo user =
      pRootDataManager.createUserAndHiddenUser("name", false, "pass".getBytes(), "linkgInfo".getBytes(), "crypted".getBytes());

    PRootDataManager reloaded = new PRootDataManager(url, directory, false);
    Glob userGlob = reloaded.getUser("name");
    assertEquals("name", userGlob.get(User.NAME));
    assertTrue(Arrays.equals("pass".getBytes(), userGlob.get(User.ENCRYPTED_PASSWORD)));
    assertTrue(Arrays.equals("linkgInfo".getBytes(), userGlob.get(User.LINK_INFO)));

    Glob hiddenUserGlob = reloaded.getHiddenUser(Encoder.b64Decode("crypted".getBytes()));
    assertEquals(user.userId, hiddenUserGlob.get(HiddenUser.USER_ID));
  }

  public void testDelete() throws Exception {
    Directory directory = ServerDirectory.getNewDirectory(url);
    PRootDataManager pRootDataManager = new PRootDataManager(url, directory, false);

    byte[] cryptedLinkInfo = "crypted".getBytes();
    pRootDataManager.createUserAndHiddenUser("name", false, "pass".getBytes(), "linkgInfo".getBytes(), cryptedLinkInfo);
    PRootDataManager reloaded = new PRootDataManager(url, directory, false);
    Glob userGlob = reloaded.getUser("name");
    assertEquals("name", userGlob.get(User.NAME));

    reloaded.deleteUser("name", cryptedLinkInfo);
    assertNull(reloaded.getUser("name"));
  }
}
