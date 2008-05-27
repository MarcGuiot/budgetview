package org.designup.picsou.server.persistence.prevayler.users;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.Encoder;
import org.designup.picsou.functests.ServerFunctionalTestCase;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;

import java.util.Arrays;

public class PRootDataManagerTest extends ServerFunctionalTestCase {

  public void test() throws Exception {
    PRootDataManager pRootDataManager = new PRootDataManager(url, ServerDirectory.getNewDirectory(url), true);

    RootDataManager.UserInfo user =
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

    RootDataManager.UserInfo user =
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
