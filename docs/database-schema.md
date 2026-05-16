# 鏁版嵁搴撹〃缁撴瀯涓庡瓧娈?
> 鍐欏疄浣撶被鏃跺紩鐢?鈥?琛ㄧ粨鏋勩€佸瓧娈点€丅aseEntity 缁ф壙瑙勫垯銆?
---

# 涓€銆佹暟鎹簱瑙勮寖锛堝繀椤讳弗鏍奸伒瀹堬級

1. 鏁版嵁搴撳悕锛歠inance
2. 瀛楃闆嗭細utf8mb4
3. 鎵€鏈夎〃蹇呴』鍖呭惈锛歩d銆乧reate_time銆乽pdate_time銆乨el_flag
4. 閫昏緫鍒犻櫎锛歞el_flag 0=鏈垹闄?1=宸插垹闄?5. 涓婚敭缁熶竴浣跨敤 BIGINT 鑷
6. 鏃堕棿瀛楁锛欴ATETIME
7. 绂佹浣跨敤澶栭敭锛屼笟鍔″眰鍏宠仈
8. 绱㈠紩蹇呴』鎸夊缓琛ㄨ鍙ュ垱寤?
瀹屾暣寤鸿〃 SQL锛歚wealth-common/src/main/resources/sql/init.sql`

鏁版嵁搴撶壒娈婁緥澶栵細
- `wea_user_favorite` 鏃?del_flag 鍜?update_time 鍒楋紙鍞竴鏃犻€昏緫鍒犻櫎鐨勮〃锛?- `ums_admin` 鏃?update_time 鍒?
# 浜屻€佸綋鍓嶉」鐩墍鏈夎〃锛堝繀椤讳弗鏍煎搴旓級

## 1. 鐢ㄦ埛妯″潡
sys_user              # 绯荤粺鐢ㄦ埛琛?
## 2. 浜у搧&琛屾儏妯″潡
wea_product           # 浜у搧琛?wea_market_data       # 琛屾儏鏁版嵁琛?
## 3. 鑷€夋ā鍧?wea_user_favorite     # 鐢ㄦ埛鑷€夎〃锛堟棤 del_flag 鍒楋紝鐗╃悊鍒犻櫎锛?
## 4. 浜ゆ槗妯″潡
wea_trade_order       # 浜ゆ槗濮旀墭鍗?
## 5. 璧勮&娑堟伅
wea_news              # 璐㈢粡璧勮
wea_message           # 绔欏唴娑堟伅

## 6. 鍚庡彴鏉冮檺妯″潡
ums_admin             # 绠＄悊鍛?ums_role              # 瑙掕壊
ums_resource          # 璧勬簮
ums_admin_role_relation
ums_role_resource_relation

# 涓夈€丅aseEntity 缁ф壙瑙勮寖

## 瀹炰綋绫昏鑼?
1. 鎵€鏈?Entity 蹇呴』缁ф壙 `com.wealth.common.entity.BaseEntity`锛堣嚜鍔ㄥ寘鍚?id/create_time/update_time/del_flag 鍥涗釜鍩虹瀛楁锛?2. `@TableName("琛ㄥ悕")` 鈥?蹇呴』鏄庣‘鎸囧畾琛ㄥ悕
3. `@TableLogic` 宸插湪 BaseEntity.delFlag 涓婂畾涔夛紝瀛愮被鏃犻渶閲嶅澹版槑
4. 鑻ヨ〃鏃?del_flag 鍒楋紝瀛愮被涓噸鍐?`@TableField(exist = false) private Integer delFlag;`
5. 瀛楁鏄犲皠缁熶竴浣跨敤 `@TableField("鍒楀悕")`
6. 鑷姩濉厖瀛楁锛歝reate_time 浣跨敤 `@TableField(fill = FieldFill.INSERT)`锛寀pdate_time 浣跨敤 `@TableField(fill = FieldFill.INSERT_UPDATE)`

### BaseEntity 瀹氫箟锛坒inance-common/entity/BaseEntity.java锛?
```java
@Data
public class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
```

### BaseEntity 缁ф壙瑙勫垯

鎵€鏈?Entity 蹇呴』缁ф壙 BaseEntity锛屾瘡涓熀纭€瀛楁鎸変互涓嬭鍒欏鐞嗭細

| 瀛楁 | BaseEntity 瀹氫箟 | 鏃犲搴斿垪鐨勫瓙绫诲鐞嗘柟寮?|
|------|----------------|----------------------|
| id | `@TableId(type = IdType.AUTO)` | 鏃犻渶澶勭悊锛岃嚜鍔ㄧ户鎵?|
| create_time | `@TableField(fill = FieldFill.INSERT)` | 鑻ヨ〃涓棤璇ュ垪锛屽瓙绫讳腑閲嶅啓锛歚@TableField(exist = false) private LocalDateTime createTime;` |
| update_time | `@TableField(fill = FieldFill.INSERT_UPDATE)` | 鑻ヨ〃涓棤璇ュ垪锛屽瓙绫讳腑閲嶅啓锛歚@TableField(exist = false) private LocalDateTime updateTime;` |
| del_flag | `@TableLogic @TableField("del_flag")` | 鑻ヨ〃涓棤璇ュ垪锛屽瓙绫讳腑閲嶅啓锛歚@TableField(exist = false) private Integer delFlag;` |

褰撳墠椤圭洰涓細
- **WeaUserFavorite** 鈥?鍞竴瑕嗙洊 delFlag锛坄exist=false`锛夊拰 updateTime锛坄exist=false`锛夌殑瀹炰綋
- **UmsAdmin** 鈥?瑕嗙洊 updateTime锛坄exist=false`锛宍ums_admin` 琛ㄦ棤璇ュ垪锛?
> 娉ㄦ剰锛氬瓙绫婚噸鍐欏瓧娈垫椂椤诲悓鏃朵娇鐢?`@EqualsAndHashCode(callSuper = true)`锛堟垨鍦ㄧ被涓婂姞 `@Getter @Setter @EqualsAndHashCode(callSuper = true)` 鏇夸唬 `@Data`锛夛紝浠ョ‘淇?Lombok 姝ｇ‘澶勭悊鐖剁被瀛楁銆?