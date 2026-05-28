# AIDD Training — 環境セットアップスクリプト (Windows / PowerShell 版)
# Usage: .\setup.ps1
# プロジェクトルート(CLAUDE.md / backend\ / frontend\ があるディレクトリ)で実行してください
#
# 初回実行前に以下が必要な場合があります(管理者不要):
#   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

#Requires -Version 5.1
$ErrorActionPreference = 'Continue'
$script:ERRORS = 0

function Write-Step { param($msg) Write-Host "`n▶ $msg" -ForegroundColor Cyan }
function Write-Ok   { param($msg) Write-Host "  ✓ $msg" -ForegroundColor Green }
function Write-Warn { param($msg) Write-Host "  ⚠ $msg" -ForegroundColor Yellow }
function Write-Fail { param($msg) Write-Host "  ✗ $msg" -ForegroundColor Red; $script:ERRORS++ }
function Write-Hint { param($msg) Write-Host "    → $msg" -ForegroundColor Yellow }

# ── ヘッダー ──────────────────────────────────────────────
Write-Host ""
Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  AIDD Training — 環境セットアップ        ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ── プロジェクトルート確認 ────────────────────────────────
if (-not (Test-Path "CLAUDE.md") -or -not (Test-Path "backend") -or -not (Test-Path "frontend")) {
    Write-Fail "CLAUDE.md / backend\ / frontend\ が見つかりません"
    Write-Hint "library-app ディレクトリで実行してください"
    exit 1
}
Write-Ok "プロジェクトルート確認"

# ── ダウンロード内容の事前告知 ────────────────────────────
Write-Host ""
Write-Host "【このスクリプトが行うこと】" -ForegroundColor White
Write-Host "  プロジェクト内:"
Write-Host "    - backend の依存ライブラリをダウンロード・コンパイル (backend\build\)"
Write-Host "    - frontend の npm パッケージをインストール (frontend\node_modules\)"
Write-Host ""
Write-Host "  ホームディレクトリ(初回のみ):"
Write-Host "    - Gradle 本体           ~\.gradle\wrapper\   (約 120 MB)"
Write-Host "    - Java 25 toolchain     ~\.gradle\jdks\      (約 350 MB) ← Java 未インストールの場合"
Write-Host "    - Gradle 依存キャッシュ  ~\.gradle\caches\   (Maven Central から取得)"
Write-Host "    - npm キャッシュ         ~\AppData\Roaming\npm-cache\  (npmjs.org から取得)"
Write-Host ""
Write-Host "  変更しないもの: PATH / 環境変数 / グローバル npm / 管理者権限 不使用"
Write-Host ""
$confirm = Read-Host "続けますか？ [y/N]"
if ($confirm -notmatch '^[Yy]$') {
    Write-Host "キャンセルしました"
    exit 0
}

# ── Java 確認 ─────────────────────────────────────────────
Write-Step "Java の確認"

$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
    $javaFull  = (& java -version 2>&1 | Select-Object -First 1).ToString()
    $javaMajor = [regex]::Match($javaFull, '"(\d+)').Groups[1].Value
    if ([int]$javaMajor -ge 25) {
        Write-Ok "Java $javaMajor 検出 ($javaFull)"
    } else {
        Write-Warn "Java が古いです (検出: $javaFull / 推奨: 25)"
        Write-Hint "Gradle toolchain が初回ビルド時に Java 25 を自動ダウンロードします"
        Write-Hint "手動: https://adoptium.net/ から Temurin 25 をインストール"
    }
} else {
    Write-Warn "java コマンドが見つかりません"
    Write-Hint "Gradle toolchain が初回ビルド時に Java 25 を自動ダウンロードします(数分かかります)"
}

# ── Node.js 確認 ──────────────────────────────────────────
Write-Step "Node.js の確認 (v22 LTS 以上が必要)"

$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if ($nodeCmd) {
    $nodeVersion = (& node --version).ToString()
    $nodeMajor   = [int][regex]::Match($nodeVersion, 'v(\d+)').Groups[1].Value
    if ($nodeMajor -ge 22) {
        Write-Ok "Node.js $nodeVersion 検出"
    } else {
        Write-Fail "Node.js が古いです (検出: $nodeVersion / 必要: v22 以上)"
        Write-Hint "公式:   https://nodejs.org/ja/download"
        Write-Hint "winget: winget install OpenJS.NodeJS.LTS"
    }
} else {
    Write-Fail "Node.js が見つかりません"
    Write-Hint "公式:   https://nodejs.org/ja/download"
    Write-Hint "winget: winget install OpenJS.NodeJS.LTS"
}

# 必須ツール不足なら早期終了
if ($script:ERRORS -gt 0) {
    Write-Host ""
    Write-Fail "必須ツールが不足しています。上記の手順でインストール後、.\setup.ps1 を再実行してください"
    Write-Host "  解決できない場合は #setup-help に出力全文を貼って相談してください"
    exit 1
}

# ── バックエンド: Gradle ビルド ───────────────────────────
Write-Step "バックエンド: Gradle ビルド"
Write-Host "  ※ 初回は Java 25 / 依存ライブラリのダウンロードで数分かかる場合があります"
Write-Host ""

Push-Location backend
& .\gradlew.bat build -x test
if ($LASTEXITCODE -ne 0) {
    Write-Fail "バックエンド ビルド失敗"
    Write-Hint "backend\ ディレクトリで .\gradlew.bat build を単独実行してエラーを確認してください"
} else {
    Write-Ok "バックエンド ビルド成功"
}
Pop-Location

# ── フロントエンド: npm install ───────────────────────────
Write-Step "フロントエンド: npm install"
Write-Host ""

if (-not (Test-Path "frontend")) {
    Write-Fail "frontend\ ディレクトリがありません"
    Write-Hint "雛形が不完全な可能性があります。講師に確認してください"
} elseif (-not (Test-Path "frontend\package.json")) {
    Write-Fail "frontend\package.json がありません"
    Write-Hint "雛形が不完全な可能性があります。講師に確認してください"
} else {
    Push-Location frontend
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "npm install 失敗"
        Write-Hint "frontend\ ディレクトリ内で npm install を手動実行してください"
    } else {
        Write-Ok "npm install 完了"
    }
    Pop-Location
}

# ── 結果サマリー ──────────────────────────────────────────
Write-Host ""
Write-Host "══════════════════════════════════════════" -ForegroundColor Cyan

if ($script:ERRORS -eq 0) {
    Write-Host "✅ セットアップ完了!" -ForegroundColor Green
    Write-Host ""
    Write-Host "  次のコマンドで起動できます:"
    Write-Host "    BE 起動:  cd backend; .\gradlew.bat bootRun  → http://localhost:8080/actuator/health"
    Write-Host "    FE 起動:  cd frontend; npm run dev      → http://localhost:5173"
    Write-Host ""
    Write-Host "  環境チェック: .\check-setup.ps1"
} else {
    Write-Host "❌ $($script:ERRORS) 件のエラーがあります" -ForegroundColor Red
    Write-Host "  上記のエラーを解消して .\setup.ps1 を再実行してください"
    Write-Host "  解決できない場合は #setup-help に出力全文を貼って相談してください"
    exit 1
}
