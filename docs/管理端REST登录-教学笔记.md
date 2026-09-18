# 管理端 REST 登录：11 个文件的教学笔记

> 写给大二的你：不要死记代码，要理解"一个请求从进来到出去，经过了哪些文件，每个文件干了什么，为什么需要它"。
>
> 核心心法：**分层 = 每个文件只干一件事，改一处不影响全局。**

---

## 一、先看大图景：一次登录的完整旅程

```
前端(Apifox)                      后端
   │  POST /api/v1/admin/login      │
   │  body: {userName, password}    │
   │ ─────────────────────────────▶ │
   │                               ① AdminLoginApiController（收请求）
   │                               ② AdminUserLoginDTO（@Valid 校验参数）
   │                               ③ AdminServiceImpl.loginAndIssueToken（业务逻辑）
   │                                  ├─ 调用原有 login() 验密码
   │                                  └─ 生成 Token → 存数据库
   │                               ④ AdminTokenMapper.insert（写库）
   │  返回 {token, profile}        ⑤ AdminLoginVO（包装返回数据）
   │ ◀───────────────────────────── │
   │
   │  GET /api/v1/admin/info
   │  Header: Authorization: Bearer xxx
   │ ─────────────────────────────▶ │
   │                               ⑥ AdminTokenInterceptor.preHandle（先于 Controller 执行）
   │                                  └─ 查 Token 是否存在、是否过期
   │                               ⑦ AdminLoginApiController.info（取 adminUserId）
   │  返回 {adminUserId, ...}      │
   │ ◀───────────────────────────── │
```

记住这张图，下面每个文件都在这张图里有位置。

---

## 二、11 个文件逐个讲

### 🟦 第一组：数据层 —— Token 存在哪？

#### 1. `V4__admin_token.sql`（建表脚本）

- **是什么**：一条 SQL，创建 `tb_newbee_mall_admin_token` 表
- **怎么做**：
  ```sql
  CREATE TABLE tb_newbee_mall_admin_token (
      admin_token_id INT PRIMARY KEY AUTO_INCREMENT,
      token VARCHAR(64) UNIQUE NOT NULL,        -- Token 字符串，唯一
      admin_user_id INT NOT NULL,               -- 哪个管理员
      expires_at DATETIME NOT NULL,             -- 什么时候过期
      create_time DATETIME DEFAULT CURRENT_TIMESTAMP
  );
  ```
- **为什么**：Token 认证的本质是"我给你一张纸条（token），你下次来拿着纸条证明你是你"。这张纸条必须有地方存，数据库就是那个地方。以后可以换成 Redis（更快），但 DB 最容易理解。
- **为什么有 expires_at**：Token 不能永久有效，否则被盗了就一直能用。设 7 天过期，到期要重新登录。

#### 2. `AdminToken.java`（实体类）

- **是什么**：Java 里代表"一条 Token 记录"的对象，和数据库表一一对应
- **怎么做**：5 个字段（adminTokenId, token, adminUserId, expiresAt, createTime），用 Lombok `@Data` 自动生成 getter/setter
- **为什么**：Java 是面向对象的，操作数据库时不能直接用 ResultSet，要把一行数据装进一个对象里。这个对象就叫 Entity。
- **对比原项目**：`Admin.java` 对应 `tb_newbee_mall_admin_user` 表，`AdminToken.java` 对应新建的 Token 表，套路完全一样。

#### 3. `AdminTokenMapper.java` + `AdminTokenMapper.xml`（数据访问层）

- **是什么**：Mapper 接口定义"能对 Token 表做哪些操作"，XML 写具体的 SQL
- **怎么做**：
  ```java
  // 接口：声明 3 个方法
  public interface AdminTokenMapper {
      int insert(AdminToken record);              // 存 Token
      AdminToken selectByToken(String token);     // 用 Token 查记录
      int deleteByToken(String token);            // 登出时删 Token
  }
  ```
  ```xml
  <!-- XML：写具体 SQL -->
  <insert id="insert" ...>
      insert into tb_newbee_mall_admin_token (token, admin_user_id, expires_at, create_time)
      values (#{token}, #{adminUserId}, #{expiresAt}, #{createTime})
  </insert>
  ```
- **为什么要分成接口 + XML**：这是 MyBatis 的约定。接口负责"签名"（方法名、参数、返回值），XML 负责"实现"（SQL 语句）。好处是改 SQL 不用动 Java 代码。
- **对比原项目**：打开原项目的 `AdminMapper.java` + `AdminMapper.xml`，结构一模一样。我就是照抄的风格。

---

### 🟩 第二组：接口层 —— 请求怎么收、数据怎么回

#### 4. `AdminUserLoginDTO.java`（入参 DTO）

