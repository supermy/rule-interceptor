import groovy.json.JsonSlurper

def (value1, value2) = '1128-2'.tokenize('-')
println value1
println value2

p1 = "123"
p2 = '{"name":"James Mo"}'
def jsonSlurper = new JsonSlurper();
def object = jsonSlurper.parseText(p2);

assert object instanceof Map;
assert object.name == 'James Mo';

println p1;
println p2;



return object.name == 'James Mo';