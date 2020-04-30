## Spring boot开源项目之个人博客（5）—搭建博客详情页

一个页面做好后，其他页面做起来就慢慢有感觉了，详情页主要进行博客内容的展示和用户的评论，整体排版比较简单，导航栏和页脚直接沿用于首页，主体主要是由几个segment包裹，下面主要写一下主体的构成。

1. 头部和图片区域

头部和图片区域都比较容易，头部直接把之前的拿过来用就行

~~~html
![详情主体1](D:\note\target\Springboot博客开源项目笔记\一些截图\详情主体1.png)<div class="ui horizontal link list">
    <div class="item">
        <img class="ui avatar image" src="https://picsum.photos/id/100/100/100" alt="">
        <div class="content">
            <a class="header">Chenjiaxun</a>
        </div>
    </div>
    <div class="item">
        <i class="calendar alternate outline icon"></i>2017-10-01
    </div>
    <div class="item">
        <i class="eye icon"></i>2342
    </div>
</div>
~~~

至于图像，需要加一个fluid类，使图片填充满整个容器。

2. 内容

内容这部分首先是有个原创的标志，然后是文章展示，在文章尾部加上文章的所属标签，最后加一个赞赏的功能。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\详情主体1.png)

此处文章内容省去

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\详情主体2.png)

文章展示这部分只是一个样板，之后会用一个插件专门做文章展示的排版，然后就是这个赞赏功能有点难度，用到了popup，在点击是会弹出支付宝和微信的收款码。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\详情主体3.png)

popup的使用方法可以在Semantic官网手册上查到，二维码部分使用了flowing popup

~~~html
<div class="ui payQR flowing popup transition hidden">
    <div class="ui orange basic label">
        <div class="ui images" style="font-size: inherit !important;">
            <div class="image">
                <img src="./static/images/wechat.png" alt="" class="ui rounded bordered image"
                     style="width: 120px">
                <div>支付宝</div>
            </div>
            <div class="image">
                <img src="./static/images/wechat.png" alt="" class="ui rounded bordered image"
                     style="width: 120px">
                <div>微信</div>
            </div>
        </div>
    </div>
</div>
~~~

payQR用来在jQuery种定位元素，而赞赏按钮是用id进行定位的

~~~html
<script>
    $('#payButton').popup({
        popup: $('.payQR.popup'), //表示弹出的元素
        on: 'click',  //表示点击时生效
        position: "bottom center"  //表示显示的位置（相对于按钮）
    });
</script>
~~~

3. 博客信息

这部分用message包裹，class里面的positive定义了元素的颜色。

4. 评论

Semantic官方文档有一节是专门说明评论的写法的，稍加改动就可以做成想要的样子。

![](D:\note\target\Springboot博客开源项目笔记\一些截图\前端\详情主体4.png)

这个页面大概就是这样，最后做一下手机端的适配工作就好。