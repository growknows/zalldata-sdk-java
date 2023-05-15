package cn.zalldigital;


import cn.zalldigital.consumer.BatchConsumer;
import cn.zalldigital.exception.InvalidArgumentException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Example {

    public static void main(String[] args) throws InvalidArgumentException, ParseException {
        /**
         * hostname  : 域名
         * project   : 项目名称
         * service   : 服务名称
         * token     : 校验token
         */
        final String serverUrl = "http://${hostname}/a?project=${project}&service=${service}&token=${token}";
        final int bulkSize = 1;
        final ZallDataAnalytics za = new ZallDataAnalytics(new BatchConsumer(serverUrl, bulkSize));


        // 用户未登录时，可以使用产品自己生成的cookieId来标注用户
        String cookieId = "abcdefgABCDEFG123456789";
        Map<String, Object> properties = new HashMap<>();

        /**
         * 通过IdTypeEnum对不同的ID进行映射，需要开发者导入项目，并在上报数据的properties中放入$distinctIdType及其对应的值
         * public enum IdTypeEnum {
         *   DEVICE_ID(0),// 设备ID
         *   LOGIN_ID(1),// 登录ID
         *   MOBILE(2),// 手机
         *   UNIONID(3),// 微信union_id
         *   OPENID(4),// 微信open_id
         *   EXTERNALID(5),// 外部ID
         *   Z_SHOP_ID(6),// ZShop_id
         *   YUN_MALL_ID(7),// YunMall_id
         *   ;
         *   private int type;
         *
         *   IdTypeEnum(int type) {
         *   this.type = type;
         *   }
         *
         *   public int getType() {
         *   return type;
         *   }
         *
         *   public void setType(int type) {
         *   this.type = type;
         *   }
         * }
         */

        // 1.1 访问首页

// 前面有$开头的property字段，是ZA提供给用户的预置字段
// 对于预置字段，已经确定好了字段类型和字段的显示名
        properties.clear();
        properties.put("$time", new Date());                // 这条event发生的时间，如果不设置的话，则默认是当前时间
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("Channel", "baidu");                 // 用户是通过baidu这个渠道过来的
        properties.put("$project","zall");
        properties.put("$distinctIdType", 0); // 设置当前distinctIdType为DEVICE_ID
        za.track(cookieId, false, "ViewHomePage", properties); // 记录访问首页这个event

// 1.2 搜索商品a
        properties.clear();
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("KeyWord", "XX手机");                 // 搜索引擎引流过来时使用的关键词
        properties.put("$distinctIdType", 0); // 设置当前distinctIdType为DEVICE_ID
        za.track(cookieId, false, "SearchProduct", properties);      // 记录搜索商品这个event

// 1.3 浏览商品
        properties.clear();
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("ProductName", "xx手机");             // 商品名称
        properties.put("ProductType", "智能手机");            // 商品类别
        properties.put("ShopName", "XX官方旗舰店");           // 店铺名称
        properties.put("$distinctIdType", 0); // 设置当前distinctIdType为DEVICE_ID
        za.track(cookieId, false, "ViewProduct", properties);      // 记录浏览商品这个event

// 2. 用户决定注册了
        String registerId = "1";                    // 用户注册时，分配给用户的注册Id

// 2.1 通过，trackSignUP，把匿名ID和注册ID贯通起来
        properties.clear();
        properties.put("RegisterChannel", "baidu");         // 用户的注册渠道
        properties.put("$distinctIdType", 1); // 设置当前distinctIdType为LOING_ID
        properties.put("$originalIdType", 0); // 设置当前originalIdType为DEVICE_ID
        za.trackSignUp(registerId, cookieId, properties);

// 2.2 用户注册时，填充了一些个人信息，可以用Profile接口记录下来
        Map<String, Object> profiles = new HashMap<String, Object>();
        profiles.put("$city", "武汉");                        // 用户所在城市
        profiles.put("$province", "湖北");                    // 用户所在省份
        profiles.put("$name", "昵称abc");                     // 用户的昵称
        profiles.put("$signup_time", new Date());            // 注册时间
        profiles.put("Gender", "male");                      // 用户的性别
// 用户的出生日期，特别注意，这个地方填入年龄是不合适的，因为年龄会随着时间而变化
        profiles.put("Birthday", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1988-11-03 00:00:00"));
        profiles.put("RegisterChannel", "baidu");            // 用户的注册渠道
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.profileSet(registerId, true, profiles);  // 此时传入的是注册ID了

// 2.3 立刻刷新一下，让数据传到ZA中
        za.flush();

        properties.clear();
        properties.put("$time", new Date());                // 这条event发生的时间，如果不设置的话，则默认是当前时间
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("Channel", "baidu");                 // 用户是通过baidu这个渠道过来的
        properties.put("$project","zall");
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.track(registerId, true, "ViewHomePage", properties); // 注意，此时使用的已经是注册ID了

// 1.2 搜索商品a
        properties.clear();
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("KeyWord", "XX手机");                 // 搜索引擎引流过来时使用的关键词
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.track(registerId, true,  "SearchProduct", properties);      // 记录搜索商品这个event

// 3. 用户注册后，进行后续行为
// 3.1 提交订单和提交订单详情 (这个订单里面包含一个手机和两个手机膜,订单的信息)
        properties.clear();
        properties.put("$os", "Windows");                    // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");                // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");            // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("OrderId", "202001024897111");        // 订单ID
        properties.put("ShipPrice", 10.0);                   // 运费
        properties.put("OrderTotalPrice", 2999.0);           // 订单的总价格，默认是元
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.track(registerId, true, "SubmitOrder", properties); // 注意，此时使用的已经是注册ID了


// 订单中手机这个商品的信息
        properties.clear();
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("OrderId", "202001024897111");       // 订单ID
        properties.put("ProductName", "XX手机");             // 商品名称
        properties.put("ProductType", "智能手机");            // 商品类别
        properties.put("ShopName", "XX官方旗舰店");           // 店铺名称
        properties.put("ProductUnitPrice", 2999.0);         // 商品单价
        properties.put("ProductAmount", 1.0);               // 商品数量，可以是个数，也可以是重量
        properties.put("ProductTotalPrice", 2999.0);        // 商品总价
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.track(registerId, true, "SubmitOrderDetail", properties);


// 订单中手机膜这个商品的信息
        properties.clear();
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("OrderId", "202001024897111");       // 订单ID
        properties.put("ProductName", "5寸钢化膜");           // 商品名称
        properties.put("ProductType", "手机配件");            // 商品类别
        properties.put("ShopName", "XX手机外设店");           // 店铺名称
        properties.put("ProductUnitPrice", 23.0);           // 商品单价
        properties.put("ProductAmount", 2.0);               // 商品数量，可以是个数，也可以是重量
        properties.put("ProductTotalPrice", 46.0);          // 商品总价
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.track(registerId, true, "SubmitOrderDetail", properties);


// 3.2 支付订单和支付订单详情 (整个订单的支付情况)
        properties.clear();
        properties.put("$os", "Windows");                   // 通过请求中的UA，可以解析出用户使用设备的操作系统是windows的
        properties.put("$os_version", "9.2");               // 操作系统的具体版本
        properties.put("$ip", "123.123.123.123");           // 请求中能够拿到用户的IP，则把这个传递给ZA，ZA会自动根据这个解析省份、城市
        properties.put("OrderId", "202001024897111");       // 订单ID
        properties.put("ShipPrice", 10.0);                  // 运费
        properties.put("OrderTotalPrice", 2999.0);          // 订单的总价格，默认是元
        properties.put("PaymentMethod", "AliPay");          // 支付方式
        properties.put("AllowanceAmount", 30.0);            // 补贴金额
        properties.put("AllowanceType", "首次下单红包");      // 补贴类型
        properties.put("PaymentAmount", 2969.0);            // 实际支付的订单金额
        properties.put("$distinctIdType", 1); // 此时传入的distinctIdType是trackSignUp()时传入的LOING_ID
        za.track(registerId, true, "PayOrder", properties);

        za.shutdown();                                  // 关闭API，关闭时会自动调用flush
    }

}
