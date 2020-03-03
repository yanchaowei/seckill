# SpringBoot与消息：Rabbitmq

## 一、概述

1. 大多应用中，可通过消息服务中间件来提升系统异步通信、扩展解耦能力 

2. 消息服务中两个重要概念： 消息代理（message broker）和目的地（destination） 当消息发送者发送消息以后，将由消息代理接管，消息代理保证消息传递到指定目 的地。 

3. 消息队列主要有两种形式的目的地 1. 队列（queue）：点对点消息通信（point-to-point） 2. 主题（topic）：发布（publish）/订阅（subscribe）消息通信。

4. **点对点式**： 

   + 消息发送者发送消息，消息代理将其放入一个队列中，消息接收者从队列中获取消息内容， 消息读取后被移出队列 

   + 消息只有唯一的发送者和接受者，但并不是说只能有一个接收者

5. **发布订阅式**： 

   + 发送者（发布者）发送消息到主题，多个接收者（订阅者）监听（订阅）这个主题，那么 就会在消息到达时同时收到消息 

6. **JMS**（Java Message Service）JAVA消息服务：

   + 基于JVM消息代理的规范。ActiveMQ、HornetMQ是JMS实现 

7. **AMQP**（Advanced Message Queuing Protocol） 

   + 高级消息队列协议，也是一个消息代理的规范，兼容JMS
   + RabbitMQ是AMQP的实现

8. **Spring支持** 

   + spring-jms提供了对JMS的支持 
   + spring-rabbit提供了对AMQP的支持 
   + 需要ConnectionFactory的实现来连接消息代理 
   + 提供JmsTemplate、RabbitTemplate来发送消息
   + @JmsListener（JMS）、@RabbitListener（AMQP）注解在方法上监听消息代理发 布的消息 
   + @EnableJms、@EnableRabbit开启支持 

9. Spring Boot自动配置 

   + JmsAutoConfiguration 
   + RabbitAutoConfiguration

## 二、RabbitMQ简介

### **RabbitMQ简介**

RabbitMQ是一个由erlang开发的AMQP(Advanved Message Queue Protocol)的开源实现。


### **核心概念**

+ **Message**
  消息，消息是不具名的，它由消息头和消息体组成。消息体是不透明的，而消息头则由一系列的可选属性组成，这些属性包括routing-key（路由键）、priority（相对于其他消息的优先权）、delivery-mode（指出该消息可能需要持久性存储）等。
+ **Publisher**
  消息的生产者，也是一个向交换器发布消息的客户端应用程序。
+ **Exchange**
  交换器，用来接收生产者发送的消息并将这些消息路由给服务器中的队列。
  Exchange有4种类型：direct(默认)，fanout, topic, 和headers，不同类型的Exchange转发消息的策略有所区别
+ **Queue**
  消息队列，用来保存消息直到发送给消费者。它是消息的容器，也是消息的终点。一个消息可投入一个或多个队列。消息一直在队列里面，等待消费者连接到这个队列将其取走。
+ **Binding**
  绑定，用于消息队列和交换器之间的关联。一个绑定就是基于路由键将交换器和消息队列连接起来的路由规则，所以可以将交换器理解成一个由绑定构成的路由表。
  Exchange 和Queue的绑定可以是多对多的关系。
+ **Connection**
  网络连接，比如一个TCP连接。
+ **Channel**
  信道，多路复用连接中的一条独立的双向数据流通道。信道是建立在真实的TCP连接内的虚拟连接，AMQP 命令都是通过信道发出去的，不管是发布消息、订阅队列还是接收消息，这些动作都是通过信道完成。因为对于操作系统来说建立和销毁 TCP 都是非常昂贵的开销，所以引入了信道的概念，以复用一条 TCP 连接。
+ **Consumer**
  消息的消费者，表示一个从消息队列中取得消息的客户端应用程序。
+ **Virtual Host**
  虚拟主机，表示一批交换器、消息队列和相关对象。虚拟主机是共享相同的身份认证和加密环境的独立服务器域。每个 vhost 本质上就是一个 mini 版的 RabbitMQ 服务器，拥有自己的队列、交换器、绑定和权限机制。vhost 是 AMQP 概念的基础，必须在连接时指定，RabbitMQ 默认的 vhost 是 / 。
