# Bug 璁板綍鏂囨。

> 璁板綍椤圭洰寮€鍙戜腑閬囧埌鐨勫叧閿棶棰樺強鍏惰В鍐虫柟妗堬紝渚涘悗缁帓鏌ュ弬鑰冦€?
---

## Bug-001: ES 鎼滅储鎶?ConversionException锛堟棩鏈熸牸寮忎笉鍖归厤锛?
**鏃ユ湡**: 2026-05-12
**妯″潡**: wealth-search
**褰卞搷**: ES 鎼滅储鎺ュ彛杩斿洖 500锛屾棤娉曟煡璇㈡暟鎹?
### 鐜拌薄

璋冪敤 `GET /search/product/search?keyword=xxx` 杩斿洖锛?
```json
{"code":500,"message":"绯荤粺閿欒锛欳onversion exception when converting document id 1"}
```

浣?ES 闆嗙兢鏈韩鏌ヨ姝ｅ父锛坉ocker exec 鐩存帴鏌ヨ ES 鎴愬姛锛夛紝绱㈠紩鏂囨。涔熷瓨鍦紙count=8锛夈€?
### 鏍瑰洜

`ProductDocument.java` 涓?`createTime` 鍜?`updateTime` 瀛楁瀹氫箟锛?
```java
@Field(type = FieldType.Date)
private LocalDateTime createTime;
```

鏈寚瀹氭棩鏈熸牸寮忔椂锛孲pring Data Elasticsearch 榛樿浣跨敤 `date_optional_time` 鏍煎紡瀛樺偍銆侲S 杩斿洖鐨?`_source` 涓棩鏈熻鎴柇涓虹函鏃ユ湡瀛楃涓诧紙濡?`"2026-05-10"`锛夛紝浣?Java 瀹炰綋瀛楁绫诲瀷涓?`LocalDateTime`锛屽弽搴忓垪鍖栨椂鏃犳硶灏?`"2026-05-10"` 杞崲涓?`LocalDateTime`锛屾姏鍑?`ConversionException`銆?
### 淇

鏄惧紡鎸囧畾鏃ユ湡鏍煎紡涓?`DateFormat.date_hour_minute_second_millis`锛?
```java
@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
private LocalDateTime createTime;
```

鍚屾椂绱㈠紩鏁版嵁鏃堕』浼犲叆瀹屾暣 ISO 鏃ユ湡鏃堕棿鏍煎紡锛堝 `"2026-05-10T11:29:46.000"`锛夈€?
### 娑夊強鏂囦欢

- `wealth-search/src/main/java/com/Wealth/platform/search/entity/ProductDocument.java`

### 鎺掓煡瑕佺偣锛堝悗缁亣鍒扮被浼奸棶棰樺厛鏌ユ娓呭崟锛?
- [ ] ES 鏌ヨ鏄惁鎶?`ConversionException` / 鎼滅储鎺ュ彛杩斿洖 500
- [ ] ES mapping 涓棩鏈熷瓧娈垫牸寮忔槸鍚︿笌 Java 瀹炰綋 `@Field` 澹版槑涓€鑷?- [ ] 绱㈠紩鏂囨。鐨?`_source` 涓棩鏈熷€兼槸鍚︿负瀹屾暣鏍煎紡锛堝惈鏃堕棿閮ㄥ垎锛?- [ ] `@Field` 涓?`FieldType` 鏄惁涓?Java 绫诲瀷鍖归厤锛堝 `BigDecimal` 鈫?`Scaled_Float`锛?- [ ] 鏃?Redis 渚濊禆鐨勬ā鍧楀惎鍔ㄦ槸鍚︽姤 `NoClassDefFoundError: RedisSerializer`
      鈫?妫€鏌?`RedisConfig` / `RedisUtil` 鏄惁鏈?`@ConditionalOnClass`
- [ ] IK 鍒嗚瘝鍣ㄦ槸鍚︾敓鏁?鈫?妫€鏌?ES mapping 涓?`analyzer` 鏄惁涓?`ik_max_word`

---

## Bug-002: wealth-search 鍚姩澶辫触锛圧edisSerializer NoClassDefFoundError锛?
**鏃ユ湡**: 2026-05-12
**妯″潡**: wealth-common / wealth-search

### 鐜拌薄

wealth-search 鍚姩鏃舵姤锛?
```
NoClassDefFoundError: org.springframework.data.redis.serializer.RedisSerializer
```

### 鏍瑰洜

`RedisConfig.java` 鍜?`RedisUtil.java` 浣嶄簬 wealth-common 涓紝浣?wealth-search 鍦?pom.xml 涓帓闄や簡 Redis 渚濊禆锛坄spring-boot-starter-data-redis`锛夈€係pring 鍚姩鏃舵壂鎻忓埌杩欎袱涓被骞跺皾璇曞姞杞斤紝鍥犵己灏?Redis 绫昏€屽け璐ャ€?
### 淇

鍦ㄤ袱涓被涓婃坊鍔?`@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")`锛?
```java
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisConfig { ... }

@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisUtil { ... }
```

