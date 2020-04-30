### Spring boot开源项目之个人博客（15）—博客详情页面展示

博客详情页分为两个部分，一个是博客详情信息的渲染，一个是评论功能。在这之前，做了一个全局搜索的功能。

#### 1. 全局搜索

在导航栏有一个搜索框，定义搜索的功能是输入字段查找标题和内容中包含字段的博客，然后把符合条件的博客分页展示。

简单处理一下页面，粘贴之前博客列表中的div，就省去了再写theamleaf去渲染了，主要内容是根据搜索字段查找符合条件的博客。从前端开始看

~~~html
<form action="#" name="search" th:action="@{/search}" method="post">
    <div class="ui icon input">
        <input type="text" placeholder="search..." name="query" th:value="${query}">
        <i onclick="document.search.submit()" class="search link icon"></i>
    </div>
</form>
~~~

用form包裹搜索框，post方式提交，给icon图标定义点击事件，通过name定位到表单，点击则提交，输入框则向后台推一个query字符串，还需要后端处理完之后，跳转页面再把query传回来并将其值渲染在搜索框里。

后台controller方法

~~~java
@PostMapping("/search")
public String search(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                     Model model, @RequestParam String query){
    model.addAttribute("page", blogService.listBlog("%" +query+ "%", pageable));
    model.addAttribute("query", query);
    return "search";
}
~~~

`@RequestParam String query`取出前端传来的搜索条件字符串，controller里主要是把查找到的数据推到前端。

service层

~~~java
@Override
public Page<Blog> listBlog(String query, Pageable pageable) {
    return blogRepository.findBlogByQuery(query, pageable);
}
~~~

把搜索条件和pageable对象传入dao层，在数据库层面查找数据，因为需要分页，需要返回`Page<Blog>`类型，这个要注意，否则前端会取不到值。

dao层

~~~java
@Query("select b from Blog b where b.title like ?1 or b.content like ?1")
Page<Blog> findBlogByQuery(String query, Pageable pageable);
~~~

通过`@Query`自定义语句进行条件查找，`like ?1`表示对第一个参数进行like查找，另外，sql的like查询的条件字段是有特定格式的：”%内容%“，在字段左右需要加一个“%”，前端传来的是字符串，所以需要进行拼接，这个工作是在controller层完成的。

这样，整个功能就完成了，当输入搜索条件点击搜索时，就会跳转到新的搜索结果页面，博客列表分页展示。

#### 2. 博客内容处理

这部分和列表差不多，先通过id找到对应的博客对象，然后推到前端使用theamleaf渲染页面，需要记录的是博客内容部分的处理。

博客内容在数据库里是以带Markdown语法的文本格式存储的，显然，直接取出来渲染到页面上，格式就全乱了，所以必须把Markdown语法格式的文本转化成HTML格式的文本，而且也不希望改变数据库的内容，因为博客的编辑也需要提取内容，那里是需要Markdown语法格式的。此外，我们有生成目录的插件，表格还希望采用semantic的ui样式，文章中的a标签链接也希望点击之后打开新的页面，这时，就需要一个灵活方便的Markdown转HTML的插件，在对内容格式进行转化之后，还能进行一些定制化操作。

首先，引入依赖

~~~xml
<!--markdown转html插件-->
<dependency>
    <groupId>com.atlassian.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.10.0</version>
</dependency>
<dependency>
    <groupId>com.atlassian.commonmark</groupId>
    <artifactId>commonmark-ext-heading-anchor</artifactId>
    <version>0.10.0</version>
</dependency>
<dependency>
    <groupId>com.atlassian.commonmark</groupId>
    <artifactId>commonmark-ext-gfm-tables</artifactId>
    <version>0.10.0</version>
</dependency>
~~~

第一个是功能的核心，实现Markdown转html，下面两个是为了实现header和table的定制化而导入的。

controller层

~~~java
@GetMapping("/blog/{id}")
public String blog(@PathVariable Long id, Model model){
    model.addAttribute("blog", blogService.getAndInvertBlog(id));
    return "blog";
}
~~~

根据id查找对应的博客对象，并在service层对博客对象的内容进行处理，再推到前端渲染。

service层

~~~java
@Override
public Blog getAndInvertBlog(Long id) {
    Blog blog = blogRepository.getOne(id);
    if (blog == null){
        throw new NotFoundException("该博客不存在");
    }
    Blog b = new Blog();
    BeanUtils.copyProperties(blog, b);
    String content = b.getContent();
    b.setContent(MarkdownUtils.markdownToHtmlExtensions(content));
    return b;
}
~~~

