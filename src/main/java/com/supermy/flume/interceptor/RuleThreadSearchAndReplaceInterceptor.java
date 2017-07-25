/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.supermy.flume.interceptor;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import groovy.lang.Binding;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Interceptor that allows search-and-replace of event body strings using
 * regular expressions. This only works with event bodies that are valid
 * strings. The charset is configurable.
 * <p>
 * Usage:
 * <pre>
 *   agent.source-1.interceptors.search-replace.searchPattern = ^INFO:
 *   agent.source-1.interceptors.search-replace.replaceString = Log msg:
 * </pre>
 * <p>
 * Any regular expression search pattern and replacement pattern that can be
 * used with {@link Matcher#replaceAll(String)} may be used,
 * including backtracking and grouping.
 */

/**
 * 搜索与替换
 * 搜索改用groovy 脚本进行；
 * 替换改用groovy 脚本进行；
 *
 * 从boddy 中抽取数据，放到head中；
 *
 *  <p>
 * Usage:
 * <pre>
 *   agent.source-1.interceptors.search-replace.searchReplaceKey = GroovyDSLName
 *   agent.source-1.interceptors.search-replace.searchReplaceDsl = GroovyDSLFile:
 * </pre>
 * <p>
 *
 */
public class RuleThreadSearchAndReplaceInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory
      .getLogger(RuleThreadSearchAndReplaceInterceptor.class);

  private final String searchReplaceKey;
  private final String searchReplaceDsl;


//  private final Pattern searchPattern;
//  private final String replaceString;

  private final Charset charset;

//  private RuleSearchAndReplaceInterceptor(Pattern searchPattern,
//                                      String replaceString,
//                                      Charset charset) {
//    this.searchPattern = searchPattern;
//    this.replaceString = replaceString;
//    this.charset = charset;
//  }

  private ExecutorService executorService = null;
  private int threadNum = 10;
  private int threadPool = 100;

  private File f ;


  private RuleThreadSearchAndReplaceInterceptor(String searchReplaceKey, String searchReplaceDsl,
                                                Charset charset, int threadNum, int threadPool) {
  this.searchReplaceKey=searchReplaceKey;
  this.searchReplaceDsl=searchReplaceDsl;
  this.charset = charset;
  this.threadNum = threadNum;
  this.threadPool = threadPool;

    f =new File(searchReplaceDsl); //规则定期更新 fixme

  }


  @Override
  public void initialize() {
//    executorService = Executors.newFixedThreadPool(threadNum);
    executorService = Executors.newFixedThreadPool(threadPool);
    //executorService = Executors.newCachedThreadPool();
    f =new File(searchReplaceDsl); //规则定期更新 fixme


  }

  @Override
  public void close() {
    executorService.shutdown();


  }

//  @Override
//  public Event intercept(Event event) {
//    String origBody = new String(event.getBody(), charset);
//    Matcher matcher = searchPattern.matcher(origBody);
//    String newBody = matcher.replaceAll(replaceString);
//    event.setBody(newBody.getBytes(charset));
//    return event;
//  }
  @Override
  public Event intercept(Event event) {
    String origBody = new String(event.getBody(), charset);
    Map<String, String> headers = event.getHeaders();

    //输入参数
    Binding binding = new Binding();
    binding.setVariable("body", origBody);
    binding.setVariable("head", headers);
    //查找匹配数据；
//    File f =new File(searchReplaceDsl);  //提升性能
    Map result =(Map) GroovyShellJsonExample.getShell(searchReplaceKey+f.lastModified(), f, binding);

    //替换匹配数据；

//    Matcher matcher = searchPattern.matcher(origBody);
//    String newBody = matcher.replaceAll(replaceString);
    logger.debug(result.get("body").toString());
    logger.debug(result.get("head").toString());

    event.setBody(result.get("body").toString().getBytes(charset));
    event.setHeaders((Map)result.get("head"));
    return event;
  }

  @Override
  public List<Event> intercept(List<Event> events) {
    //todo
    long s=System.currentTimeMillis();

    final AtomicInteger ai = new AtomicInteger(0);

    List<Future<Event>> results = new ArrayList<Future<Event>>();

    for (final Event event : events) {
      Future<Event> future = executorService.submit(new Callable<Event>() {
        @Override
        public Event call() {
          ai.incrementAndGet();
          return intercept(event);
        }
      });

      results.add(future);
    }

//        List<Event> out = Lists.newArrayList();
    for (Future<Event> future:results) {

      Event event = null;
      try {
        event = future.get(); //阻塞作用，等待线程执行
//        out.add(event);
//        if (event != null) {
//          out.add(event);
//        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }

    }

    long e=System.currentTimeMillis();

    logger.info("rules:{}个线程，规则拦截器处理数据{}",Thread.activeCount(),ai.intValue());
    logger.info("rules:每秒规则拦截器处理数据{}",ai.intValue()/(e-s)/1000);
    logger.info("rules:每秒规则拦截器处理数据{}",events.size()/(e-s)/1000);

    return events;
//    return out;
  }

  public static class Builder implements Interceptor.Builder {



    private static final String SEARCH_REPLACE_KEY = "searchReplaceKey";
    private static final String SEARCH_REPLACE_DSL = "searchReplaceDsl";
    private static final String CHARSET_KEY = "charset";

    private static final String THREAD_NUM = "threadNum";
    private static final String THREAD_POOL = "threadPool";

    private  String searchReplaceKey;
    private  String searchReplaceDsl;
    private  int threadNum = 10;
    private  int threadPool = 100;

//    private Pattern searchRegex;
//    private String replaceString;
    private Charset charset = Charsets.UTF_8;

    @Override
    public void configure(Context context) {


      searchReplaceKey = context.getString(SEARCH_REPLACE_KEY);



      Preconditions.checkArgument(!StringUtils.isEmpty(searchReplaceKey),
          "Must supply a valid search pattern " + SEARCH_REPLACE_KEY +
          " (may not be empty)");

      searchReplaceDsl = context.getString(SEARCH_REPLACE_DSL);
      Preconditions.checkNotNull(searchReplaceDsl,
          "Must supply a replacement string " + SEARCH_REPLACE_DSL +
          " (empty is ok)");

//      searchRegex = Pattern.compile(searchPattern);
      threadNum = context.getInteger(THREAD_NUM);
      threadPool = context.getInteger(THREAD_POOL);

      if (context.containsKey(CHARSET_KEY)) {
        // May throw IllegalArgumentException for unsupported charsets.
        charset = Charset.forName(context.getString(CHARSET_KEY));
      }
    }

    @Override
    public Interceptor build() {
      Preconditions.checkNotNull(searchReplaceKey,
                                 "searchReplaceKey required");
      Preconditions.checkNotNull(searchReplaceDsl,
                                 "searchReplaceDsl required");
      return new RuleThreadSearchAndReplaceInterceptor(searchReplaceKey, searchReplaceDsl, charset,threadNum,threadPool);
    }
  }
}
