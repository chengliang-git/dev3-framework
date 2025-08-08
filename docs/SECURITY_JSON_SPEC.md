### 统一异常 JSON 返回规范（Security）

本框架提供统一的安全异常 JSON 返回，未认证与权限不足均返回 JSON，便于前端统一处理。

#### 生效条件
- 引入 `guanwei-framework-security`，并使用默认的 `SecurityConfig`（或业务 SecurityConfig 注入同名处理器）。
- 自动装配的 `SecurityExceptionHandler` 存在时自动生效；业务可覆盖自定义。

#### 返回格式
- HTTP Status 固定 200，由业务 code 表达异常语义（避免部分前端拦截器将 401/403 误处理为未登录页跳转）。
- JSON 结构：

```json
{
  "code": 401,
  "message": "未认证或凭证无效",
  "timestamp": 1710000000000
}
```

或：

```json
{
  "code": 403,
  "message": "权限不足",
  "timestamp": 1710000000000
}
```

#### 状态码映射
- code=401: 未认证（AuthenticationException）
- code=403: 权限不足（AccessDeniedException）

#### 业务接入方式
1) 使用框架 Starter 默认安全配置（推荐）
- 默认启用 `SecurityExceptionHandler`（存在时），无需额外配置。

2) 业务自定义 SecurityConfig（示例）

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       SecurityExceptionHandler handler) throws Exception {
  http
    .exceptionHandling(ex -> ex
      .authenticationEntryPoint(handler)
      .accessDeniedHandler(handler)
    );
  return http.build();
}
```

#### 注意事项
- 若前端需要依赖原生 401/403，可将 `SecurityExceptionHandler` 定制为直接设置 `response.setStatus(401/403)` 并返回 JSON。
- 业务可在全局异常（非安全）继续使用 `GlobalExceptionHandler` 统一处理业务异常与参数校验异常。


