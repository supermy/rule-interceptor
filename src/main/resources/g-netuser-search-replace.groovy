import com.google.gson.Gson

//数据加工为 lua 脚本，插入到 redis
println "netuser redis script 准备"
//println head
//println body

//body = "20170621162925,113.225.23.151,test_10056368,1"
body = "20170621162925,113.225.23.152,test_10056368,2"
// eval "return redis.call('ZADD','KEYS[1]',ARGV[1],ARGV[2])" 1 keyset   123  u123
//eval "return redis.call('ZREMRANGEBYSCORE',KEYS[2],0,KEYS[4])" 4 11 113.225.23.152 11 20170621162926
//eval "redis.call('ZADD',KEYS[2],KEYS[1],KEYS[3]);redis.call('ZREMRANGEBYSCORE',KEYS[2],0,KEYS[4])" 4 11 113.225.23.152 11 20170621162926

//获取系统当前时间
//获取7天谴的时间作为结束时间
//开始时间是0
//ZREMRANGEBYSCORE ip 0 max


def split = body.split(",")


def type = split[3]
if (type.substring(0, 1) == '1') {
    split[2] = split[2] + "@Start";
} else {
    split[2] = split[2] + "@End";
}



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


Gson gson = new Gson();

Map full = new HashMap();
full.put("script", sb1.toString());
full.put("args", new ArrayList());
full.put("keys", split);


String json = gson.toJson(full);
println json

//Map m=gson.fromJson(json, HashMap.class);
//println m


def resultMap = [:]

resultMap["head"] = head
resultMap["body"] = json

return resultMap