+ **Broker**
  表示消息队列服务器实体

### RabbitMQ运行机制

**AMQP 中的消息路由**
AMQP 中消息的路由过程和 Java 开发者熟悉的 JMS 存在一些差别，AMQP 中增加了 Exchange 和 Binding 的角色。生产者把消息发布到 Exchange 上，消息最终到达队列并被消费者接收，而 Binding 决定交换器的消息应该发送到那个队列。

**Exchange 类型**

Exchange分发消息时根据类型的不同分发策略有区别，目前共四种类型：direct、fanout、topic、headers 。headers 匹配 AMQP 消息的 header 而不是路由键， headers 交换器和 direct 交换器完全一致，但性能差很多，目前几乎用不到了，所以直接看另外三种类型：

+ **direct**

  消息中的路由键（routing key）如果和 Binding 中的 binding key 一致， 交换器就将消息发到对应的队列中。路由键与队 列名完全匹配，如果一个队列绑定到交换机要求路由键为 “dog”，则只转发 routing key 标记为“dog”的消息，不会转 发“dog.puppy”，也不会转发“dog.guard”等等。它是完全 匹配、单播的模式。

+ **fanout**

  每个发到 fanout 类型交换器的消息都会分到所有绑定的队列上去。fanout 交换器不处理路由键，只是简单的将队列绑定到交换器上，每个发送到交换器的消息都会被转发到与该交换器绑定的所有队列上。很像子网广播，每台子网内的主机都获得了一份复制的消息。fanout 类型转发消息是最快的。

+ **topic** 

  topic 交换器通过模式匹配分配消息的路由键属性，将路由键和某个模式进行匹配，此时队列需要绑定到一个模式上。它将路由键和绑定键的字符串切分成单词，这些单词之间用点隔开。它同样也会识别两个通配符：符号“#”和符号“*”。#匹配0个或多个单词，*匹配一个单词。



### RabbitMQ整合

