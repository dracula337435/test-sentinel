# 试验sentinel

最好结合着```sentinel-dashboard```和```nacos```

启动```dashboard```，命令如下
```
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```
把程序注册到```dashboard```，需增加启动参数
```
-Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=some-name
```

对接```nacos```后，参考```demo```即可配置好，此时```nacos```和```dashboard```均可实时修改规则  
在```nacos```修改后可见日志类似于：
```
2019-08-09 17:02:21.488  INFO 18180 --- [-localhost_8848] c.a.n.client.config.impl.ClientWorker    : [fixed-localhost_8848] [polling-resp] config changed. dataId=test-sentinel, group=DEFAULT_GROUP
2019-08-09 17:02:21.495  INFO 18180 --- [-localhost_8848] c.a.n.client.config.impl.ClientWorker    : [fixed-localhost_8848] [data-received] dataId=test-sentinel, group=DEFAULT_GROUP, tenant=null, md5=4f5643ac25a487a2caba34cd56d7c2f6, content=[
  {
    "resource": "HelloWorld",
    "controlBehavior": 0,
    "count": 99.0,
    "grade": 1,
   ...
2019-08-09 17:02:21.495  INFO 18180 --- [-localhost_8848] c.a.nacos.client.config.impl.CacheData   : [fixed-localhost_8848] [notify-listener] time cost=0ms in ClientWorker, dataId=test-sentinel, group=DEFAULT_GROUP, md5=4f5643ac25a487a2caba34cd56d7c2f6, listener=com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource$1@212f6381 
2019-08-09 17:02:21.496  INFO 18180 --- [update-thread-1] c.a.nacos.client.config.impl.CacheData   : [fixed-localhost_8848] [notify-ok] dataId=test-sentinel, group=DEFAULT_GROUP, md5=4f5643ac25a487a2caba34cd56d7c2f6, listener=com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource$1@212f6381 
```
在```dashboard```修改，在```nacos```不会有体现；反之可以  

见[sentinel的github-wiki中启动配置项的说明](https://github.com/alibaba/Sentinel/wiki/%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E9%A1%B9)，可知：
>参数```csp.sentinel.api.port```为```本地启动 HTTP API Server 的端口号```  
可不提供，默认为 8719，若端口冲突会自动向下探测可用的端口。

示例启动命令
```
java -jar -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=test-sentinel -Dserver.port=8082 test-sentinel-1.0-SNAPSHOT.jar
```
若不指定```server.port```，默认端口号为```8081```  
示例端点为```/switchLoop```，返回开关是否打开```true/false```。若为```true```，则在约1s内调用```HelloWorld```资源20次  

还发现，如果没有发交易，```sentinel-dashboard```左侧的应用列表中是没有的