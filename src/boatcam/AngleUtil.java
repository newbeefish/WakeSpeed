package boatcam;

public final class AngleUtil {
   public static float lerp(float t, float a, float b) {
      a = mod(a);
      b = mod(b);
      return a + t * mod(b - a);
   }

   public static float mod(float angle) {
      return angle - (float)Math.floor(angle / 360.0F + 0.5) * 360.0F;
   }
}
