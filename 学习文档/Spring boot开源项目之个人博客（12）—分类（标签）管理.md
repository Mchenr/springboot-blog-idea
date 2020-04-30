### Spring boot开源项目之个人博客（12）—分类（标签）管理

分类、标签管理功能高度重合，就只记录分类管理。分类管理主要涉及到了增删查改和前端分页展示的功能，还有一些零碎的非空验证、重复验证等。

#### 1. 分页展示

前端是有两个页面，一个用来分页展示，并提供新增、编辑、删除等功能，分类、标签的管理不做条件查询，条件查询会在博客管理做；另一个是新增、编辑共用的表单提交页。

分页展示页面套用了之前做好的博客管理的分页展示页模板，这里主要记录如何用theamleaf和springboot的Pageable实现前端的分页展示。

**service层**

~~~java
@Transactional
    @Override
    public Page<Type> listType(Pageable pageable) {
        return typeRepository.findAll(pageable);
    }
~~~

定义了查询方法，返回`Page<Type>`类型的列表。

**controller层**

~~~java
@GetMapping("/types")
    public String types(@PageableDefault(size = 10, sort = {"id"}, direction = Sort.Direction.DESC)Pageable pageable, Model model){
        model.addAttribute("page", typeServiceImpl.listType(pageable));
        return "/admin/types";
    }
~~~

把查到的分类列表用Model推到前端，page里的值有以下格式

~~~javascript
{
  "content":[
    {"id":123,"title":"blog122","content":"this is blog content"},
    {"id":122,"title":"blog121","content":"this is blog content"},
    {"id":121,"title":"blog120","content":"this is blog content"},
    {"id":120,"title":"blog119","content":"this is blog content"},
    {"id":119,"title":"blog118","content":"this is blog content"},
    {"id":118,"title":"blog117","content":"this is blog content"},
    {"id":117,"title":"blog116","content":"this is blog content"},
    {"id":116,"title":"blog115","content":"this is blog content"},
    {"id":115,"title":"blog114","content":"this is blog content"},
    {"id":114,"title":"blog113","content":"this is blog content"},
    {"id":113,"title":"blog112","content":"this is blog content"},
    {"id":112,"title":"blog111","content":"this is blog content"},
    {"id":111,"title":"blog110","content":"this is blog content"},
    {"id":110,"title":"blog109","content":"this is blog content"},
    {"id":109,"title":"blog108","content":"this is blog content"}],
  "last":false,
  "totalPages":9,
  "totalElements":123,
  "size":15,
  "number":0,
  "first":true,
  "sort":[{
    "direction":"DESC",
    "property":"id",
    "ignoreCase":false,
    "nullHandling":"NATIVE",
    "ascending":false
  }],
  "numberOfElements":15
}
~~~

content里的属性名对应着实体类中的属性名。还有一些分页信息：总页数、当前页数等。

`@PageableDefault(size = 10, sort = {"id"}, direction = Sort.Direction.DESC)Pageable pageable`

可以用注解初始化分页属性：一页多少条数据、以什么方式排序等。不加注解就会使用默认的属性值。这里表示一页十条数据，以id为依据倒序排列，这样做的目的是使新增的分类显示在最前面。

**前端展示**

~~~html
<table class="ui celled table">
    <thead>
        <tr>
            <th></th>
            <th>名称</th>
            <th>操作</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="type,iterStat : ${page.content}">
            <td th:text="${iterStat.count}">1</td>
            <td th:text="${type.name}">刻意练习清单</td>
            <td>
                <a href="#" th:href="@{/admin/types/{id}/input(id=${type.id})}" class="ui mini teal basic button">编辑</a>
                <a href="#" th:href="@{/admin/types/{id}/delete(id=${type.id})}" class="ui mini red basic button">删除</a>
            </td>
        </tr>
    </tbody>
    <tfoot>
        <tr>
            <th colspan="6">
                <div class="ui mini pagination menu">
                    <a class="item" th:href="@{/admin/types(page=${page.number}-1)}" th:unless="${page.first}">上一页</a>
                    <a class="item" th:href="@{/admin/types(page=${page.number}+1)}" th:unless="${page.last}">下一页</a>
                </div>
                <a href="#" th:href="@{/admin/types/input}" class="ui mini right floated teal basic button">新增</a>
            </th>
        </tr>
    </tfoot>
