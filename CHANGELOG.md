# key-switch
2017-06-26
    分卷脚本 timestamp 加入到 head
    g-netlogact-timestamp-search-replace.groovy
    
2017-06-24

1.^A 的实际编码为\001 ；如何把文件中的tab分隔符CRTL+A,也就是\001,换成 tab分隔符?

    PIG中输入输出分隔符默认是制表符\t，而到了hive中，默认变成了八进制的\001， 也就是ASCII： 
    
    sed 's/\o001/\t/g' yourfile 
    tr '\001' '\t' < yourfile
    
    

2017-06-23

增加 gson 支持

    search-replace 支持转换为 json 数据格式；
    search-replace 支持转换为 redis-lua 脚本，支撑 flume-redis 通用组件。
    
在凌晨1点-4点进行数据清理;保留4天的数据
    
        
    StringBuffer sb1 = new StringBuffer("redis.call('ZADD',KEYS[2],KEYS[1],KEYS[3]);");
    
    //println curhour
    //凌晨进行数据清理 0-4点
    String curhour = new Date().format('HH')
    if (curhour.toInteger() >= 0 && curhour.toInteger() <= 4) {
    
        sb1.append("redis.call('ZREMRANGEBYSCORE',KEYS[2],0,KEYS[4])");
    
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DATE, date.get(Calendar.DATE) - 7);
        println date.format('yyyyMMddHHmmss')
        split = Arrays.copyOf(split, split.length + 1);
        split[4] = date.format('yyyyMMddHHmmss');
    }
   
    

2017-06-21
    增加 redis pool 调用示例
    在 IDE 直接运行脚本测试,再到 Flume 进行联调；
    完成flume-groovy-redis联调；
    
2017-06-20
    加入 redis 支持，便于进行规则计算。
    可以整合 RocksDB涉及到 key 的计算；嵌入式，外部数据更新走 web 接口。todo
    
2016-01-03
    更改项目名称；
    
    
2016-12-16 完成flume 正则拦截器的替换

Search and Replace Interceptor；--> RuleSearchAndReplaceInterceptor$Builder
Regex Filtering Interceptor；  -->RuleFilteringInterceptor$Builder
Regex Extractor Interceptor；-->RuleSearchAndReplaceInterceptor$Builder
---------------------filter dsl
println head
println body

return true

---------------------search replace dsl
println head
println body
body=body.replace('a','aaa')
head["newhead"]='abcd'

def resultMap = [:]

resultMap["head"]=head
resultMap["body"]=body

return resultMap



a1.sources.r1.interceptors = i1 i2 i3
a1.sources.r1.interceptors.i1.type = regex_extractor
a1.sources.r1.interceptors.i1.regex = (.+):(.+):(.+):(.+)
a1.sources.r1.interceptors.i1.serializers = s1 s2 s3 s4
a1.sources.r1.interceptors.i1.serializers.s1.name = routing-key
a1.sources.r1.interceptors.i1.serializers.s2.name = starttime
a1.sources.r1.interceptors.i1.serializers.s3.name = endtime
a1.sources.r1.interceptors.i1.serializers.s4.name = systime

#如果excludeEvents设为true，则表示过滤掉以lxw1234开头的events。
#在使用 Regex Filtering Interceptor的时候一个属性是excludeEvents
#当它的值为true 的时候，过滤掉匹配到当前正则表达式的一行
#当它的值为false的时候，就接受匹配到正则表达式的一行

#a1.sources.r1.interceptors = i2
a1.sources.r1.interceptors.i2.type = com.supermy.flume.interceptor.RuleFilteringInterceptor$Builder
#通过脚本的返回值，数据是否向下传递 参数 body / head(json 格式) 可以引用
a1.sources.r1.interceptors.i2.rule = /etc/flume/conf/g-filter.groovy
a1.sources.r1.interceptors.i2.ruleName = filterGroovy

a1.sources.r1.interceptors.i3.type = com.supermy.flume.interceptor.RuleSearchAndReplaceInterceptor$Builder
#通过脚本的返回值，数据是否向下传递 参数 body / head(json 格式) 可以引用
a1.sources.r1.interceptors.i3.searchReplaceDsl = /etc/flume/conf/g-search-replace.groovy
a1.sources.r1.interceptors.i3.searchReplaceKey = searchReplaceGroovy


2016-12-14 完成拦截器项目的初始化
自定义拦截器，通用的groovy 脚本语言作为规则语音。