先根据id取出博客对象，这里将取出的对象的值赋给new出的博客对象，这么做的原因是如果直接对原对象进行set操作会改变数据库的内容。然后定义了一个工具类，将Markdown转化为html，并做一些定制化的扩展功能。

~~~java
public class MarkdownUtils {

    /*
    *markdown格式转换成HTML格式
     */
    public static String markdownToHtml(String markdown){
        Parser parser = Parser.builder().build();
        org.commonmark.node.Node docoment = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(docoment);
    }

    /*
    *增加扩展[标题锚点，表格生成]
    * Markdown转换成HTML
     */
    public static String markdownToHtmlExtensions(String markdowm){
        //h标题生成id
        Set<Extension> headingAnchorExtensions = Collections.singleton(HeadingAnchorExtension.create());
        //转换table的HTML
        List<Extension> tableExtension = Arrays.asList(TablesExtension.create());
        Parser parser = Parser.builder()
                .extensions(tableExtension)
                .build();
        Node document = parser.parse(markdowm);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(headingAnchorExtensions)
                .extensions(tableExtension)
                .attributeProviderFactory(new AttributeProviderFactory() {
                    @Override
                    public AttributeProvider create(AttributeProviderContext attributeProviderContext) {
                        return new CustomAttributeProvider();
                    }
                })
                .build();
        return renderer.render(document);
    }
    /*
    *处理标签的属性
     */
    static class CustomAttributeProvider implements AttributeProvider{

        @Override
        public void setAttributes(Node node, String s, Map<String, String> map) {
            //改变a标签的target属性为_blank
            if (node instanceof Link){
                map.put("target", "_blank");
            }
            if (node instanceof TableBlock){
                map.put("class", "ui celled table");
            }
        }
    }
}
~~~

#### 3. 评论功能

评论功能分为了三个部分，先做评论列表的平铺展示，然后再实现评论的二级展示，最后加一个管理员评论。

- 评论列表的平铺展示

在这里，希望评论发布之后就在本页面立即刷新展示，而不改变页面其他部分，这就又用到了ajax方式提交。后台要接收一个Comment对象，所以先在前端定义一下表单中需要提交的数据。

~~~html
<div class="ui bottom attached segment">
    <div id="comments-container" class="ui teal segment">
        <div th:fragment="commentList">
            <div class="ui threaded comments" style="max-width: 100%">
                <h3 class="ui dividing header">评论</h3>
                <div class="comment" th:each="comment : ${comments}">
                    <a class="avatar">
                        <img class="ui avatar image" src="https://picsum.photos/id/100/100/100" th:src="@{${comment.avatar}}" alt="">
                    </a>
                    <div class="content">
                        <a class="author">
                            <span th:text="${comment.nickname}">Matt</span>
                            <div th:if="${comment.adminComment}" class="ui teal basic left pointing mini label m-padded-mini">博主</div>
                        </a>
                        <div class="metadata">
                            <span class="date" th:text="${#calendars.format(comment.createTime, 'yyyy-MM-dd HH:dd')}">Today at 5:42PM</span>
                        </div>
                        <div class="text">
                            <span th:text="${comment.content}">How artistic!</span>
                        </div>
                        <div class="actions">
                            <a class="reply" data-commentid="1" data-commentnickname="Matt" th:attr="data-commentid=${comment.id},data-commentnickname=${comment.nickname}"  onclick="reply(this)">回复</a>
                        </div>
                    </div>
                    <div class="comments" th:if="${#arrays.length(comment.replyComments)}>0">
                        <div class="comment" th:each="reply : ${comment.replyComments}">
                            <a class="avatar">
                                <img class="ui avatar image" th:src="@{${reply.avatar}}" src="https://picsum.photos/id/100/100/100" alt="">
                            </a>
                            <div class="content">
                                <a class="author">
                                    <span th:text="${reply.nickname}">Jenny Hess</span>
                                    <div th:if="${reply.adminComment}" class="ui teal basic left pointing mini label m-padded-mini">博主</div>
                                    &nbsp;<span th:text="|@ ${reply.parentComment.nickname}|" class="m-teal">@小白</span>
                                </a>
                                <div class="metadata">
                                    <span class="date" th:text="${#calendars.format(reply.createTime, 'yyyy-MM-dd HH:dd')}">Just now</span>
                                </div>
                                <div class="text">
                                    <span th:text="${reply.content}">Elliot you are always so right :)</span>
                                </div>
                                <div class="actions">
                                    <a class="reply" th:attr="data-commentid=${reply.id},data-commentnickname=${reply.nickname}"  onclick="reply(this)">回复</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="ui reply form">
        <input type="hidden" name="blog.id" th:value="${blog.id}">
        <input type="hidden" name="parentComment.id" value="-1">
        <div class="required field">
            <textarea name="content" placeholder="请评论...."></textarea>
        </div>
        <div class="fields">
            <div class="required field m-mobile-wide">
                <div class="ui left icon input">
                    <i class="user icon"></i>
                    <input type="text" placeholder="姓名" name="nickname" class="fluid" th:value="${session.user}!=null ? ${session.user.nickname}">
                </div>
            </div>
            <div class="required field m-mobile-wide">
                <div class="ui left icon input">
                    <i class="mail icon"></i>
                    <input type="email" name="email" placeholder="邮箱" th:value="${session.user}!=null ? ${session.user.email}">
                </div>
            </div>
            <div class="field m-mobile-wide">
                <button id="comment-btn" type="button" class="ui icon fluid teal button">
                    <i class="edit icon"></i>
                    发布
                </button>
            </div>
        </div>
        <div class="ui error message"></div>
    </div>
