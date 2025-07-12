# Java 17のベースイメージを使用
FROM openjdk:17-jdk-slim

# 作業ディレクトリを設定
WORKDIR /app

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

# ポート25565を公開
EXPOSE 25565

# ヘルスチェック用のポート8080も公開
EXPOSE 8080

# アプリケーションを起動
CMD ["java", "-jar", "build/libs/minecraft-server-1.0.0.jar"] 