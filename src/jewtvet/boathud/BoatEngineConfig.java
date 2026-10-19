package jewtvet.boathud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;

/**
 * BoatEngine 配置。
 * 音量以百分比表示：0 = 静音，100 = 原始，200 = 两倍。
 * 驱动形式由 ev_mode 一个开关切换：油车 / 电车。
 */
@Config(name = "wakespeed")
public class BoatEngineConfig implements ConfigData {
   /** 电车模式：引擎声换成电机声，并关闭涡轮/泄压阀/离合/挂挡/换挡掉转速 */
   public boolean ev_mode = false;

   @BoundedDiscrete(min = 0L, max = 200L) public int volume_engine = 100;
   @BoundedDiscrete(min = 0L, max = 200L) public int volume_turbo = 100;
   @BoundedDiscrete(min = 0L, max = 200L) public int volume_blowoff = 100;
   @BoundedDiscrete(min = 0L, max = 200L) public int volume_clutch = 100;
   @BoundedDiscrete(min = 0L, max = 200L) public int volume_gear = 100;
   @BoundedDiscrete(min = 0L, max = 200L) public int volume_drift = 100;

   public boolean mute_vanilla_boat = true;

   @BoundedDiscrete(min = 50L, max = 150L) public int pitch_engine = 100;

   public float engineGain()  { return volume_engine / 100.0F; }
   public float turboGain()   { return volume_turbo / 100.0F; }
   public float blowoffGain() { return volume_blowoff / 100.0F; }
   public float clutchGain()  { return volume_clutch / 100.0F; }
   public float gearGain()    { return volume_gear / 100.0F; }
   public float driftGain()   { return volume_drift / 100.0F; }
   public float enginePitch() { return pitch_engine / 100.0F; }
}