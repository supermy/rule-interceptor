
//import groovy.json.JsonSlurper
//
//def (value1, value2) = '1128-2'.tokenize('-')
//println value1
//println value2
//
//def jsonSlurper = new JsonSlurper();
//def object = jsonSlurper.parseText(p2);
//
//assert object instanceof Map;
//assert object.name == 'James Mo';
//
//println p1;
//println p2;
//
//
//
//return object.name == 'James Mo';

import redis.clients.jedis.*
import redis.clients.jedis.exceptions.JedisConnectionException

Jedis jedis = new Jedis("localhost")

jedis.set("foo", "bar")
assert jedis.get("foo") == "bar"



// let's make Jedis a little more groovy and call get/set through property accessors
Jedis.metaClass.getProperty = { String name ->
    delegate.get(name)
}

Jedis.metaClass.setProperty = { String name, value ->
    delegate.set(name, value)
}

Jedis jedis = new Jedis("localhost")

// get/set basic String values
jedis.name = "Ted Naleid"

assert jedis.name == "Ted Naleid"



jedis.beanCounter = "4"
assert jedis.incr("beanCounter") == 5
assert jedis.incr("beanCounter") == 6
jedis.del("beanCounter")
assert jedis.incr("beanCounter") == 1

jedis.notAnInteger = "foo"

try {
    jedis.incr("notAnInteger")
    throw new Exception("shouldn't get here, need an integer to increment")
} catch (redis.clients.jedis.JedisException e) {
    assert e.message == "ERR value is not an integer or out of range"
}


jedis."expire:me" = "value to expire"
jedis.expire("expire:me", 60)
assert jedis.ttl("expire:me") > 50

// -1 means "never expire"
assert jedis.ttl("brand new value") == -1


jedis.rpush("alphabet", "Bravo")
jedis.rpush("alphabet", "Charlie")

jedis.lpush("alphabet", "Alpha")

assert jedis.lrange("alphabet", 0, -1) == ["Alpha", "Bravo", "Charlie"] // whole list
assert jedis.lrange("alphabet", 0, -2) == ["Alpha", "Bravo"] // all but last item
assert jedis.lrange("alphabet", 0,  1) == ["Alpha", "Bravo"]  // first and 2nd items
assert jedis.lrange("alphabet", 1,  2) == ["Bravo", "Charlie"] // 2nd and 3rd items

assert jedis.llen("alphabet") == 3



// pop the first item off the list
assert jedis.lpop("alphabet") == "Alpha"

// pop the last item off the list
assert jedis.rpop("alphabet") == "Charlie"

// only one thing left
assert jedis.llen("alphabet") == 1
assert jedis.lrange("alphabet", 0, -1) == ["Bravo"]

// del deletes the value associated with the key (no matter the type)
jedis.del("alphabet")
// geting the whole range for a key that doesn't exist safely returns an empty list
assert jedis.lrange("alphabet", 0, -1) == []


// add a bunch of items to the palindromes set
jedis.sadd("palindromes", "radar")
jedis.sadd("palindromes", "noon")
jedis.sadd("palindromes", "kayak")

assert jedis.smembers("palindromes").containsAll("radar", "noon", "kayak")

// check to see if an item is a member of a set
assert jedis.sismember("palindromes", "kayak") == true

// remove a member from the set
jedis.srem("palindromes", "kayak")

assert jedis.sismember("palindromes", "kayak") == false

// set size (cardinality)
assert jedis.scard("palindromes") == 2

jedis.sadd("acronyms", "scuba")
jedis.sadd("acronyms", "radar")

// set union
assert jedis.sunion("palindromes", "acronyms").containsAll("scuba", "noon", "radar")

// set intersection
assert jedis.sinter("palindromes", "acronyms").containsAll("radar")

// set difference
assert jedis.sdiff("palindromes", "acronyms").containsAll("noon")
assert jedis.sdiff("acronyms", "palindromes").containsAll("scuba")


// add months to a sorted set in random order
jedis.zadd("months", 1,  "January")
jedis.zadd("months", 12, "December")
jedis.zadd("months", 5,  "May")
jedis.zadd("months", 9,  "September")
jedis.zadd("months", 3,  "March")
jedis.zadd("months", 10, "October")

// the sorted set returns them in sorted order and can be sliced similarly to lists
assert jedis.zrange("months", 0, -1).toArray() == ["January", "March", "May", "September", "October", "December"]
assert jedis.zrange("months", 3, -2).toArray() == ["September", "October"]


jedis.hset("book:1", "title", "Dune")
jedis.hset("book:1", "author", "Frank Herbert")
jedis.hset("book:1", "yearPublished", "1965")

assert jedis.hgetAll("book:1") == [author: "Frank Herbert", title: "Dune", yearPublished: "1965"]


Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
//Jedis Cluster will attempt to discover cluster nodes automatically
jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7379));
JedisCluster jc = new JedisCluster(jedisClusterNodes);
jc.set("foo", "bar");
String value = jc.get("foo");



logger.info("jedisPool test");
JedisPoolConfig config = new JedisPoolConfig();
config.setMaxTotal(12);
JedisPool pool = new JedisPool(config, "localhost", 6379);
Jedis jedis = pool.getResource();
//		jedis.set("test", "hoge");
jedis.rpush("test", "1");
jedis.rpush("test", "2");
jedis.rpush("test", "3");
jedis.rpush("test", "4");
jedis.rpush("test", "5");
jedis.rpush("test", "6");
logger.info("result:" + jedis.lrange("test", 0, -1));


//synchronized
JedisPool jedisPool;
public synchronized  Jedis getJedisConnection(String host,Integer port) {		// koushik: removed static
    try {
        if (jedisPool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(1000);
            config.setMaxIdle(10);
            config.setMinIdle(1);
            config.setMaxWaitMillis(30000);

            jedisPool = new JedisPool(config, host, port);

        }
        return jedisPool.getResource();
    } catch (JedisConnectionException e) {

        e.printStackTrace();
        throw e;
    }
}

try {

//Jedis jedis = new Jedis("172.20.149.158");
    Jedis jedis = getJedisConnection("172.20.149.158",6379);


    jedis.set("foo", "bar1")
    assert jedis.get("foo") == "bar1"

    System.out.println("88888888");
} catch(Exception e1) {
    e1.printStackTrace();

}