### 娑夊強鏂囦欢

- `wealth-common/src/main/java/com/Wealth/common/config/RedisConfig.java`
- `wealth-common/src/main/java/com/Wealth/common/utils/RedisUtil.java`

---

## Bug-003: ES 绱㈠紩鏁版嵁涓虹┖锛堢储寮曢噸寤哄悗鏈悓姝ワ級

**鏃ユ湡**: 2026-05-12
**妯″潡**: wealth-search / wealth-product

### 鐜拌薄

ES 绱㈠紩 `Wealth_product` 瀛樺湪浣嗘枃妗ｆ暟涓?0锛屾悳绱㈡棤缁撴灉銆?
### 鏍瑰洜

ES 绱㈠紩琚垹闄ら噸寤哄悗锛孧ySQL 涓殑浜у搧鏁版嵁鏈嚜鍔ㄥ悓姝ュ埌 ES銆傞」鐩洰鍓嶆棤鑷姩鍚屾鏈哄埗锛岄渶鎵嬪姩閫氳繃 search 鏈嶅姟鐨?save API 閲嶆柊绱㈠紩銆?
### 閲嶅缓姝ラ

```bash
# 1. 浠庝骇鍝佹湇鍔¤幏鍙栨墍鏈変骇鍝?curl http://localhost:8080/product/WeaProduct

# 2. 閫愭潯閫氳繃 search 鏈嶅姟鍐欏叆 ES
# POST http://localhost:8080/search/product
# Body: { "id":1, "productName":"榛勯噾ETF", "productCode":"GOLD001", ... }

# 3. 楠岃瘉
docker exec es curl -s 'http://localhost:9200/Wealth_product/_count'
```

### 娑夊強鏂囦欢

- `wealth-search/src/main/java/com/Wealth/platform/search/controller/ProductSearchController.java`
- `wealth-product/src/main/java/com/Wealth/platform/product/controller/WeaProductController.java`

---

## Bug-004: 浜ゆ槗濮旀墭鎻愪氦鎻愮ず"鐢ㄦ埛淇℃伅寮傚父"锛坲serId 涓?0锛?
**鏃ユ湡**: 2026-05-12
**妯″潡**: front-user / wealth-user
**褰卞搷**: 鐧诲綍鍚庢棤娉曟彁浜や氦鏄撳鎵樺崟

### 鐜拌薄

鐢ㄦ埛宸茬櫥褰曪紙鎸佹湁 JWT Token锛岃兘姝ｅ父璁块棶鍚勯〉闈級锛屼絾鎻愪氦浜ゆ槗濮旀墭鏃跺脊绐楁彁绀?鐢ㄦ埛淇℃伅寮傚父锛岃閲嶆柊鐧诲綍"銆侾laywright 娴嬭瘯鍏ㄩ儴閫氳繃锛?4椤癸級锛屼粎鎵嬪姩鎻愪氦娴佺▼瑙﹀彂璇ラ敊璇€?
### 鏍瑰洜

`front-user/src/store/index.ts` 涓?`login()` 鏂规硶娴佺▼锛?
```
鐧诲綍鎴愬姛 鈫?鑾峰彇 token 鈫?setToken() 鈫?璋冪敤 getUserList() 鏌ヨ鎵€鏈夌敤鎴?鈫?users.find(u => u.username === 鐧诲綍鐢ㄦ埛鍚? 鈫?鍖归厤鍒板垯璁剧疆 userId
```

`getUserList()` 鍙?GET `/user` 璇锋眰渚濊禆鍚庣鎷︽埅鍣ㄩ獙璇?Token锛岃嫢璇ヨ姹傚洜浠讳綍鍘熷洜澶辫触锛堢綉缁滆秴鏃躲€佹湇鍔℃湭灏辩华銆乀oken 鏍￠獙寮傚父绛夛級锛宍catch` 鍧楅潤榛樺悶鎺夐敊璇紝`userId` 淇濇寔涓?0銆俙setStoredUser({ userId: 0, ... })` 灏?0 鍐欏叆 localStorage銆傚悗缁〉闈?reload 鍚?`userId` 渚濈劧鏄?0銆?
浜ゆ槗濮旀墭椤?`handleSubmit()` 妫€鏌?`if (!userStore.userId)` 鈫?`!0 === true` 鈫?鏄剧ず"鐢ㄦ埛淇℃伅寮傚父"銆?
### 淇

