### Spring boot开源项目之个人博客（14）—博客列表展示

这是一个前端的展示页面，整体流程就是在dao、service层定义拿到的需要的数据的方法，在controller层把数据传给前端，前端使用theamleaf渲染，将数据展示在模板上。

#### 1. 后台从数据库拿数据

博客列表的展示一共需要4类数据：带分页的博客列表、6个以所含博客数量由大到小的排序的分类、10个以所含博客数量由大到小的排序的标签、按更新时间排序的博客列表。

带分页的博客列表是很好处理的，之前定义过带搜索条件的博客分页列表方法，现在把搜索条件去掉，就实现了我们需要的功能。

service层方法

~~~java
Page<Blog> listBlog(Pageable pageable);
~~~

其实现

~~~java
@Override
public Page<Blog> listBlog(Pageable pageable) {
    return blogRepository.findAll(pageable);
}
~~~

controller层调用

~~~java
@GetMapping("/")
public String index(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                    Model model){
    model.addAttribute("page", blogService.listBlog(pageable));
    model.addAttribute("types", typeService.listTypeTop(6));
    model.addAttribute("tags", tagService.listTagTop(10));
    model.addAttribute("recommendBlogs", blogService.listBlogRecommendTop(8));
    return "index";
}
~~~

剩下的三个就需要自己定义方法实现，主要思路是利用pageable中可以根据一定规则排序进而在数据库层面直接提取需要的数据，这样做的好处就是免去了取出多余的数据并处理的步骤，提升运行效率。

以提取分类列表方法举例：

dao层

~~~java
@Query("select t from Type t")
List<Type> findTop(Pageable pageable);
~~~

service层

~~~java
List<Type> listTypeTop(Integer size);
~~~

实现

~~~java
@Override
public List<Type> listTypeTop(Integer size) {
    Sort sort = Sort.by(Sort.Direction.DESC, "blogs.size"); //确定排序依据，按博客列表对象的大小倒序排列
    Pageable pageable = PageRequest.of(0, size, sort);//初始化pageable，单页以指定大小和排序查数据
    return typeRepository.findTop(pageable);
}
~~~

controller层就是调用service层接口实现，然后用model把拿到的数据推到前端。

在拿博客列表时，因为需要拿到的博客都是推荐状态的，所以在dao层有点区别

~~~java
@Query("select b from Blog b where b.recommend = true")
List<Blog> findTop(Pageable pageable);
~~~

而且在service层排序也要依据updateTime倒序排序`Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");`。

#### 2. 前端模板渲染

渲染主要是用`th:each`循环来做，具体使用前面也都涉及了，这边只记录一些新的知识点。

- teamleaf模板指定时间格式

```html
th:text="${#calendars.format(blog.updateTime, 'yyyy-MM-dd')}"
```

从数据库拿到的updateTIme是包含时分秒的，而前端展示只需要展示年月日，这里就可以用theamleaf直接处理。

- 计算列表大小

```html
th:text="${#arrays.length(type.blogs)}"
```

这里是实现每个分类或标签对应多少篇博客，而这是由分类或标签实体中定义的博客列表属性体现的，theamleaf提供了这种计算列表大小的方法，十分方便。其他也就没什么了，主要就是注意循环体别放错标签。