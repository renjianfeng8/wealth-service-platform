# 閲戣瀺寰湇鍔′腑鍙?(Wealth Service Platform)

> 鍩轰簬 Spring Boot 3.x + Spring Cloud Alibaba 鐨勯噾铻嶇骇寰湇鍔′腑鍙版灦鏋勯」鐩紝瑕嗙洊鐢ㄦ埛銆佽处鎴枫€佷骇鍝併€佷氦鏄撱€佹秷鎭瓑鏍稿績涓氬姟棰嗗煙锛屾彁渚涢珮鍙敤銆侀珮鎵╁睍鐨勪紒涓氱骇閲戣瀺瑙ｅ喅鏂规銆?
---

## 鐩綍

- [椤圭洰绠€浠媇(#椤圭洰绠€浠?
- [鎶€鏈爤娓呭崟](#鎶€鏈爤娓呭崟)
- [妯″潡鏋舵瀯](#妯″潡鏋舵瀯)
- [鏍稿績鍔熻兘璇存槑](#鏍稿績鍔熻兘璇存槑)
- [鐜鎼缓](#鐜鎼缓)
- [椤圭洰鍚姩姝ラ](#椤圭洰鍚姩姝ラ)
- [鎺ュ彛鏂囨。](#鎺ュ彛鏂囨。)
- [椤圭洰瑙勮寖](#椤圭洰瑙勮寖)
- [鍙樻洿璁板綍](#鍙樻洿璁板綍)
- [鍚庣画寮€鍙戝缓璁甝(#鍚庣画寮€鍙戝缓璁?

---

## 椤圭洰绠€浠?
### 椤圭洰瀹氫綅

闈㈠悜閲戣瀺涓氬姟鍦烘櫙鐨勫井鏈嶅姟涓彴锛屽皢浼犵粺鍗曚綋鏋舵瀯鎷嗗垎涓哄涓嫭绔嬮儴缃茬殑寰湇鍔℃ā鍧楋紝瀹炵幇涓氬姟瑙ｈ€︺€佺嫭绔嬭凯浠ｃ€佸脊鎬т几缂┿€?
### 涓氬姟鍦烘櫙

- **璇佸埜/鍩洪噾琛屾儏灞曠ず**锛氬疄鏃惰鎯呮暟鎹帴鍏ヤ笌灞曠ず
- **閲戣瀺浜у搧绠＄悊**锛氫骇鍝佷笂鏋躲€佸垎绫汇€佹煡璇?- **鐢ㄦ埛鑷€夌鐞?*锛氱敤鎴疯嚜閫変骇鍝佸叧娉?- **浜ゆ槗濮旀墭**锛氫氦鏄撹鍗曞彂璧蜂笌绠＄悊
- **璧勮娑堟伅**锛氳储缁忚祫璁帹閫併€佺珯鍐呮秷鎭€氱煡
- **鍚庡彴鏉冮檺绠＄悊**锛氱粺涓€鍚庡彴绠＄悊鍛樸€佽鑹层€佽祫婧愭潈闄愭帶鍒?
### 鏍稿績鍔熻兘妯″潡

| 棰嗗煙 | 妯″潡 | 鏍稿績鑳藉姏 |
|------|------|----------|
| 鐢ㄦ埛鍩?| wealth-user | 绯荤粺鐢ㄦ埛娉ㄥ唽/鐧诲綍銆佷釜浜轰俊鎭鐞?|
| 浜у搧鍩?| wealth-product | 閲戣瀺浜у搧绠＄悊銆佽鎯呮暟鎹?|
| 璐︽埛鍩?| wealth-account | 鐢ㄦ埛鑷€変骇鍝佺鐞?|
| 浜ゆ槗鍩?| wealth-trade | 浜ゆ槗濮旀墭鍗曞彂璧枫€佹挙閿€銆佹煡璇?|
| 娑堟伅鍩?| wealth-message | 璐㈢粡璧勮銆佺珯鍐呮秷鎭?|
| 鎼滅储鍩?| wealth-search | 鍩轰簬 ES 鐨勪骇鍝佸叏鏂囨绱?|
| 绯荤粺鍩?| wealth-system | 绠＄悊鍛樸€佽鑹层€佽祫婧愩€佹潈闄愭嫤鎴?|
| 缃戝叧鍩?| wealth-gateway | 缁熶竴璺敱銆丆ORS |
| 鍏叡鍩?| wealth-common | 宸ュ叿绫汇€丗eign 鎺ュ彛銆佸叏灞€寮傚父澶勭悊 |

---

## 鎶€鏈爤娓呭崟

### 鍚庣鏍稿績

| 鎶€鏈?| 鐗堟湰 | 璇存槑 |
|------|------|------|
| JDK | 21.0.3 | 闀挎湡鏀寔鐗堟湰 |
| Maven | 3.9.9 | 椤圭洰鏋勫缓绠＄悊 |
| Spring Boot | 3.3.5 | 搴旂敤鍩虹妗嗘灦 |
| Spring Cloud | 2023.0.3 | 寰湇鍔＄粍浠?|
| Spring Cloud Alibaba | 2023.0.1.2 | Alibaba 寰湇鍔＄敓鎬?|
| MyBatis-Plus | **3.5.7** | ORM 妗嗘灦锛堟渶鍚庝竴涓寘鍚?PaginationInnerInterceptor 鐨勭ǔ瀹氱増鏈級 |
| MySQL | 8.0.37 | 鍏崇郴鍨嬫暟鎹簱 |
| Redis | 5.0.14.1 | 缂撳瓨 |
| RabbitMQ | 3.10.20 | 娑堟伅闃熷垪 |
| Elasticsearch | 8.11.0 | 鎼滅储寮曟搸 |

### 鍓嶇

| 鎶€鏈?| 璇存槑 |
|------|------|
| Vue 3 | 娓愯繘寮忓墠绔鏋?|
| TypeScript | 绫诲瀷瀹夊叏鐨?JavaScript 瓒呴泦 |
| Element Plus | 鍩轰簬 Vue 3 鐨勪紒涓氱骇 UI 缁勪欢搴?|
| Vite | 鍓嶇鏋勫缓涓庡紑鍙戞湇鍔″櫒 |
| Pinia | 鐘舵€佺鐞?|
| Axios | HTTP 璇锋眰搴?|
| Vue Router | 鍓嶇璺敱 |

### 鏍稿績渚濊禆

| 缁勪欢 | 鐢ㄩ€?|
|------|------|
| Nacos (2.x) | 鏈嶅姟娉ㄥ唽鍙戠幇 + 閰嶇疆涓績 |
| Spring Cloud Gateway | 缃戝叧璺敱杞彂 |
| OpenFeign | 鏈嶅姟闂村０鏄庡紡璋冪敤 |
| JWT (jjwt 0.11.5) | 鏃犵姸鎬佽璇?|
| Knife4j (4.4.0) | API 鏂囨。 |
| BCrypt (spring-security-crypto) | 瀵嗙爜鍔犲瘑 |
| Lombok | 浠ｇ爜绠€鍖?|

### 涓棿浠剁増鏈搴?
| 涓棿浠?| 绔彛 | 閮ㄧ讲鏂瑰紡 |
|--------|------|----------|
| Nacos Server | 8848 | Docker / 鐙珛閮ㄧ讲 |
| MySQL | 3306 | Docker / 鏈湴瀹夎 |
| Redis | 6379 | Docker |
| RabbitMQ | 5672 / 15672 | Docker |
| Elasticsearch | 9200 / 9300 | Docker |

---

## 妯″潡鏋舵瀯

### 妯″潡渚濊禆鍏崇郴

```
wealth-service-platform (pom)
鈹溾攢鈹€ wealth-common      鈫?鎵€鏈夋ā鍧椾緷璧栵紙鍏叡宸ュ叿銆丗eign 鎺ュ彛銆佺粺涓€杩斿洖銆佸紓甯稿鐞嗐€佸叏灞€閰嶇疆锛?鈹溾攢鈹€ wealth-gateway     鈫?缃戝叧璺敱锛堜緷璧?common锛?鈹溾攢鈹€ wealth-system      鈫?鍚庡彴鏉冮檺锛堜緷璧?common锛岄€氳繃 Feign 璋冪敤 account/product锛?鈹溾攢鈹€ wealth-user        鈫?鐢ㄦ埛鏈嶅姟锛堜緷璧?common锛?鈹溾攢鈹€ wealth-account     鈫?鑷€夋湇鍔★紙渚濊禆 common锛?鈹溾攢鈹€ wealth-product     鈫?浜у搧鏈嶅姟锛堜緷璧?common锛?鈹溾攢鈹€ wealth-trade       鈫?浜ゆ槗鏈嶅姟锛堜緷璧?common锛?鈹溾攢鈹€ wealth-message     鈫?娑堟伅鏈嶅姟锛堜緷璧?common锛?鈹斺攢鈹€ wealth-search      鈫?鎼滅储鏈嶅姟锛堜緷璧?common锛?```

> **娉ㄦ剰**锛氫慨鏀?`wealth-common` 鍚庡繀椤诲厛鎵ц `mvn clean install -pl wealth-common -DskipTests`锛屽叾浠栨ā鍧楁墠鑳藉紩鐢ㄦ渶鏂扮増鏈€?
### 绔彛涓庝笂涓嬫枃璺緞

| 妯″潡 | 绔彛 | context-path | 鏈嶅姟鍚?|
|------|------|-------------|--------|
| wealth-gateway | 8080 | - | wealth-gateway |
| wealth-system | 8082 | /system | wealth-system |
| wealth-product | 8084 | /product | wealth-product |
| wealth-trade | 8085 | /trade | wealth-trade |
| wealth-account | 8086 | /account | wealth-account |
| wealth-message | 8087 | /message | wealth-message |
| wealth-user | 8083 | /user | wealth-user |
| wealth-search | 8089 | - | wealth-search |

### 鍖呰矾寰勮鑼?
| 妯″潡 | 鍩虹鍖?|
|------|--------|
| wealth-common | `com.wealth.common` |
| wealth-gateway | `com.wealth.gateway` |
| wealth-user | `com.wealth.user` |
| 鍏朵綑涓氬姟妯″潡 | `com.wealth.platform.{妯″潡鍚峿` |

### 鍚勬ā鍧楀寘缁撴瀯

```
com.wealth.platform.{妯″潡鍚峿
鈹溾攢鈹€ controller    # RESTful 鎺ュ彛灞?鈹溾攢鈹€ service       # 涓氬姟閫昏緫灞?鈹溾攢鈹€ mapper        # MyBatis-Plus DAO 灞?鈹溾攢鈹€ entity        # 鏁版嵁搴撳疄浣擄紙缁ф壙 BaseEntity锛?鈹溾攢鈹€ vo            # 杩斿洖缁欏墠绔殑鏁版嵁瀵硅薄
鈹溾攢鈹€ dto           # 鎺ユ敹鍓嶇鍙傛暟鐨勪紶杈撳璞?鈹溾攢鈹€ config        # 妯″潡閰嶇疆
鈹溾攢鈹€ util          # 宸ュ叿绫?鈹溾攢鈹€ constant      # 甯搁噺
鈹溾攢鈹€ exception     # 寮傚父
鈹斺攢鈹€ common        # 妯″潡鍐呭叕鍏?```

---

## 鏍稿績鍔熻兘璇存槑

### 鏉冮檺浣撶郴 (wealth-system)

鍩轰簬 RBAC (Role-Based Access Control) 妯″瀷锛屽疄鐜扮粏绮掑害鐨勫悗鍙版潈闄愭帶鍒讹細

- **绠＄悊鍛樿〃** (`ums_admin`)锛氱郴缁熺鐞嗗憳璐﹀彿
- **瑙掕壊琛?* (`ums_role`)锛氳鑹插畾涔夛紝鏀寔鐘舵€佸惎鐢?绂佺敤
- **璧勬簮琛?* (`ums_resource`)锛歎RL 璧勬簮瀹氫箟锛屾敮鎸佸垎绫荤鐞?- **鍏崇郴琛?*锛歚ums_admin_role_relation`銆乣ums_role_resource_relation` 瀹炵幇澶氬澶氬叧鑱?
**鏉冮檺鎷︽埅娴佺▼**锛?1. 璇锋眰閫氳繃 Gateway 璺敱鍒板叿浣撴湇鍔?2. `LoginInterceptor` 鏍￠獙 JWT Token 鏈夋晥鎬э紙鏀捐鐧藉悕鍗?URL锛?3. `PermissionInterceptor` 鏍￠獙褰撳墠绠＄悊鍛樻槸鍚︽嫢鏈夌洰鏍囪祫婧愭潈闄?4. 鏃犺鑹叉垨璧勬簮鏉冮檺鏃剁洿鎺ヨ繑鍥?403

### 鐢ㄦ埛妯″潡 (wealth-user)

- 鐢ㄦ埛娉ㄥ唽锛圔Crypt 鍔犲瘑瀵嗙爜锛夈€佺櫥褰曪紙杩斿洖 JWT Token锛?- 鏀寔鐘舵€佺鐞嗭紙绂佺敤璐﹀彿绂佹鐧诲綍锛?- 瀵嗙爜閲嶇疆涓庝俊鎭洿鏂?- 浣跨敤 `JwtUtil`锛坈ommon 妯″潡缁熶竴宸ュ叿锛夌敓鎴?楠岃瘉 Token

### 浜у搧涓庤鎯?(wealth-product)

- 閲戣瀺浜у搧 CRUD锛堟寜鍚嶇О/缂栫爜/绫诲瀷鎼滅储锛?- 瀹炴椂琛屾儏鏁版嵁绠＄悊锛坄wea_market_data`锛?- 鏀寔鍒嗛〉鏌ヨ

### 鑷€夌鐞?(wealth-account)

- 鐢ㄦ埛鑷€変骇鍝佹坊鍔?鍒犻櫎
- 鑷€夊垪琛ㄦ煡璇紙鎸夌敤鎴?ID锛?
### 浜ゆ槗濮旀墭 (wealth-trade)

- 浜ゆ槗濮旀墭鍗曞彂璧凤紙涔板叆/鍗栧嚭锛?- 濮旀墭鍗曟挙閿€锛堢姸鎬佹牎楠岋級
- 濮旀墭鍗曞垎椤垫煡璇紙澶氭潯浠剁瓫閫夛級

### 娑堟伅涓庤祫璁?(wealth-message)

- 璐㈢粡璧勮绠＄悊锛坄wea_news`锛?- 绔欏唴娑堟伅鎺ㄩ€侊紙`wea_message`锛岄泦鎴?RabbitMQ锛?- RabbitMQ 闃熷垪鍜屼氦鎹㈡満閰嶇疆鍦?`RabbitMqConfig` 涓粺涓€绠＄悊

### 鎼滅储鏈嶅姟 (wealth-search)

- 鍩轰簬 Elasticsearch 鐨勪骇鍝佸叏鏂囨绱?- 浜у搧鏂囨。绱㈠紩绠＄悊锛堜繚瀛樸€佸垹闄ゃ€佹寜 ID 鏌ヨ銆佸叧閿瘝鎼滅储锛?
---

## 鐜鎼缓

### 鍓嶇疆鏉′欢

- JDK 21 ([涓嬭浇](https://jdk.java.net/21/))
- Maven 3.9.x ([涓嬭浇](https://maven.apache.org/download.cgi))
- Docker Desktop ([涓嬭浇](https://www.docker.com/products/docker-desktop/))
- IDE锛欼ntelliJ IDEA 2023+ 鎴?VS Code

### 鏁版嵁搴撳垵濮嬪寲

```sql
-- 鍒涘缓鏁版嵁搴?CREATE DATABASE IF NOT EXISTS Wealth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 鎵ц寤鸿〃鑴氭湰锛堥」鐩牴鐩綍涓嬶級
source docs/sql/init.sql;
```

> 鏁版嵁搴撳垵濮嬪寲鑴氭湰鍖呭惈鍏ㄩ儴涓氬姟琛ㄥ拰鏉冮檺鏁版嵁锛岄粯璁ょ鐞嗗憳璐﹀彿锛歚admin` / `admin`锛圔Crypt 鍔犲瘑锛夈€?
### 涓棿浠堕儴缃诧紙Docker Compose锛?
```yaml
# docker-compose.yml 鍙傝€冮厤缃?version: '3.8'
services:
  nacos:
    image: nacos/nacos-server:2.2.3
    ports:
      - "8848:8848"
    environment:
      MODE: standalone

  mysql:
    image: mysql:8.0.37
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: Wealth

  redis:
    image: redis:5.0.14
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3.10.20-management
    ports:
      - "5672:5672"
      - "15672:15672"

  elasticsearch:
    image: elasticsearch:8.11.0
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      discovery.type: single-node
```

### Nacos 閰嶇疆

鍚勬ā鍧楃殑 `bootstrap.yml` 榛樿杩炴帴 `localhost:8848`銆傚湪 Nacos 涓负姣忎釜妯″潡鍒涘缓瀵瑰簲鐨?Data ID 鍜岄厤缃唴瀹癸紙鏁版嵁搴撹繛鎺ャ€丷edis銆丷abbitMQ 绛夛級銆?
---

## 椤圭洰鍚姩姝ラ

### 1. 鍏嬮殕涓庣紪璇?
```bash
# 鍏嬮殕椤圭洰
git clone https://github.com/renjianfeng8/wealth-service-platform.git
cd wealth-service-platform

# 缂栬瘧鍏叡妯″潡锛堜慨鏀?common 鍚庡繀椤婚噸鏂?install锛?mvn clean install -pl wealth-common -DskipTests

# 缂栬瘧鍏ㄩ儴妯″潡
mvn clean install -DskipTests
```

### 2. 鍚姩涓棿浠?
纭繚浠ヤ笅鏈嶅姟宸插惎鍔ㄥ苟鍙繛鎺ワ細

| 鏈嶅姟 | 鍦板潃 | 楠岃瘉鏂瑰紡 |
|------|------|----------|
| Nacos | localhost:8848 | 璁块棶 http://localhost:8848/nacos |
| MySQL | localhost:3306 | `mysql -uroot -p123456 -e "SELECT 1"` |
| Redis | localhost:6379 | `redis-cli ping` |
| RabbitMQ | localhost:5672 | 璁块棶 http://localhost:15672 |
| Elasticsearch | localhost:9200 | `curl http://localhost:9200` |

### 3. 鎸夐『搴忓惎鍔ㄦ湇鍔?
寤鸿鍚姩椤哄簭锛堜粠鏃犱緷璧栧埌鏈変緷璧栵級锛?
```bash
# 1. 鍚姩 gateway锛堢綉鍏筹紝鏃犱笟鍔′緷璧栵級
mvn spring-boot:run -pl wealth-gateway

# 2. 鍚姩 system锛堝悗鍙版潈闄愶紝鐙珛涓氬姟锛?mvn spring-boot:run -pl wealth-system

# 3. 鍚姩鏃?Feign 璋冪敤鐨勪笟鍔℃ā鍧楋紙user銆乸roduct銆乼rade銆乵essage锛?mvn spring-boot:run -pl wealth-user
mvn spring-boot:run -pl wealth-product
mvn spring-boot:run -pl wealth-trade
mvn spring-boot:run -pl wealth-message

# 4. 鍚姩鏈?Feign 璋冪敤鐨勬ā鍧楋紙account 渚濊禆 product锛?mvn spring-boot:run -pl wealth-account

# 5. 鍚姩 search锛堜緷璧?ES锛?mvn spring-boot:run -pl wealth-search
```

> 鍚勬ā鍧楀惎鍔ㄦ椂渚濊禆 Nacos 閰嶇疆涓績銆傚鏋?Nacos 涓湭鍒涘缓瀵瑰簲閰嶇疆锛屾ā鍧楀皢浣跨敤鏈湴 `application.yml` 涓殑榛樿閰嶇疆鍚姩锛坒inance-user 宸插唴缃紝鍏朵粬妯″潡闇€纭繚 Nacos 鏈夊搴旈厤缃級銆?
### 4. 鍚姩鍓嶇

```bash
cd front
npm install    # 棣栨闇€瑕佸畨瑁呬緷璧?npm run dev    # 鍚姩寮€鍙戞湇鍔″櫒
```

鍓嶇寮€鍙戞湇鍔″櫒榛樿杩愯鍦?`http://localhost:3000`锛孷ite 宸查厤缃唬鐞嗗皢 `/api` 璇锋眰杞彂鑷崇綉鍏筹紙`http://localhost:8080`锛夈€?
> 鍓嶇榛樿鐧诲綍璐﹀彿锛歛dmin / admin

### 5. 楠岃瘉鍚姩

搴旂敤鍚姩鍚庯紝閫氳繃缃戝叧璁块棶鍚勬ā鍧楀仴搴锋鏌ユ帴鍙ｏ細

```
GET http://localhost:8080/actuator/health
```

---

## 鎺ュ彛鏂囨。

### Swagger / Knife4j 璁块棶鍦板潃

| 妯″潡 | 鏂囨。鍦板潃 |
|------|----------|
| 缃戝叧缁熶竴鍏ュ彛 | http://localhost:8080/doc.html |
| 绯荤粺鏈嶅姟 | http://localhost:8082/system/doc.html |
| 鐢ㄦ埛鏈嶅姟 | http://localhost:8083/user/doc.html |
| 浜у搧鏈嶅姟 | http://localhost:8084/product/doc.html |
| 璐︽埛鏈嶅姟 | http://localhost:8086/account/doc.html |
| 浜ゆ槗鏈嶅姟 | http://localhost:8085/trade/doc.html |
| 娑堟伅鏈嶅姟 | http://localhost:8087/message/doc.html |

### 甯哥敤鎺ュ彛绀轰緥

#### 绠＄悊鍛樼櫥褰?
```bash
POST /system/umsAdmin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 鍒嗛〉鏌ヨ绠＄悊鍛樺垪琛?
```bash
GET /system/umsAdmin/list?pageNum=1&pageSize=10
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 鐢ㄦ埛娉ㄥ唽

```bash
POST /user/user/register
Content-Type: application/json

{
  "username": "test_user",
  "password": "test123",
  "nickname": "娴嬭瘯鐢ㄦ埛",
  "phone": "13800138000"
}
```

### 缁熶竴杩斿洖鏍煎紡

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 鐘舵€佺爜 | 璇存槑 |
|--------|------|
| 200 | 鎴愬姛 |
| 400 | 鍙傛暟閿欒 |
| 401 | 鏈櫥褰?|
| 403 | 鏃犳潈闄?|
| 500 | 鏈嶅姟鍣ㄥ紓甯?|

---

## 椤圭洰瑙勮寖

### 鍛藉悕瑙勮寖

| 鍏冪礌 | 瑙勮寖 | 绀轰緥 |
|------|------|------|
| 绫诲悕 | 澶ч┘宄?| `UmsAdminController` |
| 鏂规硶鍚?| 灏忛┘宄?| `getAdminList()` |
| 鍙橀噺鍚?| 灏忛┘宄?| `adminList` |
| 甯搁噺 | 澶у啓+涓嬪垝绾?| `PERMIT_ALL_URLS` |
| 鏁版嵁搴撹〃 | 灏忓啓+涓嬪垝绾?| `ums_admin` |
| 鏁版嵁搴撳瓧娈?| 灏忓啓+涓嬪垝绾?| `create_time` |

### 鍒嗗眰瑙勮寖

- **Entity**锛氱户鎵?`BaseEntity`锛宍@TableName` 鎸囧畾琛ㄥ悕锛屽瓧娈典娇鐢?`@TableField` 鏄犲皠
- **Mapper**锛氱户鎵?`BaseMapper<T>`锛岀姝㈡墜鍐欏鏉?SQL
- **Service**锛氱户鎵?`IService<T>` / `ServiceImpl<T>`
- **Controller**锛氫笉鍐欎笟鍔￠€昏緫锛岃皟鐢?Service 鍚庤繑鍥?`Result.success(BeanConvertUtil.convert())`
- **VO**锛氳繑鍥炲墠绔殑瑙嗗浘瀵硅薄锛屼笌 Entity 鍒嗙
- **DTO**锛氭帴鏀跺墠绔弬鏁扮殑浼犺緭瀵硅薄锛屾敮鎸?`@Valid` 鏍￠獙

### Entity 缁ф壙瑙勫垯

```
BaseEntity                    # 鍩虹瀛楁锛歩d, create_time, update_time, del_flag
  鈹溾攢鈹€ User                    # 琛細sys_user锛堟湁 del_flag 鈫?@TableLogic锛?  鈹溾攢鈹€ WeaProduct              # 琛細wea_product锛堟湁 del_flag 鈫?@TableLogic锛?  鈹溾攢鈹€ WeaTradeOrder           # 琛細wea_trade_order锛堟湁 del_flag 鈫?@TableLogic锛?  鈹溾攢鈹€ WeaMarketData           # 琛細wea_market_data锛堟棤 update_time 鈫?exist=false锛?  鈹溾攢鈹€ WeaNews                 # 琛細wea_news锛堟棤 update_time 鈫?exist=false锛?  鈹溾攢鈹€ WeaMessage              # 琛細wea_message锛堟棤 update_time 鈫?exist=false锛?  鈹溾攢鈹€ WeaUserFavorite         # 琛細wea_user_favorite锛堟棤 update_time/del_flag 鈫?exist=false锛?  鈹溾攢鈹€ UmsAdmin                # 琛細ums_admin锛堟棤 update_time/del_flag 鈫?exist=false锛?  鈹溾攢鈹€ UmsRole                 # 琛細ums_role锛堟棤 update_time/del_flag 鈫?exist=false锛?  鈹溾攢鈹€ UmsResource             # 琛細ums_resource锛堟棤 update_time/del_flag 鈫?exist=false锛?  鈹溾攢鈹€ UmsAdminRoleRelation    # 鍏宠仈琛紙浠?id 鈫?鍏ㄩ儴 exist=false锛?  鈹斺攢鈹€ UmsRoleResourceRelation # 鍏宠仈琛紙浠?id 鈫?鍏ㄩ儴 exist=false锛?```

### 鏁版嵁搴撹鑼?
- 鏁版嵁搴撳悕锛歚Wealth`锛屽瓧绗﹂泦锛歚utf8mb4`
- 鎵€鏈夎〃鍖呭惈锛歚id`(BIGINT 鑷)銆乣create_time`(DATETIME)銆乣update_time`(DATETIME)銆乣del_flag`(TINYINT)
- 閫昏緫鍒犻櫎锛歚del_flag` = 0 鏈垹闄わ紝1 宸插垹闄?- 绂佹浣跨敤澶栭敭锛屽叧鑱斿湪涓氬姟灞傚鐞?- 绱㈠紩鎸夊缓琛ㄨ鍙ュ垱寤?
### 鏃ュ織瑙勮寖

浣跨敤 SLF4J + Lombok `@Slf4j` 娉ㄨВ锛岀姝娇鐢?`System.out.println`锛?
```java
@Slf4j
public class SomeService {
    public void doSomething() {
        log.info("涓氬姟鎿嶄綔锛歿}", param);
        log.error("寮傚父鍙戠敓", exception);
    }
}
```

### API 瑙勮寖

- 閬靛惊 RESTful 瑙勮寖锛圙ET 鏌ヨ銆丳OST 鍒涘缓銆丳UT 鏇存柊銆丏ELETE 鍒犻櫎锛?- 鎵€鏈夋帴鍙ｄ娇鐢?`Result<T>` 缁熶竴杩斿洖
- 璇锋眰鍙傛暟浣跨敤 `@Valid` 娉ㄨВ鍚敤鏍￠獙
- Controller 鏂规硶蹇呴』娣诲姞 Swagger `@Operation` 娉ㄨВ
- Feign 鎺ュ彛璺緞蹇呴』鍖呭惈鏈嶅姟绔?`context-path`

---

## 鍙樻洿璁板綍

### 2026-05-05 椤圭洰浣撴鍏ㄩ噺淇

鍩轰簬鍏ㄩ潰鐨勯」鐩仴搴锋鏌ワ紙璇﹁ `PROJECT_HEALTH_REPORT.md`锛夛紝瀹屾垚浜嗕互涓嬪叧閿慨澶嶏細

#### P0 闃诲鎬ч棶棰?
| 闂 | 淇鍐呭 |
|------|----------|
| 鎼滅储鏈嶅姟鏈娇鐢ㄧ粺涓€杩斿洖 | `ProductSearchController` 鎵€鏈夋柟娉曡繑鍥炲€兼敼涓?`Result<T>` |
| Feign 璇锋眰 404 | FeignClient 璺緞琛ヤ笂鏈嶅姟绔?context-path 鍓嶇紑 |
| AccountFeignClient URL 鍐茬獊 | 鐙珛璺緞璁捐锛屾秷闄ゆ槧灏勫啿绐?|

#### P1 鍔熻兘姝ｇ‘鎬?
| 闂 | 淇鍐呭 |
|------|----------|
| DTO 瀛楁涓嶄竴鑷?| `WeaUserFavoriteDTO` 缁熶竴涓?`productCode` 瀛楁 |
| JWT 閲嶅瀹炵幇 | `UserServiceImpl` 澶嶇敤 common 妯″潡 `JwtUtil` |
| 瀵嗙爜鏄庢枃瑕嗙洊 | `UserController.update` 澧炲姞瀵嗙爜闃茶鐩栦繚鎶?|
| 鏉冮檺鎷︽埅鍣ㄧ┖闆嗗悎寮傚父 | IN 鏌ヨ鍓嶆鏌ョ┖闆嗗悎锛屼负绌烘椂鐩存帴杩斿洖 403 |
| 缂哄皯 @TableName | `UmsRole` 琛ュ厖 `@TableName("ums_role")` |

#### P2 瀹夊叏涓庝唬鐮佽川閲?
| 闂 | 淇鍐呭 |
|------|----------|
| MD5 瀵嗙爜鍔犲瘑 | 鍏ㄩ潰鍗囩骇涓?BCrypt锛坄BCryptPasswordEncoder`锛?|
| 缂哄皯 @Valid 鏍￠獙 | 鎵€鏈?`@RequestBody` 鍙傛暟琛ュ厖 `@Valid` |
| BeanConvertUtil 搴熷純 API | `newInstance()` 鈫?`getDeclaredConstructor().newInstance()` |

#### 鍏抽敭鏋舵瀯鍙樻洿

| 鍙樻洿椤?| 璇存槑 |
|--------|------|
| **MyBatis-Plus 3.5.10 鈫?3.5.7** | 闄嶇骇鍘熷洜锛?.5.9+ 绉婚櫎浜?`PaginationInnerInterceptor` 绫伙紝3.5.7 鏄渶鍚庝竴涓寘鍚绫荤殑鐗堟湰 |
| **MyBatisPlusConfig 鍒嗛〉鎻掍欢** | `wealth-common` 涓柊寤洪厤缃被锛屽叏灞€娉ㄥ叆 `PaginationInnerInterceptor(DbType.MYSQL)` |
| **Entity 缁熶竴缁ф壙 BaseEntity** | 12 涓?Entity 鍏ㄩ儴缁ф壙 `BaseEntity`锛屼娇鐢?`@TableField(exist = false)` 澶勭悊缂哄皯瀵瑰簲鍒楃殑鎯呭喌锛屽悓鏃朵慨澶?ums_* 琛?`@TableLogic` 寮曠敤涓嶅瓨鍦ㄥ垪鐨勯瀛?bug |
| **System.out 鈫?SLF4J** | 鍏ㄥ眬鏇挎崲涓?`@Slf4j` + `log.info/warn/error` |
| **RabbitMqConfig 杩佺Щ** | 浠?`wealth-user` 绉昏嚦 `wealth-message` 妯″潡 |
| **gateway 鎺掗櫎娌荤悊** | 閫氶厤绗?`*:*` 鏀逛负绮剧‘鎺掗櫎 `spring-boot-starter-tomcat` + `spring-webmvc` |

---

## 鍚庣画寮€鍙戝缓璁?
### 娴嬭瘯瑕嗙洊

椤圭洰褰撳墠 `pom.xml` 涓?`maven-surefire-plugin` 閰嶇疆浜?`<skipTests>true</skipTests>`锛屽缓璁細

- 涓哄悇妯″潡 Service 灞傜紪鍐欏崟鍏冩祴璇曪紙JUnit 5 + Mockito锛?- 涓?Controller 灞傜紪鍐欓泦鎴愭祴璇曪紙`@SpringBootTest` + `@AutoConfigureMockMvc`锛?- 涓?Feign 鎺ュ彛缂栧啓濂戠害娴嬭瘯
- 鍦?CI 娴佺▼涓紑鍚祴璇曪細`mvn test -DskipTests=false`

### 閰嶇疆绠＄悊

- 鍚勬ā鍧?`application.yml` 涓殑鏁版嵁搴撳瘑鐮併€丷edis 瀵嗙爜绛夋晱鎰熶俊鎭簲閫氳繃 Nacos 閰嶇疆涓績绠＄悊
- 寤鸿鏈湴寮€鍙戞椂浣跨敤 `application-local.yml` 鎴?Spring Profile 瀹炵幇鐜闅旂
- `wealth-search` 涓殑 ES 鍦板潃 `10.128.82.54:9200` 搴旂Щ鍒?Nacos 閰嶇疆涓紝閬垮厤纭紪鐮?
### 缃戝叧澧炲己

- 娣诲姞缃戝叧绾?JWT 閴存潈杩囨护鍣紝鍦ㄧ綉鍏冲眰缁熶竴楠岃瘉 Token
- 閰嶇疆缃戝叧绾ч檺娴侊紙RequestRateLimiter锛?- 琛ュ厖鍏ㄥ眬 CORS 閰嶇疆锛堝凡棰勯厤缃紝鍙寜闇€璋冩暣锛?
### 鏈嶅姟娌荤悊

- 闆嗘垚 Sentinel 瀹炵幇鏈嶅姟鐔旀柇闄嶇骇鍜屾祦閲忔帶鍒?- 涓?RabbitMQ 娣诲姞鐢熶骇纭鍜屾秷璐归噸璇曟満鍒?- 娣诲姞鍒嗗竷寮忎簨鍔℃敮鎸侊紙Seata锛?- 琛ュ厖 Feign 璋冪敤瓒呮椂鍜岄噸璇曢厤缃?
### 鐩戞帶涓庤繍缁?
- 闆嗘垚 Spring Boot Actuator锛堝凡棰勯厤缃紝鎸夐渶鍚敤璇︾粏绔偣锛?- 娣诲姞 Prometheus + Grafana 鐩戞帶
- 闆嗘垚 SkyWalking 鎴?Arthas 瀹炵幇鍒嗗竷寮忛摼璺拷韪?- 琛ュ厖 Dockerfile 鍜?docker-compose 閮ㄧ讲閰嶇疆

### 浠ｇ爜璐ㄩ噺

- 琛ュ厖鍏ㄥ眬鍙傛暟鏍￠獙妗嗘灦锛坄@Valid` 宸查泦鎴愶紝鍙墿灞曟洿澶氭牎楠岃鍒欙級
- 寮曞叆 MapStruct 鏇夸唬 BeanUtils 鎻愬崌 VO/Entity 杞崲鎬ц兘锛堝彲閫夛級
- 缁熶竴閿欒鐮佹灇涓撅紝涓板瘜閿欒淇℃伅鍥介檯鍖?
---

> 瀹屾暣浣撴鎶ュ憡瑙?[PROJECT_HEALTH_REPORT.md](./PROJECT_HEALTH_REPORT.md)
> 寮€鍙戣鑼冭鎯呰 [CLAUDE.md](./CLAUDE.md)
