###  Spring boot开源项目之个人博客（10）—使用JPA实现对数据库的操作

#### 1. 建立实体类，建表

JPA所需要的依赖在搭建框架的时候就完成了，在使用之前，还要在application.yml中配置一下。

~~~yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
~~~

这里配置了自动建表：updata表示没有表新建，有表更新操作；控制台显示建表语句。

项目把实体类分成了五个：博客、评论、标签、类型、用户。一般来说在建表之前，是需要把实体类之间的关系弄清楚的。实体类之间的关系一般有：一对多、多对一、多对多，具体关系就不记录了，这里主要写一写jpa是怎么通过注解的方式完成数据库的建表工作的。

以博客实体类为例

blog

~~~java
@Entity
@Table(name = "t_blog")
@Data
public class Blog {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private String firstPicture;
    private String articleFlag;
    private Integer viewTimes;
    private boolean appreciation;
    private boolean shareStatement;
    private boolean commentabled;
    private boolean published;
    private boolean recommend;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @ManyToOne
    private Type type;

    @ManyToMany(cascade = {CascadeType.PERSIST})
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "blog")
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    private User user;
}
~~~

`@Entity`表示这个类是一个实体类，`@Table(name = "t_blog")`表示对应的数据库表名，`@Data`是lombok中的注解，在这个注解下的类的属性不需要再写get/set方法和tostring等方法，在项目编译时会自动生成，这使得我们的代码简洁了很多。类里面主要就是定义类的属性和与其他实体类的关系，需要注意的还是实体类之间的关系，这个有时候比较绕，一定要仔细，在一对多或多对一关系中，通常“多”的一方被“一”的一方映射。像这样把其他的实体类都建立好后，运行项目就会自动为我们生成数据库表，多对多关系的实体类还会自动生成中间表。

#### 2. 简单使用

正好这个项目还需要做一个登录功能，页面就直接套用了在semantic官网找的现成的模板，这边就简单记录下在接收到前端传来的数据后是怎么和数据库交互的。

首先是dao层

~~~java
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserNameAndPassword(String username, String password);
}
~~~

定义一个接口，在接口中可以定义各种各样与数据库交互的抽象方法，这里是根据用户名和密码找到对应的用户，并返回一个User对象。

service层

~~~java
public interface UserService {
    User checkUser(String username, String password);
}
~~~

定义接口。

~~~java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User checkUser(String username, String password) {
        User user = userRepository.findByUserNameAndPassword(username, password);
        return user;
    }
}
~~~

定义接口的实现，这里`@Service`表面这是一个service类，`@Autowired`表示把dao层接口注入进来，在方法里面就可以调用dao层接口对数据库进行操作。方法里就是调用接口，返回一个User对象。

然后在controller层

~~~java
@Controller
@RequestMapping("/admin")
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String loginPage(){
        return "/admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes attributes){
        User user = userService.checkUser(username, password);
        if (user != null){
            user.setPassword(null);
            session.setAttribute("user", user);
            return "/admin/index";
        } else {
            attributes.addFlashAttribute("message", "用户名密码错误");
            return "redirect:/admin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("user");
        return "redirect:/admin";
    }
}
~~~

`@RequestMapping("/admin")`定义了这个controller类映射的地址，把service层注入进来。在login方法中，调用service层的方法，处理前端传来的数据，然后再根据返回的结果进行下一步的操作。这就是最基本的使用jpa实现对数据库进行操作的例子，下一篇就再把登录部分一些零碎的知识点记录一下。