+ 引入 spring-boot-starter-amqp依赖

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
  </dependency>
  ```

+ application.properties配置

  ```properties
  #rabbitmq
  spring.rabbitmq.host=172.19.240.60
  spring.rabbitmq.port=5672
  #spring.rabbitmq.username=mqadmin
  #spring.rabbitmq.password=mqadmin
  spring.rabbitmq.virtual-host=/
  spring.rabbitmq.listener.simple.concurrency= 10
  spring.rabbitmq.listener.simple.max-concurrency= 10
  spring.rabbitmq.listener.simple.prefetch= 1
  spring.rabbitmq.listener.simple.auto-startup=true
  spring.rabbitmq.listener.simple.default-requeue-rejected= true
  spring.rabbitmq.template.retry.enabled=true 
  spring.rabbitmq.template.retry.initial-interval=1000 
  spring.rabbitmq.template.retry.max-attempts=3
  spring.rabbitmq.template.retry.max-interval=10000
  spring.rabbitmq.template.retry.multiplier=1.0
  spring.rabbitmq.publisher-confirms=true
  spring.rabbitmq.listener.direct.acknowledge-mode=manual
  spring.rabbitmq.listener.simple.acknowledge-mode=manual
  ```

+ 测试RabbitMQ

  

+ AmqpAdmin：管理组件,该类封装了对 RabbitMQ 的管理操作

  ```java
  # org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
  @Bean
  @ConditionalOnSingleCandidate(ConnectionFactory.class)
  @ConditionalOnProperty(
      prefix = "spring.rabbitmq",
      name = {"dynamic"},
      matchIfMissing = true
  )
  @ConditionalOnMissingBean({AmqpAdmin.class})
  public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
      return new RabbitAdmin(connectionFactory);
  }
  ```

+ Exchange 操作

  ```java
  //创建四种类型的 Exchange，均为持久化，不自动删除
  rabbitAdmin.declareExchange(new DirectExchange("direct.exchange", true, false));
  rabbitAdmin.declareExchange(new TopicExchange("topic.exchange", true, false));
  rabbitAdmin.declareExchange(new FanoutExchange("fanout.exchange", true, false));
  rabbitAdmin.declareExchange(new HeadersExchange("header.exchange", true, false));
  //删除 Exchange
  rabbitAdmin.deleteExchange("header.exchange");
  ```

+ Queue 操作

  ```java
  //定义队列，均为持久化
  rabbitAdmin.declareQueue(new Queue("debug",true));
  rabbitAdmin.declareQueue(new Queue("info",true));
  rabbitAdmin.declareQueue(new Queue("error",true));
  //删除队列      
  rabbitAdmin.deleteQueue("debug");
  //将队列中的消息全消费掉
  rabbitAdmin.purgeQueue("info",false);
  ```

+ Binding 绑定

  ```java
  //绑定队列到交换器，通过路由键
  rabbitAdmin.declareBinding(new Binding("debug",Binding.DestinationType.QUEUE,
          "direct.exchange","key.1",new HashMap()));
  
  rabbitAdmin.declareBinding(new Binding("info",Binding.DestinationType.QUEUE,
          "direct.exchange","key.2",new HashMap()));
  
  rabbitAdmin.declareBinding(new Binding("error",Binding.DestinationType.QUEUE,
          "direct.exchange","key.3",new HashMap()));
  
  //进行解绑
  rabbitAdmin.removeBinding(BindingBuilder.bind(new Queue("info")).
          to(new TopicExchange("direct.exchange")).with("key.2"));
  
  //使用BindingBuilder进行绑定
  rabbitAdmin.declareBinding(BindingBuilder.bind(new Queue("info")).
          to(new TopicExchange("topic.exchange")).with("key.#"));
  
  //声明topic类型的exchange
  rabbitAdmin.declareExchange(new TopicExchange("exchange1",true,false));
  rabbitAdmin.declareExchange(new TopicExchange("exchange2",true,false));
  
  //exchange与exchange绑定
  rabbitAdmin.declareBinding(new Binding("exchange1",Binding.DestinationType.EXCHANGE,
          "exchange2","key.4",new HashMap()));
  ```

+ RabbitTemplate：

  消息发送处理组件, Spring AMQP 提供了 RabbitTemplate 来简化 RabbitMQ 发送和接收消息操作

+ **发送消息**

  + send （自定义消息 Message）

    ```java
    Message message = new Message("hello".getBytes(),new MessageProperties());
    // 发送消息到默认的交换器，默认的路由键
    rabbitTemplate.send(message);
    // 发送消息到指定的交换器，指定的路由键
    rabbitTemplate.send("direct.exchange","key.1",message);
    // 发送消息到指定的交换器，指定的路由键
    rabbitTemplate.send("direct.exchange","key.1",message,new CorrelationData(UUID.randomUUID().toString()));
    ```

  + convertAndSend（自动 Java 对象包装成 Message 对象，Java 对象需要实现 Serializable 序列化接口）

  ```java
  User user = new User("linyuan");
  // 发送消息到默认的交换器，默认的路由键
  rabbitTemplate.convertAndSend(user);
  // 发送消息到指定的交换器，指定的路由键，设置消息 ID
  rabbitTemplate.convertAndSend("direct.exchange","key.1",user,new CorrelationData(UUID.randomUUID().toString()));
  // 发送消息到指定的交换器，指定的路由键，在消息转换完成后，通过 MessagePostProcessor 来添加属性
  rabbitTemplate.convertAndSend("direct.exchange","key.1",user,mes -> {
      mes.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
          return mes;
  });
  ```

+ **接受消息**

  + receive（返回 Message 对象）

  ```java
  // 接收来自指定队列的消息，并设置超时时间
  Message msg = rabbitTemplate.receive("debug",2000l);
  ```

  + receiveAndConvert（将返回 Message 转换成 Java 对象）

  ```java
  User user = (User) rabbitTemplate.receiveAndConvert();
  ```

  

### RabbitMQ的五种队列模式与实例

**1、简单模式Hello World**

**功能**：一个生产者P发送消息到队列Q,一个消费者C接收

**生产者实现**：

```java


```

**消费者实现**：

```java


