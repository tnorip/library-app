# アーキテクチャ設計書 — みどり市立図書館管理システム

> Must have（US-003, 005, 006, 007, 013, 014, 015）のみを対象とする

---

## ER図（Mermaid）

```mermaid
erDiagram

    STAFF {
        Long id PK
        String staffNumber UK "職員番号"
        String name
        String email UK
        String passwordHash
        StaffRole role "GENERAL / CHIEF / DIRECTOR"
        Boolean active "有効フラグ"
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    MEMBER {
        Long id PK
        String memberNumber UK "利用者番号"
        String name
        String email
        String phone
        Boolean active "退会フラグ"
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    BOOK {
        Long id PK
        String isbn
        String title
        String author
        String publisher
        Integer publishedYear
        String callNumber "日本十進分類番号"
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    BOOK_COPY {
        Long id PK
        Long bookId FK
        String copyCode UK "個別管理コード（例: ISBN-001）"
        CopyStatus status "AVAILABLE / CHECKED_OUT / REPAIR / DISCARDED"
        LocalDate discardedAt
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    LOAN {
        Long id PK
        Long bookCopyId FK
        Long memberId FK
        Long staffId FK "貸出処理を行った職員"
        LocalDate loanDate
        LocalDate dueDate
        LocalDate returnedDate "NULL = 貸出中"
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    LOAN_RULE {
        Long id PK
        Integer maxLoanCount "貸出上限冊数（デフォルト10）"
        Integer loanPeriodDays "貸出期間日数（デフォルト14）"
        LocalDateTime updatedAt
        Long updatedByStaffId FK "変更した職員"
    }

    BOOK ||--o{ BOOK_COPY : "1冊の書誌に複数の現物"
    BOOK_COPY ||--o{ LOAN : "1現物に複数の貸出履歴"
    MEMBER ||--o{ LOAN : "1利用者に複数の貸出"
    STAFF ||--o{ LOAN : "職員が貸出処理"
    STAFF ||--o| LOAN_RULE : "主任以上が貸出ルールを変更"
```

---

## クラス図 — Book / BookService / BookController

