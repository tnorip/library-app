# AIDD Training — 環境セットアップ自己診断スクリプト (Windows / PowerShell 版)
# Usage: .\check-setup.ps1
# プロジェクトルートディレクトリで実行してください
#
# 初回実行前に以下が必要な場合があります(管理者不要):
#   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

#Requires -Version 5.1
$ErrorActionPreference = 'Continue'
$script:PASS = 0
$script:FAIL = 0
$script:WARN = 0

function ok   { param($msg) Write-Host "[OK]   $msg" -ForegroundColor Green;  $script:PASS++ }
function ng   { param($msg) Write-Host "[FAIL] $msg" -ForegroundColor Red;    $script:FAIL++ }
function warn { param($msg) Write-Host "[WARN] $msg" -ForegroundColor Yellow; $script:WARN++ }

Write-Host "=== AIDD Training Setup Check (Windows) ===" -ForegroundColor Cyan
Write-Host ""

# 1. プロジェクトルート確認
if ((Test-Path "CLAUDE.md") -and (Test-Path "backend") -and (Test-Path "frontend")) {
    ok "プロジェクトルートで実行中(CLAUDE.md / backend\ / frontend\ を検出)"
} else {
    ng "プロジェクトルートで実行されていません"
    Write-Host "       → library-app ディレクトリに移動してから再実行してください"
    Write-Host "       → (CLAUDE.md / backend\ / frontend\ が存在するディレクトリ)"
}

# 2. 業務領域との混在チェック
$parent = Split-Path (Get-Location) -Parent
$suspicious = Get-ChildItem $parent -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^(work|client|prod|production|customer)' } |
    Select-Object -First 3
if ($suspicious) {
    warn "親ディレクトリに業務系っぽい名前のフォルダがあります:"
    $suspicious | ForEach-Object { Write-Host "         - $($_.Name)" }
    Write-Host "       → 業務リポジトリと同じ親ディレクトリに置いていないか確認してください"
}

# 3. .claude/settings.json
if (Test-Path ".claude\settings.json") {
    ok ".claude\settings.json が存在"

    $settingsContent = Get-Content ".claude\settings.json" -Raw
    try {
        $null = $settingsContent | ConvertFrom-Json
        ok ".claude\settings.json の JSON 構文が妥当"
    } catch {
        ng ".claude\settings.json の JSON 構文エラー(末尾カンマ等を確認)"
    }

    if ($settingsContent -match '"deny"') {
        ok "deny ルールが設定されている"
    } else {
        ng "deny ルールが見つかりません(隔離が効きません)"
    }

    foreach ($pattern in @('~/.ssh', 'sudo', '\.env', 'rm -rf')) {
        if ($settingsContent -match [regex]::Escape($pattern)) {
            ok "deny 項目「$pattern」を含む"
        } else {
            warn "deny 項目「$pattern」が見つかりません(雛形の改ざん?)"
        }
    }

    if ($settingsContent -match '"defaultMode"\s*:\s*"bypassPermissions"') {
        ng "defaultMode が bypassPermissions です(隔離が無効化されています)"
    }
} else {
    ng ".claude\settings.json がありません(隔離が効きません)"
}

# 4. CLAUDE.md
if (Test-Path "CLAUDE.md") {
    ok "CLAUDE.md が存在"
    $claudeContent = Get-Content "CLAUDE.md" -Raw
    foreach ($keyword in @('Java 25', 'Spring Boot', 'jakarta', 'JUnit 5', 'React')) {
        if ($claudeContent -match [regex]::Escape($keyword)) {
            ok "CLAUDE.md にキーワード「$keyword」を含む"
        } else {
            warn "CLAUDE.md にキーワード「$keyword」が見つかりません"
        }
    }
} else {
    ng "CLAUDE.md がありません"
}

# 5. gradlew.bat
if (Test-Path "backend\gradlew.bat") {
    ok "backend\gradlew.bat が存在"
} else {
    ng "backend\gradlew.bat がありません"
}

# 6. Java バージョン (Gradle 経由)
if (Test-Path "backend\gradlew.bat") {
    Push-Location backend
    $gradleVersion = & .\gradlew.bat --version 2>&1
    Pop-Location
    $jvmLine = $gradleVersion | Where-Object { $_ -match "^(JVM|Launcher JVM):" } | Select-Object -First 1
    if ($jvmLine) {
        if ($jvmLine -match "JVM:\s*25") {
            ok "Gradle が Java 25 を使用 ($jvmLine)"
        } else {
            ng "Gradle が Java 25 で動いていません: $jvmLine"
            Write-Host "       → https://adoptium.net/ から Temurin 25 をインストール"
            Write-Host "       → または JAVA_HOME を Java 25 のパスに設定してください"
        }
    } else {
        ng "Gradle の JVM 情報が取得できません"
        Write-Host "       → backend\ ディレクトリで .\gradlew.bat --version を直接実行してエラーを確認してください"
    }
}

# 7. ビルド成果物の存在
if (Test-Path "backend\build") {
    ok "ビルドが一度は実行された形跡あり"
} else {
    warn "backend\build\ が見当たりません。cd backend; .\gradlew.bat build を実行してください"
}

