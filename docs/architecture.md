# 妯″潡鏋舵瀯涓庨厤缃綋绯?
> 璺ㄦā鍧楀紑鍙戞椂寮曠敤 鈥?妯″潡鏋舵瀯銆佺鍙ｃ€佷緷璧栧眰绾с€佺綉鍏宠矾鐢便€丯acos 閰嶇疆浣撶郴銆?
---

# 涓€銆侀」鐩ā鍧楁灦鏋?
wealth-service-platform (pom)
鈹溾攢鈹€ wealth-common      # 鍏叡渚濊禆妯″潡锛圖TO銆佸伐鍏风被銆丗eign鎺ュ彛銆佺粺涓€杩斿洖銆佸紓甯稿鐞嗐€侀€氱敤閰嶇疆锛?鈹溾攢鈹€ wealth-gateway     # 缃戝叧鏈嶅姟锛圫pring Cloud Gateway 璺敱杞彂銆佸叏灞€CORS锛?鈹溾攢鈹€ wealth-system      # 绯荤粺鏈嶅姟锛堝悗鍙版潈闄愮鐞?ums_* 琛ㄣ€佺鐞嗗憳JWT鐧诲綍銆丷BAC鏉冮檺鎷︽埅锛?鈹溾攢鈹€ wealth-user        # 鐢ㄦ埛鏈嶅姟锛堝墠绔敤鎴风鐞?sys_user锛?鈹溾攢鈹€ wealth-account     # 璐︽埛鏈嶅姟锛堣嚜閫夌鐞?wea_user_favorite锛?鈹溾攢鈹€ wealth-product     # 浜у搧鏈嶅姟锛堜骇鍝?wea_product + 琛屾儏 wea_market_data锛?鈹溾攢鈹€ wealth-trade       # 浜ゆ槗鏈嶅姟锛堝鎵樹氦鏄?wea_trade_order锛?鈹溾攢鈹€ wealth-message     # 娑堟伅鏈嶅姟锛堣祫璁?wea_news + 绔欏唴娑堟伅 wea_message锛?鈹斺攢鈹€ wealth-search      # 鎼滅储鏈嶅姟锛堝熀浜?ES 8 鐨勪骇鍝佹悳绱紝鏃犳暟鎹簱渚濊禆锛?
## 渚濊禆灞傜骇

- wealth-common 琚櫎 gateway 澶栫殑鎵€鏈夋ā鍧椾緷璧栵紙淇敼鍚庨渶鍏?mvn clean install -pl wealth-common锛?- wealth-gateway 涓嶄緷璧?wealth-common锛堥伩鍏?spring-boot-starter-web 涓?WebFlux 鍐茬獊锛?- 涓氬姟妯″潡闂撮€氳繃 Feign 鎺ュ彛璋冪敤锛團eignClient 瀹氫箟鍦?wealth-common 涓級
- wealth-system 鏄惧紡瑕嗙洊 mybatis-spring 鐗堟湰涓?3.0.5锛堢埗 POM 涓?3.0.4锛?
## 鍚勬ā鍧楃鍙ｅ彿

| 妯″潡 | 绔彛 | context-path | 璇存槑 |
|------|------|-------------|------|
| wealth-gateway | 8080 | - | Spring Cloud Gateway锛圵ebFlux锛?|
| wealth-system  | 8082 | /system | 鍚庡彴鏉冮檺绠＄悊 |
| wealth-user    | 8083 | /user | 鍓嶇鐢ㄦ埛绠＄悊 |
| wealth-product | 8084 | /product | 浜у搧 + 琛屾儏 |
| wealth-trade   | 8085 | /trade | 浜ゆ槗濮旀墭 |
| wealth-account | 8086 | /account | 鐢ㄦ埛鑷€?|
| wealth-message | 8087 | /message | 璧勮 + 娑堟伅 |
| wealth-search  | 8089 | - | ES 鎼滅储 |

