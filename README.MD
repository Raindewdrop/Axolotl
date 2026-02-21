# Axolotl Inject
使用izmk-next base
- JDK 17
- Gradle 8.x
```
Axolotl/
├── Axolotl-loader/    注入器
├── Axolotl-common/    不用管
└── Axolotl-core/      客户端
```

## 构建与运行

### 打包

```bash
gradlew :Axolotl-loader:createExe
```

输出位置: `Axolotl-loader/build/exe`

### 运行客户端 (我改炸了 现在运行runClient0只会启动原版1.20.1 调试单独注入即可)

```bash
gradlew runClient0
```
在游戏运行前启动注入器。
