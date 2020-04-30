## Spring boot开源项目之个人博客（2）—搭建页面首页（1）

上一篇把项目大体情况进行了说明，从这篇开始就慢慢跟着视频一步一步做下去，页面设计就跳过了，有兴趣的话可以试着自己设计一下，我是直接跟着视频，直接去做页面，H5页面是用Webstorm写的，我这边正好有这个软件，也就用这个，没有的用idea也完全可以实现。

**首页布局**

首先，新建项目，项目目录最好和springboot的目录保持一致，方便后面整合。

![页面结构](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\页面结构.PNG)

**index.html**文件就作为我们的首页页面，首先要把Semantic UI框架的css、js文件引入（引入方法可以去官网查看），此外和bootstrap一样，还需要引入jquery文件，我都是用CDN方式引入的。

~~~html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.css">
<script src="https://cdn.jsdelivr.net/npm/jquery@3.2/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.js"></script>

~~~



然后就开始进行页面的开发了，整个首页分成了三个部分：导航栏、主体、页脚，我们先做导航栏和页脚。

**导航栏**

~~~html
<nav class="ui inverted attached segment m-padded-tb-mini">
    <div class="ui container">
        <div class="ui inverted secondary menu" >
            <h2 class="ui teal header item">Blog</h2>
            <a href="#" class="item"><i class="home icon"></i> 首页</a>
            <a href="#" class="item"><i class="idea icon"></i>分类</a>
            <a href="#" class="item"><i class="tags icon"></i>标签</a>
            <a href="#" class="item"><i class="info icon"></i>关于我</a>
            <div class="right item">
                <div class="ui icon input">
                    <input type="text" placeholder="search....">
                    <i class="search link icon"></i>
                </div>
            </div>
        </div>
    </div>
</nav>
~~~



**页脚**

~~~html
<footer class="ui inverted vertical segment m-padded-tb-massive">
    <div class="ui center aligned container">
        <div class="ui inverted divided grid">
            <div class="three wide column">
                <div class="ui inverted link list">
                   <div class="item">
                       <img src="./static/images/wechat.png" class="ui rounded image" alt="" style="width: 110px ">
                   </div>
                </div>
            </div>
            <div class="three wide column">
                <h4 class="ui inverted header m-text-thin m-text-spaced">最新博客</h4>
                <div class="ui inverted link list">
                    <a href="#" class="item">用户故事（User Story）</a>
                    <a href="#" class="item">用户故事（User Story）</a>
                    <a href="#" class="item">用户故事（User Story）</a>
                </div>
            </div>
            <div class="three wide column">
                <h4 class="ui inverted header m-text-thin m-text-spaced">联系我</h4>
                <div class="ui inverted link list">
                    <a href="#" class="item">email: chenjiaxun2@163.com</a>
                    <a href="#" class="item">qq: 369392973</a>
                </div>
            </div>
            <div class="seven wide column">
                <h4 class="ui inverted header m-text-thin m-text-spaced">Chen</h4>
                <p class="m-text-thin m-text-spaced m-opacity-mini">这是我的个人博客，会分享关于编程、写作、思考相关的任何内容，希望可以给来到这的人有所帮助...</p>
            </div>
        </div>
        <div class="ui inverted section divider"></div>
        <p class="m-text-thin m-text-spaced m-opacity-mini">Copyright © 2019-2020 Blog Designed by Chen</p>
    </div>
</footer>
~~~

这里只把页面制作的结构安排说明一下，各个元素的样式设计细节就不描述了，写起来比较琐碎复杂，碰到不清楚作用的自己再查一查资料。另外，在设计时有时需要自定义一些样式，统一命名为`m-**-**`，比如一个元素的边距padded就是这样定义的，

~~~css
.m-padded-middle{
    padding: 1em !important;
}
~~~

可以用middle、mini等描述所定义的边距大小，如果定义一组这样的样式，以后也可以直接拿来用，再做些细微调整就可以了。

**效果图**

![导航栏页脚](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\导航栏页脚.PNG)

老师设计的页面很美观，自己设计的话可能还有点困难，这个还是需要有点前端基础的，起码应该知道基本的H5标签和css的属性有什么用，剩下的就是跟着学一下Semantic框架。我开始是一句一句跟着敲，对UI框架的使用有了一定了解后就先看一段，然后自己试着敲出来，不记得的再去看视频，用到什么就去[Semantic的官网]( https://semantic-ui.com/introduction/advanced-usage.html )学习一下官方文档。

