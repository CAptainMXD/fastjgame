### fastjgame
fastjgame 为 fast java game framework的缩写，如名字一样，该项目的目标是一个高性能，高稳定性的游戏架构。  
1. 它将是一个**分布式多进程多线程**架构，它有着优秀的多线程模型，兼具简单性和高性能。-- 结合了**Netty**与**Disruptor**两者的优势，诞生了**DisruptorEventLoop**。  
2. 高性能的网络层: 采用protoBuf实现自定义二进制协议，**体积小，编解码速度快**，再辅以代码生成 -- **简单极速的rpc调用**。
3. 代码自动生成: RpcService、Subscribe等相应代码自动生成。代码生成一时爽，一直生成一直爽。

### 暂不建议fork
由于项目处于前期，代码改动频繁，甚至很多设计都会推翻，因此暂时不建议fork。

***
### [历史重要更新](https://github.com/hl845740757/fastjgame/blob/master/%E5%8E%86%E5%8F%B2%E9%87%8D%E8%A6%81%E6%9B%B4%E6%96%B0.MD)

***
### 多线程框架的好与坏
关于多线程的游戏服务器架构，不同公司，不同项目的设计很可能都是不一样的，这很正常。
每个人的技术栈可能不一样，编程思想也有很大差别，游戏需求也不一样，
此外多线程的设计往往涉及很多取舍，每个人都有自己的取舍，因此诞生出各种各样的框架。   

一个框架的好坏不能只看某个方面，没有完美的框架，一个框架最重要的是适合自己。
虽然存在众多框架，但不一定能找到一个适合自己的框架，甚至找到具有参考价值的都不容易。  

我可能会表露我不喜欢什么样的设计，这不代表我的设计就一定比它好！  
记住：**取舍**！

#### 我坚持什么？
我不会追求极致的性能，我会尽量保持编程模型的简单性，同时尽量地拓展性能。  

#### 为什么？
编程模型如果不够简单，对于有经验的开发人员来说,容易陷入解决各种各样的复杂问题中，花在这上面的时间很可能超过他们做真正有意义的事情的时间，开发效率太低；  
而对于普通程序员，甚至可能都不知道问题的存在，导致无数的潜在bug！  
总的来说：复杂的编程模型，导致编程效率较低，且正确性很难保证！  

举个栗子：  
纯异步编程模型，当方法调用链过长的时候是个灾难，当提供回调机制的时候，更加恐怖，它可能产生内存泄漏，信号丢失，以及其它逻辑错误。  
建议：使用回调机制时，不要乱传递数据（不要乱捕获数据）。  

总的来说：  
    复杂的编程模型对团队的成员提出了更高的要求，只有团队成员都足够优秀的时候，才能保证正确性和开发效率，它可能是为了极致的性能，也可能是为了其它。  
而我往往对团队的成员并不充满信心，一个团队良莠不齐很正常，多数人是普通人，优秀的人少。因此我选择舍弃一部分性能，换得更简单的编程模型，以保证开发效率和正确性。
更简单的编程模型，更容易保证正确性，以及程序的健壮性。

#### 哪些模块适合多线程化
游戏架构的各个模块其实都可以多线程化，但是最重要的是场景服多线程化，场景服更容易遇见性能瓶颈。
至于其它模块，如果没有太大压力，单线程就足够了。

#### 如何多线程化？
对于这个问题，不同公司、不同项目的解决方案不一样。如：  
+ **按照业务逻辑拆分为独立的service**，一个service在其生命周期内绑定在固定线程上，不同service之间的线程关系是不确定的。
> 1. 注意：每一个场景也是一个service。  
> 2. 数据共享：但是玩家只有在同一个场景下时，数据才是直接可用的，可能会限制部分功能实现（涉及玩家之间交互的）。  
拿交易系统举例，只有玩家在同一个场景时，这个操作才是允许的。比如：这个进程虽然承载了5000人，但是玩家的交易范围可能只有99人，单个场景内玩家数很有限。
> 3. 性能提升：它能提升较多的性能，能很好的利用多核的优势。
> 4. 可以ALL IN ONE（一个进程启动所有类型服务器），也可以独立部署（每个进程只启动一种类型服务器）；硬件资源利用率很高。 
> 5. 关键问题：由于service太多，且service之间数据不是直接可用的，导致编程困难，开发效率很低，且安全性难以保证！！！
> 6. 我不推荐这样的模型！这种模型其实更适合做一般的企业应用或web服务器，因为这些应用service数量较少，且service之间一般只有很少的交互。
> 而对于游戏而言，service数量很多，且service之间交互很多，service之间涉及大量的数据共享，要解决这种问题，要么加锁处理竞争，要么使用消息进行通信（异步化），无论哪一种都会大大加重开发人员的负担。
> （如果你在开发的时候感觉很轻松，那么你可能没有认真的处理异步问题，或者根本没有意识到有哪些问题）  
> ps: 只有当service之间是独立的时候，才有最大的并发性能，当service之间存在大量依赖的时候，其实是不适合多线程化的。  
> 其实类似的还有actor模式，actor数量爆炸的时候，带来的就不是优势了。

