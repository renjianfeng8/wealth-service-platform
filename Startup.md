# 閲戣瀺涓彴椤圭洰 鈥?鍚姩鎸囧崡

## 椤圭洰绠€浠?
閲戣瀺涓彴锛團inance Mid Platform锛夋槸涓€涓潰鍚戦噾铻嶄笟鍔＄殑鍙岀鏋舵瀯绯荤粺锛屽寘鍚細

- **绠＄悊鍛樺悗鍙?*锛坒ront/锛夛細鍚庡彴绠＄悊绯荤粺锛岄潰鍚戣繍钀ヤ汉鍛橈紝绠＄悊鐢ㄦ埛銆佷骇鍝併€佹潈闄愮瓑
- **鐢ㄦ埛鍓嶅彴**锛坒ront-user/锛夛細鐢ㄦ埛绔棬鎴凤紝闈㈠悜鏅€氱敤鎴凤紝鎻愪緵琛屾儏鏌ョ湅銆佷氦鏄撳鎵樸€佽嚜閫夌鐞嗙瓑

鍚庣閲囩敤 Spring Cloud Alibaba 寰湇鍔℃灦鏋勶紝缁熶竴閫氳繃 Nacos 娉ㄥ唽涓績 + Spring Cloud Gateway 缃戝叧瀵瑰鎻愪緵鏈嶅姟銆?
---

## 涓€銆佹妧鏈爤

| 灞傞潰 | 鎶€鏈?| 鐗堟湰 |
|------|------|------|
| 鍚庣妗嗘灦 | Spring Boot / Spring Cloud / Alibaba | 3.3.5 / 2023.0.3 / 2023.0.1.2 |
| ORM | MyBatis-Plus | 3.5.7 |
| 娉ㄥ唽涓績/閰嶇疆涓績 | Nacos | 2.3.0 |
| 缃戝叧 | Spring Cloud Gateway | 4.1.5 |
| 鏁版嵁搴?| MySQL | 8.0.37 |
| 缂撳瓨 | Redis | 5.0.14.1 |
| 娑堟伅闃熷垪 | RabbitMQ | 3.10.20 |
| 鎼滅储寮曟搸 | Elasticsearch | 8.11.0 |
| 鍓嶇锛堝弻绔級 | Vue 3 + Vite + Element Plus + Pinia + TypeScript | 3.5.13 / 6.3.1 / 2.9.7 / 2.3.1 / 5.7 |
| E2E 娴嬭瘯 | Playwright | 1.59+ |

---

## 浜屻€佸墠缃幆澧冨噯澶?
| 缁勪欢 | 鐗堟湰瑕佹眰 | 妫€鏌ュ懡浠?|
|------|---------|---------|
| JDK | 21.0.3+ | `java -version` |
| Maven | 3.9.9+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| Docker | 24.0+ | `docker --version` |
| MySQL 瀹㈡埛绔?| 8.0+ | `mysql --version` |

---

## 涓夈€佷腑闂翠欢鍚姩锛圖ocker锛?
### 3.1 鍚姩瀹瑰櫒

```bash
# Nacos锛堟敞鍐屼腑蹇?+ 閰嶇疆涓績锛?docker run -d --name nacos -p 8848:8848 -p 9848:9848 -e MODE=standalone nacos/nacos-server:v2.3.0

# MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0.37

# Redis
docker run -d --name redis -p 6379:6379 redis:5.0.14.1

# RabbitMQ锛堝惈绠＄悊鎺у埗鍙帮級
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.10.20-management

# Elasticsearch
docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.11.0
```

楠岃瘉杩愯鐘舵€侊細

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### 3.2 Nacos 閰嶇疆涓績璁剧疆

