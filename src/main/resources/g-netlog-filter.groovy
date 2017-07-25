import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

//#当它的值为false 的时候，过滤掉匹配到当前正则表达式的一行
//#当它的值为true的时候，就接受匹配到正则表达式的一行
long s=System.currentTimeMillis();

println "netlog filter"
//println head
//println body
body = "2017-06-22 10:14:48.93858150001221.203.101.54000127.209.182.50001Mozilla/4.0 "

def split = body.split("0001")  //fixme

//def split = body.split("\\u0001")  //fixme  hive 分隔符号  \u0001
println split.size()


if(split.size()<4){
    //数据不合格过滤 网络日志数据至少有4列
    return false;
}


////非法 IP 地址过滤掉
//String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\$"
//Pattern pattern = Pattern.compile(ip);
//Matcher matcher = pattern.matcher(split[1]);
//if(!matcher.matches()){
//    //数据不合格过滤 网络日志数据至少有4列
//    println '非法IP';
//    return false;
//}

//非法日期
//String curdate = split[0].split("\\.")[0]
//String curstr = curdate.replaceAll(":","").replaceAll("-","").replaceAll(" ","");
//
//println curstr
//
////3daybefore 系统时间
//Calendar date = Calendar.getInstance();
////date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
////date.add(Calendar.DATE,-1);//1天谴
//date.add(Calendar.HOUR_OF_DAY,-1);//1个小时之前
////date.add(Calendar.MINUTE, -1);//1分钟前
//
//cur3daytime = date.format('yyyyMMddHHmmss')
//
//println cur3daytime
//
////3daybefore
//if (cur3daytime > curstr){
//    println "data time drop";
//    return false;
//}





//第一个 IP 地址作为终端 IP
def type = split[1]

long e=System.currentTimeMillis();

println(e-s)

if (type.substring(0,2) != '10') {
    return true;  //不过滤
} else {
    return false; //过滤
}
