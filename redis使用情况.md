# redis使用情况

### **系统初始化**

这一部分主要做一些redis相关初始化工作，主要将每件商品的库存和库存是否秒杀完毕标志放入redis。

```java
for (GoodsVo goodsVo : goodsVoList) {
    // 将每件秒杀商品的库存放入redis，以便在秒杀时进行递减，若减为0，则可以直接在Cotroller层将亲求返回
    redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId(), 	goodsVo.getStockCount());
    // 在内存中存放秒杀商品还有库存的标志，若为true，则可以省略redis的访问，直接返回
    localOverMap.put(goodsVo.getId(), false);
}
```

### **登陆**

用户信息存放”双份“，一份起到缓存作用，使用用户 mobile 就可以从redis中获取用户信息；另一份起验证作用，证明用户当前是登陆状态，并且一些需要登陆的操作需要用户信息时，需要从该渠道获取，作为一个”权限“。

登陆后会进入到商品列表页面，此时会将商品列表信息放在一个html页面中放入内存。

```java
// 登陆前验证user，尝试从redis中取，若redis中没有，则添加缓存
redisService.set(MiaoShaUserKey.getByNickName, mobile, user);	
// 主键为mobile值： MiaoShaUserKey:nickName:18051021896		超时时间：0
```

```java
// 将用户信息放入缓存
redisService.set(MiaoShaUserKey.token, token, user);	
// 主键为token值，值为user信息：MiaoShaUserKey:tk:4b8484cc-fcd3-49d0-8938-63ae90c1cd6c
// 超时时间：3600 *24*2
```

```java
// 缓存含有goods列表的html页面
if(!StringUtils.isEmpty(html)) {
redisService.set(prefix, key, html);	
// 主键为固定值：GoodsKey:gl:		超时时间：60
}
```

### **秒杀商品详情**

商品详情页会有借助redis缓存实现验证码功能。

```java
//把商品详情页的验证码存到redis中
int rnd = calc(verifyCode);
redisService.set(MiaoshaKey.getMiaoshaVerifyCode,user.getNickname()+"_"+goodsId,rnd);
// 主键为用户mobile和商品id构成：MiaoshaKey:vc:null_1		超时时间：300
// 若用户没登陆是否也行？
```

缓存中还会保存一段时间内，某用户请求次数，达到次数会限制用户操作，此处以到一定限流作用，也防止非法用户的非法操作。

```java
// 保存特定时间内请求次数
int second = accessLimit.second();
AccessKey accessKey = AccessKey.withExpire(second);
redisService.set(accessKey, key, 1);
// 主键为请求的uri_用户mobile：AccessKey:access:/miaosha/path_18051021896
```

```java
// 保存秒杀操作的路径特定path
String str = MD5Utils.md5(UUIDUtil.uuid() + "123456");
redisService.set(MiaoshaKey.getMiaoshaPath, user.getNickname() + "_" + goodsId, str);
// 主键为： MiaoshaKey:mp:null_1		超时时间：300
```

```java
// 预减库存
Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
// 这是将redis中存储的库存减1，可是库存什么时候添加进redis中的呢

```

**开发过程中的一个异常：**

> **java.lang.IllegalStateException: getOutputStream() has already been called for this response**

> 产生这样的异常原因:是web容器生成的servlet代码中有out.write(""),这个和JSP中调用的response.getOutputStream()产生冲突.即Servlet规范说明，不能既调用response.getOutputStream()，又调用response.getWriter(),无论先调用哪一个，在调用第二个时候应会抛出IllegalStateException，因为在jsp中，out变量实际上是通过response.getWriter得到的，你的程序中既用了response.getOutputStream，又用了out变量，故出现以上错误。