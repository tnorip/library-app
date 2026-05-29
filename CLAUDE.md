# CLAUDE.md — library-app

## プロジェクト

みどり市立図書館(利用者 8 万人 / 蔵書 25 万冊)の図書管理アプリ。monorepo 構成で BE(Spring Boot) + FE(React) を一体開発。個人情報を扱うため**セキュリティ最優先**。

---

## 技術スタック — BE

| 項目 | 採用 | 禁止 |
|---|---|---|
| 言語 | Java 25 | - |
| フレームワーク | Spring Boot 4.0.6 | - |
| ビルド | Gradle 9.5 (Kotlin DSL) | - |
| 永続化 | Spring Data JPA / Hibernate 7.x (H2 in-memory) | - |
| テスト | JUnit 5 (Jupiter) + AssertJ + Mockito | **JUnit 4** |
| JSON | Jackson 3.x (`tools.jackson.*`) | **`com.fasterxml.jackson.databind.*`** |
| Servlet | Tomcat 11 (組み込み) | Undertow |
| namespace | `jakarta.*` | **`javax.*`** |
| DTO / VO | records | **Lombok** |
| Null-safety | JSpecify (`org.jspecify.annotations.*`) | **`org.springframework.lang.Nullable`** |

## 技術スタック — FE

| 項目 | 採用 |
|---|---|
| UI | React 19 |
| ビルド | Vite 6 |
| 言語 | TypeScript 5 (strict) |
| HTTP | fetch (ネイティブ) |
| スタイリング | CSS Modules または plain CSS |

---

## コード生成ルール

- DTO・Value Object は **records を第一選択**
- 依存注入は **コンストラクタ注入**（フィールド `@Autowired` 禁止）
- `@Transactional` は同クラス内呼び出しでは効かない (Spring AOP の制約) — Service 分割で対応
- JPA fetch は基本 `LAZY`、必要なら `@EntityGraph` または fetch join (N+1 回避)
- **`build.gradle.kts` の依存関係は原則追加しない** — Spring Boot 4.0 で starter 名が再編済みのため幻覚リスクが高い
  - 例外: 公式 4.0 ドキュメントで実在確認済みのテストスライスモジュールは追加可。SB4 で `@WebMvcTest`/`@DataJpaTest` は `spring-boot-starter-test` から分離され、`spring-boot-webmvc-test`/`spring-boot-data-jpa-test` の明示追加が必須(雛形に追加済み)
- パターンマッチング (switch expressions / sealed interfaces / record patterns) は積極使用可
- Preview 機能 (Structured Concurrency 等) は `--enable-preview` が必要なため原則使用しない

---

## プロジェクト構造

```text
library-app/
├── backend/src/main/java/training/aidd/library/
│   ├── LibraryApplication.java      ← 変更不要
│   ├── book/                        ← 図書ドメイン (Controller / Service / Repository / Entity)
│   └── reservation/                 ← 予約ドメイン (Lv2以降)
├── backend/src/main/resources/application.properties
├── backend/src/test/java/training/aidd/library/
├── frontend/src/
│   ├── App.tsx
│   ├── api/client.ts
│   └── books/                       ← 図書ドメイン UI
└── .claude/settings.json            ← deny ルール (変更不要)
```

パッケージは層別 (`controller/` `service/`) ではなく**機能ドメイン別** (`book/` `reservation/`) で切る。FE も同様。

---

## テスト方針

- アサーション: **AssertJ** (`assertThat(...).isEqualTo(...)`) — Hamcrest 不可
- モック: **Mockito** (`@Mock` / `@InjectMocks`)
- JUnit 5 アノテーション: `@BeforeEach` / `assertThrows()` / `@ExtendWith()` — JUnit 4 系不可
- 統合テスト: `@SpringBootTest`(コンテキスト起動が重いので必要時のみ)
- 軽量 MVC: `@WebMvcTest` / JPA 単独: `@DataJpaTest`
- FE E2E: Playwright 推奨

---

## 生成コードのセルフチェック

