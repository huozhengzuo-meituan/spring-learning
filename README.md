# Spring 后端研习

为有 Java 基本语法的前端工程师设计：从写接口，进阶到能守住数据一致性、排查故障、交付完整系统。

**[打开课程首页](index.html)** · [48 课完整目录](COURSE.md) · [阶段路线](ROADMAP.md) · [运行示例](examples/README.md)

- 48 课中文 HTML 正文，每课含原理、前端类比、回忆测验、操作练习、答案与官方资料。
- 6 份可打印速查手册；共享样式和离线可用的测验，无 CDN 依赖。
- Java 21 LTS + Spring Boot 4.1.1；两个有中文解释性注释的 Maven 项目。
- 从无外部依赖的 hello-api，到 JPA、Flyway、Security、Redis、Kafka、outbox 综合 task-service。
- [实际验证记录](VERIFICATION.md)区分默认测试、真实中间件、HTTP 与尚未覆盖的生产场景。

## 开始学习

1. 阅读 [环境准备](SETUP.md)。当前机器已准备工作区局部 JDK 21，不改变全局默认 JDK。
2. 双击 `index.html`，或在仓库根目录运行 `python3 -m http.server 8765 --bind 127.0.0.1`，打开 `http://localhost:8765`。固定 HTTP origin 有利于在各课间共享练习进度。
3. 按 01→48 学习；每周 3–4 课是建议节奏。正文与短练习之外，为综合实验和毕业项目额外预留时间。

```bash
# 若使用本工作区已下载的JDK，在仓库根目录执行
source scripts/env.sh
./mvnw -f examples/pom.xml verify
./mvnw -f examples/hello-api/pom.xml spring-boot:run
```

学习时先预测结果、动手验证，再打开参考答案。卡住时直接告诉我课次、你原本的判断、代码或错误，我们会据此调整下一次教学。

## 文件组织

| 路径 | 用途 |
| --- | --- |
| `MISSION.md` / `NOTES.md` | 学习目标、已知背景与偏好 |
| `lessons/` | 48 个完整 HTML 课文 |
| `reference/` | 6 份速查手册 |
| `course/` | 可编辑课程源数据 |
| `assets/` | 共用样式与测验、筛选、进度组件 |
| `examples/` / `compose.yaml` | 可运行代码与本地中间件 |
| `learning-records/` | 经理解或实践证据支持的学习记录 |
| `RESOURCES.md` | 按课程映射的一手资料与可选社区 |

编辑课程源数据后运行 `node scripts/build-course.mjs`，再执行 `node scripts/check-course.mjs`。生成的 HTML 一并保存在仓库，阅读课程无需构建或安装 Node。

“已练习”存储于当前浏览器，可导出 JSON；它不等于掌握，不能自动生成学习记录。所有后续能力判断仍以你的解释和操作证据为准。
