### Spring boot开源项目之个人博客（13）—博客管理

#### 1. 分页展示

这部分一共两个内容，一个带条件查询动态分页展示，一个是ajax页面局部动态更新。

- 带条件查询动态分页展示

博客管理比分类要复杂一些，需要根据高级查询条件筛选文章进行分页展示，在流程上是差不多的。

首先是把查询条件封装成一个对象BlogQuery

~~~java
@Data
public class BlogQuery {

    private String title;
    private Long typeId;
    private boolean recommend;
}
~~~

查询标题、分类、是否推荐3项。

然后，写一下dao层接口、service层接口和实现，这部分和分类是差不多的。主要的区别就在于查找博客列表的方法。先使dao层接口继承JpaSpecificationExecutor<Blog>

~~~java
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {
}
~~~

然后定义好service层接口

~~~java
Page<Blog> listBlog(Pageable pageable, BlogQuery blog);
~~~

接口的实现方法

~~~java
@Override
public Page<Blog> listBlog(Pageable pageable, BlogQuery blog) {
    return blogRepository.findAll(new Specification<Blog>() {
        @Override
        public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();
            if (!"".equals(blog.getTitle()) && blog.getTitle() != null){
                predicates.add(criteriaBuilder.like(root.<String>get("title"), "%" + blog.getTitle() + "%"));
            }
            if (blog.getTypeId() != null){
                predicates.add(criteriaBuilder.equal(root.<Type>get("type").get("id"), blog.getTypeId()));
            }
            if (blog.isRecommend()){
                predicates.add(criteriaBuilder.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
            }
            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
            return null;
        }
    }, pageable);
}
~~~

`Root<Blog> root`表示使用高级查询的对象，criteriaQuery、criteriaBuilder可以帮助我们很方便的完成条件的拼接。

最后在controller层定义回调方法

