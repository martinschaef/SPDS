package boomerang.shared.context.targets;

import java.io.File;

public class WrappedInNewStringTarget {

  public static void main(String... args) {

    String x = new String("bar");
    String bar = doPassArgument(x);
    new File(bar);
    ;
  }

  public static String doPassArgument(String param) {
    return param;
  }
}
