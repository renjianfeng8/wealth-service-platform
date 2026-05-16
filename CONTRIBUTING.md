# Contributing to wealth-service-platform

鎰熻阿浣犲弬涓?wealth-service-platform 椤圭洰鐨勮础鐚€傛湰鏂囨。涓洪」鐩础鐚€呮彁渚涚粺涓€鐨勫紑鍙戣鑼冧笌鍗忎綔娴佺▼銆?
## 椤圭洰绠€浠?
wealth-service-platform 鏄竴涓熀浜?Spring Cloud Alibaba 寰湇鍔℃灦鏋勭殑閲戣瀺涓彴椤圭洰锛屾彁渚涗骇鍝佺鐞嗐€佽鎯呮暟鎹€佷氦鏄撳鎵樸€佺敤鎴疯嚜閫夈€佽储缁忚祫璁瓑鏍稿績閲戣瀺鏈嶅姟鑳藉姏銆?
### 鎶€鏈爤

- **鍚庣**: SpringBoot 3.3.5 + SpringCloud 2023.0.3 + Spring Cloud Alibaba 2023.0.1.2 + MyBatis-Plus 3.5.7
- **鍓嶇**: Vue 3.5.13 + Vite 6.3.1 + Element Plus 2.9.7 + TypeScript 5.7
- **涓棿浠?*: MySQL 8 + Redis 5 + RabbitMQ 3.10 + ElasticSearch 8.8.2 + Nacos 2.3.2
- **缃戝叧**: Spring Cloud Gateway锛堢鍙?8080锛?
瀹屾暣妯″潡鏋舵瀯鍙傝 [docs/architecture.md](docs/architecture.md)銆?
## 寮€鍙戠幆澧冨噯澶?
| 宸ュ叿 | 鐗堟湰瑕佹眰 | 璇存槑 |
|------|---------|------|
| JDK | 21.0.3 | 蹇呴』浣跨敤 JDK 21 |
| Maven | 3.9.9 | 椤圭洰鏋勫缓宸ュ叿 |
| Node.js | 18+锛堟帹鑽?20 LTS锛?| 鍓嶇寮€鍙戠幆澧?|
| Docker | latest | 杩愯涓棿浠跺鍣?|
| MySQL | 8.0.37 | 鏈湴瀹夎锛屾暟鎹簱 `Wealth`锛坲tf8mb4锛?|
| Git | latest | 鐗堟湰绠＄悊 |

### Docker 涓棿浠?
浠ヤ笅鏈嶅姟閫氳繃 Docker 杩愯锛岃纭繚鏈湴 Docker 鐜姝ｅ父锛?
| 鏈嶅姟 | 闀滃儚 | 绔彛 |
|------|------|------|
| Nacos | nacos/nacos-server:v2.3.2 | 8848 |
| Redis | redis:latest | 6379 |
| RabbitMQ | rabbitmq:3.10-management | 5672, 15672 |
| ElasticSearch | elasticsearch:8.8.2 | 9200 |
| Nginx | nginx:latest | 80 |