```



## 三、面试常见问题

### 1、**为什么用消息队列？**

> 业务体量很小，所以直接**单机一把梭**啥都能搞定了，但是后面业务体量不断扩大，采用**微服务的设计思想**，**分布式的部署方式**，所以拆分了很多的服务，随着体量的增加以及业务场景越来越复杂了，很多场景单机的技术栈和中间件以及不够用了，而且对系统的友好性也下降了，最后做了很多技术选型的工作，我们决定引入**消息队列中间件**。



### 2、**在什么场景用到了消息队列？**

> 三个场景也是消息队列的经典场景，基本上要烂熟于心那种，一说到消息队列就要想到**异步、削峰、解耦**。



> ### **异步：**
>
> 我们之前的场景里面有很多步骤都是在一个流程里面需要做完的，就比如说下单系统吧，本来我们业务简单，下单了付了钱就好了，流程就走完了。
>
> 但是后面来了个产品经理，搞了个**优惠券系统**，OK问题不大，流程里面多100ms去扣减优惠券。
>
> 后来产品经理灵光一闪说我们可以搞个**积分系统**啊，也行吧，流程里面多了200ms去增减积分。
>
> 再后来后来隔壁的产品老王说：下单成功后我们要给用户发短信，也将就吧，100ms去发个短信。
>
> 再后来。。。（有完没完！！！）
>
> （真正的下单流程涉及的系统绝对在10个以上（主流电商），越大的越多。）
>
> 那链路长了就慢了，但是我们发现上面的流程其实可以**同时做**的呀，你支付成功后，我去校验优惠券的同时我可以去增减积分啊，还可以同时发个短信啊。
>
> 那正常的流程我们是没办法实现的呀，怎么办，**异步**。
>
> 你对比一下是不是发现，这样子最多只用100毫秒用户知道下单成功了，至于短信你迟几秒发给他他根本不在意是吧。
>
> **说到异步，那我用线程，线程池去做不是一样的么？**

> ### **解耦**
>
> 因为用线程去做，要写代码：
>
> 你一个订单流程，你扣积分，扣优惠券，发短信，扣库存。。。等等这么多业务要调用这么多的接口，**每次加一个你要调用一个接口然后还要重新发布系统**，写一次两次还好，写多了你就说：老子不干了！
>
> 而且真的全部都写在一起的话，不单单是耦合这一个问题，你出问题排查也麻烦，流程里面随便一个地方出问题搞不好会影响到其他的点。
>
> 但是你用了**消息队列**，耦合这个问题就迎刃而解了呀。
>
> 你下单了，你就把你**支付成功的消息告诉别的系统**，他们收到了去处理就好了，你只用走完自己的流程，把自己的消息发出去，那后面要接入什么系统简单，直接订阅你发送的支付成功消息，你支付成功了我**监听就好了**。
>
> **你下单了积分没加，优惠券没扣怎么办？**
>
> 这其实是用了消息队列的一个缺点，涉及到**分布式事务**的知识点，我下面会提到。

> ### **削峰：**
>
> 拿秒杀系统来说，平时流量很低，但是你要做秒杀活动00 ：00的时候流量疯狂怼进来，你的服务器，**Redis**，**MySQL**各自的承受能力都不一样，你直接**全部流量照单全收**肯定有问题啊，直接就打挂了。
>
> **那怎么办？** 
>
> 简单，把请求放到队列里面，然后至于每秒消费多少请求，就看自己的**服务器处理能力**，你能处理5000QPS你就消费这么多，可能会比正常的慢一点，但是**不至于打挂服务器**，等流量高峰下去了，你的服务也就没压力了。

### 3、使用消息队列有什么缺点？

三个点介绍他主要的缺点：

**系统复杂性**

> 本来蛮简单的一个系统，代码随便写都没事，现在你凭空接入一个中间件在那，要考虑去维护他，而且使用的过程中是不是要考虑各种问题，比如消息**重复消费**、**消息丢失**、**消息的顺序消费**等等，反正用了之后就是贼烦。

**数据一致性**

> 这个其实是分布式服务本身就存在的一个问题，**不仅仅是消息队列的问题**，但是放在这里说是因为用了消息队列这个问题会暴露得比较严重一点。
>
> **所有的服务都成功才能算这一次下单是成功的**，那怎么才能保证数据一致性呢？
>
> **分布式事务**：把下单，优惠券，积分。。。都放在一个事务里面一样，要成功一起成功，要失败一起失败。

**可用性**

> 你搞个系统本身没啥问题，你现在突然接入一个中间件在那放着，万一挂了怎么办？我下个单**MQ挂了**，优惠券不扣了，积分不减了呢？
>
> 因此还要研究怎么保证高可用。

参考：知乎，作者：敖丙 https://www.zhihu.com/question/321144623/answer/1021886547



### 4、如何保证消息队列的高可用？

> RabbitMQ 是**基于主从**（非分布式）做高可用性的。
>
> RabbitMQ 有三种模式：单机模式、普通集群模式、镜像集群模式。



> #### 普通集群模式（无高可用性）
>
> 在多台机器上启动多个 RabbitMQ 实例，每个机器启动一个。你**创建的 queue，只会放在一个 RabbitMQ 实例上**，但是每个实例都同步 queue 的元数据（元数据可以认为是 queue 的一些配置信息，通过元数据，可以找到 queue 所在实例）。你消费的时候，实际上如果连接到了另外一个实例，那么那个实例会从 queue 所在实例上拉取数据过来。
>
> **问题**：
>
> **没做到所谓的分布式**，就是个普通集群。因为这导致你要么消费者每次随机连接一个实例然后拉取数据，要么固定连接那个 queue 所在实例消费数据，前者有**数据拉取的开销**，后者导致**单实例性能瓶颈**。
>
> **没有什么所谓的高可用性**，**这方案主要是提高吞吐量的**，就是说让集群中多个节点来服务某个 queue 的读写操作。

> #### 镜像集群模式（高可用性）
>
> 这种模式，才是所谓的 RabbitMQ 的高可用模式。跟普通集群模式不一样的是，在镜像集群模式下，你创建的 queue，无论元数据还是 queue 里的消息都会**存在于多个实例上**，就是说，每个 RabbitMQ 节点都有这个 queue 的一个**完整镜像**，包含 queue 的全部数据的意思。然后每次你写消息到 queue 的时候，都会自动把**消息同步**到多个实例的 queue 上。
>
> **好处**：
>
> 任何一个机器宕机了，没事儿，其它机器（节点）还包含了这个 queue 的完整数据，别的 consumer 都可以到其它节点上去消费数据。
>
> **坏处**：
>
> 第一，性能开销也太大，消息需要同步到所有机器上，导致网络带宽压力和消耗很重！
>
> 第二，不是分布式的，就**没有扩展性可言**。

### 5、如何避免消息重复消费？

> 用 MQ 有个基本原则，就是**数据不能多一条，也不能少一条**
>
> 重复消费，本质上还是问你**使用消息队列如何保证幂等性**，这个是你架构里要考虑的一个问题。
>
> Kafka 实际上有个 offset .

> 其实重复消费不可怕，可怕的是你没考虑到重复消费之后，**怎么保证幂等性**。
>
> 举个例子吧。假设你有个系统，消费一条消息就往数据库里插入一条数据，要是你一个消息重复两次，你不就插入了两条，这数据不就错了？但是你要是消费到第二次的时候，自己判断一下是否已经消费过了，若是就直接扔了，这样不就保留了一条数据，从而保证了数据的正确性。
>
> 一条数据重复出现两次，数据库里就只有一条数据，这就保证了系统的幂等性。
>
> 幂等性，通俗点说，就一个数据，或者一个请求，给你重复来多次，你得确保对应的数据是不会改变的，**不能出错**。
>
> 所以第二个问题来了，怎么保证消息队列消费的幂等性？
>
> 其实还是得结合业务来思考，我这里给几个思路：
>
> - 比如你拿个数据要写库，你先根据主键查一下，如果这数据都有了，你就别插入了，update 一下好吧。
> - 比如你是写 Redis，那没问题了，反正每次都是 set，天然幂等性。
> - 比如你不是上面两个场景，那做的稍微复杂一点，你需要让生产者发送每条数据的时候，里面加一个全局唯一的 id，类似订单 id 之类的东西，然后你这里消费到了之后，先根据这个 id 去比如 Redis 里查一下，之前消费过吗？如果没有消费过，你就处理，然后这个 id 写 Redis。如果消费过了，那你就别处理了，保证别重复处理相同的消息即可。
> - 比如基于数据库的唯一键来保证重复数据不会重复插入多条。因为有唯一键约束了，重复数据插入只会报错，不会导致数据库中出现脏数据。

如何保证消息的可靠性传输？或者说，

### 6、如何处理消息丢失的问题？

> 或者说，如何保证消息的可靠性传输？

> #### 生产者弄丢了数据
>
> + 用 RabbitMQ 提供的事务功能;
> + 开启 `confirm` 模式
>
> **不同**：**事务机制是同步的**，但是 `confirm` 机制是**异步**的，你发送个消息之后就可以发送下一个消息。事务机制（同步）一搞，基本上**吞吐量会下来，因为太耗性能**。
>
> #### RabbitMQ 弄丢了数据
>
> **开启 RabbitMQ 的持久化**，就是消息写入之后会持久化到磁盘，哪怕是 RabbitMQ 自己挂了，**恢复之后会自动读取之前存储的数据**，一般数据不会丢。除非极其罕见的是，RabbitMQ 还没持久化，自己就挂了，**可能导致少量数据丢失**，但是这个概率较小。
>
> #### 消费端弄丢了数据
>
> RabbitMQ 如果丢失了数据，主要是因为你消费的时候，**刚消费到，还没处理，结果进程挂了**，比如重启了，那么就尴尬了，RabbitMQ 认为你都消费了，这数据就丢了。
>
> 必须关闭 RabbitMQ 的自动 `ack`，通过 api 来调用，然后每次你自己代码里确保处理完的时候，再在程序里 `ack` 一把。

### 7、如何保证消息的顺序性？

> **RabbitMQ**
>
> 在 MQ 里面创建多个 queue，同一规则的数据（对唯一标识进行 hash），有顺序的放入 MQ 的 queue 里面，消费者只取一个 queue 里面获取数据消费，这样执行的顺序是有序的。或者还是只有一个 queue 但是对应一个消费者，然后这个消费者内部用内存队列做排队，然后分发给底层不同的 worker 来处理。
>
> **kafka**
>
> 在消费端使用内存队列，队列里的数据使用 hash 进行分发，每个线程对应一个队列，这样可以保证数据的顺序。

### 8、如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时，说说怎么解决？

> 这个问题一般不出，出了就是大问题。
>
> 看其本质：**可能你的消费端出了问题，不消费了；或者消费的速度极其慢。**
>
> 例如：消费端每次消费之后要写 mysql，结果 mysql 挂了，消费端 hang 那儿了，不动了；或者是消费端出了个什么岔子，导致消费速度极其慢。

> **大量消息在 mq 里积压了几个小时了还没解决**
>
> **解决**：解决消费端问题，然后消费掉积压的数据。
>
> 两种方式消费掉积压的数据：
>
> + **原先部署的架构**花费**几个小时**恢复到原来的状态；
> + 临时将 queue 资源和 consumer 资源扩大 10 倍，以正常的 10 倍速度来消费数据。等消费完积压的数据消费完之后，在恢复原先部署的架构，速度快。

> **mq 中的消息过期失效了**
>
> **问题分析：**消息在 queue 中积压超过一定的时间（过期时间TTL）就会被 RabbitMQ 给清理掉，这个数据就没了。就是数据丢失。
>
> **批量重导**：大量积压的时候，直接丢弃数据，然后等过了高峰期以后，写程序，将丢失的那批数据，写个临时程序，一点一点的查出来，然后重新灌入 mq 里面去，把丢的数据给他补回来。

> **mq 都快写满了**
>
> 类似上面的问题，临时写程序，接入数据来消费，**消费一个丢弃一个，都不要了**，快速消费掉所有的消息。然后走第二个方案，到了过了高峰期再补数据吧。