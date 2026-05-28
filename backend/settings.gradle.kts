// ============================================================================
//  AIDD Training - settings.gradle.kts
// ============================================================================
//  foojay リゾルバ:
//    Java 25 のtoolchainがローカルに未インストールの場合、
//    Foojay Disco API 経由で自動的にダウンロード・配置します。
//    Gradle 9 以降では必ず 1.0.0 以上を使用してください
//    (0.x 系は Gradle 9 で動作しません)。
// ----------------------------------------------------------------------------

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "library-app"
