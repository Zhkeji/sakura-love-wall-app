# 表白墙 Android 原生应用

## ⚠️ 重要：修改服务器地址

打开 `app/src/main/java/com/lovewall/app/api/ApiClient.java`，修改：

```java
public static final String BASE_URL = "https://your-domain.com";
```

将 `your-domain.com` 改为你的表白墙网站地址。

## 使用 AIDE Pro 构建

1. 安装 [AIDE Pro](https://play.google.com/store/apps/details?id=com.aidepro.aide)
2. 将 `love-wall-app` 文件夹复制到手机
3. AIDE Pro 打开项目
4. 点击运行构建 APK

## 使用 Android Studio 构建

1. Android Studio 打开项目
2. Gradle 同步
3. Build → Build APK

## 功能

- ✅ 浏览表白墙（最新/最热/评论排序）
- ✅ 登录 / 注册
- ✅ 发布表白（文字 + 标签 + 匿名）
- ✅ 点赞 / 评论
- ✅ 私信聊天
- ✅ 个人资料编辑
- ✅ 检查更新 & 版本号

## 版本更新

修改 `app/build.gradle` 中的版本号：
```
versionCode 2
versionName "1.1.0"
```

更新服务器端 `public/api/app/version.json`：
```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "https://your-domain.com/downloads/love-wall-latest.apk",
  "updateLog": "更新内容"
}
```