+ **按执行阶段拆分为单线程阶段、多线程阶段**。服务器逻辑分为事件驱动(网络事件)和心跳驱动，其中事件处理必须是单线程的，而**心跳**有机会拆分为单线程执行阶段和多线程执行阶段。
对于MMO游戏而言，**场景**是一个很好的隔离单位！场景内的心跳逻辑大多只与场景内的数据有关，而场景心跳却是场景服务器耗时最多的部分，解决了该部分，性能瓶颈基本也就解决了。
> 1. 注意：该架构要求多线程阶段执行的逻辑一定不能修改(场景)外部数据，可读、但是不能写。 eg：如果AI在多线程阶段，那么AI中的逻辑一定不能修改场景外部数据。
> 2. 执行流程：可参考FORK JOIN模式。主线程执行单线程心跳后，将后续的心跳逻辑提交为**任务**交给线程池执行（主线程也可以参与执行），然后阻塞到心跳任务全部完成(否则会导致线程安全问题)，然后主线程继续执行。
> 3. 数据共享：可以做到像单线程架构一样一个进程下的玩家数据都是可以互相访问的，玩家的体验也是最好的！且能支持远多于单线程架构的负载！这是一个很明显的优势。  
拿交易系统举例：如果一个进程表示一个大频道，那么允许该频道的玩家之间交易。这是其它模型不能办到的！
比如：这个进程承载了5000人，那么玩家的交易范围就是4999人。
注意：处理业务逻辑的时候，涉及数据共享的逻辑，必须放在单线程阶段(或本身是线程安全的)！ 
> 4. 性能提升：对场景服务器的性能提升是非常显著的，但是子线程在单线程执行阶段是空闲的！
> 5. 必须独立部署，无法ALL IN ONE；资源全部以进程为单位分配，硬件资源浪费严重（内存、cpu）。
> 6. 关键问题：对于场景服务器，场景内的逻辑开发并不容易，需要专人维护！对于其它服务器，能安全的进行并行的逻辑可能不太多。
> 7. 这个模型我当初想过尝试，该模型还是很有诱惑力的，但是最终没进行（我可能还是不太想一个场景内既有单线程代码，又有多线程代码，可能是我也hold不住吧）。。。

#### 我的思路？
+ 我的思路最早是源于Netty的EventLoop，但是把它真正用到游戏中还是花了很长时间。它的模型大概如下：  
> 1. 一个进程分为多线程模块和单线程模块。
> 2. 多线程模块用于支持EventLoopGroup内的所有EventLoop，主要目的是为了减少系统资源（内存、网络、cpu）占用,提高硬件资源利用率的。  
只有那些必须优化的部分才做到多线程模块中。eg：网络资源、DB资源、配置表、地图遮挡信息。  
如果没有逻辑提取到多线程模块中，那么单个线程其实就对应了常见的**多进程单线程**架构中的单个进程。也就是说该模型是多进程单线程模型的一种改进。
> 3. 单线程模块即单个EventLoop，也是单个World；线程(world)之间是独立同构的；日常开发都是在单线程模块中。
> 4. 数据共享：玩家在同一个world的时候，数据才是直接可用的，基本和单线程架构是一样的。  
拿交易系统举例：如果一个进程承载5000人，进程由5个场景world构成，每个world承载1000人，一个world代表一个小频道/分线，那么玩家的交易范围就是999人。
> 5. 性能提升：可以很好的提升性能，能很好的利用多核的优势。
> 6. 关键问题：在相同进程数量的时候，它有着相当多的world，这些world的管理、消息通信是不小的消耗。  
> 7. 可以ALL IN ONE，也可以独立部署；硬件资源利用率很高。
> 8. 它有着清晰的编程边界，编码难度基本与单线程架构一致，只有重点优化部分是多线程的，此外它可以很容易的退回到单线程模型。 
> 9. 如果游戏允许(显式或隐式地)拆分较多的小频道/分线，那么这种模型才会有较大的优势。

