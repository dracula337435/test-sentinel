# 试验sentinel

最好结合着```sentinel-dashboard```和```nacos```

启动```dashboard```，命令如下
```
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```
其中后半部分“-Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard”是为了```dashboard```把自己注册到```dashaboard```上去  
把程序注册到```dashboard```，需增加启动参数
```
-Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=some-name
```