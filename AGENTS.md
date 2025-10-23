# AGENTS.md: 要約筆記プログラム仕様書および開発指示書

**プロジェクト名**: SummaryWriterApp（仮称）  
**作成日**: 2025年10月23日  
**最終更新**: 2025年10月23日 09:22 JST  
**対象 AI エージェント**: `Grok Code Fast1`, `Grok4 Fast` など  
**開発環境**: JetBrains IntelliJ IDEA（Community または Ultimate、2025.2 以降推奨）

## 1. プロジェクト概要
- **目的**: 聴覚障害者向けの要約筆記ツールを開発。リアルタイムでテキストを入力・共有し、字幕として表示。IPTalk（http://www.s-kurita.net）と互換性のある HTTP ベースのテキスト共有プロトコルを採用し、ブラウザやモバイルデバイスで閲覧可能。クロスプラットフォーム対応（macOS Apple Silicon/Intel、Windows 10 以上）。
- **対象プラットフォーム**:
  - Windows 10 以上（x64、JDK 17+）
  - macOS 12.0 以上（Apple Silicon arm64 および Intel x64、JDK 17+）
- **言語/フレームワーク**:
  - Kotlin 2.0.0 / Jetpack Compose for Desktop 1.6.0+
  - UI: Jetpack Compose for Desktop（クロスプラットフォーム UI）
  - Web: Ktor 2.3.0（HTTP サーバーおよび WebSocket）
  - Kotlin Multiplatform（KMP）で共有ロジックを実装
- **互換性目標**: IPTalk の HTTP サーバー機能（`http://<IP>:8080` でのテキストストリーム配信）と互換性のあるエンドポイントを提供。リアルタイム更新は WebSocket（Ktor WebSocket）で実現し、IPTalk のテキストフォーマット（プレーンテキストまたは JSON）を模倣。VPN（SoftEther 相当）での遠隔入力対応。
- **前提条件**:
  - インターネット接続なしでローカル LAN 内で動作可能。
  - 音声認識はオプション（Google Cloud Speech-to-Text または Julius、Apple Silicon 対応ライブラリ必須）。
  - USB カメラ（WebCam）対応、Zoom クローズドキャプション API 統合。
  - Apple Silicon 向けに arm64 ネイティブビルドをサポート。

## 2. 機能要件

### 2.1 必須機能
1. **リアルタイム文字入力**:
   - キーボード入力用のテキストフィールド（Compose の `TextField`）。
   - 入力内容をリアルタイムでローカル表示およびサーバーへ送信（500ms デバウンス、Kotlin Coroutines の `debounce` 使用）。
   - 事前準備文章のインポート（`.txt`、`.csv`）。CSV 形式例: `"timestamp,user,text"`.
2. **字幕表示**:
   - 入力テキストをオーバーレイ表示（Compose の `Text` または `Canvas`）。
   - USB カメラ映像とテキストを合成（`webcam-capture` ライブラリでキャプチャ、Compose の `Image` に描画）。
   - Zoom API（`POST /meetings/{meetingId}/live_transcription`）でクローズドキャプション送信。
3. **HTTP サーバー共有**:
   - Ktor で軽量 HTTP サーバー（デフォルトポート: 8080）。
   - エンドポイント:
     - `GET /text`: プレーンテキスト返却（IPTalk 互換）。
     - `GET /api/text`: JSON 返却。
   - モバイル対応: レスポンシブデザイン（HTML/CSS を Ktor でホスト）。
4. **遠隔連携入力**:
   - 複数ユーザーによる同時入力（Ktor WebSocket でブロードキャスト）。
   - WebSocket エンドポイント: `ws://<IP>:8080/textHub`。
   - ユーザー識別用にセッション ID（UUID）を発行。
