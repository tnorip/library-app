// ============================================================================
//  AIDD Training - Library App 雛形プロジェクト
//  Spring Boot 4.0.6 / Java 25 (LTS) / Gradle 9.5
// ============================================================================
//  研修受講者は本ファイルの「依存関係」セクションを必要に応じて編集します。
//  プラグインのバージョンや toolchain は講師側で動作確認済みのため、
//  AIに「最新化して」と頼んで書き換える前に、まず動かしてください。
// ============================================================================

plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "training.aidd"
version = "0.0.1-SNAPSHOT"
description = "AIDD Training - Library Management Application"

// ----------------------------------------------------------------------------
//  Java toolchain
// ----------------------------------------------------------------------------
//  システム JDK が何であろうと、本プロジェクトは Java 25 でビルド・実行されます。
//  Java 25 が未インストールの場合、settings.gradle.kts の foojay リゾルバが
//  自動的にダウンロードします(初回のみ数分かかります)。
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// ----------------------------------------------------------------------------
//  依存関係
// ----------------------------------------------------------------------------
//  必須:図書CRUDの実装に最低限必要なものだけを列挙しています。
//  任意Lv1〜Lv4 で追加が想定される依存はファイル末尾にコメントで予告しています。
//  AIに依存を追加させる前に、本ファイルのコメントを参照してください。
dependencies {
    // --- Web 層(REST API) ---
    implementation("org.springframework.boot:spring-boot-starter-web")

    // --- Bean Validation(@Valid / @NotBlank などの入力検証) ---
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // --- 永続化(Spring Data JPA + Hibernate) ---
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // --- Actuator(/actuator/health で起動確認に使用) ---
    //  環境構築チェックリスト STEP 5-2 で必須。Day 5 のセキュリティ討議でも
    //  「公開エンドポイントの最小化」の題材に使うため残します。
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // --- 開発体験(自動リロード等。本番ビルドには含まれません) ---
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // --- データベース(研修中は H2 in-memory をデフォルト) ---
    runtimeOnly("com.h2database:h2")

    // --- テスト(JUnit 5 + AssertJ + Mockito を含む) ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ----------------------------------------------------------------------------
//  コンパイラオプション
// ----------------------------------------------------------------------------
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // -parameters: コンストラクタ・メソッド引数名をクラスファイルに残す。
    //   Spring の @RequestParam / @PathVariable で引数名解決に使われます。
    options.compilerArgs.add("-parameters")
    // Preview 機能(Structured Concurrency 等)を使うなら以下を有効化:
    // options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // Preview 機能を使う場合:
    // jvmArgs("--enable-preview")
}

tasks.bootRun {
    // Preview 機能を使う場合:
    // jvmArgs = listOf("--enable-preview")
}

// ============================================================================
//  任意レベル(Lv1〜Lv4)で追加が想定される依存の予告
// ============================================================================
//  Lv1: 予約システム
//      → 標準のJPAだけで実装可能。追加依存は不要。
//
//  Lv2: 楽観ロック
//      → JPA標準の @Version で実装可能。追加依存は不要。
//      → Structured Concurrency を使うなら Preview 機能の有効化が必要
//        (上記 --enable-preview のコメントを外す)。
//
//  Lv3: 貸出期限通知(非同期処理)
//      → Virtual Threads を有効化:
//        application.properties に spring.threads.virtual.enabled=true を追加。
//        追加依存は不要(Java 25 標準機能)。
//      → メール送信を含めるなら:
//        implementation("org.springframework.boot:spring-boot-starter-mail")
//
//  Lv4: 管理者向け利用状況API
//      → Stream Gatherers は Java 25 標準。追加依存は不要。
//
//  Day 2 セキュリティ演習で Spring Security を試す場合:
//      implementation("org.springframework.boot:spring-boot-starter-security")
//      testImplementation("org.springframework.security:spring-security-test")
// ============================================================================
