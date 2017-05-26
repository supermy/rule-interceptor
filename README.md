# flume-interceptor-ex

[![Build Status](https://travis-ci.org/supermy/rule-interceptor.svg?branch=master)](https://github.com/supermy/rule-interceptor)

##介绍

可以数据采集端，利用采集设备的能力对数据进行非业务类 进行实时的 ETL 处理；适用于 hadoop 大数据生态的各种通道环境；特色是采用 groovy 作为规则语言，能够完成各种条件的处理。

flume 全能版本拦截器 [flume-rule-interceptor](https://github.com/supermy/rule-interceptor). groovy 作为规则dsl,通过groovy 脚本编写逻辑，支持JSON 格式函数，脚本动态加载，灰度发布。

业务场景1：RuleSearchAndReplaceInterceptor， 互联网算进行数据传输的安全，通过拦截器进行加密解密；官方原有的正则不能实现此功能。

业务场景2：RuleFilteringInterceptor， 数据条件过滤，可以通过groovy 脚本进行条件过滤，非常灵活；官方原有的正则不支持条件。

业务场景3：RuleSearchAndReplaceInterceptor，数据格式变更，可以通过groovy 脚本进行数据格式转换；官方可以通过正则完成，效率较低。

业务场景3：RuleSearchAndReplaceInterceptor，定制head 属性，可以通过groovy 脚本配置head 属性；官方配置较为复杂，不能支持灵活业务。


## Install

安装:

    打包rule-interceptor-1.0-SNAPSHOT.jar拷贝到flume的plugins.d/flume-interceptor/lib目录
    拷贝groovy-all-2.4.7.jar到flume的plugins.d/flume-interceptor/libext目录

## Usage

    
    a1.sources.r1.interceptors.i2.type = com.supermy.flume.interceptor.RuleFilteringInterceptor$Builder
    a1.sources.r1.interceptors.i2.rule = /etc/flume/conf/g-filter.groovy
    a1.sources.r1.interceptors.i2.ruleName = filterGroovy
    
    a1.sources.r1.interceptors.i3.type = com.supermy.flume.interceptor.RuleSearchAndReplaceInterceptor$Builder
    a1.sources.r1.interceptors.i3.searchReplaceDsl = /etc/flume/conf/g-search-replace.groovy
    a1.sources.r1.interceptors.i3.searchReplaceKey = searchReplaceGroovy
    
        

### g-filter.groovy

过滤脚本，可以使用head and body 的数据作为条件 判定词条数据是否过滤

```  groovy

        println head
        println body
        
        return true
```



### g-search-replace.groovy

替换脚本，可以更改head and body 的数据，适配不同的业务场景，脚本支持动态更新；

``` groovy

        import  com.supermy.flume.interceptor.*
        import javax.crypto.Cipher;
        import javax.crypto.spec.SecretKeySpec;
        
        println head
        println body
        body=body.replace('a','aaa')
        head["newhead"]='abcd'
        
        
        
        String text = "Body 的数据 , I Love BONC"
        
        //
        def key = new SecretKeySpec("123456789987654321".bytes, "AES")
        def c = Cipher.getInstance("AES")
        
        //加密
        c.init(Cipher.ENCRYPT_MODE, key)
        e_text = new String(Hex.encodeHex(c.doFinal(text.getBytes("UTF-8"))))
        
        //解密
        c.init(Cipher.DECRYPT_MODE, key)
        text1 = new String(c.doFinal(Hex.decodeHex(e_text.toCharArray())))
        
        println text
        println e_text
        println text1
        
        
        def resultMap = [:]
        
        //加密数据，用于互联网数据传输
        
        
        resultMap["head"]=head
        resultMap["body"]=body
        
        return resultMap

```