5. **プラットフォーム互換**:
   - Jetpack Compose for Desktop で単一コードベース（`build.gradle.kts` でマルチターゲット: `win-x64`, `osx-x64`, `osx-arm64`）。
   - Windows IME 互換（`KeyEvent` で入力補完回避）。
   - macOS Apple Silicon: arm64 ネイティブビルド（`./gradlew packageDmgArm64`）。
6. **アクセシビリティ**:
   - 高コントラストモード（Compose の `MaterialTheme` でテーマ切り替え）。
   - フォントサイズ調整（スライダーで 10pt～32pt）。
   - 音声フィードバック（macOS: `AVSpeechSynthesizer` via JNI、Windows: `com.sun.speech.freetts`）。

### 2.2 オプション機能
1. **音声認識統合**:
   - Google Cloud Speech-to-Text（`com.google.cloud:google-cloud-speech:4.0.0`、arm64 対応）。
   - オープンソース代替: Julius（`julius4j`、Apple Silicon 向けビルド確認）。
   - 音声入力トグル（Compose の `Switch`）。
2. **VPN サポート**:
   - SoftEther VPN クライアント（JNI または `ktor-network` で接続）。
   - 設定画面で VPN 接続情報（IP、ユーザー名、パスワード）入力。
3. **ログ/記録**:
   - 入力履歴を CSV/Excel 出力（`com.opencsv:opencsv:5.9`）。
   - フォーマット例: `"2025-10-23 09:22:00,user1,こんにちは"`.
4. **英語対応**:
   - リソースファイル（`strings.properties`）で日本語/英語切り替え。
   - システムロケールに基づくデフォルト言語設定。

### 2.3 非機能要件
- **パフォーマンス**:
  - リアルタイム更新の遅延 < 1秒（WebSocket ハートビート: 3秒）。
  - メモリ使用量 < 200MB（1000行のテキストバッファ）。
  - Apple Silicon での最適化（arm64 ネイティブコード、Rosetta 2 非依存）。
- **セキュリティ**:
  - HTTPS 対応（Ktor の Netty で TLS、自己署名証明書）。
  - 入力データ暗号化（AES-256、キーはアプリ内で生成）。
- **エラーハンドリング**:
  - ネットワーク切断時、オフラインモードでローカルバッファ保存（`kotlinx.serialization` でファイル保存）。
  - Windows IME および macOS 日本語入力（`InputMethodKit`）の不具合対応。
- **テスト**:
  - 単体テスト（`JUnit5`、90% カバレッジ）。
  - クロスプラットフォーム E2E テスト（GitHub Actions、macOS arm64/x64、Windows x64）。

## 3. アーキテクチャ設計

### 3.1 全体構造
- **フロントエンド**:
  - Jetpack Compose for Desktop で UI 構築（Kotlin ベース）。
  - 主要コンポーネント:
    - `TextInputView`: 入力用 `TextField`（リアルタイム入力）。
    - `TextDisplayView`: 字幕表示用 `Text`（スクロール対応）。
    - `CameraOverlayView`: カメラ映像 + テキスト（`webcam-capture` + `Image`）。
- **バックエンド**:
  - Ktor サーバー（`io.ktor.server.netty`、arm64 最適化）。
  - Ktor WebSocket でリアルタイム通信。
- **データフォーマット**:
  - JSON: `{ "text": "入力内容", "timestamp": "yyyy-MM-dd HH:mm:ss", "user": "入力者ID" }`
  - IPTalk 互換プレーンテキスト: `入力内容\n`（改行区切り）。
- **通信フロー**:
  1. 入力側: `TextField.onValueChange` → WebSocket に送信。
  2. サーバー側: `WebSocketSession.send` でブロードキャスト。
  3. 表示側: WebSocket（`ws://<IP>:8080/textHub`）または HTTP ポーリング（`GET /text`）。