**鏂规**锛氱櫥褰曟帴鍙ｄ笉鍐嶈繑鍥炵函瀛楃涓?Token锛屾敼涓鸿繑鍥?`LoginVO { token, userId, nickname }`锛屽墠绔洿鎺ヤ粠鐧诲綍鍝嶅簲涓幏鍙?userId锛屾秷闄ゅ `getUserList()` 鐨勪簩娆¤皟鐢ㄤ緷璧栥€?
#### 鍚庣鏀瑰姩

1. 鏂板 `LoginVO`锛坄wealth-user/vo/LoginVO.java`锛?   ```java
   public class LoginVO {
       private String token;
       private Long userId;
       private String nickname;
   }
   ```

2. `UserService.login()` 杩斿洖绫诲瀷浠?`String` 鏀逛负 `LoginVO`
3. `UserController.login()` 杩斿洖绫诲瀷浠?`Result<String>` 鏀逛负 `Result<LoginVO>`

#### 鍓嶇鏀瑰姩

`front-user/src/store/index.ts` 涓?`login()`锛?
```typescript
// 涔嬪墠锛氬彇 token 鍚庝簩娆¤皟鐢?getUserList()
const token = res.data as string
this.token = token
setToken(token)
// getUserList() 鍙兘澶辫触...

// 涔嬪悗锛氱洿鎺ヤ粠鐧诲綍鍝嶅簲瑙ｆ瀯 token + userId
const { token, userId } = res.data
this.token = token
this.userId = userId
setToken(token)
setStoredUser({ username, userId, nickname, avatar })
```

### 鎺掓煡瑕佺偣锛堟坊鍔犲埌宸叉湁娓呭崟锛?
- [ ] 鍓嶇"鐢ㄦ埛淇℃伅寮傚父" 鈫?妫€鏌?`userStore.userId` 鏄惁涓?0
- [ ] 妫€鏌ョ櫥褰曟帴鍙ｅ搷搴斾腑鏄惁鍖呭惈 `userId`
- [ ] 妫€鏌?localStorage 涓?`Wealth_user_info.userId` 鍊?- [ ] 鏇存柊浠ｇ爜鍚庨』閲嶅惎 wealth-user 鏈嶅姟浣?VO 鍙樻洿鐢熸晥

### 娑夊強鏂囦欢

- `wealth-user/src/main/java/com/Wealth/user/vo/LoginVO.java`锛堟柊澧烇級
- `wealth-user/src/main/java/com/Wealth/user/service/UserService.java`
- `wealth-user/src/main/java/com/Wealth/user/service/impl/UserServiceImpl.java`
- `wealth-user/src/main/java/com/Wealth/user/controller/UserController.java`
- `front-user/src/store/index.ts`

---

## Bug-005: 浜ゆ槗濮旀墭鍒嗛〉绛涢€変笉鐢熸晥锛坥rderStatus 鍙傛暟琚拷鐣ワ級

**鏃ユ湡**: 2026-05-12
**妯″潡**: wealth-trade
**褰卞搷**: 鍓嶇绛涢€?宸叉垚浜?寰呮垚浜?宸叉挙閿€"鏃犳晥鏋滐紝濮嬬粓杩斿洖鍏ㄩ儴鏁版嵁

### 鐜拌薄

鍓嶇濮旀墭鍗曞垪琛ㄧ殑绛涢€変笅鎷夋閫夋嫨"宸叉垚浜?鎴?宸叉挙閿€"鍚庯紝鍒楄〃鏁版嵁鏈彉鍖栵紝濮嬬粓灞曠ず鍏ㄩ儴璁㈠崟銆傛祻瑙堝櫒 Network 闈㈡澘鍙湅鍒?`orderStatus` 鍙傛暟宸叉甯稿彂閫併€?
### 鏍瑰洜

`WeaTradeOrderController.page()` 鏂规硶鍙帴鏀?`pageNum` 鍜?`pageSize` 涓や釜鍙傛暟锛屾湭澹版槑 `orderStatus` 鍜?`userId` 鍙傛暟銆俙orderStatus` 鍜?`userId` 铏戒互 query string 褰㈠紡鍙戦€佸埌鍚庣锛屼絾琚?Spring MVC 蹇界暐銆?
```java
// 淇鍓嶏細鍙湁鍒嗛〉鍙傛暟锛屾棤绛涢€夊弬鏁?@GetMapping("/page")
public Result<IPage<WeaTradeOrderVO>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<WeaTradeOrder> page = new Page<>(pageNum, pageSize);
    IPage<WeaTradeOrder> entityPage = WeaTradeOrderService.page(page); // 鏃犳潯浠舵煡璇㈠叏閮?    ...
}
```

### 娑夊強鏂囦欢

- `wealth-trade/src/main/java/com/Wealth/platform/trade/controller/WeaTradeOrderController.java`
- `wealth-trade/src/main/java/com/Wealth/platform/trade/service/WeaTradeOrderService.java`
- `wealth-trade/src/main/java/com/Wealth/platform/trade/service/impl/WeaTradeOrderServiceImpl.java`

