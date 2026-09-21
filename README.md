# Player Interact 玩家交互

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-green.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.19.5-blue.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

复刻光遇经典玩法，支持 **跟随 / 背起 / 骑行** 玩家互动。按键可在游戏内自定义绑定，Shift 一键脱离交互。

## 功能

| 动作 | 默认按键 | 效果 |
|---|---|---|
| 跟随 | `G` | 发起者被他人跟随，1.5 格绳索约束，自动爬坡 |
| 背起 | `H` | 被背起玩家骑在发起者头顶 |
| 骑行 | `J` | 被骑行玩家骑在发起者背上，发起者强制趴下，可跳跃 |
| 脱离 | `X` | 解除当前交互状态 |
| 趴下 | `C` | 本地匍匐状态 |

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/) 0.19.5+
2. 下载 [Fabric API](https://modrinth.com/mod/fabric-api) 0.160.0+26.2+
3. 把本模组放入 `mods/` 文件夹

## 编译

```bash
./gradlew build