#### 总的来说
1. 划分的太细时：当游戏逻辑较复杂时，各个小的模块之间涉及大量的数据共享。
要解决这种问题，要么加锁处理竞争，要么使用消息进行通信（异步化），无论哪一种都会大大加重开发人员的负担。
这也是我极不推荐第一种划分方式的原因。
2. 划分的太粗糙时：每个线程可能存在不必要的任务，造成性能浪费。性能无法最大化。
我的建议是，可以适当的粗糙一点，而不要追求尽量的小。
3. 选择必须符合游戏需求，不能强制让游戏按照架构设计！需求决定架构，而不是架构决定需求。
4. 我不推荐纯异步的编程模型(使用消息交互)，也不推荐纯粹的多线程编程模型，推荐在确定边界内使用同步编程。
5. 我的实现：一个进程内的线程是**独立且同构的**，且每个线程**只承载部分区域**。比如某个玩法人数特别多，那么这个world便可以只启动这个玩法所在的区域。

#### 多线程化需要注意的一些问题
1. 缓存。在多线程模型下慎用全局缓存（静态变量），如果要使用缓存，建议优先考虑ThreadLocal。
2. 为了减少开发人员的错误，建议单个线程下只能获取到自己能用的数据。这要求数据存储的位置要精心设计。
3. 想到再说...
***

### 高性能易使用的网络层  
1. IO框架为Netty,HttpClient为OkHttp3;   
2. 支持断线重连/防闪断，支持websocket和tcp同时接入。  
3. 支持自定义协议解析（实现**ProtocolCodec**）。
4. 采用protoBuf实现自定义二进制协议，**体积小，编解码速度快**，再辅以代码生成 -- **简单极速的rpc调用**。
5. 支持**双向的单向消息，异步rpc调用，同步rpc调用。**
6. JVM内线程通信与跨进程通信具有几乎完全相同的API。你可以向另一个**进程**发起RPC请求，也可以向另一个**线程**发起RPC请求。

#### 服务器的节点发现
* 基于zookeeper实现，同时zookeeper作为配置中心，以及分布式锁.  
  zookeeper的配置在**game-start**的doc文件夹下可以找到。
  如果你想使用中文配置zkui，请使用我修正后的[zkui](https://github.com/hl845740757/zkui)，内部有可运行jar包，你也可以自己编译一遍。
***

#### 分布式架构下玩家数据库如何更新？
我知道的有两种：
1. 场景服务器直接操作数据库。
2. 场景服不直接操作数据库，而是将数据同步给另一个服务器(DB服务器/中心服务器)，由DB服务器完成入库。  
不论哪一种，都可能产生时序问题。原因:如果玩家在切换场景之后，改变了线程，且上一个线程的数据库更新操作还未完成（或数据还未到达DB服务器），则可能产生时序问题。

#### 如何解决时序问题？
我的初步设想：保证玩家**真正离开场景（当前线程）之前，数据库更新已完成(或数据到达了DB服务器)。**这样便保证了只有最新场景(线程)会操作该玩家的数据。  
那么玩家切换场景流程如下：
> 1. 从场景中删除玩家，玩家离开场景，将玩家挂起，此时禁止访问玩家数据，也不响应客户端请求。
> 2. 将玩家数据序列化，发起DB更新请求，如果是异步方式，则需要添加回调。
> 3. 等待数据库操作完成。
> 4. 删除玩家对象，开始进入新场景。

优点：逻辑足够简单，容易保证安全性。
缺陷：如果一个玩家要保存的数据较多，那么玩家切换场景的时间可能较长。

***
#### 如何使用注解处理器？
1. 将game-auto添加到project-structure
2. install game-auto 到本地仓库
3. 将game-auto从项目project-structure中移除，注解处理器必须以jar包形式工作。
4. 在game-parent下clean，再compile，可消除缺少类文件的报错。

#### 编译出现找不到符号问题怎么解决？
1. 确保注解处理器已install到本地仓库。
2. clean
3. compile

#### 为什么开源
1. 开源的高质量的源码太少，大多质量都太差，代码混乱，编程模型复杂。
2. 我个人非常看重游戏的创意，而这些创意很多时候在中小型游戏公司产生，而往往缺乏成熟可靠的技术。

#### 更新问题 
+ 由于要上班的，而且没有确切的需求，导致了很多东西无法继续进行，所以架构可能会不停的优化更新，但是业务逻辑可能进展很慢。

(Markdown语法不是很熟悉，排版什么的后期有空再优化~)

***