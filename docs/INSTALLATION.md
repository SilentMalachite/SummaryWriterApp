# インストールガイド

## Windows

### システム要件
- Windows 10 以上（64bit）
- JDK 17以上（アプリに同梱されている場合は不要）
- 空きディスク容量: 500MB以上

### インストール手順

1. **インストーラーのダウンロード**
   - [Releases](https://github.com/SilentMalachite/SummaryWriterApp/releases)から最新の`.msi`ファイルをダウンロード

2. **インストール**
   - ダウンロードした`.msi`ファイルをダブルクリック
   - インストールウィザードの指示に従う
   - インストール先フォルダを選択（デフォルト: `C:\Program Files\SummaryWriterApp`）
   - 「インストール」をクリック

3. **起動**
   - スタートメニューから「SummaryWriterApp」を検索して起動
   - または、デスクトップのショートカットから起動

### トラブルシューティング

**起動しない場合:**
1. Java 17以上がインストールされているか確認
   ```cmd
   java -version
   ```
2. Windows Defenderの除外設定を確認
3. 管理者権限で実行

## macOS

### システム要件
- macOS 12.0 (Monterey) 以上
- Apple Silicon (M1/M2/M3) または Intel (x64)
- 空きディスク容量: 500MB以上

### インストール手順

1. **DMGのダウンロード**
   - [Releases](https://github.com/SilentMalachite/SummaryWriterApp/releases)から最新の`.dmg`ファイルをダウンロード
   - Apple Siliconの場合: `SummaryWriterApp-arm64.dmg`
   - Intelの場合: `SummaryWriterApp-x64.dmg`

2. **インストール**
   - ダウンロードした`.dmg`ファイルをダブルクリック
   - アプリケーションアイコンを「Applications」フォルダにドラッグ

3. **初回起動**
   - Applicationsフォルダから「SummaryWriterApp」を右クリック
   - 「開く」を選択（初回のみ、Gatekeeperの警告を回避）
   - 「開く」をクリック

### セキュリティ設定

macOSのセキュリティ機能により、初回起動時に警告が表示される場合があります。

**対処方法:**
1. システム設定 → プライバシーとセキュリティ
2. 「このまま開く」をクリック

または、ターミナルで以下を実行：
```bash
xattr -cr /Applications/SummaryWriterApp.app
```

## Linux

### システム要件
- Ubuntu 20.04以上、Debian 11以上、または同等のディストリビューション
- JDK 17以上
- 空きディスク容量: 500MB以上

### インストール手順

1. **DEBパッケージのダウンロード**
   ```bash
   wget https://github.com/SilentMalachite/SummaryWriterApp/releases/latest/download/summarywriterapp_1.0.0_amd64.deb
   ```

2. **インストール**
   ```bash
   sudo dpkg -i summarywriterapp_1.0.0_amd64.deb
   sudo apt-get install -f  # 依存関係を解決
   ```

3. **起動**
   ```bash
   summarywriterapp
   ```

## ソースからビルド

開発者向けに、ソースコードからビルドする方法を説明します。

### 前提条件
- JDK 17以上
- Git

### 手順

1. **リポジトリのクローン**
   ```bash
   git clone https://github.com/SilentMalachite/SummaryWriterApp.git
   cd SummaryWriterApp
   ```

2. **ビルド**
   ```bash
   ./gradlew build
   ```

3. **実行**
   ```bash
   ./gradlew run
   ```

4. **パッケージ作成（オプション）**
   ```bash
   # macOS
   ./gradlew packageDmg
   
   # Windows
   ./gradlew packageMsi
   
   # Linux
   ./gradlew packageDeb
   ```

## ネットワーク設定

### ファイアウォール設定

アプリケーションはポート8080を使用します。他のデバイスから接続する場合は、ファイアウォールでこのポートを開放してください。

**Windows:**
```powershell
New-NetFirewallRule -DisplayName "SummaryWriterApp" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

**macOS:**
システム設定 → ネットワーク → ファイアウォール → ファイアウォールオプション → 「SummaryWriterApp」を許可

**Linux (ufw):**
```bash
sudo ufw allow 8080/tcp
```

### IPアドレスの確認

**Windows:**
```cmd
ipconfig
```

**macOS/Linux:**
```bash
ifconfig
# または
ip addr show
```

`192.168.x.x` または `10.0.x.x` のようなアドレスを確認し、ブラウザで `http://<IPアドレス>:8080` にアクセスします。

## アンインストール

### Windows
1. コントロールパネル → プログラムと機能
2. 「SummaryWriterApp」を選択
3. 「アンインストール」をクリック

### macOS
1. Applicationsフォルダから「SummaryWriterApp.app」をゴミ箱にドラッグ
2. ゴミ箱を空にする

### Linux
```bash
sudo apt-get remove summarywriterapp
```

## サポート

問題が発生した場合は、[Issue Tracker](https://github.com/SilentMalachite/SummaryWriterApp/issues)で報告してください。