### 3.2 依存ライブラリ
- `org.jetbrains.compose:compose-desktop:1.6.0`（クロスプラットフォーム UI、arm64 対応）
- `io.ktor:ktor-server-netty:2.3.0`（HTTP/WebSocket サーバー）
- `io.ktor:ktor-server-websockets:2.3.0`（リアルタイム通信）
- `com.github.sarxos:webcam-capture:0.3.12`（カメラ処理）
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0`（JSON シリアライズ）
- `com.opencsv:opencsv:5.9`（CSV 出力）
- `com.google.cloud:google-cloud-speech:4.0.0`（音声認識、オプション、arm64 対応）

### 3.3 ディレクトリ構造
```
SummaryWriterApp/
├── src/
│   ├── commonMain/kotlin/
│   │   ├── ui/
│   │   │   ├── TextInputView.kt (入力UI)
│   │   │   ├── TextDisplayView.kt (字幕表示)
│   │   │   ├── CameraOverlayView.kt (カメラ合成)
│   │   ├── services/
│   │   │   ├── TextService.kt (テキスト処理)
│   │   │   ├── WebSocketService.kt (リアルタイム通信)
│   │   │   ├── CameraService.kt (webcam-captureラッパー)
│   │   ├── models/
│   │   │   ├── TextMessage.kt (JSONデータモデル)
│   ├── jvmMain/kotlin/
│   │   ├── server/
│   │   │   ├── TextServer.kt (Ktorサーバー)
│   │   ├── main.kt (アプリケーションエントリーポイント)
├── tests/
│   ├── UnitTests/ (JUnit5テスト)
├── docs/
│   ├── AGENTS.md (この仕様書)
├── build.gradle.kts
├── settings.gradle.kts
```

### 3.4 コード例
#### 3.4.1 テキストデータモデル (`models/TextMessage.kt`)
```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class TextMessage(
    val text: String = "",
    val timestamp: String = java.time.Instant.now().toString(),
    val userId: String = ""
)
```

#### 3.4.2 WebSocket サーバー (`server/TextServer.kt`)
```kotlin
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.textServerModule(textService: TextService) {
    install(WebSockets)
    routing {
        webSocket("/textHub") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val message = Json.decodeFromString<TextMessage>(frame.readText())
                    textService.updateText(message)
                    send(Frame.Text(Json.encodeToString(message)))
                }
            }
        }
        get("/text") {
            call.respondText(textService.getLatestText().text, ContentType.Text.Plain)
        }
        get("/api/text") {
            call.respond(textService.getLatestText())
        }
    }
}
```

#### 3.4.3 入力 UI (`ui/TextInputView.kt`)
```kotlin
import androidx.compose.foundation.text.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

@Composable
fun TextInputView(webSocketService: WebSocketService) {
    val textState = remember { mutableStateOf("") }
    TextField(
        value = textState.value,
        onValueChange = { textState.value = it }
    )
    LaunchedEffect(textState) {
        snapshotFlow { textState.value }
            .debounce(500)
            .onEach { text -> webSocketService.sendText(TextMessage(text, userId = UUID.randomUUID().toString())) }
            .launchIn(this)
    }
}
```

#### 3.4.4 カメラ統合 (`services/CameraService.kt`)
```kotlin
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage

class CameraService {
    private val webcam = Webcam.getDefault()
    suspend fun captureFrame(): BufferedImage = withContext(Dispatchers.IO) {
        webcam.open()
        webcam.image
    }
}
```

#### 3.4.5 テキストサービス (`services/TextService.kt`)
```kotlin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TextService {
    private val _latestText = MutableStateFlow<TextMessage>(TextMessage())
    val latestText: StateFlow<TextMessage> = _latestText

    fun updateText(message: TextMessage) {
        _latestText.value = message
    }

    fun getLatestText(): TextMessage = _latestText.value
}
```

#### 3.4.6 アプリケーションエントリーポイント (`jvmMain/kotlin/main.kt`)
```kotlin
import androidx.compose.ui.window.application
import io.ktor.server.netty.*

