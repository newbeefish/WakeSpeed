# WakeSpeed 浪速

> **把原版木船变成赛车：赛车风格 HUD、燃油与电驱两套音效、专业赛车视角，一份模组全部搞定。**
>
> *Turn a vanilla boat into a race car: racing-style HUD, gasoline & EV engine audio, and a proper racing camera — all in one mod.*

适用于 **Minecraft 26.1.2 / 26.2（Fabric）** · Fabric 26.1.2 / 26.2

---

## 功能 / Features

### 转速表 / Tachometer（F9 可切换三种样式）

| 样式 | 说明 |
|---|---|
| **条形表（Bar）** | 拉力赛车风格 LED 分段条，绿→黄→红；下方白色涡轮条 + 涡轮转速 |
| **圆形表（Dial）** | 指针式表盘；轴心上方小圆盘用扇形显示涡轮压力 |
| **电表（E-Meter）** | 方形面板：功率 kW / 电流 A / 电压 120V / 电机转速 |

- 满转断油抖动、怠速游动（燃油车）
- 电车模式（PID 闭环）几乎不抖
- EV 模式自动隐藏涡轮指示

### 音效 / Sound

| 音效 | 来源 |
|---|---|
| 引擎循环 | Trigger Rally（CC-BY 3.0） |
| 涡轮啸叫 | 自制合成 |
| 泄压阀 / 离合 / 挂挡 | 自制合成 |
| 漂移胎叫 | Tom Haigh（CC-BY 3.0） |
| **电机（电驱）** | Wikimedia Commons 公有领域录音 |

- 每种音效**独立音量**（0–200%）
- **电车模式开关**：电机声 / 燃油车声一键切换
- 引擎音高补偿

### 赛车视角 / Racing Camera

镜头跟随船头方向、平滑插值，高速过弯更稳。**默认开启**，可在设置里关闭。

### 冰面样式 / Ice Style

把冰/浮冰/蓝冰替换成高对比纯色块，方便辨识赛道：

| 方块 | 颜色 |
|---|---|
| 冰 / 霜冰 | 橙黄 `#FFBA40` |
| 浮冰 | 浅灰 `#A8A8A4` |
| 蓝冰 | 深灰 `#58585E` |

三态：`启用（Yes）` / `关闭（Off）` / `自动（Auto，上船才启用）`

### 其他

- 小地图、漂移计时（含烧胎判定）、圈速计时
- 所有 HUD 部件可独立开关、百分比定位
- **F9 或 F10** 打开统一设置面板（音效 / 界面 两大类）

---

## 安装 / Installation

1. 安装 **Fabric Loader** 与 **Fabric API**
2. 安装 **Cloth Config API**（必需依赖）
3. 把 `WakeSpeed-1.0.0-<mcver>.jar` 放进 `.minecraft/mods/`
4. 启动游戏，按 **F9** 打开设置

### 依赖

| 依赖 | 是否必需 |
|---|---|
| Fabric Loader ≥ 0.19.5 | 必需 |
| Fabric API | 必需 |
| Cloth Config API | 必需 |
| ModMenu | 可选（仅用于在模组列表里显示） |

---

## 构建 / Building

本模组不使用 Loom，直接 `javac` 编译。需要 JDK 25。

```bat
set CP="<minecraft client.jar>;<所有依赖 jar 用分号分隔>"
javac -encoding UTF-8 -d out -cp %CP% src\**\*.java
```

然后把 `out` 与 `res` 的内容按原结构打包成 jar（`res` 内容放在 jar 根目录）。

---

## 配置 / Config

| 文件 | 内容 |
|---|---|
| `config/wakespeed.json` | 音效：音量、EV 模式、音高 |
| `config/wakespeed-hud.json` | 界面：表样式、冰面、抖动、位置 |
| `config/boatcam.json5` | 视角：平滑度、透视模式 |

---

## 授权 / License

本模组遵循 **GNU General Public License v3.0**，详见 [LICENSE](LICENSE)。

第三方代码与素材的来源、作者与授权，详见 [CREDITS.md](CREDITS.md)。

**本模组不包含任何 Minecraft 原版美术资源。** 冰面贴图均为程序化生成的纯色块。

---

## 致谢 / Credits

- **jewtvet** — [BoatHud (NFS)](https://modrinth.com/mod/boathud-nfs)（MIT），HUD 基础
- **Bstn1802 / Pigalala** — [BoatCam](https://modrinth.com/mod/boatcam)（GPL-3.0），赛车视角
- **stephan** — 电机录音（公有领域）
- **Tom Haigh** — 漂移胎叫（CC-BY 3.0）
- **Trigger Rally** — 引擎录音（CC-BY 3.0）

---

## 内置资源包 / Bundled Resource Packs

本模组内置两个资源包，均由**资源包系统**管理，可随时在「选项 → 资源包」中开启或关闭。

| 资源包 | 默认状态 | 说明 |
|---|---|---|
| **F1 2021 Boat** | **开启** | 把木船换成 F1 赛车，含 6 支车队涂装。作者 6GHz，MIT 授权 |
| **Ice as Concrete** | 关闭（可设自动） | 把冰/浮冰/蓝冰染成高对比纯色块 |

> 装了第三方资源包（如 Skyline R34 GTR）会自动覆盖内置包，因为用户资源包优先级更高。