~~~java
@GetMapping("/blogs")
public String blogs(@PageableDefault(size = 3, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                    Model model, BlogQuery blog){
    model.addAttribute("types", typeService.listType());
    model.addAttribute("page", blogService.listBlog(pageable, blog));
    return "admin/blogs";
}
~~~

这里 `model.addAttribute("types", typeService.listType());`是因为前段页面分类的条件筛选是通过一个dropdown实现的，所以需要把数据库中的Type查出来送到前端

~~~html
<div class="ui selection dropdown">
    <input type="hidden" name="typeId">
    <i class="dropdown icon"></i>
    <div class="default text">分类</div>
    <div class="menu">
        <div class="item" th:each="type : ${types}" th:data-value="${type.id}" th:text="${type.name}" data-value="1">错误日志</div>
    </div>
</div>
~~~

通过 `th:each`循环把所有的分类都加入到dropdown里，设置`th:data-value`为分类的id，显示文字为分类的名称。

查到的数据都送到前端之后，在前端把数据都展示出来

~~~html
<div id="table-container">
    <table th:fragment="blogList" class="ui celled teal table">
        <thead>
            <tr>
                <th></th>
                <th>标题</th>
                <th>类型</th>
                <th>推荐</th>
                <th>更新时间</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="blog,iterStat : ${page.content}">
                <td th:text="${iterStat.count}">1</td>
                <td th:text="${blog.title}">刻意练习清单</td>
                <td th:text="${blog.type.name}">认知升级</td>
                <td th:text="${blog.recommend} ? '是' : '否'">是</td>
                <td th:text="${blog.updateTime}">2019-12-25</td>
                <td>
                    <a href="#" th:href="@{/admin/blogs/{id}/input(id=${blog.id})}" class="ui mini teal basic button">编辑</a>
                    <a href="#" th:href="@{/admin/blogs/{id}/delete(id=${blog.id})}" class="ui mini red basic button">删除</a>
                </td>
            </tr>
        </tbody>
        <tfoot>
            <tr>
                <th colspan="6">
                    <div class="ui mini pagination menu" th:if="${totalNumber} != null">
                        <a onclick="page(this)" th:attr="data-page=${page.number}-1" th:unless="${page.first}" class="item">上一页</a>
                        <a onclick="page(this)" th:attr="data-page=${page.number}+1" th:unless="${page.last}" class="item">下一页</a>
                    </div>
                    <a href="#" class="ui mini right floated teal basic button">新增</a>
                </th>
            </tr>
        </tfoot>
    </table>
</div>
~~~

<tbody>中使用`th:each="blog,iterStat : ${page.content}"`循环填充表格和分类是一样的，编辑和删除访问的url也和分类使用的形式差不多，不一样的是上一页和下一页，按照以前的方式，直接访问url：`th:href="@{/admin/types(page=${page.number}-1)}"`，这并不会把form表单里的查询条件传回去，也就意味着，以这种方式实现下一页的话，会显示未加筛选条件的当前页的第二页。这里是希望每次点击查询或者上一页、下一页时，刷新的都只是表格中的内容而不是整个页面，这就用到了ajax，ajax能帮助我们很好的实现页面的局部动态更新。下面就记录下这部分是怎么实现的。

- ajax页面局部动态更新

先使用theamleaf将需要局部动态更新的区域定义成一个fragment：<table th:fragment="blogList" class="ui celled teal table">。

在controller层定义回调方法

~~~java
@PostMapping("/blogs/search")
public String search(@PageableDefault(size = 3, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                     Model model, BlogQuery blog){
    model.addAttribute("page", blogService.listBlog(pageable, blog));
    return "admin/blogs :: blogList";
}
~~~

和之前的分页展示差不多，最大的差别是这里：`return "admin/blogs :: blogList";`。

前端定义ajax提交的方法

~~~js
function loaddata() {
        $("#table-container").load(/*[[@{admin/blogs/search}]]*/"/admin/blogs/search", {
            title: $("[name='title']").val(),
            typeId: $("[name='typeId']").val(),
            recommend: $("[name='recommend']").prop('checked'),
            page: $("[name='page']").val()
        });
    }
~~~

通过包裹table的div的id取到这部分`$("#table-container")`，然后使用load方法，第一个参数是提交的url，第二个参数是提交的数据。这里数据都是通过定义input的name属性来获取的，以title为例

~~~html
<input type="text" placeholder="标题" name="title">
~~~

定义标题输入框的name为title，使用`$("[name='title']").val()`的方式取到这里面的值。

这些就是ajax的基本使用方法，需要注意的是，ajax提交是post方式提交。因为有了ajax方式，所以之前包裹条件域的form表单标签可以直接换成div。最后通过查询按钮实现ajax的提交。把查询按钮的type设置为button，给他一个id `id="search-btn"`，然后使用jQuery定义点击事件方法

~~~js
$("#search-btn").click(function () {
    $("[name='page']").val(0);
    loaddata();
});
~~~

这样，当点击搜索的时候，就会实现ajax的局部动态刷新页面，将搜索条件提交到后台，后台查到对应的Page对象传回前台，在对应部位局部刷新数据，另外，还要把page的值清零，使得点击搜索后永远从第一页开始显示。在点击上一页、下一页时，还要针对页数做一个处理，定义其点击事件

~~~js
function page(obj) {
    $("[name='page']").val($(obj).data("page"));
    loaddata();
}
~~~

以`$(obj).data("page")`方式取到之前通过`th:attr="data-page=${page.number}-1"`定义的值，再把这个值赋给隐藏的input，以便ajax取到这个值，最后就是调用ajax局部刷新页面。

#### 2. 博客新增

这部分和分类部分的区别主要是内容多了，需要更多的处理，也是根据这部分的学习明白了前端表单提交是怎么对应后端实体类属性的赋值的。也就主要说一下这部分。

- 前端表单数据与后端实体类的映射

前端表单部分代码

~~~html
<form id="blog-form" action="#" th:action="@{/admin/blogs}" method="post" class="ui form">
    <input type="hidden" name="published">
    <div class="required field">
        <div class="ui left labeled input">
            <div class="ui dropdown orange basic compact label">
                <div class="text">原创</div>
                <i class="dropdown icon"></i>
                <div class="menu">
                    <div class="item" data-value="原创">原创</div>
                    <div class="item" data-value="转载">转载</div>
                    <div class="item" data-value="翻译">翻译</div>
                </div>
            </div>
            <input type="text" name="title" placeholder="文章标题">
        </div>
    </div>
    <div class="required field">
        <div id="md-content" style="z-index: 1 !important;">
            <textarea name="content" placeholder="博客内容" style="display: none"></textarea>
        </div>
    </div>
    <div class="two fields">
        <div class="required field">
            <div class="ui left labeled action input">
                <div class="ui teal basic label">分类</div>
                <div class="ui fluid selection dropdown">
                    <input type="hidden" name="typeId">
                    <i class="dropdown icon"></i>
                    <div class="default text"></div>
                    <div class="menu">
                        <div class="item" th:each="type : ${types}" data-value="1" th:data-value="${type.id}" th:text="${type.name}">错误报告</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="field">
            <div class="ui left labeled action input">
                <div class="ui teal basic label">标签</div>
                <div class="ui fluid selection multiple search dropdown">
                    <input type="hidden" name="tagIds">
                    <i class="dropdown icon"></i>
                    <div class="default text"></div>
                    <div class="menu">
                        <div class="item" th:each="tag : ${tags}" data-value="1" th:data-value="${tag.id}" th:text="${tag.name}">java</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="required field">
        <div class="ui left labeled input">
            <label class="ui teal basic label">首图</label>
            <input type="text" name="firstPicture" placeholder="首图引用地址">
        </div>
    </div>

    <div class="inline fields">
        <div class="field">
            <div class="ui checkbox">
                <input type="checkbox" name="recommend" checked class="hidden" id="recommend">
                <label for="recommend">推荐</label>
            </div>
        </div>
        <div class="field">
            <div class="ui checkbox">
                <input type="checkbox" name="shareStatement" class="hidden" id="shareStatement">
                <label for="shareStatement">转载声明</label>
            </div>
        </div>
        <div class="field">
            <div class="ui checkbox">
                <input type="checkbox" name="appreciation" class="hidden" id="appreciation">
                <label for="appreciation">赞赏</label>
            </div>
        </div>
        <div class="field">
            <div class="ui checkbox">
                <input type="checkbox" name="commentabled" class="hidden" id="commentabled">
                <label for="commentabled">评论</label>
            </div>
        </div>
    </div>
    <div class="ui error message"></div>
    <div class="ui center aligned container">
        <button type="button" class="ui button" onclick="windows.history.go(-1)">返回</button>
        <button type="button" id="save-btn" class="ui secondary button">保存</button>
        <button type="button" id="input-btn" class="ui teal button">发布</button>
    </div>

</form>
~~~

我们看到，表单中像dropdown、checkbox等元素定义input标签隐藏域，通过name属性的名称映射实体类属性名，name对应名称的值则是dropdown或checkbox等元素中的值，而普通的input则是直接定义name属性的名称映射实体类属性名，值就是输入框中的值。表单提交后，后台只需在回调方法中传入相对应的对象，这样，前端的值就自动赋给了对象的各属性。这时就可以调用service层方法实现具体业务，并持久化数据。需要特别注意的是，草稿和发布的状态是通过保存和发布两个按钮控制的，所以这个属性的赋值和其他的也有区别，这时的做法是在任意位置定义一个隐藏域，然后定义按钮的点击事件，在点击事件里对其赋值。

~~~js
$("#save-btn").click(function () {
    $("[name='published']").val(false);
    $("#blog-form").submit();
});
$("#input-btn").click(function () {
    $("[name='published']").val(true);
    $("#blog-form").submit();
});
~~~

通过id找到按钮，然后定义其点击事件，对特定name赋值，再调用表单提交方法，把数据传回后台。

- 其他

另外的就很零碎了，博客有些属性表单里是没有的，比如创建时间、更新时间等，可以这样处理

~~~java
@Transactional
@Override
public Blog saveBlog(Blog blog) {
    blog.setCreateTime(new Date());
    blog.setUpdateTime(new Date());
    blog.setViewTimes(0);
    return blogRepository.save(blog);
}
~~~

在service层的保存方法中，初始化这些属性。

博客是有`Type`和`List<Tag>`属性的，所以保存时也要把表单中提交的`Type`和`List<Tag>`对象赋给`Blog`对应的属性。

~~~java
@PostMapping("/blogs")
public String post(Blog blog, RedirectAttributes attributes, HttpSession session){
    blog.setUser((User) session.getAttribute("user"));
    blog.setType(typeService.getType(blog.getTypeId()));
    blog.setTags(tagService.listTag(blog.getTagIds()));
    Blog b = blogService.saveBlog(blog);
    if (b == null){
        //新增失败
        attributes.addFlashAttribute("message", "操作失败");
    }else {
        //新增成功
        attributes.addFlashAttribute("message", "操作成功");
    }
    //写成 return "redirect:admin/blogs"; 是不行的，必须加/
    return "redirect:/admin/blogs";
}
~~~

这里是通过利用前端传来的分类和标签的id值找到对应的对象，然后赋给blog，另外，还要把session中的user取出来赋给blog。比较复杂的就是标签赋值，因为标签是个集合，需要对前端传来的id字符串进行处理。

标签的service层接口定义`listTag(String ids)`方法

~~~java
List<Tag> listTag(String ids);
~~~

定义其实现

~~~java
@Transactional
@Override
public List<Tag> listTag(String ids) {
    return tagRepository.findAllById(convertToList(ids));
}

public List<Long> convertToList(String source){
    List<Long> target = new ArrayList<>();
    if (!"".equals(source) && source != null){
        String[] sourceArray = source.split(",");
        for (int i=0;i<sourceArray.length;i++){
            target.add(new Long(sourceArray[i]));
        }
    }
    return target;
}
~~~

首先，说明一下前端传回的值的形式，当选择了标签后，tagIds的值会以"id1,id2,id3,....."字符串的形式赋给blog对象，这里就可以使用`findAllById(java.lang.Iterable<ID> iterable)`方法，对id进行迭代，返回tag集合，但id需要以`List<Long>`形式传参，所以定义了一个`String`转`List<Long>`的方法。这样把标签集合查出来之后通过set方法赋给blog就可以了。

新增部分就到这了。

#### 3. 博客编辑

博客编辑和新增共用一个页面，在controller层要比新增多传一个id，后台通过id查到要编辑的博客对象，再把这个对象推到前端，通过theamleaf拿到对象，将数据回显到blogs-input页面。

controller层

~~~java
@GetMapping("/blogs/{id}/input")
public String editInput(@PathVariable Long id, Model model){
    Blog blog = blogService.getBlog(id);
    model.addAttribute("blog", blog);
    model.addAttribute("types", typeService.listType());
    model.addAttribute("tags", tagService.listTag());
    return "admin/blogs-input";
}
~~~

`<form id="blog-form"  th:action="@{/admin/blogs}" th:object="${blog}......"`在form拿到blog对象，通过`th:value="*{id}"`、`th:value="*{articleFlag}"`、`th:text="*{content}"`、`th:checked="*{recommend}"`等将对象里的值填充到页面，对于`th:value="*{tagIds}"`标签的填充，视频里好像做的多余了，因为blog实体类定义的tagIds属性也是String类型的，数据格式和前端传来的数据格式一样，都是类似"id1,id2,id3....."这种，所以直接取出来用就好了，视频中是又把这个重新构建了一遍，加了”，“。

编辑修改完之后，提交到后台的方法是一样的，只是在service层对save方法做了简单处理，通过检查是否有id来判断是新增操作还是编辑操作，然后执行不同的语句。

~~~java
public Blog saveBlog(Blog blog) {
    if (blog.getId() == null){
        blog.setCreateTime(new Date());
        blog.setUpdateTime(new Date());
        blog.setViewTimes(0);
    }
    blog.setUpdateTime(new Date());
    return blogRepository.save(blog);
}
~~~

新增操作中创建、更新时间和浏览量都需要初始化，而编辑操作只需更新一下更新时间就可以了。

#### 4. 博客删除

service层

~~~java
@Transactional
@Override
public void deleteBlog(Long id) {
    blogRepository.deleteById(id);
}
~~~

controller层

~~~java
@GetMapping("blogs/{id}/delete")
public String delete(@PathVariable Long id, RedirectAttributes attributes){
    blogService.deleteBlog(id);
    attributes.addFlashAttribute("message", "删除成功");
    return "redirect:/admin/blogs";
}
~~~

这部分很简单，就不详细说明了。至此，整个后端管理部分也就完成了，接下来做前端展示部分。