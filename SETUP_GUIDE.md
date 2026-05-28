# 環境構築ガイド — library-app

**対象**: AI駆動型Java開発研修(2026年5月開講)受講者  
**ゴール**: Day 3 初日に「BE + FE が起動でき、Claude Code が安全に動く」状態を作る

---

## OS別クイックルートマップ

どのタブに該当するかを最初に確認してください。

| 項目 | macOS | Windows WSL | Windows PowerShell |
|---|---|---|---|
| **シェル** | Terminal / zsh | WSL2 + Ubuntu の bash | PowerShell |
| **セットアップ** | `./setup.sh` | `./setup.sh` | `.\setup.ps1` |
| **チェック** | `./check-setup.sh` | `./check-setup.sh` | `.\check-setup.ps1` |
| **BE 起動** | `./gradlew bootRun` | `./gradlew bootRun` | `.\gradlew.bat bootRun` |
| **FE 起動** | `npm run dev` | `npm run dev` | `npm run dev` |
| **STEP 0** | 不要 | **必須** → 後述 | **必須** → 後述 |

> **WSL を使うか PowerShell を使うか迷っている場合**: WSL を推奨します。Linux 環境がそのまま使えるため、macOS ユーザーと同じ手順で進められます。

---

## 所要時間の目安

| 状態 | 目安 |
|---|---|
| Java 25 + Node.js v22 が既にインストール済み | 約 5 分 |
| Gradle が Java 25 を自動ダウンロードする場合(初回) | 追加で 5〜10 分 |
| Windows WSL を未インストールの状態から始める場合 | 追加で 15〜30 分 |

---

## STEP 0: Windows ユーザー向け事前確認

