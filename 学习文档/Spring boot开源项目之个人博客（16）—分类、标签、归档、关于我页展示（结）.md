### Spring boot开源项目之个人博客（16）—分类、标签、归档、关于我页展示（结）

这是这个开源项目最后一部分了，还剩四个页面需要处理：按特定分类或标签分页展示博客列表、按年份对博客进行归档、关于我页的跳转。

#### 1. 分类页

页面原型之前都做好了，这部分也没有数据的提交，基本思路就是通过typeId查博客列表

controller层

~~~java
@GetMapping("/types/{typeId}")
public String typeShow(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                       @PathVariable Long typeId, Model model) {
    BlogQuery blogQuery = new BlogQuery();
    List<Type> types = typeService.listTypeTop(1000);
    if (typeId == -1) {
        typeId = types.get(0).getId();
    }
    blogQuery.setTypeId(typeId);
    model.addAttribute("page", blogService.listBlog(pageable, blogQuery));
    model.addAttribute("types", types);
    model.addAttribute("activeTypeId", typeId);
    return "types";
}
~~~

`if (typeId == -1)`为true的话，页面就是从导航页跳转的，需要默认激活排在第一位的分类，分类列表是按博客数量从大到小排序的，之前做首页就写过相关功能的实现，这边是直接使用了。根据typeId查博客列表之前也写过类似的方法，是在高级条件查询的时候，所以这边也可以直接使用，最后到前端渲染一下就好了。

#### 2. 标签页

这部分和分类页类似，只是查分页博客列表的方法需要写一下。

controller层

~~~java
@GetMapping("/tags/{tagId}")
public String typeShow(@PageableDefault(size = 6, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                       @PathVariable Long tagId, Model model) {
    List<Tag> tags = tagService.listTagTop(1000);
    if (tagId == -1) {
        tagId = tags.get(0).getId();
    }
    model.addAttribute("page", blogService.listBlogByTagId(pageable, tagId));
    model.addAttribute("tags", tags);
    model.addAttribute("activeTagId", tagId);
    return "tags";
}
~~~

controller层的逻辑和分类是一样的，只是获取`page<Blog>`对象的方法需要实现一下

service层

~~~java
@Override
public Page<Blog> listBlogByTagId(Pageable pageable, Long tagId) {
    return blogRepository.findAll(new Specification<Blog>() {
        @Override
        public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
            Join join = root.join("tags");
            return criteriaBuilder.equal(join.get("id"), tagId);
        }
    }, pageable);
}
~~~

还是用Jpa的Specification高级条件查询，这个使用还是比较复杂的，这边也没法记录了，不怎么懂，有空学一下可以记个专题。最后到前端渲染就可以了。

#### 3. 归档页

归档页涉及到了更深层次的对数据库的操作，具体为将数据库中的更新时间字段按年份格式分割，并排序，再通过年份查找各自对应的博客列表，一共两步操作。另外还使用了Map集合存放年份及其映射的博客列表，然后在前端通过theamleaf渲染。

controller层

~~~java
@GetMapping("/archives")
public String archiveShow(Model model){
    model.addAttribute("count", blogService.countBlog());
    model.addAttribute("archiveMap",blogService.archiveBlog());
    return "archives";
}
~~~

将博客总数和存放归档信息的Map集合推到前端。

service层

~~~java
@Override
public Long countBlog() {
    return (long) blogRepository.findAll().size();
}
~~~

计数很简单，计算一下存放所有博客的列表的大小。

~~~java
@Override
public Map<String, List<Blog>> archiveBlog() {
    List<String> years = blogRepository.findGroupYear();
    Map<String, List<Blog>> map = new LinkedHashMap<>();
    for (String year : years){
        map.put(year, blogRepository.findBlogByYear(year));
    }
    return map;
}
~~~

通过调用dao层方法拿到年份的集合，在遍历年份，查对应的博客列表并存放到map集合中。这里的map要使用`LinkedHashMap<>()`，这种是按存入的顺序取出，而`HashMap<>()`数据存入之后顺序会被打乱，这样在前端渲染的时候就会出问题。

dao层

~~~java
@Query("select function('date_format', b.updateTime, '%Y') as year from Blog b group by function('date_format', b.updateTime, '%Y') order by function('date_format', b.updateTime, '%Y') desc ")
List<String> findGroupYear();

@Query("select b from Blog b where function('date_format', b.updateTime, '%Y') = ?1")
List<Blog> findBlogByYear(String year);
~~~

使用Query语句，比较复杂，复杂的数据库语句还是个盲区。这个Query语句对应的sql语句为：

~~~sql
SELECT date_format(b.update_time, '%Y') as year from t_blog b GROUP BY year ORDER BY year DESC;
SELECT * from t_blog b where date_format(b.update_time, '%Y') = '2017'; 
~~~

sql语句还是慢慢学吧。这样后端就可以了。

前端

~~~html
<th:block th:each="archive : ${archiveMap}">
    <h2 class="ui centered header" th:text="${archive.key}">2017</h2>
    <div class="ui fluid vertical menu">
        <a href="#" target="_blank" th:each="blog : ${archive.value}" th:href="@{/blog/{id}(id=${blog.id})}" class="item">
            <span>
                <i class="mini teal circle icon"></i><span th:text="${blog.title}">关于刻意练习的清单</span>
                <div class="ui teal basic left pointing label m-padded-mini" th:text="${#calendars.format(blog.updateTime, 'MM-dd')}">9月9号</div>
            </span>
            <div class="ui orange basic label" th:text="${blog.articleFlag}">原创</div>
        </a>
    </div>
</th:block>
~~~

map集合分为key和value，在这里，年份就作为key，其映射的博客集合就作为value，前端取的时候也要按照这种规则取值。

#### 4. 关于我

这部分基本是写死的，就写一个controller控制页面跳转就好了。

#### 5. 结

最后还有些小修小改，也都是重复的知识再使用，就不在记录了，这个开源项目一点点跟着做，做了蛮久，但也还算做的稳扎稳打，后端基本采用先看视频学一遍，自己再敲一遍，最后再写博客记录知识点，跌跌撞撞的也算是坚持下来了。很感谢有这么一个完整的项目，有这么完整的视频教学，这个项目学完，对springboot的理解也算是上了一个台阶。