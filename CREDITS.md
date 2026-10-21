WakeSpeed 浪速 - 来源、授权与修改声明
==========================================

本模组遵循 GNU General Public License v3.0（见 jar 内 LICENSE 文件）。
完整源码：https://github.com/newbeefish/WakeSpeed

【一、代码来源】

1. HUD（速度表 / 小地图 / 漂移计时 / 圈速）
   原作者: jewtvet
   原模组: BoatHud (NFS Most Wanted) - https://modrinth.com/mod/boathud-nfs
   原授权: MIT（jar 内另附 Unlicense 文本）
   修改: 移植到 Minecraft 26.1.2 / 26.2；重写仪表绘制；新增条状表与电表；
         新增冰面切换、设置面板、圈速计时、手动挡与离合系统

2. 赛车视角（Racing camera）
   原作者: Bstn1802, Pigalala
   原模组: BoatCam - https://modrinth.com/mod/boatcam
   原授权: GPL-3.0 - https://github.com/Pigalala/BoatCam
   修改: 移植到 Minecraft 26.1.2 / 26.2；移除 YACL 配置界面，
         改为并入 WakeSpeed 统一设置面板；新增开关

3. 挡位系统、音效系统、设置面板
   本模组新增，GPL-3.0

【二、音频素材来源】

  engine_loop.ogg（燃油引擎）
    Trigger Rally 项目引擎录音（qubodup 处理）
    基础录音: freesound.org qubodup #147243，CC-BY 3.0
    引擎音色处理: Trigger Rally (GPL-2.0+)，https://trigger-rally.sourceforge.net
    授权: CC-BY 3.0
    处理: 保留 Trigger Rally 的游戏化调音（增强谐波与压缩）

  motor.ogg（电机，真实录音）
    来源: Wikimedia Commons "Droning train motor on halt.ogg"
    作者: stephan (pdsounds.org #287)
    授权: 公有领域（Public Domain）
    https://commons.wikimedia.org/wiki/File:Droning_train_motor_on_halt.ogg
    处理: 截取 1.00-2.60s，包络压平 + 60ms 交叉淡化

  drift_skid.ogg（漂移胎叫，真实录音）
    来源: OpenGameArt "Car tire squeal skid loop"
    作者: audible-edge (Tom Haigh)，qubodup 上传
    授权: CC-BY 3.0
    https://opengameart.org/content/car-tire-squeal-skid-loop

  turbo_whine.ogg（涡轮啸叫）
    自制合成音（正弦扫频 + 谐波 + 气流动噪声）

  clutch.ogg（离合）
    自制合成音（等待替换为真实录音）

  blowoff.ogg（泄压阀 / 排气爆鸣）
    自制合成音（等待替换为真实录音）

  gear_shift.ogg（挂挡）
    自制合成音（等待替换为真实录音）

【三、贴图素材】

  冰面纯色块（ice / packed_ice / blue_ice / frosted_ice_0-3）
    自制 16x16 程序化生成纯色，不含任何 Mojang 素材

  模组图标 icon.png
    自制程序化绘制

【四、许可证说明】

  本模组整体: GPL-3.0
  第三方素材: 各自授权（公有领域 / CC-BY 3.0），已逐项署名
  注意: GPL-3.0 要求分发时提供完整对应源码，并保留以上署名