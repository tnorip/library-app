# `.claude/settings.json` 運用ガイド

研修プロジェクトに同梱されている `.claude/settings.json` は、**業務環境を Claude Code から守るための隔離設定**です。本ドキュメントはその設計意図と運用ルールを説明します。

---

## なぜこの設定が必要か

研修では、皆さんの業務PCで Claude Code を動かします。Claude Code は強力なツールですが、適切な権限設定なしでは:

- 業務リポジトリ(`~/work/` 等)を意図せず読み書きしてしまう
- `~/.ssh/` `~/.aws/` の認証情報を読み出してしまう
- `.env` `*.pem` 等のシークレットを読み出してしまう
- `~/.bashrc` `~/.zshrc` を書き換えてシェル環境を破壊する
- `rm -rf ~/` のような破壊操作を、ユーザーの意図せず実行する

これらを **構造的に防ぐ** のが本ファイルの役割です。

---

## 権限ルールの評価順序(Claude Code の仕様)

```
deny → ask → allow → defaultMode
```

- **deny**: 即拒否(最優先・絶対に勝つ)
- **ask**: 確認ダイアログを出す
- **allow**: 自動承認(プロンプトなし)
- **defaultMode** = `default`: どのルールにも該当しない操作は確認を求める

設定ファイルで使われる **3つの層** は次の通りです。

---

## Layer 1: deny(絶対に拒否するもの)

業務環境保護の最終防衛ライン。ここに入れたものは、たとえ受講者が「実行してください」と頼んでも Claude Code は実行しません。

### 1-1. 破壊的 Bash 操作

```
Bash(rm -rf /*)        ルートからの再帰削除
Bash(rm -rf ~*)        ホームディレクトリからの再帰削除
Bash(sudo *)           特権昇格全般
Bash(:(){ :|:& };:)    Fork bomb
Bash(curl * | sh*)     パイプ経由でのリモートスクリプト実行
Bash(dd if=*)          ディスクの低レベル操作
```

> なお Claude Code には **circuit breaker** があり、`bypassPermissions` モードでも `rm -rf /` `rm -rf ~` は必ずプロンプトが出ます。本 deny は、それに至る前に弾く第一防衛線です。

### 1-2. 業務領域・認証情報への参照

`Bash(...)` と `Read(...)` を併記しているのは、`ls` `cat` 等の builtin read-only コマンドが Claude Code 標準仕様では**プロンプトなしに実行される**ためです。Bash レベルでも明示的に塞いでいます。

```
Read(~/.ssh/**)              SSH鍵
Read(~/.aws/**)              AWS資格情報
Read(~/.gnupg/**)            GPG鍵
Read(~/.docker/config*)      Dockerレジストリ認証
Read(~/.kube/config*)        Kubernetes認証
Read(~/.netrc)               旧来のホスト別認証
Read(~/.npmrc)               npm認証トークン
Read(**/.env)                環境変数ファイル
Read(**/*.pem)               証明書・秘密鍵
Read(**/id_rsa*)             SSH秘密鍵
Read(**/*credentials*)       一般的な認証ファイル
Read(**/*secret*)            一般的なシークレットファイル
Read(**/*.keystore)          Java keystore
Read(**/*.jks)               Java keystore (旧)
```

