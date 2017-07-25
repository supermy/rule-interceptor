package com.supermy.flume.interceptor;

/**
 * Created by moyong on 16/11/24.
 */

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.flume.Event;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试内存是否存在问题
 * 很长时间之后
 * Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "main"
 * <p/>
 * 位置前移解决问题
 * static Map<String, Object> scriptCache = new ConcurrentHashMap<String, Object>();
 */
public class GroovyShellJsonExample {
    private static Map<String, Object> scriptCache = new ConcurrentHashMap<String, Object>();
    private static Map<String, Class> scriptCache1 = new ConcurrentHashMap<String, Class>();

    public static void main(String args[]) {
//        execDsl();
//        oral();
        //JSON 的测试例子
        String scriptname = "jsontest";
        String script = "/Users/moyong/project/env-myopensource/3-tools/flume-rule-interceptor/src/main/resources/hello.groovy";
//                File script = new ClassPathResource("hello.groovy").getFile();
        File f = new File(script);
        int i = 0;
        while (i<=100) {
            i++;
            String json = "{ \"name\": \"James Mo\" }";
            Binding binding = new Binding();
            binding.setVariable("p1", "200");
            binding.setVariable("p2", json);


            try {
//            String script = "println\"Welcome to $language\"; y = x * 2; z = x * 3; return x ";
//            Object hello = GroovyShellJsonExample.getShell("hello", script, binding);


                System.out.println("f.lastModified():"+f.lastModified());


                Object hello = GroovyShellJsonExample.getShell(scriptname+f.lastModified(), f, binding);

                System.out.println(hello);
                System.out.println(i);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 为Groovy Script增加缓存
     * 解决Binding的线程安全问题
     *
     * @return
     */
    public static Object getShell(String cacheKey, File f, Binding binding) {


        Object scriptObject = null;
        try {


            Class<Script>  scriptClass = null;
                if (scriptCache1.containsKey(cacheKey)) {
                    //System.out.println("===============scriptCache:"+cacheKey);

                    scriptClass = scriptCache1.get(cacheKey);
                } else {
                    //System.out.println("===============");
                    CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
                    GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(),config);
//                    GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
                    scriptClass = groovyClassLoader.parseClass(f);
                    Script groovyScript = scriptClass.newInstance();


                }


            Script script = InvokerHelper.createScript(scriptClass, binding);
            scriptObject =  script.run();


            // clear
            binding.getVariables().clear();
            binding = null;

            if (!scriptCache1.containsKey(cacheKey)) {
                //shell.setBinding(null);
                scriptCache1.put(cacheKey, scriptClass);
            }
        } catch (Exception t) {
            t.printStackTrace();
            //System.out.println("groovy script eval error. script: " + script, t);
        }

        return scriptObject;
    }


    public static Object getShell1(String cacheKey, File f, Event event) {

        Object scriptObject = null;
        try {


            Class<Script>  scriptClass = null;
            if (scriptCache1.containsKey(cacheKey)) {
                //System.out.println("===============scriptCache:"+cacheKey);

                scriptClass = scriptCache1.get(cacheKey);
            } else {
                //System.out.println("===============");
                CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
//                config.getOptimizationOptions().put("indy", true);
//                config.getOptimizationOptions().put("int", false);
//                GroovyShell shell = new GroovyShell(config);

                config.setScriptBaseClass(Script.class.getName());
                GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(),config);
//                    GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
                scriptClass = groovyClassLoader.parseClass(f);
//                Script groovyScript = scriptClass.newInstance();


            }

            //输入参数
            Binding binding = new Binding();
            binding.setVariable("body", new String(event.getBody()));
            binding.setVariable("head", event.getHeaders());

            Script script = InvokerHelper.createScript(scriptClass, binding);
            scriptObject =  script.run();


            // clear
            binding.getVariables().clear();
            binding = null;

            if (!scriptCache1.containsKey(cacheKey)) {
                //shell.setBinding(null);
                scriptCache1.put(cacheKey, scriptClass);
            }
        } catch (Exception t) {
            t.printStackTrace();
            //System.out.println("groovy script eval error. script: " + script, t);
        }

        return scriptObject;
    }

    /**
     * 传统调用方式；
     */
    private static void oral() {
        Binding binding = new Binding();

        binding.setVariable("x", 10);

        binding.setVariable("language", "Groovy");

        GroovyShell shell = new GroovyShell(binding);

        Object value = shell.evaluate
                ("println\"Welcome to $language\"; y = x * 2; z = x * 3; return x ");

        assert value.equals(10);

        assert binding.getVariable("y").equals(20);

        assert binding.getVariable("z").equals(30);
    }

    public static void execDsl()  {

        String path = "/Users/moyong/project/env-myopensource/3-tools/flume-rule-interceptor/src/main/resources/";

        GroovyScriptEngine gse = null;
        try {
            gse = new GroovyScriptEngine(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Binding binding = new Binding();

        binding.setVariable("input", "Groovy");

        try {
            gse.run("hello.groovy", binding);
        } catch (ResourceException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        System.out.println(binding.getVariable("output"));

    }
}
