# key-switch
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
