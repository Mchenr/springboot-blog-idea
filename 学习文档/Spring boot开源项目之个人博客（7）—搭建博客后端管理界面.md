## Spring boot开源项目之个人博客（7）—搭建博客后端管理界面

后端管理页面主要包括博客管理页和博客发布页。内容不多，主要是模板做好之后需要集成一下插件。

### 1. 博客管理页面

导航栏的变化主要在右边，把搜索框换成了用户头像和用户名，并且加了一个dropdown的效果，在点击它的时候会弹出一个有注销用户的下拉菜单。这一块的实现使用了semantic的dropdown效果

~~~html
<div class="ui dropdown item">
    <!--要显示的部分-->
    <div class="text">
        <img class="ui avatar image" src="https://picsum.photos/id/100/100/100">
        Chen
    </div>
     <!--倒三角的图标-->
    <i class="dropdown icon"></i>
     <!--dropdown的内容-->
    <div class="menu">
        <a href="#" class="item">注销</a>
    </div>
</div>
~~~

这个做好了之后点击用户名并不会出现下拉框，原因是我们还需要加入dropdown的js实现

~~~js
$('.ui.dropdown')
    .dropdown()

;
~~~

这个功能在后面的页面设计仍要多次使用，结合semantic官方文档找到想要的样式套用即可。

另外，我们加了一个二级导航，用于发布页和列表页的切换。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\导航栏变化.png)

~~~html
<div class="ui attached pointing menu">
    <div class="ui container">
        <div class="right menu">
            <a href="#" class="item">发布</a>
            <a href="#" class="teal active item">列表</a>
        </div>
    </div>
</div>
~~~

主体部分分为两部分：筛选搜索和列表展示。

筛选搜索使用form表单，用inline把各个field排成一行；列表展示使用了table

~~~html
<table class="ui celled table">
    <!--表头-->
    <thead>
        <!--tr(行）-->
        <tr>
            <!--th(列)-->
            <th></th>
            <th>标题</th>
            <th>类型</th>
            <th>推荐</th>
            <th>更新时间</th>
            <th>操作</th>
        </tr>
    </thead>
    <!--表主体-->
    <tbody>
        <tr>
            <td>1</td>
            <td>刻意练习清单</td>
            <td>认知升级</td>
            <td>是</td>
            <td>2019-12-25</td>
            <td>
                <a href="#" class="ui mini teal basic button">编辑</a>
                <a href="#" class="ui mini red basic button">删除</a>
            </td>
        </tr>
    </tbody>
    <!--表尾-->
    <tfoot>
        <tr>
            <th colspan="6">
                <div class="ui mini pagination menu">
                    <a class="item">上一页</a>
                    <a class="item">下一页</a>
                </div>
                <a href="#" class="ui mini right floated teal basic button">新增</a>
            </th>
        </tr>
    </tfoot>
</table>
~~~

### 2. 博客发布页面

这个页面用于发布博客，主体部分主要有博客标题、博客内容、分类标签等定义、是否可评论转载和保存发布按钮。

整个主体都在一个表单中，提交方式为post。一般来说发布博客标题必须要输入，所以当没有输出标题时会提示错误信息，在标题对应的field加上required，然后定义一个div用于展示错误信息

~~~html
<div class="ui error message"></div>
~~~

另外还要配合js定位，给出一定的规则

~~~js
$('.ui.form').form({
      fields:{
          title:{
              identifier: 'title',  //对应元素的name
              rules:[{
                  type: 'empty',   //定义的规则，当输入为空的时候
                  prompt : '标题: 请输入标题'  //推送的信息
              }]
          }
      }
    });
~~~

其他的一点点做就好了，做法大概都是差不多的。

### 3. 插件集成

1. Markdown插件集成

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\Markdown插件.png)

这是博客发布页中集成了插件的效果，首先要先把插件下载下来( [Markdown插件官网](https://pandao.github.io/editor.md/ ))

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\Markdown插件下载1.png)

点击下载安装

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\Markdown插件下载2.png)

点击GitHub下载，这时会下载一个插件的压缩包，下载好之后解压把相关文件加入项目中，文件目录如下

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\Markdown插件结构.png)

引入css、js文件，这个插件也需要jQuery支持，不过引入semantic框架时已经引用过了，这里就不需要再引入jQuery。

~~~html
<link rel="stylesheet" href="../static/lib/editormd/css/editormd.min.css">
<script src="../static/lib/editormd/editormd.min.js"></script>
~~~

通过参照插件给的简单案例，把插件相关代码敲进去

~~~html
<div class="field">
    <!--通过z-index把这个放在最上层，使之全屏可覆盖所有元素-->
    <div id="md-content" style="z-index: 1 !important;">
        <textarea name="content" placeholder="博客内容" style="display: none">[TOC]

            #### Disabled options

            - TeX (Based on KaTeX);
            - Emoji;
            - Task lists;
            - HTML tags decode;
            - Flowchart and Sequence Diagram;

        </textarea>
    </div>
</div>
~~~

~~~js
var contentEditor;
    $(function() {
        contentEditor = editormd("md-content", {
            width   : "100%",
            height  : 640,
            syncScrolling : "single",
            path    : "../static/lib/editormd/lib/"
        });

    });
~~~

至此，这个插件就集成好了。其余的插件基本都在博客详情页，就不一个一个记录了，主要是加了一个目录工具栏，使用button、popup基本都能完成。