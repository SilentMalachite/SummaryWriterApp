# 貢献ガイドライン

SummaryWriterAppへの貢献に興味を持っていただきありがとうございます！

## 開発環境のセットアップ

### 必要な環境
- JDK 17以上（JDK 21推奨）
- Git
- IntelliJ IDEA（推奨）またはお好みのIDE

### クローンとビルド
```bash
git clone https://github.com/SilentMalachite/SummaryWriterApp.git
cd SummaryWriterApp
./gradlew build
```

## 貢献の方法

### 1. Issue の作成
バグ報告や機能提案は、まずIssueを作成してください。

**バグ報告に含めるべき情報:**
- OS（macOS/Windows）とバージョン
- Javaバージョン
- 再現手順
- 期待される動作
- 実際の動作
- エラーメッセージ（あれば）

**機能提案に含めるべき情報:**
- 機能の概要
- ユースケース
- 期待される効果

### 2. プルリクエストの作成

1. **フォーク**
   ```bash
   # GitHubでリポジトリをフォーク
   git clone https://github.com/YOUR_USERNAME/SummaryWriterApp.git
   ```

2. **ブランチを作成**
   ```bash
   git checkout -b feature/your-feature-name
   # または
   git checkout -b fix/your-bug-fix
   ```

3. **変更を実装**
   - コーディング規約に従う（下記参照）
   - テストを追加/更新
   - ドキュメントを更新

4. **コミット**
   ```bash
   git add .
   git commit -m "feat: 機能の説明"
   ```

5. **プッシュとPR作成**
   ```bash
   git push origin feature/your-feature-name
   ```
   その後、GitHubでプルリクエストを作成

## コミットメッセージ規約

コミットメッセージは以下の形式に従ってください：

```
<type>: <subject>

<body>
```

**Type:**
- `feat`: 新機能
- `fix`: バグ修正
- `docs`: ドキュメントのみの変更
- `style`: コードの意味に影響しない変更（フォーマットなど）
- `refactor`: バグ修正でも機能追加でもないコード変更
- `perf`: パフォーマンス改善
- `test`: テストの追加・修正
- `chore`: ビルドプロセスやツールの変更

**例:**
```
feat: WebSocket接続の統計情報エンドポイントを追加

/api/stats エンドポイントを追加し、以下の情報を提供：
- アクティブ接続数
- 総メッセージ数
- 履歴サイズ
```

## コーディング規約

### Kotlin
- Kotlin公式スタイルガイドに従う
- 関数名はlowerCamelCase
- クラス名はUpperCamelCase
- 定数はUPPER_SNAKE_CASE
- インデント: 4スペース

### コメント
- 複雑なロジックには説明コメントを追加
- 公開APIにはKDocコメントを追加
- TODOコメントには Issue番号を含める

### テスト
- 新機能には必ずテストを追加
- テストカバレッジは80%以上を目指す
- テスト名は `should〜When〜` 形式

## レビュープロセス

1. すべてのテストが通過すること
2. ビルドが成功すること
3. コードレビューで承認を得ること
4. Conflictがないこと

## 質問・サポート

- Issue: バグ報告・機能提案
- Discussions: 一般的な質問・議論

## 行動規範

- 敬意を持ってコミュニケーション
- 建設的なフィードバックを心がける
- 多様性を尊重する

## ライセンス

貢献したコードはMITライセンスの下でリリースされます。
