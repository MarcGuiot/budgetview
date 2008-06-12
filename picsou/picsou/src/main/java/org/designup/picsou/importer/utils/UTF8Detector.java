package org.designup.picsou.importer.utils;


public class UTF8Detector {

  public static Coder first = FirstByteUTF8Coder.instance;
  public static UNDEF undef = new UNDEF();

  public interface Coder {
    Coder push(int car);
  }

  public static class UNDEF implements Coder {

    public Coder push(int car) {
      return this;
    }
  }

  private static class FirstByteUTF8Coder implements Coder {

    static FirstByteUTF8Coder instance = new FirstByteUTF8Coder();

    public Coder push(int car) {
      if ((car & 0x80) == 0) { // 0xxxxxxx
        return this;
      }
      else if ((car & 0xE0) == 0xC0) {  // 110xxxxx ==> 10xxxxxx
        return OneSecondByteUTF8Code.instance;
      }
      else if ((car & 0xF0) == 0xE0) {  // 1110xxxx ==> 10xxxxxx
        return TwoSecondByteUTF8Code.instance;
      }
      else if ((car & 0xF8) == 0xF0) {  // 11110xxx ==> 10xxxxxx
        return ThreeSecondByteUTF8Code.instance;
      }
      else if ((car & 0xFC) == 0xF8) {  // 111110xx ==> 10xxxxxx
        return FourSecondByteUTF8Code.instance;
      }
      else if ((car & 0xFE) == 0xFC) {  // 111110xx ==> 10xxxxxx
        return FiveSecondByteUTF8Code.instance;
      }
      else {
        return undef;
      }
    }
  }

  static class OneSecondByteUTF8Code implements Coder {
    static Coder instance = new OneSecondByteUTF8Code();

    public Coder push(int car) {
      if ((car & 0xC0) == 0x80) {
        return FirstByteUTF8Coder.instance;
      }
      return undef;
    }
  }

  static class TwoSecondByteUTF8Code implements Coder {
    static Coder instance = new TwoSecondByteUTF8Code();

    public Coder push(int car) {
      if ((car & 0xC0) == 0x80) {
        return OneSecondByteUTF8Code.instance;
      }
      return undef;
    }
  }

  static class ThreeSecondByteUTF8Code implements Coder {
    static Coder instance = new ThreeSecondByteUTF8Code();

    public Coder push(int car) {
      if ((car & 0xC0) == 0x80) {
        return TwoSecondByteUTF8Code.instance;
      }
      return undef;
    }
  }

  static class FourSecondByteUTF8Code implements Coder {
    static Coder instance = new FourSecondByteUTF8Code();

    public Coder push(int car) {
      if ((car & 0xC0) == 0x80) {
        return ThreeSecondByteUTF8Code.instance;
      }
      return undef;
    }
  }

  static class FiveSecondByteUTF8Code implements Coder {
    static Coder instance = new FiveSecondByteUTF8Code();

    public Coder push(int car) {
      if ((car & 0xC0) == 0x80) {
        return ThreeSecondByteUTF8Code.instance;
      }
      return undef;
    }
  }
}

