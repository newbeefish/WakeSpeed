WakeSpeed 浪速 - 来源、授权与修改声明
==========================================

本模组遵循 GNU General Public License v3.0（见 jar 内 LICENSE 文件）。
完整源码：<请在此填写你的仓库地址>

【一、代码来源】

1. HUD（速度表 / 小地图 / 漂移计时 / 圈速）
   原作者: jewtvet
   原模组: BoatHud (NFS Most Wanted) - https://modrinth.com/mod/boathud-nfs
   原授权: MIT（jar 内另附 Unlicense 文本）
   修改: 移植到 Minecraft 26.1.2 / 26.2；重写仪表绘制；新增条状表与电表；
         新增冰面切换、设置面板、圈速计时

2. 赛车视角（Racing camera）
   原作者: Bstn1802, Pigalala
   原模组: BoatCam - https://modrinth.com/mod/boatcam
   原授权: GPL-3.0 - https://github.com/Pigalala/BoatCam
   修改: 移植到 Minecraft 26.1.2 / 26.2；移除 YACL 配置界面，
         改为并入 WakeSpeed 统一设置面板；新增开关

3. 引擎 / 电机音效与设置系统
   本模组新增，GPL-3.0

【二、素材来源】

  motor.ogg（电机）
    Wikimedia Commons "Droning train motor on halt.ogg"
    作者 stephan (pdsounds.org #287)，公有领域
    https://commons.wikimedia.org/wiki/File:Droning_train_motor_on_halt.ogg
    处理: 截取 1.00-2.60s，包络压平 + 60ms 交叉淡化

  drift_skid.ogg（漂移胎叫）
    OpenGameArt "Car tire squeal skid loop"
    作者 audible-edge (Tom Haigh)，CC-BY 3.0
    https://opengameart.org/content/car-tire-squeal-skid-loop

  engine_loop.ogg（燃油引擎）
    Trigger Rally，CC-BY 3.0

  turbo_whine.ogg / clutch.ogg / blowoff.ogg / gear_shift.ogg
    自制合成音

  冰面混凝土贴图（ice / packed_ice / blue_ice / frosted_ice_0-3）
    自制 16x16 程序化生成，不含任何 Mojang 素材

【三、许可证说明】

  本模组整体: GPL-3.0
  第三方素材: 各自授权（公有领域 / CC-BY 3.0），已逐项署名
  注意: GPL-3.0 要求分发时提供完整对应源码，并保留以上署名
【资源包 / Resource Pack】

  F1 2021 Boat（内置于本模组，默认开启）
    作者: 6GHz
    授权: MIT License
    来源: https://modrinth.com/resourcepack/f1-2021-boat
    说明: 原包内未附 LICENSE 文件，此处按 Modrinth 页面标注的 MIT 授权补充声明。
          用户可在 选项 → 资源包 中随时关闭，或改用其他资源包覆盖。
    修改: 仅重写了 pack.mcmeta 的 description 字段，其余文件未改动。

    MIT License
    Copyright (c) 6GHz

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.