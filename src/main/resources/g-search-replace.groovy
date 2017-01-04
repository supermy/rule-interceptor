import  com.supermy.flume.interceptor.*

import java.security.Key

println head
println body
body=body.replace('a','aaa')
head["newhead"]='abcd'

key = AESCoder.initSecretKey();
Key k = AESCoder.toKey(key)
enc = AESCoder.encrypt("abcd".getBytes(),k);
dec = AESCoder.decrypt(enc);

println "abcd"
println enc
println dec

def resultMap = [:]

//加密数据，用于互联网数据传输


resultMap["head"]=head
resultMap["body"]=body

return resultMap