---

## Bug-006: 浜у搧涓績鍒嗙被绛涢€変笉鐢熸晥锛坧roductType 鍙傛暟琚拷鐣ワ級

**鏃ユ湡**: 2026-05-12
**妯″潡**: wealth-product
**褰卞搷**: 鍓嶇绛涢€?璐甸噾灞?鐞嗚储浜у搧/鍩洪噾/鑲＄エ"鏃犳晥鏋滐紝濮嬬粓鏄剧ず鍏ㄩ儴浜у搧

### 鏍瑰洜

涓?Bug-005 鐩稿悓妯″紡 鈥?`WeaProductController.page()` 鍙帴鏀?`pageNum` 鍜?`pageSize`锛屾湭澹版槑 `productType` 鍙傛暟锛屽墠绔紶鍙傝 Spring MVC 蹇界暐銆?
### 娑夊強鏂囦欢

- `wealth-product/src/main/java/com/Wealth/platform/product/controller/WeaProductController.java`
- `wealth-product/src/main/java/com/Wealth/platform/product/service/WeaProductService.java`
- `wealth-product/src/main/java/com/Wealth/platform/product/service/impl/WeaProductServiceImpl.java`

---

## Bug-007: 璐㈢粡璧勮/娑堟伅涓績鍒嗙被绛涢€変笉鐢熸晥锛坣ewsType/userId 鍙傛暟琚拷鐣ワ級

**鏃ユ湡**: 2026-05-12
**妯″潡**: wealth-message
**褰卞搷**: 璐㈢粡璧勮鐨勫垎绫荤瓫閫夛紙琛屼笟鍔ㄦ€?甯傚満鍒嗘瀽/鏀跨瓥瑙ｈ/鍏徃鍏憡锛夊拰娑堟伅涓績鐨勭敤鎴风瓫閫変笉鐢熸晥

### 鏍瑰洜

涓?Bug-005/Bug-006 鐩稿悓妯″紡 鈥?`WeaNewsController.page()` 鍜?`WeaMessageController.page()` 鍙帴鏀跺垎椤靛弬鏁帮紝鏈０鏄?`newsType`/`userId` 绛涢€夊弬鏁般€?
### 娑夊強鏂囦欢

- `wealth-message/src/main/java/com/Wealth/platform/message/controller/WeaNewsController.java`
- `wealth-message/src/main/java/com/Wealth/platform/message/service/WeaNewsService.java`
- `wealth-message/src/main/java/com/Wealth/platform/message/service/impl/WeaNewsServiceImpl.java`
- `wealth-message/src/main/java/com/Wealth/platform/message/controller/WeaMessageController.java`
- `wealth-message/src/main/java/com/Wealth/platform/message/service/WeaMessageService.java`
- `wealth-message/src/main/java/com/Wealth/platform/message/service/impl/WeaMessageServiceImpl.java`

---

## Bug-008: 鍋滃敭浜у搧浠嶅彲鐐瑰嚮"鍘讳氦鏄?璺宠浆浜ゆ槗椤?
**鏃ユ湡**: 2026-05-12
**妯″潡**: front-user
**褰卞搷**: 鏍囪涓?鍋滃敭"鐨勪骇鍝侊紝鐢ㄦ埛浠嶅彲閫氳繃璇︽儏寮圭獥涓殑"鍘讳氦鏄?鎸夐挳杩涘叆浜ゆ槗椤典笅鍗?
### 鐜拌薄

浜у搧鍗＄墖涓婃樉绀?鍋滃敭"鏍囩鐨勪骇鍝侊紝鐐瑰嚮鏌ョ湅璇︽儏鍚庯紝璇︽儏寮圭獥搴曢儴鐨?鍘讳氦鏄?鎸夐挳浠嶅彲鐐瑰嚮锛屼細璺宠浆鍒颁氦鏄撳鎵橀〉骞跺甫鍏ヤ骇鍝佷唬鐮侊紝鐢ㄦ埛鍙兘瀵瑰仠鍞骇鍝佷笅鍗曘€?
### 鏍瑰洜

璇︽儏寮圭獥鐨?鍘讳氦鏄?鎸夐挳鏈牴鎹?`status` 瀛楁鍋氭潯浠剁鐢紝濮嬬粓鍙偣鍑伙細

```html
<!-- 淇鍓嶏細濮嬬粓鍙偣鍑?-->
<el-button type="primary" @click="goTrade(detailItem)">鍘讳氦鏄?/el-button>

<!-- 淇鍚庯細鍋滃敭鏃剁鐢?-->
<el-button type="primary" :disabled="detailItem?.status !== 1" @click="goTrade(detailItem)">鍘讳氦鏄?/el-button>
```

### 娑夊強鏂囦欢

- `front-user/src/views/product/index.vue`
