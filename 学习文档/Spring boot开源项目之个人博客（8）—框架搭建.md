## Spring boot开源项目之个人博客（8）—框架搭建

项目使用idea作为开发IDE，接下来先新建一个springboot项目。

#### 1. 新建项目，引入springboot模块

打开idea，点击新建项目

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\框架搭建1.png)

选中spring initializer这个选项，这是idea为我们提供的一个springboot自定义初始化工具，设置如上图所示，然后点击next。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\框架搭建2.png)

填写好项目的信息，type选择maven project，其他的都是自动配置好的，点击next。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\框架搭建3.png)

这个开源项目需要引入web、Thymeleaf、JPA、MySQL、aspects、devtools等组件，由于idea版本不同，初始化配置时组件的名称也有所变化，在idea2019版本中，对应的配置如上图所示，这里也可以把lombok选上，在构建实体类的时候会用得上。另外，aspects这个组价在初始化中是找不到的，也不用担心，在项目构建好之后再从maven的pom文件里导入相关依赖就可以了。选好之后点击next，最后直接点击finish完成项目的创建。

现在一个springboot的项目就创建好了，可以来看一下项目结构

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\框架搭建5.png)

log是后面在日志配置后才生成的文件夹，先不用管。src里面存放的是整个项目程序文件，java文件夹存放java后端代码，resource存放前端代码文件等。resource里static存放前端的一些静态文件css、js、image等等，template存放h5页面模板。在项目刚刚创建完，有一个application.properties配置文件，这里需要把配置文件后缀改为yml，这种格式的配置文件更加灵活简洁，是现在主流的springboot项目的配置文件格式。test文件夹是做单元测试用的。最后一个很重要的文件就是pom.xml，这是maven的配置文件，刚刚我们构建项目时并没有加入aspects组件，现在我们可以动手尝试通过添加maven依赖的形式把我们想要的组件添加进去。

~~~xml
<dependencies>
    <!--AOP-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <!--JPA-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <!--thymeleaf-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <!--web-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!--devtools-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    <!--mysql-->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!--junit-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.vintage</groupId>
                <artifactId>junit-vintage-engine</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.8</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
~~~

aspects是spring的切面，主要针对web请求做日志处理，组件名称是aop，其他的都是我们当初构建项目时添加的组件，我们可以看到，本质上就是idea帮我们把相应依赖自动添加到了pom文件中。

#### 2. 项目配置

配置文件分三份，一份环境通用的配置，一份开发环境中的配置，一份生产环境的配置。

application.yml

~~~yml
spring:
  thymeleaf:
    mode: HTML
  profiles:
    active: dev   //指定使用的配置文件
~~~

无论在哪个环境中，这部分的配置永远生效。

application-dev.yml

~~~yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf-8
    username: root
    password: chenjiaxun2@

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    root: info
    com.chenj: debug
  file:
    path: ./log
~~~

application-pro.yml

~~~yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/blog?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true
    username: root
    password: chenjiaxun2@

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true

logging:
  level:
    root: warn
    com.chenj: info
  file: log/blog-pro.log
server:
  port: 8081
~~~

需要注意的是，配置文件的命名必须按照这样的规则命名，这样在主配置文件中指定的相应配置文件才能生效。

另外这里对日志文件也做了处理，通过对logback的重写，指定日志文件的大小、每份日志的大小、日志命名等属性，不过，不知道出了什么问题，刚建立项目时可以实现功能的，到了后来再做的时候，日志文件就不能按指定的格式生成了，这个bug尚未解决。

来补一下，这个bug解决了。还是日志文件配置有问题，见logback配置文件

logback-spring.xml

~~~xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <!--包含Spring boot对logback日志的默认配置-->
    <include resource="org/springframework/boot/logging/logback/defaults.xml" />
    <!--这里加上springProperty这一项，在下面就可以直接识别到logging.file.path对应的属性值了-->
    <springProperty scope="context" name="logging.file.path" source="logging.file.path"/>
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}}/spring.log}"/>
    <include resource="org/springframework/boot/logging/logback/console-appender.xml" />

    <!--重写了Spring Boot框架 org/springframework/boot/logging/logback/file-appender.xml 配置-->
    <appender name="TIME_FILE"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>

        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--这里${logging.file.path}取出了logging.file.path的值作为路径，再加上“/希望的文件名”就可以了-->
            <fileNamePattern>${logging.file.path}/blog-dev.%d{yyyy-MM-dd}.%i</fileNamePattern>
            <!--保留历史日志一个月的时间-->
            <maxHistory>30</maxHistory>
            <!--
            Spring Boot默认情况下，日志文件10M时，会切分日志文件,这样设置日志文件会在100M时切分日志
            -->
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>

        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="TIME_FILE" />
    </root>

</configuration>
~~~

文件名加上-spring是spring官方推荐的命名。这回对logback配置文件熟悉了不少，整体配置都在，也知道了一些细节该怎么配置，以后真用到了就小修小改就完事了，也对logging.file和logging.file.path的机制了解了不少，总算知道怎么写会实现怎样的效果了。

另外还需要新建一个数据库

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\框架搭建4.png)

我这里用的是navicat for mysql，这是一个数据库可视化管理软件，建好之后运行项目测试，发现报错，这个好像也没保存下来，经过查资料，最后是数据库时区的问题，在mysql8中必须指定时区，在配置文件中加上后缀

~~~
?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true
~~~

但是，加上后缀后仍然不行，最后试了一下找到数据库的配置文件，在里面加上默认时区，然后重启mysql服务，最后成功解决。mysql8用起来相当烦，莫名其妙就会出各种问题，如果没什么要求最好还是用低版本的mysql。

#### 3. 导入页面模板

由于之前是在Webstorm上做的前端模板，现在要把文件全都复制到我们现在这个项目中，当然如果一开始就在idea中开发，就免了这些麻烦，复制的时候注意勾选自动检查的那个选项，完了之后，模板中静态文件的路径很多都需要修改，这时就用到了动态模板引擎themleaf做些处理。这部分将在下一篇中记录，至此，这个项目的框架就已经搭建好了。



