# library-app 雛形プロジェクト構成説明

**配布対象**: AI駆動型Java開発研修(2026年5月開講)受講者40名
**役割**: Day 3〜5 のハンズオン(図書CRUD実装+任意Lv1〜4)の出発点

---

## ファイル一覧と役割

### セットアップ・設定

| ファイル | 役割 | 受講者は触る? |
|---|---|---|
| `setup.sh` / `setup.ps1` | 初回セットアップ(ツール確認・BE ビルド・FE npm install) | 触らない |
| `check-setup.sh` / `check-setup.ps1` | 環境自己診断スクリプト | 触らない |
| `CLAUDE.md` | コーディング規約・技術スタック | **書き換える**(Day 3 の主要ワーク) |
| `.claude/settings.json` | Claude Code の deny ルール(隔離の要) | **触らない**(個人カスタマイズは `settings.local.json` へ) |
| `.claude/SETTINGS_GUIDE.md` | settings.json の設計意図と運用ルール | **読む**(隔離の理解に必読) |
| `.gitignore` | IDE/ビルド成果物/秘密情報を除外 | 拡張は可 |

### バックエンド(Spring Boot / BE at root)

| ファイル | 役割 | 受講者は触る? |
|---|---|---|
| `build.gradle.kts` | Spring Boot 4.0.6 / Java 25 toolchain / 依存関係定義 | **触る**(任意Lv で依存追加) |
| `settings.gradle.kts` | foojay 1.0.0 で Java 25 を自動取得 | 触らない |
| `gradle.properties` | 並列ビルド・JVMオプション・プロキシ枠 | プロキシ追記のみ |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.5 を指定 | 触らない |
| `gradlew` / `gradlew.bat` | Gradle 起動スクリプト | 触らない |
| `src/main/java/training/aidd/library/LibraryApplication.java` | Spring Boot エントリーポイント | パッケージ追加で拡張 |
| `src/main/resources/application.properties` | DB・Actuator・ログ設定 | 必要に応じて編集 |
| `src/test/java/.../LibraryApplicationTests.java` | スモークテスト | 拡張可 |

### フロントエンド(React / Vite)

| ファイル | 役割 | 受講者は触る? |
|---|---|---|
| `frontend/package.json` | React 19 / Vite 6 / TypeScript 5 の依存定義 | **触る**(ライブラリ追加時) |
| `frontend/vite.config.ts` | Vite 設定・BE へのプロキシ設定 | **触る**(エンドポイント追加時) |
| `frontend/tsconfig*.json` | TypeScript コンパイラ設定 | 触らない |
| `frontend/index.html` | HTML エントリーポイント | 触らない |
| `frontend/src/main.tsx` | React アプリ起点 | 触らない |
| `frontend/src/App.tsx` | ルートコンポーネント | **触る** |
| `frontend/src/api/client.ts` | fetch ラッパー(GET/POST/PUT/DELETE) | 参照・拡張可 |
| `frontend/src/books/BookList.tsx` | 図書一覧(Day 4 実装課題) | **実装する** |
| `frontend/src/books/BookForm.tsx` | 図書登録フォーム(Day 4 実装課題) | **実装する** |

---

## バージョン採用根拠(2026年5月時点)

| 技術 | 採用バージョン | 採用理由 |
|---|---|---|
| **Spring Boot** | **4.0.6**(2026/4/23 リリース) | Java 25 first-class supportあり / 3.5系はサポート終了が 2026/6/30 と近すぎる / 4.0系は GA 後 6 ヶ月経過し安定パッチが順調にリリース |
| **Gradle** | **9.5**(2026/4/29 リリース) | Java 25 フルサポート(9.1+)/ Spring Boot 4.0 が公式に Gradle 9 をサポート |
| **Java toolchain** | **JavaLanguageVersion.of(25)** | カリキュラム本体が指定する LTS バージョン |
| **foojay-resolver-convention** | **1.0.0** | Gradle 9 互換は 1.0.0 以上が必須(0.x系は `JvmVendorSpec.IBM_SEMERU` 削除で動作不能) |
| **Spring Dependency Management Plugin** | **1.1.7** | Spring Initializr の現行デフォルト |

