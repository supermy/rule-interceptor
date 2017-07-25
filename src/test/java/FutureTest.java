/**
 * Created by moyong on 17/7/21.
 */

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 试验 Java 的 Future 用法
 */
public class FutureTest {

    public static class Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            String tid = String.valueOf(Thread.currentThread().getId());
//            System.out.printf("Thread#%s : in call\n", tid);
            return tid;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        testThread();
        testThread2();
    }

    public static void testThread() throws InterruptedException, ExecutionException {
        List<Future<String>> results = new ArrayList<Future<String>>();
//        ExecutorService es = Executors.newCachedThreadPool();
        ExecutorService es = Executors.newFixedThreadPool(30);

//        long s=System.nanoTime();
        long s=System.currentTimeMillis();
        for(int i=0; i<60000;i++)
            results.add(es.submit(new Task()));

        for(Future<String> res : results)
            res.get();
//            System.out.println(res.get());

//        long e=System.nanoTime();
        long e=System.currentTimeMillis();

        System.out.print(String.format("have %s/%s ,%s \n", results.size(),(e-s),results.size()/(e-s)));

        es.shutdown();
    }

    public static void testThread2() {
        ExecutorService executorService = Executors.newFixedThreadPool(30);

        long s=System.currentTimeMillis();


        List<Future<String>> results = new ArrayList<Future<String>>();

        for(int i=0; i<60000;i++){
            Future<String> future = executorService.submit(new Callable<String>() {
                @Override
                public String call() {
                    String tid = String.valueOf(Thread.currentThread().getId());
                    //System.out.printf("Thread#%s : in call\n", tid);
                    return tid;
                }
            });

            results.add(future);
        }

        List<String> out = Lists.newArrayList();
        for (Future<String> future:results) {
            String outString = null;
            try {
                outString = future.get();
//                System.out.println(outString);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (outString != null) {
                out.add(outString);
            }
        }

        long e=System.currentTimeMillis();

        System.out.print(String.format("have %s/%s ,%s \n", results.size(),(e-s),results.size()/(e-s)));
        System.out.print(String.format("have %s/%s ,%s \n", out.size(),(e-s),out.size()/(e-s)));

        executorService.shutdown();
        //return out;
    }

}