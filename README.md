```text
   __  ____          ___         ___   
  /  |/  (_)__  ___ / _ \___ ___/ (_)__
 / /|_/ / / _ \/ -_) , _/ -_) _  / (_-<
/_/  /_/_/_//_/\__/_/|_|\__/\_,_/_/___/
```

# MineRedis

[![version](https://img.shields.io/github/v/release/CarmJos/MineRedis)](https://github.com/CarmJos/MineRedis/releases)
[![License](https://img.shields.io/github/license/CarmJos/MineRedis)](https://opensource.org/licenses/GPL-3.0)
[![workflow](https://github.com/CarmJos/MineRedis/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/CarmJos/MineRedis/actions/workflows/maven.yml)
![CodeSize](https://img.shields.io/github/languages/code-size/CarmJos/MineRedis)
[![CodeFactor](https://www.codefactor.io/repository/github/carmjos/MineRedis/badge)](https://www.codefactor.io/repository/github/carmjos/MineRedis)
![](https://visitor-badge.glitch.me/badge?page_id=MineRedis.readme)

适用于Redis的独立运行库插件，支持多种服务端，适用于MineCraft全版本。

## 优势

## 安装

1. 从 [Releases(发行)](https://github.com/CarmJos/MineRedis/releases/)
   中点击 [最新版](https://github.com/CarmJos/MineRedis/releases/latest) 下载 `MineRedis-x.y.z.jar` 。
2. 将下载的 `MineRedis-x.y.z.jar` 放入服务器 `plugins/` 文件夹下。
3. 启动服务器，预加载配置文件后关闭服务器。
4. 修改 `plugins/MineRedis/config.yml` 以配置您的数据库选项。
5. 启动服务器，若配置无误，则您会看到 MineRedis一切正常的提示消息。

## 配置

### 插件配置文件 [`config.yml`](.doc/example-config.yml)

完整示例配置请见 [示例配置文件](.doc/example-config.yml)。

## 指令

插件主指令为 `/MineRedis` ，所有指令只允许后台执行。

```text
# help
- 查看插件指令帮助。

# version
- 查看当前插件、核心库(lettuce-core)与连接池依赖版本。

# list
- 列出当前所有的数据源管理器与相关信息。

# info <数据源名称>
- 查看指定数据源的统计信息与当前仍未关闭的查询。
```

## 开发

### 依赖方式

<details>
<summary>展开查看 Maven 依赖方式</summary>

```xml

<project>
    <repositories>

        <repository>
            <!--采用Maven中心库，安全稳定，但版本更新需要等待同步-->
            <id>maven</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
        </repository>

        <repository>
            <!--采用github分支依赖库，稳定更新快-->
            <id>MineRedis</id>
            <name>GitHub Branch Repository</name>
            <url>https://raw.githubusercontent.com/CarmJos/MineRedis/repo/</url>
        </repository>

        <repository>
            <!--采用我的私人依赖库，简单方便，但可能因为变故而无法使用-->
            <id>carm-repo</id>
            <name>Carm's Repo</name>
            <url>https://repo.carm.cc/repository/maven-public/</url>
        </repository>

    </repositories>

    <dependencies>

        <dependency>
            <groupId>cc.carm.plugin</groupId>
            <artifactId>MineRedis-api</artifactId>
            <version>[LATEST RELEASE]</version>
            <scope>provided</scope>
        </dependency>

    </dependencies>
</project>
```

</details>

<details>
<summary>展开查看 Gradle 依赖方式</summary>

```groovy
repositories {

    //采用Maven中心库，安全稳定，但版本更新需要等待同步
    mavenCentral()

    // 采用github分支依赖库，稳定更新快
    maven { url 'https://raw.githubusercontent.com/CarmJos/MineRedis/repo/' }

    // 采用我的私人依赖库，简单方便，但可能因为变故而无法使用
    maven { url 'https://repo.carm.cc/repository/maven-public/' }
}

dependencies {
    compileOnly "cc.carm.plugin:MineRedis-api:[LATEST RELEASE]"
}
```

</details>

### 操作示例

本插件接口入口类为 `MineRedis` ，更多方法详见 [MineRedis-Javadoc](https://carmjos.github.io/MineRedis/) 。

关于 LettuceRedis的使用方法，请详见 [Lettuce开发文档](https://github.com/lettuce-io/lettuce-core/wiki) 。


<details>
  <summary>点击查看简单实例</summary>

```java

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        // do something...
      
    }

}

```

</details> 

## 开源协议

本项目源码采用 [GNU General Public License v3.0](https://opensource.org/licenses/GPL-3.0) 开源协议。

<details>
  <summary>关于 GPL 协议</summary>

> GNU General Public Licence (GPL) 有可能是开源界最常用的许可模式。GPL 保证了所有开发者的权利，同时为使用者提供了足够的复制，分发，修改的权利：
>
> #### 可自由复制
> 你可以将软件复制到你的电脑，你客户的电脑，或者任何地方。复制份数没有任何限制。
> #### 可自由分发
> 在你的网站提供下载，拷贝到U盘送人，或者将源代码打印出来从窗户扔出去（环保起见，请别这样做）。
> #### 可以用来盈利
> 你可以在分发软件的时候收费，但你必须在收费前向你的客户提供该软件的 GNU GPL 许可协议，以便让他们知道，他们可以从别的渠道免费得到这份软件，以及你收费的理由。
> #### 可自由修改
> 如果你想添加或删除某个功能，没问题，如果你想在别的项目中使用部分代码，也没问题，唯一的要求是，使用了这段代码的项目也必须使用
> GPL 协议。
>
> 需要注意的是，分发的时候，需要明确提供源代码和二进制文件，另外，用于某些程序的某些协议有一些问题和限制，你可以看一下
> @PierreJoye 写的 Practical Guide to GPL Compliance 一文。使用 GPL 协议，你必须在源代码代码中包含相应信息，以及协议本身。
>
> *以上文字来自 [五种开源协议GPL,LGPL,BSD,MIT,Apache](https://www.oschina.net/question/54100_9455) 。*
</details> 
