## WakeSpeed 1.0.0 浪速

Minecraft 赛车船模组第一个正式版本。把原版木船变成赛车。

> First stable release. Turns a vanilla Minecraft boat into a race car.

**适用版本 / Compatible:** Fabric 26.1.2 / 26.2

---

## 中文

### 仪表
- 三种表盘：**条形表**（拉力 LED 分段）、**圆形表**（指针）、**电表**（功率/电流/电压/转速）
- 涡轮指示：条形表下方白色横条；圆形表轴心上方扇形小圆盘
- 电车模式自动隐藏涡轮指示
- 满转断油抖动（燃油车）、PID 稳定（电车）、怠速游动

### 音效
- 燃油：引擎 / 涡轮 / 泄压阀 / 离合 / 挂挡，**各自独立音量 0–200%**
- 电驱：真实电机录音，0.23 秒拉满、松手 0.1 秒归零、静止全静音
- 一个开关切换油车 / 电车
- 引擎音高补偿

### 赛车视角
- 镜头跟随船头方向、平滑插值，高速过弯不甩
- 默认开启，可关闭，平滑度可调

### 冰面样式
- 冰 / 霜冰 → 橙黄 `#FFBA40`
- 浮冰 → 浅灰
- 蓝冰 → 深灰
- 三态：启用 / 关闭 / 上船自动

### 其他
- 小地图、漂移计时（含烧胎判定）、圈速计时
- 全部 HUD 部件可开关、百分比定位
- **F9 / F10** 统一设置面板

---

## English

### Gauges
- Three dashboards: **bar** (rally LED), **dial** (needle), **EV meter** (kW / A / V / RPM)
- Turbo: white bar under the LED strip, or pie-slice disc above the dial hub
- Turbo hidden automatically in EV mode
- Rev-limiter jitter (gas), PID-stable (EV), idle flutter

### Audio
- Gas: engine / turbo / blow-off / clutch / gear shift, **independent 0–200% volume**
- EV: real motor recording, full spool in 0.23 s, silent 0.1 s after lifting off
- One toggle switches gas ↔ EV
- Engine pitch offset

### Racing camera
- Follows boat heading with smooth interpolation
- On by default, toggleable, adjustable smoothness

### Ice style
- Ice / frosted ice → orange-yellow `#FFBA40`
- Packed ice → light grey
- Blue ice → dark grey
- Three states: on / off / auto-on-boat

### Extras
- Minimap, drift timer with burnout detection, lap timer
- Every HUD element toggleable and positionable
- Unified **F9 / F10** settings screen

---

## 依赖 / Dependencies

| 必需 / Required |
|---|
| Fabric Loader ≥ 0.19.5 |
| Fabric API |
| Cloth Config API |

---

## 安装 / Install

1. 安装 Fabric Loader + Fabric API + Cloth Config
2. 把对应版本的 jar 放进 `.minecraft/mods/`
3. 启动游戏，按 **F9** 打开设置

下载 26.1.2 用 `WakeSpeed-1.0.0-26.1.2.jar`，26.2 用 `WakeSpeed-1.0.0-26.2.jar`。

---

## 授权 / License

**GPL-3.0** — 源码：https://github.com/newbeefish/WakeSpeed

修改自 [BoatHud (NFS)](https://modrinth.com/mod/boathud-nfs)（jewtvet, MIT）与 [BoatCam](https://modrinth.com/mod/boatcam)（Bstn1802 & Pigalala, GPL-3.0）。
音效：公有领域 / CC-BY 3.0，详见仓库 `CREDITS.md`。
