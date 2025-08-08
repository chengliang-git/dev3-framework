### CAP 幂等消费与去重策略

为防止消息重复投递或重复消费，框架提供幂等去重能力：

#### 核心思路
- 在消费前，按“幂等键”调用去重存储 `tryMarkProcessed(key)`。
- 若返回 `false`，说明已处理过（或处于处理中），直接 ACK 跳过；返回 `true` 才进入处理逻辑。

#### 默认实现
- `DedupStorage` 接口：抽象去重存储。
- `OracleDedupStorage`：基于 Oracle 的去重实现，利用唯一键插入语义实现 SETNX；可选过期时间。
- `CapSubscriberImpl`：
  - 若存在 `JdbcTemplate`，`CapAutoConfiguration` 自动注入 `OracleDedupStorage`。
  - 默认以 `message.getId()` 作为幂等键；若业务需要，可拓展在消息头中携带业务幂等键。

#### 业务自定义幂等键（可选）
- 发布时在消息头增加业务唯一键（如订单号等），订阅端提取后作为 `tryMarkProcessed` 的 key。

#### 去重窗口
- `tryMarkProcessed(key, ttlSeconds)` 的 `ttlSeconds` 用于控制去重时间窗口（默认 24 小时示例）。
- 建议窗口覆盖“消息最大重试周期 + 业务可容忍的重复时间”。

#### 注意
- 幂等处理仅保证“至多一次处理”。业务仍需保证处理逻辑的幂等性（例如 Upsert、去重检查等）。


