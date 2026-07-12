# SlimeCoins

Paper 1.21.11 服务器经济插件，提供货币存储、玩家交易、管理命令和 API 接口。

## 特性

- SQLite / MySQL / H2 三种数据库支持，配置切换
- 内存缓存余额，读写高效
- 完整的操作流水记录，可追溯所有余额变动
- 公开 API 和 Bukkit 事件，供其他插件集成
- PlaceholderAPI 占位符支持

## 安装

1. 将 `slimecoins-1.0-SNAPSHOT.jar` 放入 `plugins/` 目录
2. 重启服务器或执行 `/reload confirm`（不推荐热重载）
3. 编辑 `plugins/SlimeCoins/config.yml` 配置数据库和参数
4. 如需自定义消息，编辑 `plugins/SlimeCoins/messages.yml`

## 命令

主命令 `/slimecoins`，别名：`/sc` `/scoin` `/scoins` `/slime`

| 命令 | 权限 | 说明 |
|------|------|------|
| `/sc bal [玩家]` | 公开 | 查看自己或他人的余额 |
| `/sc pay <玩家> <金额>` | 公开 | 向其他玩家转账 |
| `/sc top [页数]` | 公开 | 查看余额排行榜 |
| `/sc give <玩家> <金额> [备注]` | slimecoins.admin | 给予玩家货币 |
| `/sc take <玩家> <金额> [备注]` | slimecoins.admin | 扣除玩家货币 |
| `/sc set <玩家> <金额> [备注]` | slimecoins.admin | 设置玩家余额 |
| `/sc check <玩家>` | slimecoins.admin | 查看玩家交易流水 |

## 权限

| 节点 | 默认 | 说明 |
|------|------|------|
| slimecoins.bal | 所有人 | 查看余额 |
| slimecoins.pay | 所有人 | 玩家转账 |
| slimecoins.top | 所有人 | 排行榜 |
| slimecoins.admin | OP | 所有管理命令 |

## 配置文件 (config.yml)

```yaml
database:
  type: SQLITE          # SQLITE / MYSQL / H2

economy:
  initial-balance: 0.0
  currency-symbol: "💰"
  top-page-size: 10
  minimum-payment: 0.01
  maximum-payment: 1000000.0

log:
  enabled: true
  max-return-rows: 50
```

切换数据库只需修改 `database.type` 并填写对应配置项，重启后自动建表。

## API

其他插件通过 `SlimeCoinsAPI` 调用：

```java
SlimeCoinsAPI api = SlimeCoinsAPI.getInstance();

// 查询余额
BigDecimal balance = api.getBalance(playerUUID);

// 存款 / 扣款 / 设置
EconomyResult result = api.deposit(uuid, amount, "原因");
EconomyResult result = api.withdraw(uuid, amount, "原因");
EconomyResult result = api.setBalance(uuid, amount, "原因");

// 转账
EconomyResult result = api.pay(fromUUID, toUUID, amount);

// 排行榜
List<BalanceRecord> top = api.getTopBalances(10, 0);

// 交易流水
List<TransactionLog> logs = api.getLogs(uuid, 50);
```

### 事件

- `BalanceChangeEvent` — 余额变动时触发，可取消
- `PaymentEvent` — 玩家间支付时触发，可取消

## PlaceholderAPI

| 占位符 | 输出 |
|--------|------|
| `%slimecoins_balance%` | 余额数值 |
| `%slimecoins_balance_formatted%` | 余额格式化（含千分位） |
| `%slimecoins_top_player_<1-10>%` | 排行榜第 N 名玩家名 |
| `%slimecoins_top_balance_<1-10>%` | 排行榜第 N 名余额 |

## 构建

```bash
mvn clean package
```

输出：`target/slimecoins-1.0-SNAPSHOT.jar`

依赖：
- Paper API 1.21.11-R0.1-SNAPSHOT
- PlaceholderAPI 2.11.6（可选）
- SQLite JDBC / MySQL Connector / H2（shade 打包）