> 瀹屾暣瀹瑰櫒鍒楄〃璇﹁ [docs/architecture.md](docs/architecture.md#鍩虹璁炬柦-docker-瀹瑰櫒)銆?
## 鏈湴寮€鍙戞祦绋?
### 1. 鎷夊彇浠ｇ爜

```bash
git clone https://github.com/renjianfeng8/wealth-service-platform.git
cd wealth-service-platform
```

### 2. 鍚姩鍩虹璁炬柦

```bash
docker start nacos redis rabbitmq es nginx
```

### 3. 鍒濆鍖栨暟鎹簱

鍦?MySQL 涓垱寤?`Wealth` 搴擄紙瀛楃闆?`utf8mb4`锛夛紝鐒跺悗鎵ц寤鸿〃鑴氭湰锛?
```bash
mysql -u root -p Wealth < wealth-common/src/main/resources/sql/init.sql
```

### 4. 閰嶇疆 Nacos

璁块棶 Nacos 鎺у埗鍙?`http://localhost:8848`锛屽湪 DEFAULT_GROUP 涓嬪垱寤哄叡浜厤缃?`wealth-shared.yaml`锛屽唴瀹瑰寘鍚?JWT 瀵嗛挜鍜屾暟鎹簮閰嶇疆銆傝缁嗛厤缃唴瀹瑰弬瑙?[docs/architecture.md](docs/architecture.md#nacos-閰嶇疆涓績docker-nacosnacos-serverv232)銆?
### 5. 缂栬瘧椤圭洰

棣栨缂栬瘧锛堟垨淇敼浜?wealth-common 鍚庯級闇€瑕佸厛瀹夎鍏叡妯″潡锛?
```bash
# 缂栬瘧鍏叡妯″潡
mvn clean install -pl wealth-common -DskipTests

# 鍏ㄩ噺缂栬瘧
mvn clean install -DskipTests
```

### 6. 鎸夐『搴忓惎鍔ㄥ井鏈嶅姟

鏈嶅姟闂村瓨鍦ㄤ緷璧栧叧绯伙紝璇蜂弗鏍兼寜鐓т互涓嬮『搴忓惎鍔細

```
gateway(8080) 鈫?system(8082) 鈫?user(8083) 鈫?product(8084)
鈫?account(8086) 鈫?trade(8085) 鈫?message(8087) 鈫?search(8089)
```

```bash
# 鍚姩鍗曚釜妯″潡
mvn spring-boot:run -pl wealth-{妯″潡鍚峿

# 绀轰緥锛氬惎鍔ㄧ綉鍏?mvn spring-boot:run -pl wealth-gateway
```

### 7. 鍚姩鍓嶇

```bash
cd front-user
npm install
npx vite
```

鍓嶇榛樿杩愯鍦?`http://localhost:3000`锛岄€氳繃缃戝叧 `http://localhost:8080` 璋冪敤鍚庣鎺ュ彛銆?
## 浠ｇ爜瑙勮寖

### Java 鍚庣

- 鍖呯粨鏋勶細`com.wealth.platform.{妯″潡鍚峿`锛屾寜 controller/service/mapper/entity/vo/dto 鍒嗗眰
- 鎵€鏈?Entity 蹇呴』缁ф壙 `BaseEntity`锛岃嚜鍔ㄥ～鍏?`create_time`/`update_time`
- 鎺ュ彛缁熶竴杩斿洖 `Result<T>` 鏍煎紡锛坈ode + message + data锛?- 浣跨敤 `BeanConvertUtil` 杩涜 Entity 鈫?VO 杞崲
- 鏇存柊鎿嶄綔浣跨敤 `copyNonNullProperties` 閬垮厤 null 瑕嗙洊
- 涓氬姟寮傚父浣跨敤 `ServiceException(code, message)` 鑰岄潪 RuntimeException
- 鍐欐搷浣滃繀椤诲姞 `@Transactional(rollbackFor = Exception.class)`
- 鎵€鏈?`@RequestBody` DTO 蹇呴』鍔?`@Valid`
- 鍒嗛〉鏌ヨ浣跨敤 MyBatis-Plus `Page` + `PaginationInnerInterceptor`

璇︾粏瑙勮寖鍙傝 [CLAUDE.md](CLAUDE.md)銆?
### 鍓嶇

- Vue 3 缁勫悎寮?API + TypeScript
- 缁勪欢搴撲娇鐢?Element Plus
- 鐘舵€佺鐞嗕娇鐢?Pinia
- 璺敱浣跨敤 Vue Router 4

### 鏁版嵁搴?
- 鎵€鏈夎〃蹇呴』鍖呭惈 `id`銆乣create_time`銆乣update_time`銆乣del_flag`
- 閫昏緫鍒犻櫎锛歚del_flag` 0=鏈垹闄?1=宸插垹闄?- 涓婚敭缁熶竴浣跨敤 BIGINT 鑷
- 绂佹浣跨敤澶栭敭

琛ㄧ粨鏋勭粏鑺傚弬瑙?[docs/database-schema.md](docs/database-schema.md)銆?
## 鎻愪氦瑙勮寖

鎵€鏈?git 鎻愪氦蹇呴』閬靛惊 [Conventional Commits](https://www.conventionalcommits.org/) 瑙勮寖锛?
```
<type>(<scope>): <description>
```

### type 绫诲瀷

| type | 璇存槑 |
|------|------|
| feat | 鏂板姛鑳?|
| fix | 淇 bug |
| docs | 鏂囨。鍙樻洿 |
| style | 浠ｇ爜鏍煎紡璋冩暣 |
| refactor | 浠ｇ爜閲嶆瀯 |
| perf | 鎬ц兘浼樺寲 |
| test | 娣诲姞鎴栦慨鏀规祴璇?|
| chore | 鏋勫缓/宸ュ叿鍙樺姩 |
| ci | CI 閰嶇疆鍙樻洿 |
| build | 渚濊禆/鏋勫缓绯荤粺鍙樻洿 |

- **scope**锛堝彲閫夛級: common / gateway / system / user / product / account / trade / message / search
- **description**: 鍛戒护寮忚姘旓紝棣栧瓧姣嶅皬鍐欙紝鏈熬涓嶅姞鍙ュ彿

绀轰緥锛?
```
feat(product): 娣诲姞浜у搧鍒嗛〉鏌ヨ鎺ュ彛
fix(trade): 淇浜ゆ槗濮旀墭閲戦璁＄畻绮惧害闂
docs: 鏇存柊 API 鏂囨。
refactor(gateway): 鎻愬彇 CORS 閰嶇疆涓虹嫭绔嬬被
```

## 鍒嗘敮绠＄悊绛栫暐

| 鍒嗘敮 | 鐢ㄩ€?| 璇存槑 |
|------|------|------|
| `main` | 绋冲畾鐗堟湰 | 淇濇姢鍒嗘敮锛岀姝㈢洿鎺ユ帹閫?|
| `feature/*` | 鏂板姛鑳藉紑鍙?| 浠?`main` 鍒囧嚭锛屽畬鎴愬悗閫氳繃 PR 鍚堝叆 `main` |
| `fix/*` | Bug 淇 | 浠?`main` 鍒囧嚭锛屼慨澶嶅悗閫氳繃 PR 鍚堝叆 `main` |

### 鍒嗘敮鍛藉悕瑙勮寖

- `feature/description` 鈥?濡?`feature/product-search`
- `fix/description` 鈥?濡?`fix/trade-amount-precision`

淇濇寔姣忎釜鍒嗘敮鑱氱劍浜庡崟涓€鏀瑰姩锛屼竴涓垎鏀彧瑙ｅ喅涓€涓棶棰樸€?
## PR 鎻愪氦涓庡鏍告祦绋?
### 鎻愪氦鍓嶆鏌?
- [ ] 浠ｇ爜缂栬瘧閫氳繃锛歚mvn clean compile`
- [ ] 閬靛惊浠ｇ爜瑙勮寖鍜屾彁浜よ鑼?- [ ] 鑷祴閫氳繃锛屽叧閿矾寰勫凡楠岃瘉
- [ ] 鏃犲浣欒皟璇曚唬鐮併€佹敞閲婁唬鐮?- [ ] 鍒嗘敮宸?rebase 鍒版渶鏂扮殑 `main`

### PR 瑕佹眰

1. **鍏宠仈 Issue** 鈥?鍦?PR 鎻忚堪涓叧鑱斿搴?Issue锛坄Closes #123`锛?2. **鎻忚堪鍙樻洿** 鈥?绠€瑕佽鏄庡彉鏇村唴瀹广€佸師鍥犲強褰卞搷鑼冨洿
3. **閫氳繃 CI** 鈥?纭繚缂栬瘧鍜屾祴璇曢€氳繃
4. **浠ｇ爜瀹℃煡** 鈥?鑷冲皯 1 浜?Review 閫氳繃鍚庢柟鍙悎骞?5. **淇濇寔绠€娲?* 鈥?涓€涓?PR 鍙В鍐充竴涓棶棰橈紝閬垮厤鏃犲叧鏀瑰姩

### PR 鎻忚堪妯℃澘

```markdown
## 鍙樻洿鍐呭
[绠€瑕佹弿杩版敼浜嗕粈涔堬紝涓轰粈涔堟敼]

## 鍏宠仈 Issue
Closes #123

## 娴嬭瘯璇存槑
[濡備綍楠岃瘉鏀瑰姩鐨勬纭€

## 娑夊強妯″潡
[gateway / system / user / product / ...]
```

## 闂鍙嶉

鍙戠幇 Bug 鎴栨湁鏀硅繘寤鸿鏃讹細

1. **浼樺厛鏌ラ槄** [Bug.md](Bug.md) 鈥?纭鏄惁涓哄凡鐭ラ棶棰橈紝鏌ョ湅宸叉湁鐨勪慨澶嶆柟妗堝拰鎺掓煡瑕佺偣
2. **鎻愪氦 Issue** 鈥?鑻ヤ负鏂伴棶棰橈紝璇烽檮涓婏細
   - 澶嶇幇姝ラ
   - 鏈熸湜琛屼负涓庡疄闄呰涓?   - 鐜淇℃伅锛堟ā鍧椼€佺増鏈瓑锛?   - 鐩稿叧鏃ュ織鎴栨埅鍥?