```mermaid
classDiagram

    %% ─────────────────────────────
    %% Layer: Presentation
    %% ─────────────────────────────
    class BookController {
        -BookService bookService
        +BookController(BookService)
        +listBooks(q, callNumber) List~BookResponse~
        +getBook(id) BookResponse
        +createBook(BookRequest) BookResponse
        +updateBook(id, BookRequest) BookResponse
        +deleteBook(id) void
        +listCopies(bookId) List~BookCopyResponse~
        +addCopy(bookId, BookCopyRequest) BookCopyResponse
        +updateCopyStatus(bookId, copyId, status) BookCopyResponse
        +deleteCopy(bookId, copyId) void
    }

    %% ─────────────────────────────
    %% Layer: Application Logic
    %% ─────────────────────────────
    class BookService {
        <<Application Logic>>
        -BookRepository bookRepository
        -BookCopyRepository bookCopyRepository
        +BookService(BookRepository, BookCopyRepository)

        %% [Application Logic] リクエスト→Entity変換・永続化・レスポンス組み立て
        +findAll(q, callNumber) List~BookResponse~
        +findById(id) BookResponse
        +create(BookRequest) BookResponse
        +update(id, BookRequest) BookResponse
        +delete(id) void
        +findCopies(bookId) List~BookCopyResponse~
        +addCopy(bookId, BookCopyRequest) BookCopyResponse
        +updateCopyStatus(bookId, copyId, CopyStatus) BookCopyResponse
        +deleteCopy(bookId, copyId) void

        %% [Business Logic] ドメインルールの検証
        -validateNoCopiesExist(bookId) void
        -validateCopyNotCheckedOut(BookCopy) void
    }

    note for BookService "【Application Logic / ここまで】\n- DTO ↔ Entity 変換\n- Repository 呼び出し・トランザクション管理\n- レスポンスの組み立て\n\n【Business Logic / ここから】\n- 書誌削除時: 現物が存在していたらNG\n- 現物削除時: 貸出中だったらNG\n- ステータス遷移の妥当性チェック\n  例) CHECKED_OUT → AVAILABLE は貸出処理経由のみ許可"

    %% ─────────────────────────────
    %% Layer: Domain
    %% ─────────────────────────────
    class Book {
        +Long id
        +String isbn
        +String title
        +String author
        +String publisher
        +Integer publishedYear
        +String callNumber
        +List~BookCopy~ copies
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class BookCopy {
        +Long id
        +Book book
        +String copyCode
        +CopyStatus status
        +LocalDate discardedAt
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class CopyStatus {
        <<enumeration>>
        AVAILABLE
        CHECKED_OUT
        REPAIR
        DISCARDED
    }

    %% ─────────────────────────────
    %% Layer: Infrastructure
    %% ─────────────────────────────
    class BookRepository {
        <<interface>>
        +findByTitleContainingOrAuthorContaining(t, a) List~Book~
        +findByCallNumberStartingWith(callNumber) List~Book~
    }

    class BookCopyRepository {
        <<interface>>
        +findByBookId(bookId) List~BookCopy~
    }

    %% ─────────────────────────────
    %% DTO
    %% ─────────────────────────────
    class BookRequest {
        <<record>>
        +String isbn
        +String title
        +String author
        +String publisher
        +Integer publishedYear
        +String callNumber
    }

    class BookResponse {
        <<record>>
        +Long id
        +String isbn
        +String title
        +String author
        +String publisher
        +Integer publishedYear
        +String callNumber
        +List~BookCopyResponse~ copies
    }

    class BookCopyRequest {
        <<record>>
        +String copyCode
    }

    class BookCopyResponse {
        <<record>>
        +Long id
        +String copyCode
        +CopyStatus status
    }

    %% ─────────────────────────────
    %% Relations
    %% ─────────────────────────────
    BookController --> BookService : uses
    BookController ..> BookRequest : receives
    BookController ..> BookResponse : returns

    BookService --> BookRepository : uses
    BookService --> BookCopyRepository : uses
    BookService ..> BookRequest : reads
    BookService ..> BookResponse : builds
    BookService ..> BookCopyRequest : reads
    BookService ..> BookCopyResponse : builds

    BookRepository --> Book : manages
    BookCopyRepository --> BookCopy : manages
    Book "1" --> "0..*" BookCopy : has
    BookCopy --> CopyStatus : has
```

---

## 責務分担まとめ

| クラス | 責務 | 責務の境界 |
|-------|------|-----------|
| `BookController` | HTTPリクエストの受付・レスポンス返却 | **URLマッピング・HTTPステータス管理のみ**。ビジネス判断は一切しない |
| `BookService` | アプリケーションロジック + ビジネスロジック | 下表参照 |
| `Book` / `BookCopy` | データ構造の定義（JPA Entity） | 状態を持つだけ。振る舞いはServiceに委ねる |
| `BookRequest/Response` | データ転送オブジェクト（record） | Entityを外部に直接露出しない |

### BookService の内部境界

```
┌─────────────────────────────────────────────────┐
│                  BookService                    │
│                                                 │
│  【Application Logic】                          │
│  ・DTO → Entity 変換                            │
│  ・Repository の呼び出し                        │
│  ・@Transactional によるトランザクション管理     │
│  ・Entity → ResponseDTO への組み立て            │
│                                                 │
│  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─   │
│                                                 │
│  【Business Logic】                             │
│  ・書誌削除時: 現物が1冊以上あれば削除禁止      │
│  ・現物削除時: 貸出中（CHECKED_OUT）なら禁止    │
│  ・ステータス遷移ルールの検証                   │
│    CHECKED_OUT → AVAILABLE は                  │
│    貸出処理（LoanService）経由のみ許可          │
└─────────────────────────────────────────────────┘
```

> **なぜ Business Logic を Service に置くか**
> Entityはデータ構造の定義に留め、ドメインルールはServiceに集約することで、
> テスト（Mockito）でRepositoryをモック化しながらビジネスルールだけを検証できる。

---

## エンティティ設計の補足

### BOOK と BOOK_COPY の分離について

