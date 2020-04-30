###  Spring boot开源项目之个人博客（9）—使用themleaf调整前端模板文件

这个前端模板目前是分为两大块，一个是前端展示，一个是后台管理。把项目迁移到idea之后，一些本地的静态文件路径都是有问题的，而目前在head、导航和页脚部分相似度是很高的，而本地文件的引入基本也都是在head中，所以用themleaf模板的fragment特性来做就非常合适。跟着视频做了一下，前端的修修改改也很复杂，没必要把每个步骤全记下来，这里就针对一点来记录下fragments的使用方式，以后用的时候回头来看这篇文章能回想起使用的方法就够了。后面呢，也就是想把最关键的那部分知识提取出来，记录使用方法和说明以及遇到的问题，这比一步一步去记录要更舒服，也更有效率。

#### 1. fragments 使用

fragments功能的关键词其实就是复用，重复的东西放一起，起个名字，什么地方需要用了就来拿。这个项目所有的fragment全部放在_fragments.html文件中

~~~html
<nav th:fragment="menu(n)">
    <div class="ui inverted attached segment m-padded-tb-mini m-shadow-small">
        <div class="ui container">
            <div class="ui inverted secondary stackable menu">
                <h2 class="ui teal header item">Blog</h2>
                <a href="#" class="m-item item m-mobile-hide" th:classappend="${1==n} ? 'active'"><i class="small home icon"></i>首页</a>
                <a href="#" class="m-item item m-mobile-hide" th:classappend="${2==n} ? 'active'"><i class="small idea outline icon"></i>分类</a>
                <a href="#" class="m-item item m-mobile-hide" th:classappend="${3==n} ? 'active'"><i class="small tags icon"></i>标签</a>
                <a href="#" class="m-item item m-mobile-hide" th:classappend="${4==n} ? 'active'"><i class="small copy outline icon"></i>归档</a>
                <a href="#" class="m-item item m-mobile-hide" th:classappend="${5==n} ? 'active'"><i class="small info icon"></i>关于我</a>
                <div class="right m-item item m-mobile-hide">
                    <div class="ui icon input">
                        <input type="text" placeholder="search...">
                        <i class="search link icon"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <a href="#" class="ui menu toggle black button m-right-top m-mobile-show"><i class="sidebar icon"></i></a>
</nav>
~~~

`th:fragment="menu(n)"`定义了一个fragment的名字，还可以传参数，下面这个是在其中一个页面的引用

~~~html
<nav th:replace="_fragments :: menu(1)">
    <div class="ui inverted attached segment m-padded-tb-mini m-shadow-small">
        <div class="ui container">
            <div class="ui inverted secondary stackable menu">
                <h2 class="ui teal header item">Blog</h2>
                <a href="#" class="active m-item item m-mobile-hide"><i class="small home icon"></i>首页</a>
                <a href="#" class="m-item item m-mobile-hide"><i class="small idea outline icon"></i>分类</a>
                <a href="#" class="m-item item m-mobile-hide"><i class="small tags icon"></i>标签</a>
                <a href="#" class="m-item item m-mobile-hide"><i class="small copy outline icon"></i>归档</a>
                <a href="#" class="m-item item m-mobile-hide"><i class="small info icon"></i>关于我</a>
                <div class="right m-item item m-mobile-hide">
                    <div class="ui icon input">
                        <input type="text" placeholder="search...">
                        <i class="search link icon"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <a href="#" class="ui menu toggle black button m-right-top m-mobile-show"><i class="sidebar icon"></i></a>
</nav>
~~~

`th:replace="_fragments :: menu(1)"`就表示这个标签会用_fragments.html中名为menu的fragment替换掉，并且传了一个参数1进去。这样一个fragment其实就做好了，但是它的使用并没有这么简单，在实际开发中，即便重复的模板也还是存在有差别的地方，就比如这个导航栏，它需要在顶部菜单高亮显示当前正在浏览的页面

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\active_use.png)

所以在上面的实例中才传了一个参数进去，它的功能就是控制哪个菜单高亮的，实现方式是用`th:classappend="${1==n} ? 'active'"`，当三元表达式结果为true的时候，就把active加到class中，这样就实现了传特定的参数就能让对应的菜单高亮显示。

当然除了`th:replace`外还可以使用`th:insert`，他们的区别是：`th:replace`会把标签替换成fragment对应的标签，而`th:insert`是在标签内直接插入fragment的内容。看一个实例：

有一个html的fragment如下：

~~~html
<footer th:fragment="copy">
  &copy; 2011 The Good Thymes Virtual Grocery
</footer>
~~~

用三种不同的方法插入：