## 鍚勬ā鍧?Java 鍖呭熀璺緞

| 妯″潡 | 鍩虹鍖?|
|------|--------|
| wealth-common  | com.wealth.common |
| wealth-gateway | com.wealth.gateway |
| wealth-user    | com.wealth.user |
| 鍏朵綑涓氬姟妯″潡    | com.wealth.platform.{妯″潡鍚峿 |

## 缃戝叧璺敱

gateway锛堢鍙?8080锛夎礋璐ｇ粺涓€璺敱杞彂锛屾墍鏈夊墠绔姹傜粺涓€缁忕綉鍏宠闂悇妯″潡锛?
| 璺敱鍓嶇紑 | 鐩爣鏈嶅姟 |
|---------|---------|
| /system/** | wealth-system |
| /user/** | wealth-user |
| /product/** | wealth-product |
| /account/** | wealth-account |
| /trade/** | wealth-trade |
| /message/** | wealth-message |
| /search/** | wealth-search |

---

# 浜屻€侀厤缃綋绯伙紙寮哄埗閿佸畾锛屼笉寰椾慨鏀癸級

## 閰嶇疆鎬诲垯锛堥搧寰嬶級

> **馃毇 绂佹淇敼浠讳綍閰嶇疆鏂囦欢** 鈥?鍖呮嫭浣嗕笉闄愪簬锛氭墍鏈夋ā鍧楃殑 application.yml銆乥ootstrap.yml銆乸om.xml銆丯acos 閰嶇疆銆?>
> 鎵€鏈変笟鍔￠厤缃凡鍦?Nacos 閰嶇疆涓績缁熶竴绠＄悊锛屾湰鍦伴厤缃枃浠朵负涓€娆℃€у啓鍏ョ殑鍥哄畾鍊笺€?> 浠讳綍閰嶇疆鍙樻洿闇€姹傚繀椤荤粡杩囨灦鏋勮瘎瀹★紝涓嶅緱绉佽嚜淇敼銆?
---

## Nacos 閰嶇疆涓績锛圖ocker: nacos/nacos-server:v2.3.2锛?
鍦板潃锛歚localhost:8848`锛堟棤闇€璁よ瘉锛?
### wealth-shared.yaml锛圖EFAULT_GROUP锛孻AML 鏍煎紡锛?
鎵€鏈夋ā鍧楀叡浜殑鍞竴 Nacos 閰嶇疆銆傚唴瀹瑰涓嬶細

```yaml
jwt:
  secret: wealth-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
```

> **浣滅敤鑼冨洿**锛歚wealth-shared.yaml` 閫氳繃鍚勬ā鍧?bootstrap.yml 鐨?`shared-configs` 寮曠敤锛岃鎵€鏈夋ā鍧楀姞杞姐€?> **瑕嗙洊浼樺厛绾?*锛歂acos shared-configs 鐨勪紭鍏堢骇浣庝簬鍚勬ā鍧楁湰鍦?application.yml锛屼絾楂樹簬 bootstrap.yml 涓殑榛樿鍊笺€?> 鏈厤缃彁渚?JWT 瀵嗛挜/杩囨湡鏃堕棿鍜?MySQL 鏁版嵁婧愶紝鍚勬ā鍧楀嚟姝よ繛鎺ユ暟鎹簱銆?
### 閰嶇疆鍔犺浇閾捐矾

```
bootstrap.yml                     # 1. 鍚姩鏃跺姞杞?鈥斺€?閰嶇疆 Nacos 鍦板潃銆佸簲鐢ㄥ悕
  鈹斺啋 Nacos (wealth-shared.yaml)  # 2. Nacos 杩滅▼閰嶇疆 鈥斺€?JWT + 鏁版嵁婧?      鈹斺啋 application.yml          # 3. 鏈湴閰嶇疆 鈥斺€?绔彛銆乧ontext-path銆乵ybatis-plus
```

---

## 鏈湴閰嶇疆鏂囦欢娓呭崟锛堝凡鏈夊唴瀹癸紝绂佹淇敼锛?
### 1. bootstrap.yml锛堟墍鏈変笟鍔℃ā鍧楃粺涓€妯″紡锛?
鎵€鏈?8 涓ā鍧楃殑 `bootstrap.yml` 鍐呭瀹屽叏涓€鑷达紙浠?`application.name` 涓嶅悓锛夛細

```yaml
spring:
  application:
    name: wealth-{妯″潡鍚峿
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yaml
        shared-configs:
          - data-id: wealth-shared.yaml
            refresh: true
```

鍚勬ā鍧?`application.name` 瀵瑰簲鍊硷細

| 妯″潡 | application.name |
|------|-----------------|
| gateway | wealth-gateway |
| system | wealth-system |
| user | wealth-user |
| product | wealth-product |
| account | wealth-account |
| trade | wealth-trade |
| message | wealth-message |
| search | wealth-search |

### 2. application.yml 鍚勬ā鍧楄鎯?
#### wealth-gateway锛堢鍙?8080锛屾棤 context-path锛?```yaml
server:
  port: 8080
spring:
  cloud:
    gateway:
      routes:
        - id: wealth-system
        - id: wealth-user
        - id: wealth-product
        - id: wealth-account
        - id: wealth-trade
        - id: wealth-message
        - id: wealth-search
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origin-patterns: http://localhost:3000, http://localhost:8080, http://127.0.0.1:3000
```
> gateway 鏃犳暟鎹簮锛屼笉渚濊禆 wealth-common锛圵ebFlux 涓?spring-boot-starter-web 鍐茬獊锛夈€?
#### wealth-system锛堢鍙?8082锛宑ontext-path: /system锛?```yaml
server:
  port: 8082
  servlet:
    context-path: /system
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```
> 娉細`password: ${DB_PASSWORD}` 鐢?Nacos `wealth-shared.yaml` 涓殑 `spring.datasource.password: 123456` 瑕嗙洊銆?
#### wealth-user锛堢鍙?8083锛宑ontext-path: /user锛?```yaml
server:
  port: 8083
  servlet:
    context-path: /user
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-product锛堢鍙?8084锛宑ontext-path: /product锛?```yaml
server:
  port: 8084
  servlet:
    context-path: /product
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-account锛堢鍙?8086锛宑ontext-path: /account锛?```yaml
server:
  port: 8086
  servlet:
    context-path: /account
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-trade锛堢鍙?8085锛宑ontext-path: /trade锛?```yaml
server:
  port: 8085
  servlet:
    context-path: /trade
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-message锛堢鍙?8087锛宑ontext-path: /message锛?```yaml
server:
  port: 8087
  servlet:
    context-path: /message
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-search锛堢鍙?8089锛屾棤 context-path锛屾棤鏁版嵁婧愶級
```yaml
server:
  port: 8089
spring:
  elasticsearch:
    uris: ${ES_URIS:http://localhost:9200}
    username: ${ES_USERNAME:elastic}
    password: ${ES_PASSWORD:}
```
> wealth-search 涓嶈繛鎺?MySQL锛屼粎杩炴帴 ElasticSearch 8銆傛棤 `spring.datasource` 閰嶇疆銆?
---

## 鏈湴閰嶇疆鏂囦欢涓?Nacos 瑕嗙洊鍏崇郴

| 閰嶇疆椤?| 鏈湴鍖哄煙 | Nacos 瑕嗙洊 | 鐢熸晥缁撴灉 |
|--------|---------|-----------|---------|
| server.port | application.yml | 鏃?| 鏈湴鍊?|
| server.servlet.context-path | application.yml | 鏃?| 鏈湴鍊?|
| spring.datasource.url | application.yml | 鏃?| 鏈湴鍊硷紙Nacos 鍚屽悕閰嶇疆宸茶姝よ鐩栵級 |
| spring.datasource.username | application.yml | N/A | 鏈湴鍊?|
| **spring.datasource.password** | application.yml (`${DB_PASSWORD}`) | **`123456`** | **Nacos 瑕嗙洊鐢熸晥** |
| spring.elasticsearch.* | application.yml (search) | 鏃?| 鏈湴鍊?|
| mybatis-plus.* | application.yml | 鏃?| 鏈湴鍊?|
| springdoc.* | application.yml | 鏃?| 鏈湴鍊?|
| **jwt.secret** | 鏃?| **wealth-shared.yaml** | **浠?Nacos** |
| **jwt.expire** | 鏃?| **wealth-shared.yaml** | **浠?Nacos** |

> 鍏抽敭锛歚password: ${DB_PASSWORD}` 鏈韩鏄棤鏁堢殑鐜鍙橀噺寮曠敤锛堢郴缁熶腑鏈缃?`DB_PASSWORD`锛夛紝鏁版嵁搴撳瘑鐮佺敱 Nacos `wealth-shared.yaml` 涓殑 `spring.datasource.password: 123456` 鎻愪緵銆侼acos 閰嶇疆浼樺厛绾ч珮浜庢湰鍦伴厤缃腑鐨勭幆澧冨彉閲忓紩鐢ㄣ€?
---

## 鍩虹璁炬柦 Docker 瀹瑰櫒

| 鏈嶅姟 | 闀滃儚 | 绔彛 |
|------|------|------|
| Nacos | nacos/nacos-server:v2.3.2 | 8848, 9848-9849 |
| MySQL | 8.0.37 (鏈湴瀹夎) | 3306 |
| Redis | redis:latest | 6379 |
| RabbitMQ | rabbitmq:3.10-management | 5672, 15672 |
| ElasticSearch | elasticsearch:8.8.2 | 9200, 9300 |
| Nginx | nginx:latest | 80 |

---

## 宸茬煡 v1.4.0 閰嶇疆闄愬埗锛堝緟淇锛屼絾涓嶅彲鐩存帴鏀归厤缃級

| 闂 | 褰卞搷 | 璇存槑 |
|------|------|------|
| **RedisConfig 缂哄皯 @ConditionalOnClass** | wealth-search 鍚姩澶辫触 | `RedisConfig.java` 鍜?`RedisUtil.java` 缂哄皯 `@ConditionalOnClass` 鏉′欢娉ㄨВ锛屽鑷存棤 Redis 渚濊禆鐨勬ā鍧楋紙wealth-search锛夊惎鍔ㄦ椂 `NoClassDefFoundError`銆備慨澶嶉渶鏀?Java 婧愮爜锛屼笉鏀归厤缃€?|
| **AuthConstant.PERMIT_ALL_URLS 缂哄皯 user 妯″潡璺緞** | wealth-user 鍏ㄩ儴鎺ュ彛杩斿洖 401 | 褰撳墠鍙湁 `/system/umsAdmin/login` 鍦ㄦ斁琛屽垪琛ㄤ腑锛宍/user/user/login` 绛夎矾寰勮 LoginInterceptor 鎷︽埅銆傛柊澧炴ā鍧楁椂椤诲悓鏃舵洿鏂?`AuthConstant.java`銆?|
| **PERMIT_ALL_URLS 鏈湪 LoginInterceptor 娉ㄥ唽妯″潡涓娇鐢?* | 璇ュ父閲忕洰鍓嶆湭琚?LoginInterceptor 鎵€鍦ㄦā鍧楀紩鐢?| LoginInterceptor 浣嶄簬 wealth-common锛屼絾 user 妯″潡鏈敞鍐屾鎷︽埅鍣ㄣ€傚綋鍓?user 妯″潡娌℃湁 WebMvcConfigurer銆?|