璁块棶 Nacos 鎺у埗鍙?[http://localhost:8848/nacos](http://localhost:8848/nacos)锛堥粯璁よ处鍙凤細nacos / nacos锛夛紝鍒涘缓鍏变韩閰嶇疆锛?
- **Data ID**锛歚wealth-shared.yaml`
- **閰嶇疆鏍煎紡**锛歒AML
- **鍐呭**锛?
```yaml
jwt:
  secret: wealth-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000
```

> JWT 瀵嗛挜蹇呴』 鈮?32 瀛楄妭锛堝綋鍓嶅瘑閽?58 瀛楄妭锛夛紝鍚﹀垯鏈嶅姟鍚姩鏃朵細鐩存帴鎶ラ敊銆?
---

## 鍥涖€佹暟鎹簱鍒濆鍖?
```bash
# 鍒涘缓鏁版嵁搴?mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS Wealth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 瀵煎叆寤鸿〃璇彞 + 娴嬭瘯鏁版嵁
mysql -u root -p123456 Wealth < wealth-common/src/main/resources/sql/init.sql
```

鏁版嵁搴?`Wealth` 鍖呭惈 12 寮犺〃锛岃鐩栧叏涓氬姟鍦烘櫙銆?
---

## 浜斻€佺紪璇戦」鐩?
**缂栬瘧椤哄簭**锛氬繀椤诲厛缂栬瘧 `wealth-common`锛屽啀缂栬瘧鍏朵粬妯″潡銆?
```bash
# 1. 缂栬瘧鍏叡妯″潡骞跺畨瑁呭埌鏈湴浠撳簱锛堜慨鏀?common 鍚庡繀椤婚噸鏂版墽琛屾姝ワ級
mvn clean install -pl wealth-common -DskipTests

# 2. 鍏ㄩ噺缂栬瘧
mvn clean compile

# 3. 鎵撳寘鎵€鏈夋ā鍧?mvn clean package -DskipTests

# 4. 锛堝彲閫夛級瀹夎鎵€鏈夋ā鍧?mvn clean install -DskipTests
```

鍚勬ā鍧?JAR 鍖呰矾寰勶細`{妯″潡鍚峿/target/{妯″潡鍚峿-1.0.0.jar`

---

## 鍏€佸悗绔湇鍔″惎鍔紙鎸夐『搴忥級

鏁版嵁搴撳瘑鐮佺粺涓€閫氳繃鐜鍙橀噺浼犲叆锛圵indows 浣跨敤 `set`锛孡inux/Mac 浣跨敤 `export`锛夛細

```bash
# Windows PowerShell
$env:DB_PASSWORD="123456"

# Windows CMD
set DB_PASSWORD=123456

# Linux / Mac
export DB_PASSWORD=123456
```

### 6.1 缃戝叧锛堟渶鍏堝惎鍔紝渚濊禆 Nacos锛?
```bash
java -jar wealth-gateway/target/wealth-gateway-1.0.0.jar > gateway.log 2>&1 &
```

- 绔彛锛?*8080**
- Nacos 鏈嶅姟鍚嶏細wealth-gateway
- 绫诲瀷锛歋pring Cloud Gateway锛圵ebFlux锛?
### 6.2 绯荤粺鏈嶅姟锛堟彁渚涚櫥褰曢壌鏉冨拰 RBAC 鏉冮檺锛?
```bash
DB_PASSWORD=123456 java -jar wealth-system/target/wealth-system-1.0.0.jar > system.log 2>&1 &
```

- 绔彛锛?*8082**锛宑ontext-path锛歚/system`
- Nacos 鏈嶅姟鍚嶏細wealth-system
- 鍔熻兘锛氱鐞嗗憳 CRUD銆佽鑹茬鐞嗐€佽祫婧愮鐞嗐€丣WT 鐧诲綍銆佹潈闄愭嫤鎴?
### 6.3 涓氬姟鏈嶅姟锛堟棤鍏堝悗渚濊禆锛屽彲骞惰鍚姩锛?
```bash
# 鐢ㄦ埛鏈嶅姟锛堝墠绔敤鎴风鐞嗭級
DB_PASSWORD=123456 java -jar wealth-user/target/wealth-user-1.0.0.jar > user.log 2>&1 &

# 浜у搧鏈嶅姟锛堜骇鍝?+ 琛屾儏锛?DB_PASSWORD=123456 java -jar wealth-product/target/wealth-product-1.0.0.jar > product.log 2>&1 &

# 璐︽埛鏈嶅姟锛堢敤鎴疯嚜閫夛級
DB_PASSWORD=123456 java -jar wealth-account/target/wealth-account-1.0.0.jar > account.log 2>&1 &

# 浜ゆ槗鏈嶅姟锛堝鎵樹氦鏄擄級
DB_PASSWORD=123456 java -jar wealth-trade/target/wealth-trade-1.0.0.jar > trade.log 2>&1 &

# 娑堟伅鏈嶅姟锛堣祫璁?+ 绔欏唴娑堟伅锛?DB_PASSWORD=123456 java -jar wealth-message/target/wealth-message-1.0.0.jar > message.log 2>&1 &

# 鎼滅储鏈嶅姟锛圗S 浜у搧鎼滅储锛屾棤鏁版嵁搴撲緷璧栵級
DB_PASSWORD=123456 java -jar wealth-search/target/wealth-search-1.0.0.jar > search.log 2>&1 &
```

### 6.4 楠岃瘉鍚庣鏈嶅姟

```bash
# 妫€鏌ョ鍙ｇ洃鍚?netstat -ano | findstr ':8080 :8082 :8083 :8084 :8085 :8086 :8087 :8089'

# 妫€鏌ュ惎鍔ㄦ棩蹇?grep "Started" gateway.log system.log user.log product.log account.log trade.log message.log search.log
```

棰勬湡杈撳嚭 8 琛?`Started xxxApplication in ...`锛堟瘡涓湇鍔′竴琛岋級銆?
---

## 涓冦€佺鐞嗗憳鍚庡彴鍚姩锛坒ront/锛?
```bash
cd front

# 瀹夎渚濊禆锛堥娆℃垨渚濊禆鍙樻洿鏃舵墽琛岋級
npm install

# 鍚姩寮€鍙戞湇鍔″櫒
npm run dev
```

- 绔彛锛?*3000**
- Vite 浠ｇ悊锛歚/api` 鈫?`http://localhost:8080`锛堢綉鍏筹級
- 鐧诲綍璐﹀彿锛歚admin` / `admin123`锛坲ms_admin 琛級

楠岃瘉锛?
```bash
curl -s http://localhost:3000 | head -5
# 搴斿寘鍚?<div id="app"></div>
```

---

## 鍏€佺敤鎴峰墠鍙板惎鍔紙front-user/锛?
```bash
cd front-user

# 瀹夎渚濊禆锛堥娆℃垨渚濊禆鍙樻洿鏃舵墽琛岋級
npm install

# 鍚姩寮€鍙戞湇鍔″櫒
npm run dev
```

- 绔彛锛?*3001**
- Vite 浠ｇ悊锛歚/api` 鈫?`http://localhost:8080`锛堢綉鍏筹級
- 鐧诲綍璐﹀彿锛歚zhangwei` / `123456`锛坰ys_user 琛級

楠岃瘉锛?
```bash
curl -s http://localhost:3001 | head -5
# 搴斿寘鍚?<div id="app"></div>
```

---

## 涔濄€佸叏閾捐矾楠岃瘉

### 9.1 鐧诲綍鎺ュ彛锛堢鐞嗗憳锛?
```bash
curl -s --noproxy "*" -X POST "http://localhost:8080/system/umsAdmin/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

棰勬湡杩斿洖 `200` + JWT Token銆?
### 9.2 鐧诲綍鎺ュ彛锛堢敤鎴峰墠鍙帮級

```bash
curl -s --noproxy "*" -X POST "http://localhost:8080/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangwei","password":"123456"}'
```

棰勬湡杩斿洖 `200` + JWT Token銆?
### 9.3 涓氬姟鎺ュ彛娴嬭瘯锛堟惡甯?Token锛?
```bash
# 璁剧疆 Token锛堟浛鎹负瀹為檯杩斿洖鐨?token锛?TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# 绠＄悊鍛樺垎椤?curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/system/umsAdmin/page?pageNum=1&pageSize=10"

# 鐢ㄦ埛鍒嗛〉
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/user/page?pageNum=1&pageSize=10"

# 浜у搧鍒嗛〉
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/WeaProduct/page?pageNum=1&pageSize=10"

# 琛屾儏鏁版嵁
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/WeaMarketData/list?productCode=GOLD001"

# 浜ゆ槗璁㈠崟鍒嗛〉
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/trade/WeaTradeOrder/page?pageNum=1&pageSize=10"

# 鏂伴椈鍒嗛〉
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/message/WeaNews/page?pageNum=1&pageSize=10"

# 鐢ㄦ埛鑷€夊垪琛?curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/account/WeaUserFavorite/list?userId=1"

# 鎼滅储锛堥渶 ES 杩愯锛?curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/search/product/search?keyword=榛勯噾&page=1&size=10"
```

### 9.4 娴忚鍣ㄨ闂?
| 搴旂敤 | URL |
|------|-----|
| 绠＄悊鍛樺悗鍙?| [http://localhost:3000](http://localhost:3000) |
| 鐢ㄦ埛鍓嶅彴 | [http://localhost:3001](http://localhost:3001) |

---

## 鍗併€丒2E 鑷姩鍖栨祴璇曪紙Playwright锛?
### 10.1 鐢ㄦ埛鍓嶅彴 E2E 娴嬭瘯锛堟柊澧?Playwright 濂椾欢锛?
```bash
cd front-user

# 棣栨杩愯瀹夎 Playwright 娴忚鍣?npx playwright install chromium

# 杩愯鍏ㄩ儴 37 涓祴璇曠敤渚?npm run test:e2e

# 鏌ョ湅娴嬭瘯鎶ュ憡
npm run test:e2e:report

# 璋冭瘯妯″紡锛堝甫 UI锛?npm run test:e2e:ui
```

娴嬭瘯瑕嗙洊锛?1 涓ā鍧楀叡 37 涓敤渚嬶級锛氱櫥褰曪紙鍚敊璇瘑鐮侀獙璇侊級銆佷华琛ㄧ洏銆佷骇鍝佷腑蹇冦€佸疄鏃惰鎯呫€佹垜鐨勮嚜閫夈€佷氦鏄撳鎵樸€佽储缁忚祫璁€佹秷鎭腑蹇冦€佷釜浜轰腑蹇冦€侀€€鍑虹櫥褰曘€佸鑸彍鍗曘€?
娴嬭瘯璐﹀彿锛歚zhangwei` / `123456`锛坰ys_user 琛級

### 10.2 鍏ㄩ摼璺?E2E 娴嬭瘯锛堟棫鐗堣剼鏈級

```bash
# 纭繚鍦ㄩ」鐩牴鐩綍
cd wealth-service-platform

# 瀹夎渚濊禆锛堥娆★級
npm install

# 杩愯鍏ㄩ摼璺祴璇?node e2e-test.mjs
```

娴嬭瘯瑕嗙洊 35 椤癸紝鍖呮嫭锛氬熀纭€璁炬柦妫€鏌ワ紙Nacos/缃戝叧/鍓嶇锛夈€丄PI 鐧诲綍銆侀〉闈㈠姞杞姐€佽彍鍗曞鑸紙12 椤甸潰锛夈€丄PI 璇锋眰锛?0 鎺ュ彛锛夈€佸墠绔唬鐞嗐€丣S 閿欒妫€鏌ャ€傛姤鍛婅緭鍑哄埌 `e2e-test-report.md`銆?
娴嬭瘯璐﹀彿锛歚admin` / `admin123`锛坲ms_admin 琛級

---

## 鍗佷竴銆佺鍙ｅ鐓ц〃

| 妯″潡 | 绔彛 | context-path | Nacos 鏈嶅姟鍚?| 璇存槑 |
|------|:----:|:-----------:|-------------|------|
| **涓棿浠?* | | | | |
| Nacos | 8848 | - | - | 娉ㄥ唽涓績/閰嶇疆涓績 |
| MySQL | 3306 | - | - | 鏁版嵁搴?|
| Redis | 6379 | - | - | 缂撳瓨 |
| RabbitMQ | 5672 / 15672 | - | - | 娑堟伅闃熷垪 / 绠＄悊鎺у埗鍙?|
| Elasticsearch | 9200 / 9300 | - | - | 鎼滅储寮曟搸 |
| **鍚庣鏈嶅姟** | | | | |
| wealth-gateway | **8080** | - | wealth-gateway | 缃戝叧锛堢粺涓€鍏ュ彛锛?|
| wealth-system | **8082** | /system | wealth-system | 鍚庡彴鏉冮檺绠＄悊 |
| wealth-user | **8083** | /user | wealth-user | 鍓嶇鐢ㄦ埛绠＄悊 |
| wealth-product | **8084** | /product | wealth-product | 浜у搧 + 琛屾儏 |
| wealth-trade | **8085** | /trade | wealth-trade | 浜ゆ槗濮旀墭 |
| wealth-account | **8086** | /account | wealth-account | 鐢ㄦ埛鑷€?|
| wealth-message | **8087** | /message | wealth-message | 璧勮 + 娑堟伅 |
| wealth-search | **8089** | - | wealth-search | ES 鎼滅储 |
| **鍓嶇** | | | | |
| 绠＄悊鍛樺悗鍙?| **3000** | - | - | front/锛圴ite 寮€鍙戞湇鍔″櫒锛?|
| 鐢ㄦ埛鍓嶅彴 | **3001** | - | - | front-user/锛圴ite 寮€鍙戞湇鍔″櫒锛?|

---

## 鍗佷簩銆佹祴璇曡处鍙疯鏄?
| 韬唤 | 鐢ㄦ埛鍚?| 瀵嗙爜 | 鎵€灞炶〃 | 鐧诲綍绔?| 璇存槑 |
|------|--------|------|--------|--------|------|
| 绠＄悊鍛?| `admin` | `admin123` | ums_admin | 绠＄悊鍛樺悗鍙?(port 3000) | 鎷ユ湁鍚庡彴鍏ㄩ儴鏉冮檺 |
| 鍓嶅彴鐢ㄦ埛 | `zhangwei` | `123456` | sys_user | 鐢ㄦ埛鍓嶅彴 (port 3001) | E2E 娴嬭瘯榛樿鐢ㄦ埛 |

---

## 鍗佷笁銆佸父瑙侀棶棰樻帓鏌?
### 绔彛鍗犵敤

```bash
# 鏌ョ湅鍗犵敤绔彛鐨勮繘绋?netstat -ano | findstr ":8080"

# 寮哄埗缁堟杩涚▼锛圵indows锛?taskkill /PID <PID> /F
```

### 鍚庣鍚姩澶辫触

1. **Nacos 杩炴帴澶辫触** 鈥?妫€鏌?Nacos 瀹瑰櫒鏄惁杩愯锛歚docker ps | findstr nacos`
2. **JWT 閰嶇疆缂哄け** 鈥?纭 `wealth-shared.yaml` 宸插彂甯冨埌 Nacos
3. **鏁版嵁搴撹繛鎺ュけ璐?* 鈥?纭 `DB_PASSWORD` 鐜鍙橀噺宸茶缃笖瀵嗙爜姝ｇ‘
4. **鏁版嵁搴撹〃涓嶅瓨鍦?* 鈥?纭宸叉墽琛?`init.sql`

### 鍓嶇鍚姩澶辫触

1. **渚濊禆瀹夎澶辫触** 鈥?鍒犻櫎 `node_modules` 閲嶆柊瀹夎锛?   ```bash
   rm -rf node_modules && npm install
   ```
2. **绔彛琚崰鐢?* 鈥?淇敼 `vite.config.ts` 涓殑 `server.port`
3. **浠ｇ悊 502** 鈥?纭缃戝叧宸插惎鍔ㄥ苟鍙闂?`http://localhost:8080`

### 璺ㄥ煙闂

缃戝叧宸插湪 `wealth-gateway` 涓叏灞€閰嶇疆 CORS锛屽厑璁?`localhost:3000`銆乣localhost:3001` 鍜?`localhost:8080`銆傚鏋滈亣鍒拌法鍩熼敊璇紝妫€鏌ョ綉鍏虫槸鍚︽甯歌繍琛屻€?
### E2E 娴嬭瘯澶辫触

1. **娴忚鍣ㄦ湭瀹夎** 鈥?鎵ц `npx playwright install chromium`
2. **鍚庣鏈惎鍔?* 鈥?纭繚鎵€鏈?8 涓悗绔湇鍔″凡鍦ㄨ繍琛?3. **娴嬭瘯鐢ㄦ埛涓嶅瓨鍦?* 鈥?妫€鏌?`sys_user` 琛ㄤ腑鏄惁鏈?`zhangwei` 涓斿瘑鐮佷负 BCrypt 鍔犲瘑鐨?`123456`

---

## 鍗佸洓銆佸惎鍔ㄩ『搴忎緷璧栧浘

```
Docker 瀹瑰櫒锛圢acos / MySQL / Redis / RabbitMQ / ES锛?        鈹?        鈻?wealth-common锛圡aven 渚濊禆锛屽繀椤诲厛 mvn install锛?        鈹?        鈻?wealth-gateway锛堟渶鍏堝惎鍔紝渚濊禆 Nacos锛?        鈹?        鈻?wealth-system锛堢浜屽惎鍔紝鎻愪緵鐧诲綍閴存潈 + 鏉冮檺鎷︽埅锛?        鈹?        鈻?wealth-user 鈹?wealth-product 鈹?wealth-account
wealth-trade 鈹?wealth-message 鈹?wealth-search
锛堟棤鍏堝悗渚濊禆锛屽彲骞惰鍚姩锛?        鈹?        鈹溾攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€ 鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?        鈻?                            鈻?  绠＄悊鍛樺悗鍙?front/             鐢ㄦ埛鍓嶅彴 front-user/
  (port 3000)                   (port 3001)
  npm run dev                   npm run dev
```

> **娉ㄦ剰**锛氭瘡娆′慨鏀?`wealth-common` 涓殑浠ｇ爜鍚庯紝蹇呴』閲嶆柊鎵ц `mvn clean install -pl wealth-common -DskipTests`锛屽啀閲嶆柊鎵撳寘渚濊禆瀹冪殑涓氬姟妯″潡銆?