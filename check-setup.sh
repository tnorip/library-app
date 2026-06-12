#!/usr/bin/env bash
# AIDD Training - 環境セットアップ自己診断スクリプト
# 実行は library-app プロジェクトルートで(CLAUDE.md / backend/ / frontend/ がある場所)。

set -u

PASS=0
FAIL=0
WARN=0

# WSL 検出
IS_WSL=false
if grep -qi microsoft /proc/version 2>/dev/null || [ -n "${WSL_DISTRO_NAME:-}" ]; then
  IS_WSL=true
fi

ok()   { echo "[OK]   $1"; PASS=$((PASS+1)); }
ng()   { echo "[FAIL] $1"; FAIL=$((FAIL+1)); }
warn() { echo "[WARN] $1"; WARN=$((WARN+1)); }

if $IS_WSL; then
  echo "=== AIDD Training Setup Check (WSL) ==="
else
  echo "=== AIDD Training Setup Check ==="
fi
echo

# 0. WSL: Windows ファイルシステム上で実行していないかチェック
if $IS_WSL; then
  if [[ "$(pwd)" == /mnt/* ]]; then
    warn "WSL 環境で Windows ファイルシステム(/mnt/...) 上で実行しています"
    echo "       → パフォーマンスとファイルロック回避のため ~/projects/ 等に移動してください"
    echo "       → 例: cp -r . ~/projects/library-app && cd ~/projects/library-app"
  else
    ok "WSL ネイティブファイルシステム上で実行中"
  fi
fi

# 1. プロジェクトルートで実行されているか
if [ -f "CLAUDE.md" ] && [ -d "backend" ] && [ -d "frontend" ]; then
  ok "プロジェクトルートで実行中(CLAUDE.md / backend/ / frontend/ を検出)"
else
  ng "プロジェクトルートで実行されていません"
  echo "       → library-app ディレクトリに移動してから再実行してください"
  echo "       → (CLAUDE.md / backend/ / frontend/ が存在するディレクトリ)"
fi

# 2. 業務領域との混在を簡易チェック
PARENT="$(dirname "$(pwd)")"
SUSPICIOUS_NEIGHBORS=$(ls "$PARENT" 2>/dev/null | grep -iE '^(work|client|prod|production|customer)' | head -3)
if [ -n "$SUSPICIOUS_NEIGHBORS" ]; then
  warn "親ディレクトリに業務系っぽい名前のフォルダがあります:"
  echo "$SUSPICIOUS_NEIGHBORS" | sed 's/^/         - /'
  echo "       → 業務リポジトリと同じ親ディレクトリに置いていないか確認してください"
fi

# 3. .claude/settings.json
if [ -f ".claude/settings.json" ]; then
  ok ".claude/settings.json が存在"

  if command -v python3 >/dev/null 2>&1; then
    if python3 -m json.tool .claude/settings.json >/dev/null 2>&1; then
      ok ".claude/settings.json の JSON 構文が妥当"
    else
      ng ".claude/settings.json の JSON 構文エラー(末尾カンマ等を確認)"
    fi
  fi

  if grep -q '"deny"' .claude/settings.json; then
    ok "deny ルールが設定されている"
  else
    ng "deny ルールが見つかりません(隔離が効きません)"
  fi

  for pattern in '~/.ssh' 'sudo' '\.env' 'rm -rf'; do
    if grep -q "$pattern" .claude/settings.json; then
      ok "deny 項目「${pattern}」を含む"
    else
      warn "deny 項目「${pattern}」が見つかりません(雛形の改ざん?)"
    fi
  done

  if grep -q '"defaultMode"\s*:\s*"bypassPermissions"' .claude/settings.json; then
    ng "defaultMode が bypassPermissions です(隔離が無効化されています)"
  fi
else
  ng ".claude/settings.json がありません(隔離が効きません)"
fi

# 4. CLAUDE.md
if [ -f "CLAUDE.md" ]; then
  ok "CLAUDE.md が存在"

  for keyword in 'Java 25' 'Spring Boot' 'jakarta' 'JUnit 5' 'React'; do
    if grep -qF "$keyword" CLAUDE.md; then
      ok "CLAUDE.md にキーワード「${keyword}」を含む"
    else
      warn "CLAUDE.md にキーワード「${keyword}」が見つかりません"
    fi
  done
else
  ng "CLAUDE.md がありません"
fi

# 5. backend/gradlew
if [ -x "backend/gradlew" ]; then
  ok "backend/gradlew に実行権限あり"
else
  ng "backend/gradlew が実行できません(chmod +x backend/gradlew で修正)"
fi

# 6. Java バージョン (backend/gradlew 経由)
if [ -x "backend/gradlew" ]; then
  JVM_LINE=$(cd backend && ./gradlew --version 2>/dev/null | grep -E "^(JVM|Launcher JVM):" | head -1)
  if [ -z "$JVM_LINE" ]; then
    ng "Gradle の JVM 情報が取得できません(cd backend && ./gradlew --version が失敗?)"
    echo "       → backend/ ディレクトリで ./gradlew --version を直接実行してエラーを確認してください"
  elif echo "$JVM_LINE" | grep -qE "JVM:[[:space:]]*25"; then
    ok "Gradle が Java 25 を使用 ($JVM_LINE)"
  else
    ng "Gradle が Java 25 で動いていません: $JVM_LINE"
  fi
fi

# 7. ビルド成果物の存在
if [ -d "backend/build" ] || [ -d "backend/.gradle" ]; then
  ok "ビルドが一度は実行された形跡あり"
else
  warn "backend/build/ が見当たりません。cd backend && ./gradlew build を実行してください"
fi

# 7.5 BE 必須ソースファイル
MAIN_JAVA="backend/src/main/java/training/aidd/library/LibraryApplication.java"
APP_PROPS="backend/src/main/resources/application.properties"
TEST_JAVA="backend/src/test/java/training/aidd/library/LibraryApplicationTests.java"

if [ -f "$MAIN_JAVA" ]; then
  ok "メインクラス $MAIN_JAVA が存在"
  if grep -q '@SpringBootApplication' "$MAIN_JAVA"; then
    ok "メインクラスに @SpringBootApplication が記述されている"
  else
    ng "メインクラスに @SpringBootApplication が見つかりません"
  fi
else
  ng "メインクラスがありません: $MAIN_JAVA"
  echo "       → 雛形配布が不完全な可能性。講師に連絡してください"
fi

if [ -f "$APP_PROPS" ]; then
  ok "$APP_PROPS が存在"
else
  warn "$APP_PROPS が見当たりません(空でも build は通るが、起動時に DB 接続できない)"
fi

if [ -f "$TEST_JAVA" ]; then
  ok "テストクラス $TEST_JAVA が存在"
else
  warn "テストクラスが見当たりません(必須ではないが、./gradlew test は失敗します)"
fi

# 8. Node.js / npm (FE 用)
if command -v node >/dev/null 2>&1; then
  NODE_VER=$(node --version)
  NODE_MAJOR=$(echo "$NODE_VER" | sed 's/v\([0-9]*\).*/\1/')
  if [ "${NODE_MAJOR:-0}" -ge 22 ] 2>/dev/null; then
    ok "Node.js ${NODE_VER} (v22 LTS 以上)"
  else
    ng "Node.js が古いです (検出: ${NODE_VER} / 必要: v22 以上)"
    if $IS_WSL; then
      echo "       → WSL: nvm install 22 && nvm use 22"
    else
      echo "       → nvm:      nvm install 22 && nvm use 22"
      echo "       → Homebrew: brew install node@22"
    fi
  fi
else
  ng "Node.js が見つかりません (FE ビルドに必要)"
  if $IS_WSL; then
    echo "       → WSL: curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash"
    echo "       →      source ~/.bashrc && nvm install 22 && nvm use 22"
  else
    echo "       → nvm install 22 && nvm use 22"
  fi
fi

if command -v npm >/dev/null 2>&1; then
  ok "npm $(npm --version) 利用可"
else
  ng "npm が見つかりません"
fi

# 8.5 フロントエンド構成ファイル
if [ -d "frontend" ]; then
  ok "frontend/ ディレクトリが存在"

  if [ -f "frontend/package.json" ]; then
    ok "frontend/package.json が存在"
  else
    ng "frontend/package.json がありません (雛形が不完全な可能性)"
  fi

  if [ -d "frontend/node_modules" ]; then
    ok "frontend/node_modules が存在 (npm install 済み)"
  else
    ng "frontend/node_modules がありません"
    echo "       → frontend/ ディレクトリで npm install を実行してください"
    echo "       → または ./setup.sh を実行してください"
  fi

  for fe_file in "frontend/vite.config.ts" "frontend/src/App.tsx" "frontend/src/api/client.ts"; do
    if [ -f "$fe_file" ]; then
      ok "$fe_file が存在"
    else
      ng "$fe_file がありません (雛形が不完全な可能性)"
    fi
  done
else
  ng "frontend/ ディレクトリがありません"
  echo "       → ./setup.sh を実行してください"
fi

# 9. Claude Code コマンド
if command -v claude >/dev/null 2>&1; then
  CLAUDE_VER=$(claude --version 2>/dev/null | head -n 1)
  ok "claude コマンド利用可: $CLAUDE_VER"
else
  ng "claude コマンドが PATH にありません"
fi


# 11. クラウド同期フォルダの簡易検知
PWD_LOWER="$(pwd | tr '[:upper:]' '[:lower:]')"
if echo "$PWD_LOWER" | grep -qE 'onedrive|icloud|dropbox|google drive|googledrive|box sync'; then
  warn "クラウド同期フォルダ配下にプロジェクトがある可能性があります"
  echo "       → 同期対象外のフォルダに移動することを推奨(FAQ Q7 参照)"
  if $IS_WSL; then
    echo "       → WSL の場合: ~/projects/ に移動してください"
  fi
fi

echo
echo "=== Result ==="
echo "  Pass: $PASS / Warn: $WARN / Fail: $FAIL"
echo

if [ "$FAIL" -eq 0 ]; then
  echo "✅ All checks passed. You are ready for Day 3 / Day 4."
  echo
  echo "   起動コマンド (macOS / WSL):"
  echo "     BE:  cd backend && ./gradlew bootRun  → http://localhost:8080/actuator/health"
  echo "     FE:  cd frontend && npm run dev       → http://localhost:5173"
  if [ "$WARN" -gt 0 ]; then
    echo
    echo "   ($WARN 件の警告がありますが、研修進行は可能です。気になれば #setup-help で相談を)"
  fi
  exit 0
else
  echo "❌ $FAIL 件の重要な問題があります。Slack #setup-help に出力全文を貼って相談してください。"
  echo "   まず ./setup.sh を実行すると多くの問題が自動解決します。"
  exit 1
fi