# 7.5 必須ソースファイル
$mainJava = "backend\src\main\java\training\aidd\library\LibraryApplication.java"
$appProps  = "backend\src\main\resources\application.properties"
$testJava  = "backend\src\test\java\training\aidd\library\LibraryApplicationTests.java"

if (Test-Path $mainJava) {
    ok "メインクラス $mainJava が存在"
    if ((Get-Content $mainJava -Raw) -match '@SpringBootApplication') {
        ok "メインクラスに @SpringBootApplication が記述されている"
    } else {
        ng "メインクラスに @SpringBootApplication が見つかりません"
    }
} else {
    ng "メインクラスがありません: $mainJava"
    Write-Host "       → 雛形配布が不完全な可能性。講師に連絡してください"
}

if (Test-Path $appProps) {
    ok "$appProps が存在"
} else {
    warn "$appProps が見当たりません"
}

if (Test-Path $testJava) {
    ok "テストクラス $testJava が存在"
} else {
    warn "テストクラスが見当たりません"
}

# 8. Node.js / npm
$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if ($nodeCmd) {
    $nodeVersion = (& node --version).ToString()
    $nodeMajor   = [int][regex]::Match($nodeVersion, 'v(\d+)').Groups[1].Value
    if ($nodeMajor -ge 22) {
        ok "Node.js $nodeVersion (v22 LTS 以上)"
    } else {
        ng "Node.js が古いです (検出: $nodeVersion / 必要: v22 以上)"
        Write-Host "       → 公式:   https://nodejs.org/ja/download"
        Write-Host "       → winget: winget install OpenJS.NodeJS.LTS"
    }
} else {
    ng "Node.js が見つかりません (FE ビルドに必要)"
    Write-Host "       → 公式:   https://nodejs.org/ja/download"
}

$npmCmd = Get-Command npm -ErrorAction SilentlyContinue
if ($npmCmd) {
    ok "npm $(& npm --version) 利用可"
} else {
    ng "npm が見つかりません"
}

# 8.5 フロントエンド構成ファイル
if (Test-Path "frontend") {
    ok "frontend\ ディレクトリが存在"

    if (Test-Path "frontend\package.json") {
        ok "frontend\package.json が存在"
    } else {
        ng "frontend\package.json がありません (雛形が不完全な可能性)"
    }

    if (Test-Path "frontend\node_modules") {
        ok "frontend\node_modules が存在 (npm install 済み)"
    } else {
        ng "frontend\node_modules がありません"
        Write-Host "       → frontend\ ディレクトリで npm install を実行してください"
        Write-Host "       → または .\setup.ps1 を実行してください"
    }

    foreach ($feFile in @("frontend\vite.config.ts", "frontend\src\App.tsx", "frontend\src\api\client.ts")) {
        if (Test-Path $feFile) {
            ok "$feFile が存在"
        } else {
            ng "$feFile がありません (雛形が不完全な可能性)"
        }
    }
} else {
    ng "frontend\ ディレクトリがありません"
    Write-Host "       → .\setup.ps1 を実行してください"
}

# 9. Claude Code
$claudeCmd = Get-Command claude -ErrorAction SilentlyContinue
if ($claudeCmd) {
    $claudeVer = (& claude --version 2>&1 | Select-Object -First 1).ToString()
    ok "claude コマンド利用可: $claudeVer"
} else {
    ng "claude コマンドが PATH にありません"
}


# 11. クラウド同期フォルダの簡易検知
$pwd = (Get-Location).Path.ToLower()
if ($pwd -match 'onedrive|icloud|dropbox|google.drive|box.sync') {
    warn "クラウド同期フォルダ配下にプロジェクトがある可能性があります"
    Write-Host "       → 同期対象外のフォルダに移動することを推奨(FAQ Q7 参照)"
}

# ── 結果 ──────────────────────────────────────────────────
Write-Host ""
Write-Host "=== Result ===" -ForegroundColor Cyan
Write-Host "  Pass: $($script:PASS) / Warn: $($script:WARN) / Fail: $($script:FAIL)"
Write-Host ""

if ($script:FAIL -eq 0) {
    Write-Host "✅ All checks passed. You are ready for Day 3 / Day 4." -ForegroundColor Green
    Write-Host ""
    Write-Host "   起動コマンド:"
    Write-Host "     BE:  cd backend; .\gradlew.bat bootRun  → http://localhost:8080/actuator/health"
    Write-Host "     FE:  cd frontend; npm run dev     → http://localhost:5173"
    if ($script:WARN -gt 0) {
        Write-Host ""
        Write-Host "   ($($script:WARN) 件の警告がありますが、研修進行は可能です。気になれば #setup-help で相談を)"
    }
    exit 0
} else {
    Write-Host "❌ $($script:FAIL) 件の重要な問題があります。" -ForegroundColor Red
    Write-Host "   Slack #setup-help に出力全文を貼って相談してください。"
    Write-Host "   まず .\setup.ps1 を実行すると多くの問題が自動解決します。"
    exit 1
}