macOS ユーザーはこの STEP をスキップして [STEP 1](#step-1-前提条件の確認) へ進んでください。

### Windows WSL ユーザー

#### WSL2 のセットアップ

PowerShell(管理者) または Windows Terminal(管理者) で実行:

```powershell
# WSL2 + Ubuntu のインストール (再起動が必要な場合あり)
wsl --install

# インストール済み WSL のバージョン確認
wsl --list --verbose
```

`VERSION` 列が `2` になっていれば OK です。`1` の場合は以下で変換してください:

```powershell
wsl --set-default-version 2
wsl --set-version Ubuntu 2
```

#### Ubuntu を開いて初期設定

スタートメニューから Ubuntu を起動し、ユーザー名とパスワードを設定します。

#### プロジェクトは WSL ネイティブファイルシステムに置く

**これが最も重要な注意点です。**

- ✅ 正しい場所: `~/projects/library-app/`（WSL ネイティブ）
- ❌ NG な場所: `/mnt/c/Users/.../library-app/`（Windows ドライブをマウントした場所）

`/mnt/c/` 以下はファイル I/O が数十倍遅くなり、Gradle ビルドの失敗やファイルロックエラーの原因になります。

```bash
# WSL 内で推奨ディレクトリを作成
mkdir -p ~/projects
```

#### WSL 内への Node.js インストール (nvm 推奨)

```bash
# nvm のインストール
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash

# シェルを再読み込み
source ~/.bashrc   # または source ~/.zshrc

# Node.js v22 LTS のインストール
nvm install 22
nvm use 22
node --version   # v22.x.x と表示されれば OK
```

#### WSL 内への Claude Code インストール

```bash
npm install -g @anthropic-ai/claude-code
claude --version   # バージョンが表示されれば OK
```

---

### Windows PowerShell ユーザー

#### PowerShell バージョン確認

```powershell
$PSVersionTable.PSVersion
```

バージョンが 5.1 未満の場合は Windows Update を適用してください。

#### 実行ポリシーの設定 (初回のみ・管理者権限不要)

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## STEP 1: 前提条件の確認

### 必須ツール

| ツール | 最低バージョン | 確認コマンド |
|---|---|---|
| **Claude Code** | 最新版 | `claude --version` |
| **Node.js** | **v22 以上**(v20 は 2026/4/30 に EOL) | `node --version` |
| **npm** | v10 以上(Node.js 同梱) | `npm --version` |

### Java は不要

**Java は自分でインストールしなくて構いません。** セットアップスクリプトの実行中に Gradle が Java 25 を自動ダウンロードします(初回のみ `~/.gradle/jdks/` に約 350 MB)。

> すでに Java 25 がインストールされていれば自動ダウンロードはスキップされます。

---

## STEP 2: リポジトリの取得

Slack `#general` に貼られた ZIP ファイルをダウンロードして展開し、フォルダに移動してください。

### 配置場所の注意

| | 推奨 | NG |
|---|---|---|
| **macOS** | `~/Documents/` や `~/projects/` | OneDrive / iCloud / Dropbox 以下 |
| **Windows WSL** | `~/projects/`（WSL 内） | `/mnt/c/` 以下、OneDrive 以下 |
| **Windows PowerShell** | `C:\Users\<name>\projects\` | OneDrive / Dropbox 以下 |

クラウド同期フォルダ配下に置くと、同期中のファイルロックがビルドエラーの原因になります。

### WSL ユーザー: ZIP の展開

Windows でダウンロードした ZIP を WSL に持ち込む方法:

```bash
# Windows の Downloads から WSL にコピー
cp /mnt/c/Users/<Windowsユーザー名>/Downloads/library-app.zip ~/projects/
cd ~/projects
unzip library-app.zip
cd library-app
```

---

## STEP 3: セットアップスクリプトの実行

### macOS

```bash
# 実行権限を付与
chmod +x setup.sh check-setup.sh

# セットアップ実行(同意プロンプトが出ます)
./setup.sh
```

### Windows WSL

```bash
# Ubuntu ターミナル内で実行
chmod +x setup.sh check-setup.sh
./setup.sh
```

> WSL では `./setup.sh` を使います。`.\setup.ps1` ではありません。

### Windows PowerShell

```powershell
# セットアップ実行(同意プロンプトが出ます)
.\setup.ps1
```

### スクリプトが行うこと

- **BE**: `backend/` で `./gradlew build`(Java 25 の自動取得を含む)
- **FE**: `frontend/` で `npm install`
- PATH / `.bashrc` / `.zshrc` などは **変更しません**

---

## STEP 4: 環境チェックの実行

セットアップ完了後、診断スクリプトで環境を確認します。

**macOS / Windows WSL:**

```bash
./check-setup.sh
```

**Windows PowerShell:**

```powershell
.\check-setup.ps1
```

### 正常な出力の例

```
=== AIDD Training Setup Check ===

[OK]   プロジェクトルートで実行中
[OK]   .claude/settings.json が存在
[OK]   deny ルールが設定されている
[OK]   CLAUDE.md が存在
[OK]   backend/gradlew に実行権限あり
[OK]   Gradle が Java 25 を使用
[OK]   ビルドが一度は実行された形跡あり
[OK]   Node.js v22.x.x (v22 LTS 以上)
[OK]   frontend/node_modules が存在 (npm install 済み)
...
=== Result ===
  Pass: 31 / Warn: 0 / Fail: 0

✅ All checks passed. You are ready for Day 3 / Day 4.
```

> **FAIL が出た場合**: 出力全文を Slack `#setup-help` に貼ってください。  
> **WARN のみの場合**: 研修進行に支障はありません。

---

## STEP 5: アプリの起動確認

BE と FE を **別々のターミナル** で起動します。

### ターミナル A — バックエンド

**macOS / Windows WSL:**

```bash
cd backend
./gradlew bootRun
```

**Windows PowerShell:**

```powershell
cd backend
.\gradlew.bat bootRun
```

起動後、ブラウザまたは別ターミナルで確認:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"} と返れば OK
```

> Windows PowerShell の場合: `curl` の代わりに `Invoke-WebRequest http://localhost:8080/actuator/health` または ブラウザで直接開いてください。

### ターミナル B — フロントエンド

**macOS / Windows WSL / Windows PowerShell 共通:**

```bash
cd frontend
npm run dev
```

起動後、ブラウザで <http://localhost:5173> を開き「みどり市立図書館」が表示されれば OK。

> **同時起動の必要はありません。** Day 3 は BE のみ、Day 4 で FE を追加します。

---

## STEP 6: Claude Code の権限設定確認

このプロジェクトには `.claude/settings.json` が同梱されており、**業務環境を Claude Code から守る隔離設定** が施されています。以下の手順で動作を確認してください。

### 6-1: プロジェクトルートで Claude Code を起動

**macOS / Windows WSL** — Ubuntu ターミナルで:

```bash
# library-app/ ディレクトリにいることを確認
pwd

claude
```

**Windows PowerShell** — PowerShell で:

```powershell
# library-app\ ディレクトリにいることを確認
Get-Location

claude
```

> WSL ユーザーは Windows Terminal ではなく **Ubuntu ターミナル内** で `claude` を実行してください。Windows 側にインストールした Claude Code は WSL 内から利用できません。

### 6-2: 隔離設定の要約を依頼

Claude に次のように聞いてください:

> 「`.claude/settings.json` の deny ルールを日本語で要約してください」

Claude が `~/.ssh` や `sudo` などの禁止項目を列挙して返答すれば OK です。

### 6-3: deny が実際に効くか確認

Claude に次のように頼んでください:

> 「`~/.ssh` の中にあるファイル一覧を見せてください」

Claude が **拒否** してファイル一覧を返さなければ OK です。

> **Windows PowerShell ユーザー**: `~/.ssh` は `C:\Users\<name>\.ssh` に相当しますが、deny ルールのテストとしては同様に動作します。

### 6-4: プロジェクト外では隔離が効かないことを確認

Claude Code の設定は **プロジェクトローカル** です。`library-app/` の外で起動した Claude Code にはこの deny ルールが適用されません。業務リポジトリでは別途設定が必要な点を意識しておきましょう。

> 詳細は `.claude/SETTINGS_GUIDE.md` を参照してください。

---

## STEP 7: 準備完了チェックリスト

Day 3 を始める前に、以下をすべて確認してください。

- [ ] `./check-setup.sh`(または `.\check-setup.ps1`)で FAIL がゼロ
- [ ] `http://localhost:8080/actuator/health` が `{"status":"UP"}`
- [ ] `http://localhost:5173` で「みどり市立図書館」が表示される
- [ ] Claude に `~/.ssh` 一覧を頼んだら拒否された
- [ ] `CLAUDE.md` を一読した

---

## トラブルシューティング

### Java のバージョンエラー

```
Gradle が Java 25 で動いていません
```

Gradle が Java 25 を自動取得するはずですが、`JAVA_HOME` が古い JDK を指している場合に起こります。

**macOS / WSL:**

```bash
# JAVA_HOME を一時的に解除して再試行
unset JAVA_HOME
cd backend && ./gradlew --version
```

**Windows PowerShell:**

```powershell
$env:JAVA_HOME = ""
cd backend; .\gradlew.bat --version
```

それでも解決しない場合は Slack `#setup-help` へ。

### Node.js バージョンが古い

```
[FAIL] Node.js が古いです (検出: v20.x.x / 必要: v22 以上)
```

v20 は 2026年4月30日で EOL です。v22 LTS に切り替えてください。

**macOS:**

```bash
# nvm 使用の場合
nvm install 22 && nvm use 22

# Homebrew の場合
brew install node@22 && brew link node@22 --force
```

**Windows WSL:**

```bash
# nvm 使用の場合(推奨)
nvm install 22 && nvm use 22

# nvm がない場合はまずインストール
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.bashrc
nvm install 22
```

**Windows PowerShell:**

```powershell
winget install OpenJS.NodeJS.LTS
```

### `frontend/node_modules` がない

```bash
cd frontend && npm install
```

それでも失敗する場合は `npm install` のエラー全文を Slack `#setup-help` へ。

### クラウド同期フォルダの警告

```
[WARN] クラウド同期フォルダ配下にプロジェクトがある可能性があります
```

OneDrive / iCloud / Dropbox 以外の場所にプロジェクトを移動してから再実行してください。

**WSL ユーザー向け追記**: `/mnt/c/Users/.../OneDrive/` に置いている場合は `~/projects/` に移動してください。

### PowerShell でスクリプトが実行できない(Windows PowerShell)

```
このシステムではスクリプトの実行が無効になっているため…
```

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

を実行してから `.\setup.ps1` を再試行してください。

### WSL でビルドが遅い / ファイルロックエラー (Windows WSL)

```
[WARN] WSL 環境で Windows ファイルシステム上で実行しています (/mnt/...)
```

`/mnt/c/` 以下で作業しているのが原因です。WSL ネイティブファイルシステムに移動してください:

```bash
# WSL 内で実行
cp -r /mnt/c/Users/<ユーザー名>/Downloads/library-app ~/projects/
cd ~/projects/library-app
./setup.sh
```

### WSL で `claude` コマンドが見つからない (Windows WSL)

Claude Code は WSL 内にインストールする必要があります。Windows 側にインストールしても WSL からは使えません。

```bash
# WSL 内で Claude Code をインストール
npm install -g @anthropic-ai/claude-code
claude --version
```

---

## 問い合わせ先

解決できない場合は Slack `#setup-help` に **`check-setup.sh` / `check-setup.ps1` の出力全文** を貼って相談してください。