fun main() {
    // Ktor サーバー起動
    embeddedServer(Netty, port = 8080) {
        textServerModule(TextService())
    }.start(wait = false)

    // Compose アプリケーション起動
    application {
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
}
```

## 4. IntelliJ IDEA でのプロジェクトセットアップ
以下の手順で、IntelliJ IDEA を使用してプロジェクトをセットアップします。AI エージェント（`Grok Code Fast1`, `Grok4 Fast`）は、これらの手順を参考にプロジェクトファイルを生成します。

### 4.1 IntelliJ IDEA 環境準備
1. **IntelliJ IDEA のインストール**:
   - JetBrains IntelliJ IDEA（Community または Ultimate、2025.2 以降）をインストール:
     ```bash
     winget install JetBrains.IntelliJIDEA.Community
     ```
   - 必要プラグイン:
     - Kotlin プラグイン（デフォルトで有効）
     - Gradle プラグイン（デフォルトで有効）
     - Jetpack Compose プラグイン（`File > Settings > Plugins > Marketplace` から「Compose」を検索してインストール）
2. **JDK 17 の設定**:
   - IntelliJ IDEA で JDK 17 を設定:
     - `File > Project Structure > SDKs > Add SDK > Download JDK`
     - ベンダー: Eclipse Temurin、バージョン: 17
   - プロジェクト SDK を設定:
     - `File > Project Structure > Project > Project SDK` を JDK 17 に設定

### 4.2 プロジェクトの作成
1. **新規プロジェクト作成**:
   - IntelliJ IDEA を起動し、`File > New > Project` を選択。
   - テンプレート: `Kotlin > JVM | Desktop | Compose for Desktop`
   - プロジェクト名: `SummaryWriterApp`
   - 場所: `~/Documents/SummaryWriterApp`
   - Kotlin DSL（`build.gradle.kts`）を選択。
   - プロジェクト SDK: JDK 17
2. **Gradle 設定**:
   - IntelliJ IDEA が生成した `build.gradle.kts` を以下に更新:
     ```kotlin
     plugins {
         kotlin("multiplatform") version "2.0.0"
         id("org.jetbrains.compose") version "1.6.0"
     }
     group = "com.example"
     version = "1.0-SNAPSHOT"

     repositories {
         mavenCentral()
         maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
     }

     kotlin {
         jvm {
             withJava()
         }
         sourceSets {
             val commonMain by getting {
                 dependencies {
                     implementation(compose.desktop.common)
                     implementation("io.ktor:ktor-server-netty:2.3.0")
                     implementation("io.ktor:ktor-server-websockets:2.3.0")
                     implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                     implementation("com.github.sarxos:webcam-capture:0.3.12")
                     implementation("com.opencsv:opencsv:5.9")
                     implementation("com.google.cloud:google-cloud-speech:4.0.0")
                 }
             }
             val jvmMain by getting {
                 dependencies {
                     implementation(compose.desktop.jvm)
                 }
             }
         }
     }

     tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
         kotlinOptions.jvmTarget = "17"
     }
     ```
   - **AI エージェント指示**: `Grok Code Fast1` は、`build.gradle.kts` を生成し、依存ライブラリを IntelliJ IDEA の Gradle 同期で検証。
3. **プロジェクト構造の設定**:
   - IntelliJ IDEA でディレクトリ構造を作成:
     ```bash
     mkdir -p src/commonMain/kotlin/ui
     mkdir -p src/commonMain/kotlin/services
     mkdir -p src/commonMain/kotlin/models
     mkdir -p src/jvmMain/kotlin/server
     mkdir -p tests/UnitTests
     mkdir -p docs
     touch src/commonMain/kotlin/ui/TextInputView.kt
     touch src/commonMain/kotlin/ui/TextDisplayView.kt
     touch src/commonMain/kotlin/ui/CameraOverlayView.kt
     touch src/commonMain/kotlin/services/TextService.kt
     touch src/commonMain/kotlin/services/WebSocketService.kt
     touch