</table>
~~~

`th:each="type,iterStat : ${page.content}"`把content里的内容送到type中，通过theamleaf的`th:each`实现遍历，iterStat的作用的实现分页按序号计数，使得每页的序号都从1开始。

`th:text="${type.name}"`会把对应属性中的值取出来填充到表格中相应的位置。

`th:href="@{/admin/types(page=${page.number}-1)}" th:unless="${page.first}"`这里判断了是否为第一页，若为第一页则不显示上一页，`page.number`表示当前页数，从0开始计数，点击上一页会使当前页数减1。

下一页功能与上一页类似。

#### 2. 增删查改

- 新增、编辑

新增和编辑是公用的一个页面，实现也十分类似，就放在一起记录。他们的区别主要是新增在回调的时候不需要传id，而编辑需要传回当前需要编辑的分类的id，存数据库时也是一个需要id，一个不需要。

**service层**

~~~java
//新增
@Transactional
@Override
public Type saveType(Type type) {
    return typeRepository.save(type);
}
//编辑更新
@Transactional
@Override
public Type update(Long id, Type type) {
    Type t = getType(id);
    if (t == null){
        throw new NotFoundException("不存在");
    }
    BeanUtils.copyProperties(type, t);
    return typeRepository.save(t);
}
~~~

**controller层**

~~~java
//新增
//跳转到新增页面
@GetMapping("/types/input")
public String input(Model model){
    model.addAttribute("type", new Type());
    return "/admin/types-input";
}
//保存到数据库，进行非空、重复验证
@PostMapping("/types")
public String save(@Valid Type type, BindingResult result, RedirectAttributes attributes){
    Type type1 = typeServiceImpl.getType(type.getName());
    if (type1 != null){
        result.rejectValue("name", "nameError", "该分类已存在！");
    }
    if (result.hasErrors()){
        return "/admin/types-input";
    }
    Type t = typeServiceImpl.saveType(type);
    if (t == null){
        //新增失败
        attributes.addFlashAttribute("message", "新增失败");
    }else {
        //新增成功
        attributes.addFlashAttribute("message", "新增成功");
    }
    return "redirect:/admin/types";
}
~~~

需要注意的是，最后必须要以`"redirect:/admin/types"`的方式回到分页展示页面，并可以通过`RedirectAttributes`推送操作的结果消息到前端。这里还进行了重复检测，思路是拿到用户输入的分类对象，通过`getType(String name)`方法查找这个名称对应的对象，如果返回的对象存在，说明这个名称已存在。而验证的消息是通过`@Valid`实现的，另外后端的非空验证也是通过这个注解实现。

~~~java
//编辑
//带id跳转到新增页面
@GetMapping("/types/{id}/input")
public String editInput(@PathVariable Long id, Model model){
    model.addAttribute("type", typeServiceImpl.getType(id));
    return "/admin/types-input";
}
//带id更新编辑当前分类
@PostMapping("/types/{id}")
public String edit(@Valid Type type, BindingResult result, @PathVariable Long id, RedirectAttributes attributes){
    Type type1 = typeServiceImpl.getType(type.getName());
    if (type1 != null){
        result.rejectValue("name", "nameError", "该分类已存在！");
    }
    if (result.hasErrors()){
        return "/admin/types-input";
    }
    Type t = typeServiceImpl.update(id, type);
    if (t == null){
        //新增失败
        attributes.addFlashAttribute("message", "更新失败");
    }else {
        //新增成功
        attributes.addFlashAttribute("message", "更新成功");
    }
    return "redirect:/admin/types";
}
~~~

