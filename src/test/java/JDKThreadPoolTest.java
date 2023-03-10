import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class JDKThreadPoolTest {

    //Executors.newFixedThreadPool
    @Test
    public void test1(){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for(int i=0;i<3;i++){
            int j=i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ":" + j);
                }
            };
            executor.execute(runnable);
        }
    }

    //自定义线程工厂——可以自定义名字
    @Test
    public void test2(){
        //自定义线程工厂
        ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private AtomicInteger i=new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r,"mypool-" + i);
                i.getAndIncrement();
                return t;
            }
        });
        //测试
        for(int i=0;i<3;i++){
            int j=i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ":" + j);
                }
            };
            executor.execute(runnable);
        }
    }

    //测试SynchronousQueue
    @Test
    public void test3() throws InterruptedException {
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("开始存放任务1");
                    //如果没有线程来取就会阻塞
                    queue.put("task1");
                    System.out.println("任务1存放完毕");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    System.out.println("开始存放任务2");
                    queue.put("task2");
                    System.out.println("任务2存放完毕");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    //如果没有线程存放就会阻塞
                    String take = (String) queue.take();
                    System.out.println("取到" + take);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(2000);
                    String take = (String) queue.take();
                    System.out.println("取到" + take);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    //newCachedThreadPool
    @Test
    public void test4() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
            private AtomicInteger i = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "mypool-" + i);
                i.getAndIncrement();
                return t;
            }
        });

        for(int i=0;i<20;i++){
            int j=i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + j);
                }
            };
            executor.execute(r);
        }

        Thread.sleep(3000);
    }

    //测试newSingleThreadExecutor中某个任务出现异常
    @Test
    public void test5() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + ":1");
                        int i = 1 / 0;
                    }
                };
                executor.execute(r);
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + ":2");
                    }
                };
                executor.execute(r);
            }
        };
        Thread t3 = new Thread() {
            @Override
            public void run() {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + ":3");
                    }
                };
                executor.execute(r);
            }
        };
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    //测试submit方法
    @Test
    public void test6() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<String> result = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("接收任务");
                Thread.sleep(1000);
                return "ok";
            }
        });

        System.out.println(result.get());
    }

    //invokeAll
    @Test
    public void test7() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.shutdownNow();
        ArrayList<Callable<String>> list = new ArrayList<>();
        for(int i=0;i<5;i++){
            Integer j = i;
            Callable<String> c = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Thread.sleep(1000);
                    System.out.println(j);
                    return j.toString();
                }
            };
            list.add(c);
        }
        List<Future<String>> futures = executor.invokeAll(list);
        for(Future f:futures){
            System.out.print(f.get());
        }
    }

    //shutdown
    @Test
    public void test8() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String>  result1= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务1结束");
                return  "任务1";
            }
        });
        Future<String>  result2= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务2结束");
                return  "任务2";
            }
        });
        Future<String>  result3= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务3结束");
                return  "任务3";
            }
        });
        pool.shutdown();
        System.out.println("线程池关闭");
//        Future<String>  result4= pool.submit(new Callable<String>() {
//            @Override
//            public String call() throws Exception {
//                Thread.sleep(1000);
//                System.out.println("任务4结束");
//                return  "任务4";
//            }
//        });
      Thread.sleep(4000);
    }

    //shutdownNow
    @Test
    public void test9() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String>  result1= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务1结束");
                return  "任务1";
            }
        });
        Future<String>  result2= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务2结束");
                return  "任务2";
            }
        });
        Future<String>  result3= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务3结束");
                return  "任务3";
            }
        });
        List<Runnable> runnables = pool.shutdownNow();
        System.out.println("线程池关闭");

        Thread.sleep(4000);

    }

    //
    @Test
    public void test10() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String>  result1= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("任务1结束");
                return  "任务1";
            }
        });
        Future<String>  result2= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务2结束");
                return  "任务2";
            }
        });
        Future<String>  result3= pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                System.out.println("任务3结束");
                return  "任务3";
            }
        });
        List<Runnable> runnables = pool.shutdownNow();
        System.out.println("线程池关闭");

        Thread.sleep(4000);

    }

    //线程饥饿
    @Test
    public void test11() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = Executors.newFixedThreadPool(2);

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("1号点单");
                Callable<String> c = new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("做可乐鸡翅");
                        Thread.sleep(1000);
                        return "可乐鸡翅";
                    }
                };
                Future<String> future = pool2.submit(c);
                try {
                    String s = future.get();
                    System.out.println("上" + s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                System.out.println("2号点单");
                Callable<String> c = new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("做啤酒鸭");
                        Thread.sleep(1000);
                        return "啤酒鸭";
                    }
                };
                Future<String> future = pool2.submit(c);
                try {
                    String s = future.get();
                    System.out.println("上" + s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        pool.execute(r1);
        pool.execute(r2);

        Thread.sleep(2000);
    }

    //间隔任务
    @Test
    public void test12() throws InterruptedException {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println(LocalDateTime.now() + ",执行");
            }
        };
        System.out.println(LocalDateTime.now() + ",start");
        pool.schedule(r,3000,TimeUnit.MILLISECONDS);
        Thread.sleep(5000);
    }

    //定时任务1
    @Test
    public void test13() throws InterruptedException {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        Runnable r = new Runnable(){
            @Override
            public void run() {
                System.out.println(LocalDateTime.now() + ",执行");
            }
        };
        System.out.println(LocalDateTime.now()+"，start");
        pool.scheduleAtFixedRate(r,1,1,TimeUnit.SECONDS);
        Thread.sleep(10000);
    }

    //定时任务2
    @Test
    public void test14() throws InterruptedException {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        Runnable r = new Runnable(){
            @Override
            public void run() {
                System.out.println(LocalDateTime.now() + ",执行");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        System.out.println(LocalDateTime.now()+"，start");
        pool.scheduleWithFixedDelay(r,1,1,TimeUnit.SECONDS);
        Thread.sleep(20000);
    }

    //异常处理
    @Test
    public void test15() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<Object> result = pool.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                int i = 1 / 0;
                System.out.println("end");
                return null;
            }
        });
        Thread.sleep(2000);
        result.get();
    }

   @Test
    public void test16() throws InterruptedException {
       ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
       ScheduledFuture<?> end = pool.schedule(new Runnable() {
           @Override
           public void run() {
               int i = 1 / 0;
               System.out.println("end");
           }
       }, 1, TimeUnit.SECONDS);

       Thread.sleep(10000);
   }

   @Test
    public void test17(){
       System.out.println(1111);
       System.out.println("hot fix 3333,remote");
      
   }
}