---

## 依存関係の最小セット(必須CRUD用)

| starter | 用途 |
|---|---|
| `spring-boot-starter-web` | REST API(`@RestController` 等) |
| `spring-boot-starter-validation` | Bean Validation(`@Valid` `@NotBlank`) |
| `spring-boot-starter-data-jpa` | 永続化 + Hibernate |
| `spring-boot-starter-actuator` | `/actuator/health` で起動確認 |
| `spring-boot-devtools`(developmentOnly) | 自動リロード |
| `h2`(runtimeOnly) | in-memory DB |
| `spring-boot-starter-test`(testImplementation) | JUnit 5 + AssertJ + Mockito |

> Spring Boot 4.0 では starter 名は基本的に維持されています(`-web` `-data-jpa` 等)。内部はモジュラー化されていますが、研修レベルでは意識する必要はありません。

---

## 任意Lv1〜Lv4 で追加が想定される依存

`build.gradle.kts` のファイル末尾コメントに予告済み:

| Lv | 機能 | 必要な追加 |
|---|---|---|
| **Lv1** | 予約システム | **追加なし**(JPA標準) |
| **Lv2** | 楽観ロック | **追加なし**(`@Version`)/ Structured Concurrency を使うなら `--enable-preview` |
| **Lv3** | 貸出期限通知(非同期) | `application.properties` に `spring.threads.virtual.enabled=true` / メール送信を含めるなら `spring-boot-starter-mail` |
| **Lv4** | 管理者向け利用状況API | **追加なし**(Stream Gatherers は Java 25 標準) |

---

## `check-setup.sh` の最低構成チェックとの対応

`check-setup.sh` は以下を検証する設計になっており、本雛形はそれをすべて通過します。

| check-setup.sh の項目 | 雛形での対応 |
|---|---|
| `build.gradle.kts` の存在(プロジェクトルート判定) | ✅ ルート直下に配置 |
| `.claude/settings.json` の存在 + deny ルール | ✅ **確定済み**(deny/ask/allow の3層構成、`SETTINGS_GUIDE.md` 参照) |
| `CLAUDE.md` の存在 + 必須キーワード | ✅ **確定済み**(`Java 25` `Spring Boot` `jakarta` `JUnit 5` を含む) |
| `gradlew` の実行権限 | ✅ 実バイナリ別途同梱(`chmod +x`) |
| `JVM: 25.x.x`(`./gradlew --version`) | ✅ toolchain で 25 を指定、未インストール時は foojay が自動取得 |
| ビルド成果物の存在 | ✅ STEP 5-1 で `./gradlew build` 実行を指示 |
| `claude` コマンドの存在 | 受講者の前提環境(チェックリスト前提条件) |
| `git` の存在 | 受講者の前提環境 |

---

## 配布前に講師が行う作業

1. `gradlew` `gradlew.bat` `gradle-wrapper.jar` を Gradle 9.5 で生成して同梱
   - 任意のJava環境で `gradle wrapper --gradle-version 9.5` を実行
2. `setup.sh` `check-setup.sh` に実行権限を付与(`chmod +x setup.sh check-setup.sh`)
3. **動作確認: `./setup.sh` を実行し、BE ビルドと FE npm install が成功することを確認**
4. **動作確認: `./check-setup.sh` を実行し、すべて `[OK]` になることを確認**
5. **隔離テスト: Claude Code を起動し `~/.ssh` の一覧を依頼 → 拒否されることを確認**
6. リポジトリを Push し、URL を Slack `#general` で配布

> **Windows 受講者向け**: `setup.ps1` / `check-setup.ps1` を使用。初回のみ実行ポリシー設定が必要:
> `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

---

## ライセンスと注意

本雛形は研修目的での内部利用を想定しています。本番環境では以下を必ず変更してください:

- `spring.jpa.hibernate.ddl-auto=update` → `validate` または `none`
- `spring.h2.console.enabled=true` → `false`(または依存自体を削除)
- H2 in-memory → 本番DB(PostgreSQL等)
- Actuator のエンドポイント公開設定の見直し