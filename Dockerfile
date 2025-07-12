# Java 17のベースイメージを使用
FROM openjdk:17-jdk-slim

# 作業ディレクトリを設定
WORKDIR /app

# 必要なパッケージをインストール
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Gradleラッパーをコピー
COPY gradlew .
COPY gradle gradle
COPY build.gradle .

# 依存関係をダウンロード
RUN ./gradlew dependencies

# ソースコードをコピー
COPY src src

# アプリケーションをビルド
RUN ./gradlew build -x test

# 必要なディレクトリを作成
RUN mkdir -p worlds plugins logs backups config

# ポート25565（Minecraft）と8080（HTTP）を公開
EXPOSE 25565 8080

# 起動スクリプトを作成
RUN echo '#!/bin/bash\n\
echo "Starting HTTP health check server..."\n\
java -cp build/libs/minecraft-server-1.0.0.jar com.minecraft.server.network.HttpServer &\n\
echo "HTTP Server started in background"\n\
echo "Starting Minecraft Server..."\n\
java -jar build/libs/minecraft-server-1.0.0.jar' > start.sh && chmod +x start.sh

# アプリケーションを起動
CMD ["./start.sh"] 