> **絶対パスも併記** しているのは、`~` 展開のタイミングに関するバグ(Claude Code issue #18160 等)への保険です。`~/.ssh/**` だけでは Linux/macOS で動作しない可能性があるため、`/Users/*/.ssh/**` `/home/*/.ssh/**` も入れています。

### 1-3. 自身の上書き禁止

```
Edit(.claude/settings.json)
Write(.claude/settings.json)
Edit(.claude/settings.local.json)
Write(.claude/settings.local.json)
```

**これは隔離の自己保護です**。Claude Code 自身に「権限ルールを緩めてください」と頼むことができないようにします。設定変更は受講者が直接エディタで行ってください。

### 1-4. シェル環境の破壊禁止

```
Edit(~/.bashrc)        ログインシェル設定
Edit(~/.zshrc)
Edit(~/.profile)
Edit(~/.gitconfig)     Gitグローバル設定
Edit(/etc/**)          システム設定全般
```

### 1-5. クラウドメタデータエンドポイント

```
WebFetch(domain:169.254.169.254)
```

EC2/GCP/Azure のインスタンスメタデータ経由で資格情報を抜き取られないようにする保険(クラウドVM上で研修を行う場合の備え)。

---

## Layer 2: ask(実行前に確認させるもの)

研修進行を妨げないが、**重要な決断は受講者の目視確認を強制** します。

### 2-1. ネットワークアクセス

```
Bash(curl *)
Bash(wget *)
WebFetch
```

外部サイトへのアクセスは原則確認。ただし `localhost` への curl は allow に入れています(STEP 5-2 の `/actuator/health` 確認用)。

### 2-2. パッケージインストール

```
Bash(npm install*)
Bash(pip install*)
Bash(brew install*)
Bash(apt install*)
```

**新規依存の追加は本研修の主要な学習項目** です。CLAUDE.md にも「`build.gradle.kts` の依存関係追加は AI に書かせない」と明記していますが、CLI レベルでも ask で再確認させます。

### 2-3. 破壊的 Git 操作

```
Bash(git push*)              リモートへの反映
Bash(git reset --hard*)      ローカル変更の破棄
Bash(git rebase*)            履歴の書き換え
Bash(git clean -fd*)         追跡外ファイルの削除
```

### 2-4. ファイル削除・移動

```
Bash(rm *)
Bash(mv *)
```

`rm -rf ~` 等は deny で即拒否されますが、それ以外の `rm` も ask で確認を強制します。

### 2-5. リモート接続・コンテナ操作

```
Bash(ssh *)
Bash(scp *)
Bash(rsync *)
Bash(docker *)
Bash(podman *)
Bash(systemctl *)
```

研修で必須ではないが、誤操作防止のため確認を入れます。

---

## Layer 3: allow(自動承認するもの)

研修頻出かつ安全な操作のみ。プロンプトを最小化して研修進行を滑らかにします。

### 3-1. Gradle 関連

```
Bash(./gradlew build)
Bash(./gradlew bootRun)
Bash(./gradlew test)
Bash(./gradlew test --tests *)
Bash(./gradlew dependencies)
```

**`./gradlew clean` は ask に入れています**(誤って成果物を消さないよう確認を入れる)。

### 3-2. 安全な Git 操作

```
Bash(git status)
Bash(git diff *)
Bash(git log *)
Bash(git add *)
Bash(git commit -m *)
Bash(git checkout *)
Bash(git switch *)
```

**`git config` は ask です**(設定変更は確認を入れる)。

### 3-3. ローカルアクセスのみ許可

```
Bash(curl http://localhost:*)
Bash(curl https://localhost:*)
Bash(curl http://127.0.0.1:*)
```

外部 curl は ask、ローカルだけ allow。

### 3-4. プロジェクト内ファイル参照

```
Read(src/**)
Read(build.gradle.kts)
Read(CLAUDE.md)
Read(*.md)
```

明示的な許可で「プロジェクト内は自由に読んでよい」を表現。

---

## チェックリスト STEP 6 との対応

環境構築チェックリストの STEP 6 では、本設定の動作を実地テストします。

| STEP | 期待される動作 | 設定との対応 |
|---|---|---|
| 6-2: deny ルールの要約 | Claude が deny ルールを列挙できる | ✅ 本ファイルが読める設定なので応答可 |
| 6-3: `~/.ssh` の一覧依頼 | 拒否される | ✅ `Bash(* ~/.ssh*)` `Read(~/.ssh/**)` |
| 6-4: プロジェクト外起動 | deny ルール非適用 | ✅ プロジェクトローカル設定の仕様通り |

---

## やってはいけない改変

研修中、Claude Code と対話していて「権限ルールが厳しすぎる」と感じることがあるかもしれません。**以下の改変は研修期間中、絶対にやらないでください**:

1. ❌ `defaultMode` を `bypassPermissions` に変更する
2. ❌ deny ルールを削除する
3. ❌ `Bash(*)` を allow に追加する(全 Bash コマンドの白紙委任)
4. ❌ `Read(~/**)` を allow に追加する(ホーム全域読み取り許可)
5. ❌ `additionalDirectories` に業務リポジトリのパスを追加する

これらは隔離を無効化します。Day 2 のセキュリティ演習でも、まさにこうした「悪い設定」がどう問題になるかを扱います。

---

## やってよい改変

研修進行に必要なら、以下は許容されます。

1. ✅ 自分用の `Bash(...)` を allow に追加(例:`Bash(./gradlew jacocoTestReport)`)
2. ✅ ask に追加して「毎回確認したい」を明示する
3. ✅ プロジェクト内の特定パスへの Read/Write の明示

> 改変するときは **`.claude/settings.local.json` に書く** ことを推奨します(`.gitignore` で除外されているので、個人カスタマイズに最適)。本ファイルは雛形そのものを汚さず、Layer 1〜3 の方針を維持できます。

---

## bypassPermissions モードの危険性(参考)

Claude Code には `--dangerously-skip-permissions` という起動オプションがあります。**研修では絶対に使わないでください**。これを使うと:

- 本ファイルの deny ルール以外、ほぼすべての操作が無確認で実行される
- ただし `rm -rf /` `rm -rf ~` 等の極端なものは circuit breaker でプロンプトが出る
- `.git` `.claude` `.vscode` 等への書き込みも依然プロンプトが出る

このモードが許容されるのは「コンテナ・VM・CIの一時的な隔離環境」のみです。業務PCでの常用は事故の元です(Day 2 のセキュリティ演習で詳述)。

---

## 既知の制約(2026年5月時点)

Claude Code の deny ルール実装には、過去にいくつかの不具合報告があります(GitHub issue #6699 #27040 #18160)。ほとんどは修正済みですが、**完全な保護のためには将来的に PreToolUse hook の併用** を検討する余地があります。

研修期間中は本設定で十分ですが、業務利用に展開する際は:

1. Claude Code を最新版に保つ
2. 機密性の高いファイル(本番資格情報等)は別ホスト・別ユーザーで扱う
3. 必要なら PreToolUse hook で二重チェックを追加(`.claude/hooks/` 配下)

を検討してください。

---

## 参考リンク

- [Claude Code: Configure permissions](https://code.claude.com/docs/en/permissions)
- [Claude Code: Settings reference](https://docs.claude.com/en/docs/claude-code/overview)
- [Claude Code: Sandboxing](https://code.claude.com/docs/en/permissions)(macOS Seatbelt / Linux bubblewrap)
