### Spring boot开源项目之个人博客（11）—登录功能实现

这部分就把一下重要的点记录一下。

- 前端表单非空验证

~~~html
<script>
    $('.ui.form').form({
        fields:{
            username:{
                identifier: 'username',
                rules:[{
                    type: 'empty',
                    prompt : '请输入用户名'
                }]
            },
            password:{
                identifier: 'password',
                rules:[{
                    type: 'empty',
                    prompt : '请输入密码'
                }]
            }
        }
    });
</script>
~~~

再用一个div放错误信息

~~~html
<!--非空验证信息-->
<div class="ui error message"></div>
<!--用户名密码错误信息，在message非空时才显示-->
<div class="ui mini negative message" th:unless="${#strings.isEmpty(message)}" th:text="${message}"></div>
~~~

效果是这样的

![](D:\note\target\Springboot博客开源项目笔记\一些截图\后端\登录1.png)

- controller层一些细节

~~~java
@PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes attributes){
        User user = userService.checkUser(username, password);
        if (user != null){
            //密码不能放到session里，需要提前把密码清空
            user.setPassword(null);
            session.setAttribute("user", user);
            return "/admin/index";
        } else {
            //这里的attributes是RedirectAttributes下的，不是的话message传不回重定向后的页面 
            attributes.addFlashAttribute("message", "用户名密码错误");
            //这里要注意用"redirect:/admin"，不能直接return "/admin";包括下面注销那里也是一样
            return "redirect:/admin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("user");
        return "redirect:/admin";
    }
~~~

- MD5加密登录

新建一个util包，再新建一个MD5Utils类

~~~java
public class MD5Utils {
    /**
     * MD5加密类
     *
     * @param str 要加密的字符串
     * @return 加密后的字符串
     */
    public static String code(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteDigest = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void main(String[] args) {
        System.out.println(code("111111"));
    }
}
~~~

改一下数据库，把密码改成加密后的密码，然后在services层稍加修改

~~~java
public User checkUser(String username, String password) {
        User user = userRepository
            .findByUserNameAndPassword(username, MD5Utils.code(password));
        return user;
    }
~~~

- 设置登录拦截器

新建一个interceptor包，在包里新建LoginInterceptor类，令其继承HandlerInterceptorAdapter类

~~~java
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (request.getSession().getAttribute("user") == null){
            response.sendRedirect("/admin");
            return false;
        }
        return true;
    }
}
~~~

重写里面的preHandle方法，通过查session里是否有user判断是否为登录状态，若未登录则重定向到登录页面。另一方面还要配置拦截器拦截的地址，新建一个WebConfig类，并加上`@Configuration`表示这个类是一个配置类。

~~~java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin")
                .excludePathPatterns("/admin/login");
    }
}
~~~

使之实现WebMvcConfigurer接口，重写里面的addInterceptors方法，把刚才做好的拦截器加入进去，在配置好拦截的路径。至此，登录功能就全部实现了。