- **是什么**：Data Transfer Object，专门用来接收前端传来的数据
- **怎么做**：
  ```java
  @Data
  public class AdminUserLoginDTO {
      @NotBlank(message = "请输入登录名")
      private String userName;
      @NotBlank(message = "请输入密码")
      private String password;
  }
  ```
- **为什么不用散装 @RequestParam**：原项目是 `@RequestParam("userName") String userName`，参数一多就乱。DTO 把相关参数打包成一个类，而且可以在字段上加 `@NotBlank` 注解自动校验——**不用再写 if 判空**。
- **`@NotBlank` 怎么生效**：Controller 方法参数前加 `@Valid`，Spring 会自动检查 DTO 里的注解，校验失败直接返回 400，根本进不了 Service 代码。

#### 5. `AdminLoginVO.java`（出参 VO）

- **是什么**：View Object，专门用来返回给前端的数据
- **怎么做**：
  ```java
  public class AdminLoginVO {
      private String token;                    // 登录凭证
      private AdminUserProfileVO profile;      // 管理员资料（复用原项目的 VO）
  }
  ```
- **为什么不能直接返回 Admin 实体**：`Admin` 实体里有 `password` 字段（密码哈希）。如果直接返回给前端，密码哈希就泄漏了。VO 就是一道"防火墙"——只挑安全的字段返回。
- **DTO vs VO vs Entity 速记**：
  - **Entity**：和数据库表对应，内部用
  - **DTO**：前端 → 后端（入参）
  - **VO**：后端 → 前端（出参）
  - 三者分开 = 数据不会乱串，密码不会泄漏

#### 6. `AdminLoginApiController.java`（控制器）

- **是什么**：URL 的"门卫"，决定哪个 URL 由哪个方法处理
- **怎么做**：3 个接口
  ```
  POST /api/v1/admin/login   → 登录，返回 token
  POST /api/v1/admin/logout  → 登出，删 token
  GET  /api/v1/admin/info    → 获取当前登录管理员信息（需带 token）
  ```
- **Controller 该多瘦有多瘦**：好的 Controller 只做三件事——收参、调 Service、返回结果。**不写业务逻辑**。
  ```java
  @PostMapping("/login")
  public Result<AdminLoginVO> login(@Valid @RequestBody AdminUserLoginDTO dto) {
      return ResultGenerator.genSuccessResult(adminService.loginAndIssueToken(dto));
  }
  ```
  这就是好的 Controller：一行调用，没有 if、没有 try-catch、没有数据库操作。
- **对比原项目页面版 Controller**：原项目 `AdminController.login()` 有 30 行（判空、验验证码、存 Session、跳转），REST 版只有 1 行业务调用。差距就在"职责分离"。

---

### 🟧 第三组：业务层 —— 登录逻辑在哪？

#### 7. `AdminService.java` + `AdminServiceImpl.java`（修改，非新建）

- **是什么**：Service 层放业务逻辑。我没新建 Service，而是在原有的 `AdminService` 里加了两个方法
- **怎么做**：
  ```java
  // 接口新增
  AdminLoginVO loginAndIssueToken(AdminUserLoginDTO dto);
  void logoutByToken(String token);
  ```
  ```java
  // 实现
  public AdminLoginVO loginAndIssueToken(AdminUserLoginDTO dto) {
      Admin admin = login(dto.getUserName(), dto.getPassword());  // 复用原有的密码校验！
      if (admin == null) throw new NewBeeMallException("登录失败，请检查用户名和密码");

      String token = UUID.randomUUID().toString().replace("-", "");
      AdminToken adminToken = new AdminToken();
      adminToken.setToken(token);
      adminToken.setAdminUserId(admin.getAdminUserId());
      adminToken.setExpiresAt(new Date(System.currentTimeMillis() + 7天));
      adminTokenMapper.insert(adminToken);

      return AdminLoginVO.of(token, getAdminUserProfileById(admin.getAdminUserId()));
  }
  ```
- **为什么复用原有的 `login()` 方法**：原项目 `login()` 已经写好了"MD5 加密密码 + 查数据库 + 校验 locked 状态"的逻辑。**不要重写已经能跑的代码**——复用，出 bug 的概率更低。这就是"开闭原则"：对扩展开放，对修改关闭。
- **Token 怎么生成**：`UUID.randomUUID()` 是 Java 自带的，生成一个全球唯一的字符串，去掉横线就是 32 位 token。简单、够用。以后可以换成 JWT，但 UUID 最容易理解。

---

### 🟥 第四组：安全层 —— 后续请求怎么证明你已登录？

#### 8. `AdminTokenInterceptor.java`（拦截器，核心！）

