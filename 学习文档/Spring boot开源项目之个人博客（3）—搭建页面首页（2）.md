## Spring boot开源项目之个人博客（3）—搭建页面首页（2）

本篇是做首页的主体内容。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\主页1.PNG)

主页分为了两个部分（博客列表和标签分类），所以我们可以用**grid**分别做这两部分，semantic中的grid和bootstrap还有点区别，bootstrap是将页面分为了12份，而semantic是将页面分为了**16**份，这里左半边博客列表分了11份，右边分类标签栏分了5份。

**博客列表：**

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\主页2.png)

分为上下两部分，上部是一个列表头，下部是文章列表。这半边可以用**segment**来做。

头部由一个segment包裹，划分两列grid，按样式把内容填进去，

~~~html
<div class="ui top attached segment">
    <div class="ui middle aligned two column grid">
        <div class="column">
            <h3 class="ui teal header">博客</h3>
        </div>
        <div class="right aligned column">
            共 <h3 class="ui orange header m-text-thin m-inline-block">14</h3> 篇
        </div>
    </div>
</div>
~~~

列表内容同样是用segment做，再利用segment下的attached属性配合，把segment拼接到一起。

~~~html
<div class="ui attached segment">
    <div class="ui padded vertical segment m-padded-middle">
        <div class="ui grid">
            <div class="eleven wide column">
                <h3 class="ui header">安利|7件不到百元提升幸福感的好物分享</h3>
                <p class="m-text">大家好，我是Rain 这是一篇安利文，下列所有推荐都是由我自己用过的心头好，会愿意去回购的产品，分享给大家。 1、 TangleAngel天使梳...</p>
                <div class="ui grid">
                    <div class="eleven wide column">
                        <div class="ui mini horizontal link list">
                            <div class="item">
                                <img src="https://picsum.photos/id/1/100/100" alt="" class="ui avatar image">
                                <div class="content"><a href="#" class="header">Chen</a> </div>
                            </div>
                            <div class="item">
                                <i class="calendar icon"></i> 2019-11-11
                            </div>
                            <div class="item">
                                <i class="eye icon"></i> 222
                            </div>
                        </div>
                    </div>
                    <div class="right aligned five wide column">
                        <a href="#" target="_blank" class="ui teal basic label m-padded-mini m-text-thin">认识升级</a>
                    </div>
                </div>
            </div>
            <div class="five wide column">
                <a href="#" target="_blank">
                    <img src="https://picsum.photos/id/1/800/600" alt="" class="ui rounded image">
                </a>
            </div>
        </div>
    </div>
</div>
~~~

每一篇的博客详情展示又有其单独的布局，**右边**展示一张图片，**左边**有文章标题、文章开头摘抄及文章作者信息等。标题就用**<h3\>**做，文章摘抄用**\<p\>**。文章信息又是一个grid，**左边**用一个水平**list**，**右边**的标签以链接形式展示。

一篇博客展示做好后，再复制几个先把页面撑起来，再来做右半部分。

**标签分类：**

右边这部分一共有分类、标签、最新推荐、二维码这四个部分，前三个结构大体相同，先拿**分类**来说。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\主页3.PNG)

分成标题栏和分类列表栏两个部分，标题还是用两列的grid，分类标签栏则是用垂直menu包含一个个item，分类标签用\<a\>描述。

后面的标签、最新推荐结构安排和分类这部分相差不多，按照想要的效果多尝试下就可以了。

最后是放一个二维码，我们需要用一个分割线把这部分和上面区分开

~~~html
<h4 class="ui horizontal divider header">扫码关注我</h4>
~~~

然后放二维码图片时是用了一个card属性

~~~html
<div class="ui centered card" style="width: 11em">
    <img src="./static/images/wechat.png" class="ui rounded image">
</div>
~~~



这样整个页面的结构安排和内容的填充就做好了，再根据个人喜好对页面做些调整首页部分就基本完成了。不过最后要抛出一个问题，我们把页面拉伸到手机浏览的页面比例后发现，页面的结构被破坏，并没有根据尺寸适配，所以做首页的最后一步就是做些调整，使之能适配手机的浏览模式。