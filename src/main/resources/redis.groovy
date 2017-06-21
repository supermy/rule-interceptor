
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


import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.exceptions.JedisConnectionException

@Singleton
public class SingleRedis {

    //synchronized
    private JedisPool jedisPool;

    public Jedis getJedisConnection(String host, Integer port) {        // koushik: removed static
        try {
            if (jedisPool == null) {
                JedisPoolConfig config = new JedisPoolConfig();
                //config.setMaxTotal(100);
                config.setMaxIdle(50);
                config.setMinIdle(20);
                //config.setMaxWaitMillis(30000);

                jedisPool = new JedisPool(config, host, port);

            }
            return jedisPool.getResource();
        } catch (JedisConnectionException e) {

            e.printStackTrace();
            throw e;
        }
    }

    //普通的方法
    public void singleMethor() {
        System.out.println("singleMethor");
    }

}

try {

//    Jedis jedis = new Jedis("172.20.149.158");
    Jedis jedis = SingleRedis.instance.getJedisConnection("172.20.149.158", 6379);


    jedis.set("foo", "bar1")
    assert jedis.get("foo") == "bar1"

    System.out.println("88888888");
} catch (Exception e1) {
    e1.printStackTrace();

}


