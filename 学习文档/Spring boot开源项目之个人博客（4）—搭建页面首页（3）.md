## Spring boot开源项目之个人博客（4）—搭建页面首页（3）

首页的最后一部分就是让它能与手机屏幕适配，在Semantic中也有相应的方法。

第一个用到的是stackable，这是一个可以应用于menu和grid等标签的class，效果是当屏幕宽度变窄时，自动将元素向下堆叠，以导航栏来说，我们希望达到以下效果：

之前做好的导航栏是这样的：

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\手机适配导航栏1.PNG)

到了手机端时，把菜单栏隐藏起来，加一个按钮，点击则显示被隐藏的菜单栏：

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\手机适配导航栏2.PNG)

当点击右上方的按钮时，菜单栏被隐藏/显示。这部分的实现主要是由写css完成的。首先给menu加一个stackable的class，然后在css文件中定义一下菜单元素被隐藏的条件，

~~~css
@media screen and (max-width: 768px){
    .m-mobile-show {
        display: block !important;
    }
}
~~~

最后把我们自定义的样式加到想要隐藏的元素的class中就完成了。

~~~html
<div class="ui inverted secondary stackable menu">
    <h2 class="ui teal header item">Blog</h2>
    <a href="#" class="m-item item m-mobile-hide"><i class="mini home icon"></i>首页</a>
    <a href="#" class="m-item item m-mobile-hide"><i class="mini idea outline icon"></i>分类</a>
    <a href="#" class="m-item item m-mobile-hide"><i class="mini tags icon"></i>标签</a>
    <a href="#" class="m-item item m-mobile-hide"><i class="mini copy outline icon"></i>归档</a>
    <a href="#" class="m-item item m-mobile-hide"><i class="mini info icon"></i>关于我</a>
    <div class="right m-item item m-mobile-hide">
        <div class="ui icon input">
            <input type="text" placeholder="search...">
            <i class="search link icon"></i>
        </div>
    </div>
</div>
~~~

其余部分就是简单的用stackable将需要堆叠的部分排好版，没什么需要改变的地方了，可以根据个人喜好适当调整样式，使得整个页面看起来美观舒服。

最后，我们可以给所有的页面加一个背景图片，[Subtle Patterns]( https://www.toptal.com/designers/subtlepatterns/ ) 是个不错的网站，在上面可以寻找自己喜欢的背景图片。加背景图片的方法：

~~~css
body{
    background: url("../images/background.png");
}
~~~

另外还要注意，在引用样式表的时候，一定要把自己定义的放在后面

~~~html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.css">
<link rel="stylesheet" href="./static/css/me.css">
~~~

我一开始把me.css放在了上面，背景图片就显示不出来，也不知道具体是什么原因。

至此，首页部分就基本完成了，下一篇开始做博客详情页。