コード生成・編集のたびに確認すること:

- [ ] `com.fasterxml.jackson.databind.*` が混入していないか
- [ ] `javax.*` が混入していないか → `jakarta.*` を使う
- [ ] `org.springframework.lang.Nullable` が混入していないか → JSpecify を使う
- [ ] JUnit 4 系 (`@Before` / `@Test(expected=...)` / `@RunWith`) が混入していないか
- [ ] Hamcrest が混入していないか → AssertJ を使う
- [ ] フィールド注入 (`@Autowired` フィールド直貼り) をしていないか
- [ ] N+1 クエリ・遅延読み込みの罠はないか
- [ ] 例外を握りつぶしていないか (`catch(Exception e) {}`)

---

## AI駆動開発フロー（研修ワーク）

実装に入る前に、以下のフローで要件定義〜設計〜テスト設計を自分で行うこと。
**講師から要件・設計を受け取るのではなく、自分でClaudeを動かすことが目的。**

```text
Step 1  /library-interview       ← 図書館スタッフ・市民・館長にインタビューする
                                    （どんな質問をするかで引き出せる要件が変わる）

Step 2  /requirements-workshop   ← インタビュー結果を整理し要件定義書を作る
                                    （docs/requirements-draft.md に出力）

Step 3  設計（自由プロンプト）   ← docs/requirements-draft.md を渡してClaudeに依頼する
                                    依頼内容（例）:
                                      「要件定義書をもとに以下を含む docs/ARCHITECTURE.md を作って:
                                        - JPA Entityのクラス設計（フィールド・型・リレーション）
                                        - Mermaid記法のER図
                                        - REST APIエンドポイント一覧（メソッド・パス・責務）」
                                    レビューポイント:
                                      - 自分のユーザーストーリーと対応しているか
                                      - 技術制約（records / Jakarta / JSpecify）に沿っているか
                                      - 「なぜこの設計か」を自分の口で説明できるか

Step 4  テスト設計（自由プロンプト） ← docs/ARCHITECTURE.md を渡してClaudeに依頼する
                                    依頼内容（例）:
                                      「設計書をもとに失敗状態（Red）のテストコード雛形を生成して:
                                        - BookServiceTest（Mockito）
                                        - BookControllerTest（@WebMvcTest）」
                                    ポイント: まずテストを書き、実装は後から（TDD）

Step 5  実装（Red → Green → Refactor）
                                    テストを通すコードをClaudeと一緒に書く
                                    1サイクルの流れ:
                                      Red    → テストが失敗することを確認
                                      Green  → 最小限のコードでテストを通す
                                      Refactor → コードを整理（テストは通ったまま）

Step 6  レビュー（自由プロンプト） ← 実装したコードをClaudeにレビューさせる
                                    依頼内容（例）:
                                      「このPRをレビューして。Must fix / Suggestion に分類して」
```

> **ポイント**: `1人1人のインタビュー結果が異なる → 要件が異なる → 設計が異なる → テストが異なる`
> Step 3〜6 はスキルではなく自分のプロンプトで動かす。**「Claudeをどう使うか」がスキルになる。**

---

## 任意レベルの実装方針

Lv の実装を依頼された場合、以下の方針に従うこと。

### Lv1: 検索 / フィルタ

- BE: `/books?q=キーワード` でタイトル・著者の部分一致検索
- FE: 検索フォーム UI + ソート切り替え

### Lv2: 予約システム

- BE: 予約登録・キャンセル・一覧取得。`Book` と `User` の関連エンティティ。重複チェックは Service 層
- FE: 予約操作はモーダル UI

### Lv3: 楽観ロック

- BE: JPA `@Version` フィールド。`OptimisticLockingFailureException` をハンドリング
- FE: コンフリクト発生時にエラーメッセージを表示、再試行を促す UI

### Lv4: 管理ダッシュ

- BE: Stream Gatherers で集計ロジックを記述。集計 API エンドポイントを追加
- FE: React チャートライブラリで可視化 (Chart.js / Recharts 等)
