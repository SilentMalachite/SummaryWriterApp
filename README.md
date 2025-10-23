# SummaryWriterApp - 要約筆記アプリケーション

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple)](https://kotlinlang.org/)
[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()

聴覚障害者向けのリアルタイム要約筆記ツールです。

## 📖 目次
- [概要](#概要)
- [機能](#機能)
- [必要な環境](#必要な環境)
- [インストール](#インストール)
- [使い方](#使い方)
- [APIエンドポイント](#apiエンドポイント)
- [開発](#開発)
- [トラブルシューティング](#トラブルシューティング)
- [貢献](#貢献)
- [ライセンス](#ライセンス)

## 概要

聴覚障害者向けのリアルタイム要約筆記ツールです。

IPTalkと互換性のあるHTTPベースのテキスト共有プロトコルを採用し、複数のデバイス（PC、スマートフォン、タブレット）で同時にリアルタイムテキストを表示できます。

### 主な特徴
- 🚀 **リアルタイム同期**: WebSocketによる低遅延テキスト配信
- 🌐 **クロスプラットフォーム**: Windows・macOS対応
- 📱 **マルチデバイス**: ブラウザからアクセス可能
- 🎥 **カメラ統合**: Webカメラ映像とテキストの合成表示
- 🔒 **プライバシー**: ローカルネットワーク内で完結
- 🎨 **カスタマイズ**: フォントサイズ、コントラストモード調整可能

## 必要な環境

- **Java**: JDK 17 以上を推奨（JDK 21でも動作確認済み）
  - **注意**: Java 25 では現在ビルドエラーが発生します
- **Kotlin**: 2.0.0
- **OS**: macOS 12.0以上（Apple Silicon arm64 および Intel x64対応）、Windows 10以上

## Java バージョンの確認と変更

現在のJavaバージョンを確認:
```bash
java -version
```

このプロジェクトは **JDK 17以上** で動作します。現在 **JDK 21** でビルド設定されています。

### macOS での Java バージョン変更

Homebrewでインストールされている場合:
```bash
# JDK 21をインストール（推奨）
brew install openjdk@21

# または JDK 17をインストール
brew install openjdk@17

# シンボリックリンクを作成（21の場合）
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# 環境変数を設定 (~/.zshrc に追加)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

SDKMANを使用する場合（推奨）:
```bash
# SDKMANのインストール
curl -s "https://get.sdkman.io" | bash

# JDK 21をインストールして使用
sdk install java 21.0.8-zulu
sdk use java 21.0.8-zulu

# または JDK 17をインストール
sdk install java 17.0.12-zulu
sdk use java 17.0.12-zulu
```

### JDKバージョンを17に変更する場合

`build.gradle.kts` の以下の行を変更:
```kotlin
kotlin {
    jvmToolchain(17)  // 21 から 17 に変更
}
```

## インストール

### 方法1: リリースから実行ファイルをダウンロード（推奨）
1. [Releases](https://github.com/SilentMalachite/SummaryWriterApp/releases)から最新版をダウンロード
2. macOSの場合: `.dmg`ファイルを開いてインストール
3. Windowsの場合: `.msi`ファイルを実行してインストール

### 方法2: ソースからビルド

```bash
# リポジトリをクローン
git clone https://github.com/SilentMalachite/SummaryWriterApp.git
cd SummaryWriterApp

# ビルド
./gradlew build

# テスト実行
./gradlew test

# アプリケーション起動
./gradlew run
```

### 方法3: パッケージの作成

```bash
# macOS用DMG作成
./gradlew packageDmg

# Windows用MSI作成（Windows上で実行）
./gradlew packageMsi

# Linux用DEB作成（Linux上で実行）
./gradlew packageDeb
```

## 使い方

### 1. アプリケーションの起動

デスクトップアプリケーションを起動すると、自動的にHTTPサーバーが `http://localhost:8080` で起動します。

### 2. テキスト入力

「入力」タブでテキストを入力します。入力内容は自動的に全ての接続されたクライアントに配信されます。

### 3. 字幕表示

#### デスクトップアプリで表示
「字幕表示」タブで入力されたテキストをリアルタイム表示します。

#### ブラウザで表示
別のデバイスのブラウザから `http://<PCのIPアドレス>:8080` にアクセスすると、字幕が表示されます。

#### カメラ映像と合成
「カメラ表示」タブでWebカメラ映像と字幕を合成表示できます。

### 4. 設定

「設定」タブで以下をカスタマイズできます：
- フォントサイズ（10pt～32pt）
- 高コントラストモード
- 履歴のクリア

## 機能

- ✅ リアルタイムテキスト入力と表示
- ✅ WebSocket経由でのリアルタイム同期
- ✅ HTTP APIエンドポイント（IPTalk互換）
- ✅ カメラ映像とテキストの合成表示
- ✅ ブラウザからの字幕表示対応
- ✅ 複数クライアント同時接続対応
- ✅ Java 21最適化済み（スレッドセーフ、並行処理）

## APIエンドポイント

アプリケーション起動後、以下のエンドポイントが利用可能です:

- `http://localhost:8080/` - Webブラウザ用字幕表示画面（改善版UI）
- `http://localhost:8080/text` - プレーンテキスト取得 (IPTalk互換)
- `http://localhost:8080/api/text` - JSON形式でのテキスト取得
- `http://localhost:8080/api/history` - テキスト履歴の取得
- `http://localhost:8080/api/stats` - サーバー統計情報（接続数、メッセージ数など）
- `ws://localhost:8080/textHub` - WebSocketエンドポイント（リアルタイム双方向通信）

## トラブルシューティング

### ビルドエラー: "IllegalArgumentException: 25"

これはJava 25の互換性問題です。JDK 17または21に変更してください。

### Java 21で動作確認済み

このプロジェクトは以下のバージョンで動作確認済みです:
- OpenJDK 21.0.8 (Zulu)
- Kotlin 2.0.0
- Gradle 8.5
- Jetpack Compose for Desktop 1.6.11

### Gradleデーモンのリセット

問題が解決しない場合、Gradleデーモンをリセット:
```bash
./gradlew --stop
./gradlew clean build
```

## プロジェクト構造

```
SummaryWriterApp/
├── src/
│   ├── main/kotlin/
│   │   ├── ui/              # UIコンポーネント
│   │   ├── server/          # Ktorサーバー
│   │   ├── services/        # ビジネスロジック
│   │   ├── models/          # データモデル
│   │   └── Main.kt          # エントリーポイント
│   └── test/kotlin/         # テストコード
├── build.gradle.kts         # ビルド設定
└── README.md                # このファイル
```

## 開発

### ビルド環境
このプロジェクトは以下の技術で構築されています：
- **言語**: Kotlin 2.0.0
- **フレームワーク**: Jetpack Compose for Desktop 1.6.11
- **サーバー**: Ktor 2.3.12
- **ビルドツール**: Gradle 8.5

### 開発者向けドキュメント
- [AGENTS.md](AGENTS.md) - AI開発エージェント向けプロジェクト仕様書
- [CONTRIBUTING.md](CONTRIBUTING.md) - 貢献ガイドライン
- [docs/JAVA21_MIGRATION.md](docs/JAVA21_MIGRATION.md) - Java 21移行詳細

## 貢献

プロジェクトへの貢献を歓迎します！詳細は [CONTRIBUTING.md](CONTRIBUTING.md) をご覧ください。

### 貢献方法
1. このリポジトリをフォーク
2. 新しいブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'feat: Add amazing feature'`)
4. ブランチにプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを作成

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。詳細は [LICENSE](LICENSE) ファイルをご覧ください。

## 謝辞

- [IPTalk](http://www.s-kurita.net) - 要約筆記プロトコルの参考
- JetBrains - Kotlin & Compose for Desktop
- Ktor チーム - Webサーバーフレームワーク

## 関連リンク

- [GitHub Repository](https://github.com/SilentMalachite/SummaryWriterApp)
- [Issue Tracker](https://github.com/SilentMalachite/SummaryWriterApp/issues)
- [Discussions](https://github.com/SilentMalachite/SummaryWriterApp/discussions)

---

**Made with ❤️ for the hearing impaired community**