</div>
~~~

首先在外层定义了一个`id="comments-container"`的div和`th:fragment="commentList"`，用来实现ajax提交数据、局部刷新页面。在form中，定义了两个隐藏输入域`name="blog.id"`和`name="parentComment.id"`，用来提交当前评论的博客id和父级评论id，父级评论id默认为-1，即没有父级。后面定义了显示输入域中和对象属性对应的name。当点击回复时，需要在评论框内的placeholder加上“@用户名”，并将其父级id设置为所回复评论的id，所以定义了当前对象的一些属性值

`th:attr="data-commentid=${comment.id},data-commentnickname=${comment.nickname}"`

然后定义其点击事件

~~~js
function reply(obj) {
    var commentId = $(obj).data('commentid');
    var commentNickname = $(obj).data('commentnickname');
    $("[name='parentComment.id']").val(commentId);
    $("[name='content']").attr("placeholder", "@" + commentNickname).focus();
}
~~~

定义ajax方式提交方法

~~~js
function postdata(){
    $("#comments-container").load(/*[[@{/comments}]]*/"/comments", {
        "parentComment.id": $("[name='parentComment.id']").val(),
        "blog.id": $("[name='blog.id']").val(),
        "content": $("[name='content']").val(),
        "nickname": $("[name='nickname']").val(),
        "email": $("[name='email']").val()
    },function (responseTxt, statusTxt, xhr) {
        cleardata();
    });
}

function cleardata() {
    $("[name='parentComment.id']").val(-1);
    $("[name='content']").val("");
    $("[name='content']").attr("placeholder", "请评论....").focus();
}
~~~

使用load方法，将特定的数据提交到后台，还可以定义其回调方法，这里调用了`cleardata()`，功能是重新初始化父级id，清空评论框的内容，并重新初始化其placeholder。需要注意的是，如果使用了theamleaf的`/*[[@{/comments}]]*/`这种动态获取id的方式，需要在外层的script标签加上`th:inline="javascript"`，这样才能生效。

定义发布按钮事件

~~~js
$("#comment-btn").click(function () {
    var boo = $('.ui.form').form('validate form');
    if (boo){
        console.log("校验成功");
        postdata();
    }else {
        console.log("校验失败");
    }

});
~~~

`var boo = $('.ui.form').form('validate form');`boo是boolean类型的，值为form表单验证的结果，验证通过就调用ajax提交的方法。表单验证部分的代码就不记录了，前面应该是记过了。

controller层

~~~java
@GetMapping("/comments/{blogId}")
public String comments(@PathVariable Long blogId, Model model){
    model.addAttribute("comments" , commentService.listCommentByBlogId(blogId));
    return "blog :: commentList";
}