~~~html
<body>

  ...

  <div th:insert="footer :: copy"></div>

  <div th:replace="footer :: copy"></div>

  <div th:include="footer :: copy"></div>
  
</body>
~~~

结果如下：

~~~html
<body>

  ...

  <div>
    <footer>
      &copy; 2011 The Good Thymes Virtual Grocery
    </footer>
  </div>

  <footer>
    &copy; 2011 The Good Thymes Virtual Grocery
  </footer>

  <div>
    &copy; 2011 The Good Thymes Virtual Grocery
  </div>
  
</body>
~~~

`th:include`在theamleaf3中已经不推荐使用了，使用最多的还是`th:replace`。

fragment的基本使用就是这样，在此基础上还是要根据实际需求来查阅官方文档慢慢学习。就按照这种方法，把head、nav、footer、script标签都做成fragment，在对应位置用fragment替换掉，这样这些通用的部分一旦有改动，直接改fragment就可以了，免去了一个一个修改的麻烦。

#### 2. 实际项目中应用到的知识点

一个一个做fragment就不记录了，就把一些零碎知识点都整理一下。

_fragments.html

~~~html
<head th:fragment="head(title)">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:replace="${title}">博客详情</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.css">
    <link rel="stylesheet" href="../static/css/typo.css" th:href="@{/css/typo.css}">
    <link rel="stylesheet" href="../static/css/animate.css" th:href="@{/css/animate.css}">
    <link rel="stylesheet" href="../static/lib/prism/prism.css" th:href="@{/lib/prism/prism.css}">
    <link rel="stylesheet" href="../static/lib/tocbot/tocbot.css" th:href="@{/lib/tocbot/tocbot.css}">
    <link rel="stylesheet" href="../static/css/me.css" th:href="@{/css/me.css}">
</head>
~~~

index.html

~~~html
<head th:replace="_fragments :: head(~{::title})">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>首页</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.css">
    <link rel="stylesheet" href="../static/css/me.css" th:href="@{/css/me.css}">
</head>
~~~

- 一种传参的方式

`th:fragment="head(title)"`定义fragment的形参为title，这个实现的是一个带标签的传参，把首页的title标签及里面的内容取出来传进去来替代fragment里面的对应标签。这种特性就使得fragment具有复用性的同时又能针对一些差异进行定制化改造，非常好用。

- 链接一般引入格式

在theamleaf中，引入本地或者网络资源一般格式是这样的`th:href="@{/css/me.css}"`（css样式表格式）、`th:src="@{/images/wechat.png}"`（图片、js等文件格式）。

- 不带标签引入fragment

引入js文件时，通常不会有其他的标签包裹，又不能一个一个去定义fragment，这时就可以使用`<th:block th:replace="_fragments :: script">`把这部分定义为一个块

~~~html
<th:block th:fragment="script">
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.2.1/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.js"></script>
    <script src="//cdn.jsdelivr.net/npm/jquery.scrollto@2.1.2/jquery.scrollTo.min.js"></script>
    <script src="../static/lib/prism/prism.js" th:src="@{/lib/prism/prism.js}"></script>
    <script src="../static/lib/tocbot/tocbot.min.js" th:src="@{/lib/tocbot/tocbot.min.js}"></script>
    <script src="../static/lib/qrcode/qrcode.min.js" th:src="@{/lib/qrcode/qrcode.min.js}"></script>
    <script src="../static/lib/waypoints/jquery.waypoints.min.js" th:src="@{/lib/waypoints/jquery.waypoints.min.js}"></script>
    <script src="../../static/lib/editormd/editormd.min.js" th:src="@{/lib/editormd/editormd.min.js}"></script>
</th:block>
~~~

在首页中可以这样引入

~~~html
<!--/*/<th:block th:replace="_fragments :: script">/*/-->
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.2.1/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.js"></script>
<!--/*/</th:block>/*/-->
~~~

这部分是配合theamleaf的仅原型注释语法使用，应用它主要基于这样的考虑，在html原型中，不存在`<th:block >`这样的语法，所以要想使用这个就需要配合theamleaf的<!--/*/ and /*/-->仅原型注释使用，这里面的and是我们自己的内容，比如`<!--/*/</th:block>/*/-->`，这个注释就使得在静态页面中这里面的“/\*/ and /\*/”这部分会被作为注释处理，不会报错，而经过theamleaf渲染之后，“and”里面的内容就会被当做真正的内容而不是注释出现。所以`<th:block >`一般都会和<!--/*/and /*/-->注释格式一起使用。

fragments的使用基本上就这些，下一篇要记录一下jpa的使用。