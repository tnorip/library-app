#!/usr/bin/env bash
# AIDD Training — 環境セットアップスクリプト
# Usage: ./setup.sh
# プロジェクトルート(CLAUDE.md / backend/ / frontend/ があるディレクトリ)で実行してください
#
# 実行内容:
#   1. 必須ツール(Java / Node.js)のバージョン確認
#   2. gradlew に実行権限付与
#   3. バックエンド: ./gradlew build (依存ダウンロード + コンパイル)
#   4. フロントエンド: npm install
#
# Windows ユーザー: WSL または Git Bash 上で実行してください

set -uo pipefail

# ── WSL 検出 ─────────────────────────────────────────────
IS_WSL=false
if grep -qi microsoft /proc/version 2>/dev/null || [ -n "${WSL_DISTRO_NAME:-}" ]; then
  IS_WSL=true
fi

# ── 色・記号定義 ──────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; BOLD='\033[1m'; NC='\033[0m'

step() { echo; echo -e "${BOLD}${BLUE}▶ $1${NC}"; }
ok()   { echo -e "  ${GREEN}✓${NC} $1"; }
warn() { echo -e "  ${YELLOW}⚠${NC} $1"; }
fail() { echo -e "  ${RED}✗${NC} $1"; }
hint() { echo -e "    ${YELLOW}→${NC} $1"; }

ERRORS=0
err() { fail "$1"; ERRORS=$((ERRORS+1)); }

# ── ヘッダー ──────────────────────────────────────────────
echo
echo -e "${BOLD}╔══════════════════════════════════════════╗${NC}"
echo -e "${BOLD}║  AIDD Training — 環境セットアップ        ║${NC}"
echo -e "${BOLD}╚══════════════════════════════════════════╝${NC}"
echo

# ── プロジェクトルート確認 ────────────────────────────────
if [ ! -f "CLAUDE.md" ] || [ ! -d "backend" ] || [ ! -d "frontend" ]; then
  fail "プロジェクトルートが見つかりません (CLAUDE.md / backend/ / frontend/ が必要)"
  hint "library-app ディレクトリで実行してください"
  exit 1
fi
ok "プロジェクトルート確認"