@PostMapping("/comments")
public String postComment(Comment comment, HttpSession session){
    Long blogId = comment.getBlog().getId();
    comment.setBlog(blogService.getBlog(blogId));
    User user = (User) session.getAttribute("user");
    if (user != null){
        comment.setAvatar(user.getAvatar());
        comment.setAdminComment(true);
    }else {
        comment.setAvatar(avatar);
        comment.setAdminComment(false);
    }
    commentService.saveComment(comment);
    return "redirect:/comments/" + comment.getBlog().getId();
}
~~~

第一个方法是根据博客id把评论对象发送到前端，局部刷新页面。第二个方法是ajax提交数据后，对数据进行一定处理，除了ajax提交的数据之外，还有头像、博客对象、创建时间等属性需要单独处理。头像采用了在配置文件声明，在类里取值的方法

application.yaml中的声明

~~~yaml
comment.avatar: /images/avatar.jpg
~~~

在controller中拿值

~~~java
@Value("${comment.avatar}")
String avatar;
~~~

因为Comment里有Blog对象属性，所以通过前端隐藏域定义`name="blog.id"`，后端取到的Comment对象中就自动给Blog对象属性的id赋了值。这里边的判断语句实现的是第三部分管理员评论的功能，通过查session判断管理员是否登录，根据结果给Comment对象赋不同的值，这部分也简单，而且功能不是很好，就不单独再记录了，遇到就写一下。

最后就是调用service方法，实现数据持久化。重定向url，调用`comments(@PathVariable Long blogId, Model model)`方法，这种接收和推送数据分开的模式很值得学习。

service层

~~~java
@Override
public List<Comment> listCommentByBlogId(Long blogId) {
    Sort sort = Sort.by(Sort.Direction.ASC,"createTime");
    List<Comment> comments = commentRepository.findByBlogIdAndParentCommentNull(blogId, sort);
    return eachComment(comments);
}


@Override
public Comment saveComment(Comment comment) {
    Long parentCommentId = comment.getParentComment().getId();
    if (parentCommentId != -1){
        comment.setParentComment(comment.getParentComment());
    }else {
        comment.setParentComment(null);
    }
    comment.setCreateTime(new Date());
    return commentRepository.save(comment);
}
~~~

第二个方法的功能为持久化Comment对象，第一个则是根据博客id返回一个`List<Comment>`，列表根据创建时间顺序排列。前端渲染之后，评论是平铺展示的，作为回复的二级评论和一级评论平级展示。而最终我们想要的效果是每个一级评论下面的各级评论都作为二级评论平铺在一级评论里面

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\评论前端.png)

像这样，所有的二级、三级及以后的都平铺在二级评论部分。

- Comment对象数据结构转化实现二级评论展示功能。

这部分数据结构需要做这样的转化

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\数据结构.png)

数据库中的数据结构是类似于左图这种树形结构，而我们需要转化成左图这种二级结构。从代码层面讲，需要把所有父级放在一个集合中，把所有子集放在一个集合中，并且要保持对应的父级子级之间的映射关系。在Comment对象中有专门的属性存放回复的Comment对象`private List<Comment> replyComments = new ArrayList<>();`数据结构的转化是在service层完成的，一共有三个方法：

~~~java
/*
    *循环每个顶级的节点
     */
private List<Comment> eachComment(List<Comment> comments){
    List<Comment> commentView = new ArrayList<>();
    for (Comment comment : comments){
        Comment c = new Comment();
        BeanUtils.copyProperties(comment,c);
        commentView.add(c);
    }
    combileChildren(commentView);
    return commentView;
}

private void combileChildren(List<Comment> comments){
    for (Comment comment : comments){
        List<Comment> replys1 = comment.getReplyComments();
        for (Comment reply1 : replys1){
            recursively(reply1);
        }
        comment.setReplyComments(tempReplys);
        tempReplys = new ArrayList<>();
    }
}

private void recursively(Comment comment) {
    tempReplys.add(comment);
    if (comment.getReplyComments().size() > 0){
        List<Comment> replys = comment.getReplyComments();
        for (Comment reply : replys){
            tempReplys.add(reply);
            if (reply.getReplyComments().size() > 0){
                recursively(reply);
            }
        }
    }
}
~~~

第一个方法是复制了一份父级评论，以防对原有对象进行改变而改变数据库中的数据。第二个方式是合并每个父级下的所有子级，第三个方法是关键，使用迭代的方式遍历所有子级，将这些子级加入tempReplys临时容器中，并在第二个方法中set到ReplyComments属性中。然后把数据推到前端，前端做一些渲染之后就实现了想要的效果。管理员评论就不记录了，做的不太好。