- `BOOK`：ISBN・タイトル・著者など「書誌情報」を表す。同じ本が複数冊あっても1レコード。
- `BOOK_COPY`：物理的な1冊を表す。ステータス（在庫／貸出中／修繕中／廃棄）は冊ごとに管理。
- これにより US-006「同一ISBNで複数冊を管理できる」を満たす。

### LOAN の返却判定について

- `returnedDate` が `NULL` → 貸出中
- `returnedDate` に日付あり → 返却済み
- 返却処理時に `BOOK_COPY.status` を `AVAILABLE` に更新する。

### LOAN_RULE について

- システム全体で1レコードのみ保持するシングルトン設定テーブル。
- 変更操作は `STAFF.role = CHIEF or DIRECTOR` のみ許可（US-015, US-007）。

---

## REST APIエンドポイント一覧（図書CRUD）

> 対象: `BOOK`（書誌情報）および `BOOK_COPY`（現物管理）

### 書誌情報 `/api/books`

| メソッド | パス | 責務 | 権限 |
|---------|------|------|------|
| `GET` | `/api/books` | 書誌一覧取得。`q`（タイトル・著者部分一致）、`callNumber`（分類番号）でフィルタ可 | 全職員 |
| `GET` | `/api/books/{id}` | 書誌1件取得。紐づく現物（`BookCopy`）一覧も含めて返す | 全職員 |
| `POST` | `/api/books` | 書誌新規登録（タイトル・著者・ISBN・出版社・出版年・分類番号） | 全職員 |
| `PUT` | `/api/books/{id}` | 書誌情報の全項目更新 | 全職員 |
| `DELETE` | `/api/books/{id}` | 書誌論理削除。紐づく現物が1冊以上ある場合は `400 Bad Request` | CHIEF以上 |

### 現物管理 `/api/books/{bookId}/copies`

| メソッド | パス | 責務 | 権限 |
|---------|------|------|------|
| `GET` | `/api/books/{bookId}/copies` | 指定書誌の現物一覧取得（ステータス・管理コード含む） | 全職員 |
| `POST` | `/api/books/{bookId}/copies` | 現物1冊を新規登録（管理コード・初期ステータス=AVAILABLE） | 全職員 |
| `PATCH` | `/api/books/{bookId}/copies/{copyId}/status` | 現物のステータス変更（AVAILABLE / REPAIR / DISCARDED） | 全職員 |
| `DELETE` | `/api/books/{bookId}/copies/{copyId}` | 現物の論理削除（廃棄処理）。貸出中の場合は `409 Conflict` | CHIEF以上 |

### レスポンス設計の補足

- `GET /api/books/{id}` のレスポンスイメージ:
  ```json
  {
    "id": 1,
    "isbn": "978-4-xxx-xxxxx-x",
    "title": "吾輩は猫である",
    "author": "夏目漱石",
    "publisher": "○○出版",
    "publishedYear": 1905,
    "callNumber": "913.6",
    "copies": [
      { "id": 1, "copyCode": "978-4-xxx-001", "status": "AVAILABLE" },
      { "id": 2, "copyCode": "978-4-xxx-002", "status": "CHECKED_OUT" }
    ]
  }
  ```

- エラーレスポンスは統一形式:
  ```json
  { "status": 400, "message": "現物が登録されているため書誌を削除できません" }
  ```

---

### TDDにおける責務分離の活かし方

**Application Logic（機械的な処理）**
- DTO ↔ Entity 変換
- Repository 呼び出し・`@Transactional` 管理
- レスポンス組み立て

**Business Logic（ドメインのルール）**
- 書誌削除 → 現物ありなら禁止
- 現物削除 → 貸出中なら禁止
- ステータス遷移の妥当性（`CHECKED_OUT → AVAILABLE` は `LoanService` 経由のみ）

**この分離がTDDで活きる理由:**
- Business Logic は Repository をモックすれば単体で検証できる
- `BookServiceTest` で `when(bookCopyRepository.findByBookId(...)).thenReturn(...)` を使って「現物あり→削除禁止」だけをテストできる

---

### STAFF.role について

- `GENERAL`（一般職員）：貸出・返却・蔵書参照のみ
- `CHIEF`（主任）：利用者個人情報の閲覧・貸出ルール変更を追加許可
- `DIRECTOR`（館長）：全機能 + 職員アカウント管理