# ── WSL: Windows ファイルシステム上の警告 ─────────────────
if $IS_WSL; then
  if [[ "$(pwd)" == /mnt/* ]]; then
    warn "WSL 環境で Windows ファイルシステム(/mnt/...) 上で実行しています"
    hint "パフォーマンスとファイルロック回避のため WSL ネイティブファイルシステムを推奨:"
    hint "  cp -r . ~/projects/library-app && cd ~/projects/library-app && ./setup.sh"
  else
    ok "WSL ネイティブファイルシステム上で実行中"
  fi
fi

# ── ダウンロード内容の事前告知 ────────────────────────────
echo
echo -e "${BOLD}【このスクリプトが行うこと】${NC}"
echo "  プロジェクト内:"
echo "    - backend の依存ライブラリをダウンロード・コンパイル (backend/build/)"
echo "    - frontend の npm パッケージをインストール (frontend/node_modules/)"
echo
echo "  ホームディレクトリ(初回のみ):"
echo "    - Gradle 本体          ~/.gradle/wrapper/    (約 120 MB)"
echo "    - Java 25 toolchain    ~/.gradle/jdks/       (約 350 MB) ← Java 未インストールの場合"
echo "    - Gradle 依存キャッシュ ~/.gradle/caches/    (Maven Central から取得)"
echo "    - npm キャッシュ        ~/.npm/               (npmjs.org から取得)"
echo
echo "  変更しないもの: PATH / .bashrc / .zshrc / グローバル npm / sudo 不使用"
echo
echo -n "続けますか？ [y/N]: "
read -r CONFIRM
if [[ ! "${CONFIRM}" =~ ^[Yy]$ ]]; then
  echo "キャンセルしました"
  exit 0
fi

# ── Java 確認 ─────────────────────────────────────────────
step "Java の確認"

if command -v java >/dev/null 2>&1; then
  JAVA_FULL=$(java -version 2>&1 | head -1)
  JAVA_MAJOR=$(java -version 2>&1 | head -1 | sed 's/.*version "\([0-9]*\).*/\1/')
  if [ "${JAVA_MAJOR:-0}" -ge 25 ] 2>/dev/null; then
    ok "Java ${JAVA_MAJOR} 検出 (${JAVA_FULL})"
  else
    warn "Java が古いです (検出: ${JAVA_FULL} / 推奨: 25)"
    hint "Gradle toolchain が初回ビルド時に Java 25 を自動ダウンロードします"
    hint "手動インストール: brew install --cask temurin@25"
    hint "              または: sdk install java 25-tem  (SDKMAN)"
  fi
else
  warn "java コマンドが見つかりません"
  hint "Gradle toolchain が初回ビルド時に Java 25 を自動ダウンロードします(数分かかります)"
fi

# ── Node.js 確認 ──────────────────────────────────────────
step "Node.js の確認 (v22 LTS 以上が必要)"

if command -v node >/dev/null 2>&1; then
  NODE_FULL=$(node --version)
  NODE_MAJOR=$(echo "${NODE_FULL}" | sed 's/v\([0-9]*\).*/\1/')
  if [ "${NODE_MAJOR:-0}" -ge 22 ] 2>/dev/null; then
    ok "Node.js ${NODE_FULL} 検出"
  else
    err "Node.js が古いです (検出: ${NODE_FULL} / 必要: v22 以上)"
    if $IS_WSL; then
      hint "WSL: nvm install 22 && nvm use 22"
    else
      hint "nvm:      nvm install 22 && nvm use 22"
      hint "Homebrew: brew install node@22 && brew link node@22 --force"
      hint "公式:     https://nodejs.org/ja/download"
    fi
  fi
else
  err "Node.js が見つかりません"
  if $IS_WSL; then
    hint "WSL: curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash"
    hint "     source ~/.bashrc && nvm install 22 && nvm use 22"
  else
    hint "nvm:      nvm install 22 && nvm use 22"
    hint "Homebrew: brew install node@22 && brew link node@22 --force"
    hint "公式:     https://nodejs.org/ja/download"
  fi
fi

# 必須ツール不足なら早期終了
if [ "${ERRORS}" -gt 0 ]; then
  echo
  fail "必須ツールが不足しています。上記の手順でインストール後、./setup.sh を再実行してください"
  echo "  解決できない場合は #setup-help に出力全文を貼って相談してください"
  exit 1
fi

# ── gradlew 実行権限 ──────────────────────────────────────
step "gradlew の実行権限を設定"
chmod +x backend/gradlew
ok "backend/gradlew に実行権限を付与"

# ── バックエンド: Gradle ビルド ───────────────────────────
step "バックエンド: Gradle ビルド"
echo "  ※ 初回は Java 25 / 依存ライブラリのダウンロードで数分かかる場合があります"
echo

pushd backend > /dev/null
if ./gradlew build -x test 2>&1 | sed 's/^/    /'; then
  ok "バックエンド ビルド成功"
else
  err "バックエンド ビルド失敗"
  hint "backend/ ディレクトリで ./gradlew build を単独実行してエラーを確認してください"
fi
popd > /dev/null

# ── フロントエンド: npm install ───────────────────────────
step "フロントエンド: npm install"

if [ ! -d "frontend" ]; then
  err "frontend/ ディレクトリがありません"
  hint "雛形が不完全な可能性があります。講師に確認してください"
elif [ ! -f "frontend/package.json" ]; then
  err "frontend/package.json がありません"
  hint "雛形が不完全な可能性があります。講師に確認してください"
else
  echo
  pushd frontend > /dev/null
  if npm install 2>&1 | sed 's/^/    /'; then
    ok "npm install 完了"
  else
    err "npm install 失敗"
    hint "frontend/ ディレクトリ内で npm install を手動実行してください"
  fi
  popd > /dev/null
fi

# ── 結果サマリー ──────────────────────────────────────────
echo
echo -e "${BOLD}══════════════════════════════════════════${NC}"

if [ "${ERRORS}" -eq 0 ]; then
  echo -e "${GREEN}${BOLD}✅ セットアップ完了!${NC}"
  echo
  echo "  次のコマンドで起動できます:"
  echo "    BE 起動:  cd backend && ./gradlew bootRun"
  echo "    FE 起動:  cd frontend && npm run dev"
  echo
  echo "  起動後の確認:"
  echo "    BE:  http://localhost:8080/actuator/health"
  echo "    FE:  http://localhost:5173"
  echo
  echo "  環境チェック: ./check-setup.sh"
else
  echo -e "${RED}${BOLD}❌ ${ERRORS} 件のエラーがあります${NC}"
  echo "  上記のエラーを解消して ./setup.sh を再実行してください"
  echo "  解決できない場合は #setup-help に出力全文を貼って相談してください"
  exit 1
fi
