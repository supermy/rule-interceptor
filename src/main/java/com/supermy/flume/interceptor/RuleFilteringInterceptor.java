/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.supermy.flume.interceptor;


import com.google.common.collect.Lists;
import groovy.lang.Binding;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static com.supermy.flume.interceptor.RuleFilteringInterceptor.Constants.*;

/**
 * Interceptor that filters events selectively based on a configured regular
 * expression matching against the event body.
 *
 * This supports either include- or exclude-based filtering. A given
 * interceptor can only perform one of these functions, but multiple
 * interceptor can be chained together to create more complex
 * inclusion/exclusion patterns. If include-based filtering is configured, then
 * all events matching the supplied regular expression will be passed through
 * and all events not matching will be ignored. If exclude-based filtering is
 * configured, than all events matching will be ignored, and all other events
 * will pass through.
 *
 * Note that all regular expression matching occurs through Java's built in
 * java.util.regex package.
 *
 * Properties:<p>
 *
 *   regex: Regular expression for matching excluded events.
 *          (default is ".*")<p>
 *
 *   excludeEvents: If true, a regex match determines events to exclude,
 *                  otherwise a regex determines events to include
 *                  (default is false)<p>
 *
 * Sample config:<p>
 *
 * <code>
 *   agent.sources.r1.channels = c1<p>
 *   agent.sources.r1.type = SEQ<p>
 *   agent.sources.r1.interceptors = i1<p>
 *   agent.sources.r1.interceptors.i1.type = REGEX<p>
 *   agent.sources.r1.interceptors.i1.regex = (WARNING)|(ERROR)|(FATAL)<p>
 * </code>
 *
 */

/**
 * @author james mo
 * 利用脚本作为规则判定；
 *
 * Properties:<p>
 *
 *   rulename:
 *   rule: groovy 脚本进行匹配 Regular expression for matching excluded events.
 *          (default is ".*")<p>
 *
 *   excludeEvents: If true, a regex match determines events to exclude,
 *                  otherwise a regex determines events to include
 *                  (default is false)<p>
 *
 *
 * <code>
 *   agent.sources.r1.channels = c1<p>
 *   agent.sources.r1.type = SEQ<p>
 *   agent.sources.r1.interceptors = i1<p>
 *   agent.sources.r1.interceptors.i1.type = RULE<p>
 *   agent.sources.r1.interceptors.i1.rule = (WARNING)|(ERROR)|(FATAL)<p>
 * </code>
 *
 */
public class RuleFilteringInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory
            .getLogger(RuleFilteringInterceptor.class);

    private final String rule; //groovy file
    private final String rulename;

    private final boolean excludeEvents;

    /**
     * Only {@link RuleFilteringInterceptor.Builder} can build me
     */
    private RuleFilteringInterceptor(String rule, String rulename, boolean excludeEvents) {

        this.rule = rule;
        this.rulename = rulename;
        this.excludeEvents = excludeEvents;
    }

    @Override
    public void initialize() {
        // no-op
    }


    @Override
    /**
     * Returns the event if it passes the regular expression filter and null
     * otherwise.
     *
     * 单个event拦截逻辑
     *
     */
    public Event intercept(Event event) {
        // We've already ensured here that at most one of includeRegex and
        // excludeRegex are defined.
        // 调用脚本规则进行判定 groovy

        //输入参数
        Binding binding = new Binding();
        binding.setVariable("body", new String(event.getBody()));
        binding.setVariable("head", event.getHeaders());

        logger.debug(String.format(
                "flume 的值:  body=%s,head=%s",
                new String(event.getBody()), event.getHeaders()));

        logger.debug(String.format(
                "flume 配置参数:  rule=%s,rulename=%s",
                rule, rulename));

        File f =new File(rule);
        boolean result = (Boolean) GroovyShellJsonExample.getShell(rulename+f.lastModified(), f, binding);

        if (!excludeEvents) {
            //if (regex.matcher(new String(event.getBody())).find()) {// TODO: 16/12/14
            if (result) {
                return event;
            } else {
                return null;
            }
        } else {
            //if (regex.matcher(new String(event.getBody())).find()) {// TODO: 16/12/14
            if (result) {// TODO: 16/12/14
                return null;
            } else {
                return event;
            }
        }
    }

    /**
     * Returns the set of events which pass filters, according to
     * 批量event拦截逻辑
     * {@link #intercept(Event)}.
     * @param events
     * @return
     */
    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> out = Lists.newArrayList();
        for (Event event : events) {
            Event outEvent = intercept(event);
            if (outEvent != null) {
                out.add(outEvent);
            }
        }
        return out;
    }

    @Override
    public void close() {
        // no-op
    }

    /**
     * Builder which builds new instance of the StaticInterceptor.
     * 相当于自定义Interceptor的工厂类
     * 在flume采集配置文件中通过制定该Builder来创建Interceptor对象
     * 可以在Builder中获取、解析flume采集配置文件中的拦截器Interceptor的自定义参数：
     * 字段分隔符，字段下标，下标分隔符、加密字段下标 ...等
     *
     */
    public static class Builder implements Interceptor.Builder {

        private String rule;
        private String rulename;
        private boolean excludeEvents;

        @Override
        public void configure(Context context) {
            String ruleString = context.getString(RULE, DEFAULT_RULE);
            String ruleNameString = context.getString(RULE_NAME, DEFAUNLT_RULE_NAME);

            //rule = Pattern.compile(ruleString); //// TODO: 16/12/14  groovy 脚本替换
            rule = ruleString;
            rulename = ruleNameString;

            excludeEvents = context.getBoolean(EXCLUDE_EVENTS,
                    DEFAULT_EXCLUDE_EVENTS);
        }

        @Override
        public Interceptor build() {
            logger.info(String.format(
                    "Creating RegexFilteringInterceptor: rule=%s,rulename=%s,excludeEvents=%s",
                    rule, rulename, excludeEvents));
            return new RuleFilteringInterceptor(rule, rulename, excludeEvents);
        }
    }

    /**
     * 常量
     */
    public static class Constants {

        public static final String RULE = "rule";
        public static final String RULE_NAME = "ruleName";

        public static final String DEFAULT_RULE = ".*";
        public static final String DEFAUNLT_RULE_NAME = "filterRule";


        public static final String EXCLUDE_EVENTS = "excludeEvents";
        public static final boolean DEFAULT_EXCLUDE_EVENTS = false;
    }

}