- **是什么**：Spring 的拦截器，在 Controller 方法执行**之前**先跑一遍。相当于"进门查票"。
- **怎么做**：
  ```java
  public boolean preHandle(request, response, handler) {
      // 1. 从请求头取出 token
      String auth = request.getHeader("Authorization");  // "Bearer xxx"
      String token = auth.substring(7);                  // 去掉 "Bearer "

      // 2. 去数据库查这个 token
      AdminToken adminToken = adminTokenMapper.selectByToken(token);

      // 3. 查不到 或 已过期 → 返回 401，不放行
      if (adminToken == null || adminToken.getExpiresAt().before(new Date())) {
          返回 401 JSON "未登录或Token已失效";
          return false;
      }

      // 4. 校验通过 → 把 adminUserId 放进 request，给后面的 Controller 用
      request.setAttribute("adminUserId", adminToken.getAdminUserId());
      return true;
  }
  ```
- **为什么需要拦截器**：如果没有拦截器，每个 Controller 方法里都要自己写"取 token、查数据库、判过期"的代码，10 个接口就重复 10 遍。拦截器把这段逻辑抽出来，**一处写，处处用**。
- **对比原项目 Session 拦截器**：原项目 `AdminLoginInterceptor` 从 Session 里找 `loginUser`，找不到就 `sendRedirect` 跳登录页。REST 版从 Authorization 头找 token，找不到就返回 401 JSON。逻辑一样，只是"凭证"的存放位置变了。

#### 9. `NeeBeeMallWebMvcConfigurer.java`（修改：注册拦截器）

- **是什么**：Spring MVC 的配置类，告诉 Spring"哪些 URL 要被哪个拦截器拦截"
- **怎么做**：
  ```java
  registry.addInterceptor(adminTokenInterceptor)
          .addPathPatterns("/api/v1/admin/**")      // 拦所有管理端 API
          .excludePathPatterns("/api/v1/admin/login");  // 放行登录（登录时还没有 token）
  ```
- **为什么要放行 login**：登录接口本身不需要 token（用户来登录就是为了拿 token）。如果不放行，就成了"你要先登录才能登录"——死循环。
- **对比原项目**：原项目在这里注册了 `AdminLoginInterceptor`（拦 `/admin/**` 页面），我只是**追加**了一个 `AdminTokenInterceptor`（拦 `/api/v1/admin/**`），原有注册没动。

#### 10. `CsrfTokenFilter.java`（修改：放行 /api/**）

- **是什么**：原项目的 CSRF 防护过滤器，要求所有 POST 请求带 CSRF token
- **为什么要改**：CSRF 攻击的原理是"浏览器自动带 Cookie"。但我们的 REST 接口用 `Authorization` 头认证，**不用 Cookie**，所以攻击者根本伪造不了这个头——**Token 头认证天然免疫 CSRF**。所以 `/api/**` 不需要 CSRF 校验，直接放行。
- **这是面试常考点**：面试官问"你的接口怎么防 CSRF"，答"我用 Bearer Token 认证，不依赖 Cookie，天然免疫 CSRF"。

---

### 额外（第 11 个文件）：`pom.xml`（修改依赖）

把 `spring-boot-starter-validation` 加进来（给 `@Valid` 用），其他依赖和原项目保持一致。

---

## 三、一句话总结每个文件的职责

| 文件 | 一句话职责 |
|---|---|
| V4 SQL | 建 Token 表 |
| AdminToken | Token 的 Java 对象 |
| AdminTokenMapper + XML | 操作 Token 表 |
| AdminUserLoginDTO | 接收登录参数 + 自动校验 |
| AdminLoginVO | 返回给前端的安全数据 |
| AdminLoginApiController | URL 门卫，收请求转 Service |
| AdminService/Impl | 登录业务逻辑（复用原密码校验） |
| AdminTokenInterceptor | 查票：每次请求验证 Token |
| WebMvcConfigurer | 告诉 Spring 哪些 URL 要查票 |
| CsrfTokenFilter | 放行 API（Token 头免疫 CSRF） |
| pom.xml | 加 validation 依赖 |

---

## 四、中厂工程思维提炼（面试能用上）

1. **分层解耦**：Controller 只管收参返回，Service 只管业务，Mapper 只管 SQL。改 SQL 不动 Controller，改业务不动 Mapper。
2. **DTO/VO/Entity 三分离**：入参、出参、数据库实体绝不混用，防止密码等敏感字段泄漏。
3. **开闭原则**：不删原有的 `login()`，而是复用它、在旁边加 `loginAndIssueToken()`。老代码不动，新功能叠加。
4. **横切逻辑抽拦截器**：认证这种"每个接口都要做"的事，写成拦截器，一处配置全局生效。
5. **错误统一处理**：`throw NewBeeMallException` 比"返回字符串让上层 equals 判断"优雅，异常处理器统一转 JSON。
6. **RESTful 约定**：GET 查、POST 增/操作、PUT 改、DELETE 删；路径用名词（`/admin/login`），不用动词（`/admin/doLogin`）。
