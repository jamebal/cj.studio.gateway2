
# Gateway2.0

## 在1.0的基础上重构，

	Gateway即可用于开发网站项目，也可用于开发微服务项目，称之为网关应用，在作为分布式平台使用时，支持微服务的注册、发现、路由、断路重试等功能。它可以发展为N层分布式架构，层级由运维人员自建。
 	Gateway对于中小型互联网公司来说是一种福音，因为这类公司往往没有实力或者没有足够的资金投入去开发自己的分布式系统基础设施，使用Gateway一站式解决方案能在从容应对业务发展的同时大大减少开发成本。同时，随着近几年微服务架构和Docker容器概念的火爆，也会让Gateway在未来越来越“云”化的软件开发风格中立有一席之地，尤其是在目前五花八门的分布式解决方案中提供了标准化的、全站式的技术方案，意义可能会堪比当年Servlet规范的诞生，有效推进服务端软件系统技术水平的进步。
 	
 	cj.studio还有与之配套的以下产品:
 	
 	ecm开发工具包兼有spring、osgi、nodejs的功能。支持面向模块开发与部署，热插拔。 	
	net开发工具包,支持web的开发，并且可以完全使用js开发web应用，它的语法更接近于流行的nodejs，其它功能包含有基于netty的nio也包含有自行开发的nio框架rio,rio叫响应式nio框架，它即有nio的优势，又有同步等待响应的能力。
	plus开发工具包，进一步增强了连接的能力，如web应用的插件化（支持像eclipse这样的插件架构），支持像webservice这种远程服务的能力，支持云计算芯片的开发。
	netdisk是基于mongodb的增强开发工具包，它实现了网盘的各种功能，支持文件随机存取及结构化数据存取，支持多维，用户配额，开发上类似sql语法支持、对象映射支持。
	neuron工具，具有像tomcat/jetty等服务容器的功能，更多的是它具有向后连接的特性，是组建大型分布式神经网络的节点工具。它的目的就是组建神经网络集群。
	mdisk命令行工具，它是以命令行窗口实现的网盘工具，以netdisk为核心，方便mongodb的开发、测试和运维管理。它用起来非常简单，只要连到你的mongodb即可将mongodb当成网盘数据库，且对原mongodb的库不受影响。
	cjnet 用于调试neuron中的应用程序和netsite中的应用程序，它是一个cj studio产品系中有关net产品开发和调试必不可少的工具。
	netsite也是一个像tomcat/jetty等服务容器的命令行工具，它与神经元的区别在于，它只能部署在神经网络的终端，而不能成为其中间节点。它的优点在于，它可以部署成百上千个应用，而在一个神经元节点上一般不这么做。此工具暂时停止了升级。