`@PathVariable`可以拿到路径中的参数，通过id找到当前要编辑的对象，并把这个对象推到前端。`typeServiceImpl.update(id, type)`因为是编辑，只需把之前的sava方法改成update就可以。

**前端页面**

~~~html
<form action="#" method="post" th:object="${type}" th:action="*{id}==null ? @{/admin/types} : @{/admin/types/{id}(id=*{id})}" class="ui form">
    <input type="hidden" name="id" th:value="*{id}">
    <div class="required field">
        <div class="ui left labeled input">
            <label class="ui teal basic label">分类</label>
            <input type="text" name="name" placeholder="分类的名称" th:value="*{name}">
        </div>
    </div>

    <div class="ui error message"></div>
    <!--/*/
	<div class="ui negative message" th:if="${#fields.hasErrors('name')}">
	<i class="close icon"></i>
	<div class="header">
	验证失败：
	</div>
	<p th:errors="*{name}">This is a special notification which you can dismiss if 	you're bored with it.</p>
	</div>
	/*/-->
    <div class="ui center aligned container">
        <button type="button" class="ui button" onclick="window.history.go(-1)">返回</button>
        <button class="ui teal submit button">发布</button>
    </div>
</form>
~~~

前端主要就是一个form表单，post方式提交，`th:action="*{id}==null ? @{/admin/types} : @{/admin/types/{id}(id=*{id})}"`用三元条件符做一个判断，通过判断是否有id决定回调的方法。在这之前，需要使用`th:object="${type}"`取到type对象，在controller层中，新增时跳转到新增页面时是`model.addAttribute("type", new Type());`new的Type对象，而编辑时是通过id查到Type对象，所以可以用id是否为空来判断是新增还是编辑。这里取id是通过这种方式`*{id}`，这个就表示已经定义的object的属性。

- 删除

这个就比较简单：

**service层**

~~~java
@Transactional
@Override
public void deleteType(Long id) {
    typeRepository.deleteById(id);
}
~~~

**controller层**

~~~java
@GetMapping("/types/{id}/delete")
public String delete(@PathVariable Long id, RedirectAttributes attributes){
    typeServiceImpl.deleteType(id);
    attributes.addFlashAttribute("message", "删除成功");
    return "redirect:/admin/types";
}
~~~

最后需要注意用`"redirect:/admin/types"`的方式回到分页展示页。

#### 3. 非空、重复验证

最后一部分就是这个验证，非空验证前后端都做了，前端比较简单，登录也做过，稍微改一下就好，主要记录下后端的非空验证。

后端非空验证主要用了`@Valid`注解，在实体类需要验证的属性上加上`@NotBlank(message = "分类名称不能为空")`注解，里面的message是可选的，一般来说我们需要加上message，并把这个message显示到前端提醒用户。

~~~java
@NotBlank(message = "分类名称不能为空")
private String name;
~~~

然后需要在表单提交的回调方法中加上`@Valid`注解，这个才生效。

~~~java
@PostMapping("/types")
public String save(@Valid Type type, BindingResult result, RedirectAttributes attributes)
~~~

`BindingResult`必须紧跟`@Valid`注解之后声明，因为加了`@NotBlank`注解，所以name属性为空时，`result.hasErrors()`就返回为true，并可以通过先用`th:object="${type}"`定义type对象，然后用`th:errors="*{name}"`把错误信息显示出来，这个错误信息都是注解捆绑的。另外，也能自定义错误，比如重复验证中，如果重复了，那么，可以通过`result.rejectValue("s:name", "s1:nameError", "s2:该分类已存在！")`方法自定义错误，s为错误绑定的属性名，s1为自定义的错误类型，s2为错误信息。在前端用同样的方法接收后台传来的错误信息。

分类管理涉及到的知识点基本都记录完了，做的过程也比较顺利，下一步是博客的管理，体量就要比分类管理要大，也多了一个条件查询，大体实现思路应该都差不多。