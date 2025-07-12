# Minecraft サーバー

本物のMinecraftサーバーを構築するプロジェクトです。

## 🎯 目標

- Minecraft 1.20+ 対応のサーバー
- マルチプレイヤー対応
- プラグインシステム
- ワールド管理
- プレイヤー認証

## 🛠️ 技術スタック

- **Java 17+**: メイン言語
- **Netty**: ネットワーク通信
- **NBT**: ワールドデータ形式
- **Protocol Buffers**: パケット通信
- **SQLite/MySQL**: データベース

## 📁 プロジェクト構造

```
Minecraft Emu/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── minecraft/
│   │   │           ├── server/
│   │   │           │   ├── MinecraftServer.java
│   │   │           │   ├── network/
│   │   │           │   ├── world/
│   │   │           │   ├── player/
│   │   │           │   └── plugin/
│   │   │           └── protocol/
│   │   └── resources/
│   └── test/
├── build.gradle
├── server.properties
└── README.md
```

## 🚀 セットアップ

### ローカル実行

#### 前提条件
- Java 17 以上
- Gradle 7.0 以上

#### ビルド
```bash
./gradlew build
```

#### 実行
```bash
./gradlew run
```

### Render上での実行

#### 1. GitHubにプッシュ
```bash
git add .
git commit -m "Add Render deployment support"
git push origin main
```

#### 2. Renderでデプロイ
1. [Render](https://render.com)にサインアップ
2. "New Web Service"を選択
3. GitHubリポジトリを接続
4. 以下の設定でデプロイ:
   - **Name**: minecraft-server
   - **Environment**: Java
   - **Build Command**: `./gradlew build`
   - **Start Command**: `java -jar build/libs/minecraft-server-1.0.0.jar`
   - **Plan**: Starter (無料)

#### 3. 環境変数の設定
Renderのダッシュボードで以下の環境変数を設定:
- `SERVER_PORT`: 25565
- `MAX_PLAYERS`: 20
- `ONLINE_MODE`: false
- `MOTD`: "Minecraft Server on Render"

#### 4. 接続
デプロイ完了後、Renderが提供するURLでサーバーに接続できます。

## 🔧 設定

`server.properties`ファイルでサーバー設定を変更できます：

```properties
server-port=25565
max-players=20
gamemode=survival
difficulty=normal
spawn-protection=16
```

## 📋 実装予定機能

- [x] 基本的なサーバー構造
- [ ] プレイヤー接続処理
- [ ] ワールド生成
- [ ] ブロック操作
- [ ] インベントリ管理
- [ ] チャットシステム
- [ ] コマンドシステム
- [ ] プラグインAPI

## 🎮 クライアント接続

標準的なMinecraftクライアント（1.20+）で接続可能：

```
サーバーアドレス: localhost:25565
```

## 📝 開発ログ

- プロジェクト初期化
- 基本的なサーバー構造の設計
- ネットワークレイヤーの実装予定

---

**注意**: このプロジェクトは教育目的で作成されています。 