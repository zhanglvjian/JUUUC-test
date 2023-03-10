import com.sun.deploy.security.SelectableSecurityManager;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import org.junit.Test;
import sun.misc.Unsafe;
import sun.rmi.server.Activation$ActivationSystemImpl_Stub;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.GuardedObject;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarOutputStream;

public class JUCtest {

    @Test
    public void test1() {
        Thread thread = new Thread("t1") {
            public void run() {
                System.out.println(Thread.currentThread().getName());
            }
        };
        thread.start();

        System.out.println(Thread.currentThread().getName());

    }

    @Test
    public void test2() {
        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName());
            }
        };
        Thread t1 = new Thread(runnable, "t1");
        t1.start();

        System.out.println(Thread.currentThread().getName());

    }

    @Test
    public void test3() throws ExecutionException, InterruptedException {
        //实现callable的call方法
        Callable<Integer> callable = new Callable() {
            public Integer call() throws Exception {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(3000);
                return 1;
            }
        };
        //创建任务对象
        FutureTask task = new FutureTask<Integer>(callable);
        //创建线程对象
        Thread t1 = new Thread(task, "t1");
        t1.start();
        //主线程阻塞，同步等待 task 执行完毕的结果
        Integer i = (Integer) task.get();
        System.out.println(Thread.currentThread().getName() + i);
    }

    @Test
    public void test4() {
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("run...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("被打断");
                    e.printStackTrace();
                }
                System.out.println("t1继续运行");
            }
        }, "t1");
        t1.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t1.interrupt();

    }

    @Test
    public void test5() throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    System.out.println("执行监控:" + i);
                    i += 1;
                    if (interrupted()) {
                        System.out.println("被打断");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //设置打断标记，因为在睡眠时被打断会清除打断标记
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        t1.start();
        Thread.sleep(3500);
        t1.interrupt();
    }

    @Test
    public void test6() throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("守护线程运行");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t1.setDaemon(true);
        t1.start();
        Thread.sleep(5000);
        System.out.println("主线程结束");
    }

    @Test
    public void test7() {
        //new
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
            }
        };
        //runnable
        final Thread t2 = new Thread("t2") {
            @Override
            public void run() {
                while (true) {
                }
            }
        };
        //terminated
        Thread t3 = new Thread("t3") {
            @Override
            public void run() {
            }
        };
        //timed_waiting（限时等待，以sleep的时间为准）
        Thread t4 = new Thread("t4") {
            @Override
            public void run() {
                synchronized (JUCtest.class) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //waiting（无限时等待）
        Thread t5 = new Thread("t5") {
            @Override
            public void run() {
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        //blocked(与t4抢锁，若抢不到则进入阻塞状态）
        Thread t6 = new Thread("t6") {
            @Override
            public void run() {
                synchronized (JUCtest.class) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(t1.getName() + ":" + t1.getState());
        System.out.println(t2.getName() + ":" + t2.getState());
        System.out.println(t3.getName() + ":" + t3.getState());
        System.out.println(t4.getName() + ":" + t4.getState());
        System.out.println(t5.getName() + ":" + t5.getState());
        System.out.println(t6.getName() + ":" + t6.getState());

    }

    static Integer i = 0;
    static Object object = new Object();

    @Test
    public void test8() throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                for (int a = 0; a < 1000; a++) {
                    synchronized (object) {
                        i++;
                    }
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                for (int a = 0; a < 1000; a++) {
                    synchronized (object) {
                        i--;
                    }
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);

    }

    class Counter {
        int number = 0;

        public void add() {
            synchronized (this) {
                number++;
            }
        }

        public void sub() {
            synchronized (this) {
                number--;
            }
        }

        public int getNum() {
            synchronized (this) {
                return number;
            }
        }
    }

    @Test
    public void test9() throws InterruptedException {
        final Counter counter = new Counter();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    counter.add();
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    counter.sub();
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        int num = counter.getNum();
        System.out.println(num);
    }

    //卖票窗口类
    class TicketWindow {
        private int ticketCount;

        TicketWindow(int count) {
            ticketCount = count;
        }

        //卖票方法,返回结果为卖出票数
        public synchronized int sell(int count) {
            if (count < ticketCount) {
                ticketCount -= count;
                return count;
            } else {
                return 0;
            }
        }

        //获取余票方法
        public int getTicketCount() {
            return ticketCount;
        }
    }

    //测试卖票
    @Test
    public void test10() throws InterruptedException {
        //卖票窗口
        final TicketWindow window = new TicketWindow(1000);
        //卖票数量集合（vector保证线程安全）
        final Vector<Integer> ticketList = new Vector<Integer>();
        for (int i = 0; i < 200; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    int sell = window.sell(new Random().nextInt(4) + 1);
                    ticketList.add(sell);
                }
            };
            t.start();
        }
        Thread.sleep(3000);
        //获取余票
        System.out.println("余票：" + window.getTicketCount());
        //获取卖出票数
        int total = 0;
        for (Integer i : ticketList) {
            total += i;
        }
        System.out.println("共卖出:" + total);
    }

    //账户类
    class Account {
        int balance;

        Account(int money) {
            balance = money;
        }

        //查询余额方法
        public int getBalance() {
            return balance;
        }

        //转账方法
        public void transfer(Account account, int money) {
            synchronized (Account.class) {
                if (money < balance) {
                    this.balance -= money;
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    account.balance += money;
                }
            }
        }
    }

    //测试互相转账
    @Test
    public void test11() throws InterruptedException {
        final Account a = new Account(1000);
        final Account b = new Account(1000);
        Thread t1 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    a.transfer(b, new Random().nextInt(100));
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    b.transfer(a, new Random().nextInt(100));
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(a.getBalance() + b.getBalance());

    }

    //验证synchronized是非公平锁
    @Test
    public void test12() throws InterruptedException {
        final Object o = new Object();

        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                synchronized (o) {
                    //拿到锁后睡眠3秒
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(LocalDateTime.now() + ":" + Thread.currentThread().getName() + "进入");
                }
            }
        };

        Thread t2 = new Thread("t2") {
            @Override
            public void run() {
                //睡眠1秒
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(LocalDateTime.now() + ":" + Thread.currentThread().getName() + "阻塞");
                synchronized (o) {
                    System.out.println(LocalDateTime.now() + ":" + Thread.currentThread().getName() + "进入");
                }
            }
        };

        Thread t3 = new Thread("t3") {
            @Override
            public void run() {
                //睡眠2秒
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(LocalDateTime.now() + ":" + Thread.currentThread().getName() + "阻塞");
                synchronized (o) {
                    System.out.println(LocalDateTime.now() + ":" + Thread.currentThread().getName() + "进入");
                }
            }
        };
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();

    }

    //测试wait和notify
    @Test
    public void test13() throws InterruptedException {
        Object lock = new Object();

        new Thread() {
            @Override
            public void run() {
                System.out.println("线程1启动");
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程1被唤醒");
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                System.out.println("线程2启动");
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程2被唤醒");
                }
            }
        }.start();

        Thread.sleep(2000);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    //保护性暂停模式
    class GuardObject {
        private final Object lock = new Object();
        private Object response;

        public Object get() {
            synchronized (lock) {
                //用while避免虚假唤醒
                while (Objects.isNull(response)) {
                    //未满足条件，获取不到结果，等待
                    try {
                        System.out.println("GuardObject:等待结果");
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
            return response;
        }

        public void complete(Object o) {
            synchronized (lock) {
                //获取到数据，等待线程
                response = o;
                lock.notifyAll();
                System.out.println("GuardObject:获取到数据");
            }
        }
    }

    @Test
    public void test14() {
        GuardObject guardObject = new GuardObject();
        new Thread() {
            @Override
            public void run() {
                //获取资源线程
                try {
                    System.out.println("开始获取资源");
                    Thread.sleep(3000);
                    guardObject.complete("resource");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        String resource = (String) guardObject.get();
        System.out.println(resource);
    }

    /*
     * 生产者消费者模式
     * */
    //消息
    class Message {
        private int id;
        private String message;

        Message(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() {
            return id;
        }


        public String getMessage() {
            return message;
        }
    }

    //消息队列
    class MessageQueue {
        private final LinkedList<Message> queue;
        private int capacity;

        MessageQueue(int capacity) {
            this.capacity = capacity;
            this.queue = new LinkedList<Message>();
        }

        //存消息方法
        public void putMessage(Message message) {
            synchronized (queue) {
                while (queue.size() == capacity) {
                    //队列已满
                    try {
                        System.out.println(Thread.currentThread().getName() + "：队列已满");
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                queue.addFirst(message);
                System.out.println(Thread.currentThread().getName() + "：存放消息" + message.getId());
                //唤醒消费者取消息
                queue.notifyAll();
            }
        }

        //取消息方法
        public Message getMessage() {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    //队列为空
                    try {
                        System.out.println(Thread.currentThread().getName() + "：队列为空");
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message message = queue.removeLast();
                //唤醒生产者放消息
                queue.notifyAll();
                return message;
            }
        }
    }

    //测试
    @Test
    public void test15() throws InterruptedException {
        MessageQueue queue = new MessageQueue(2);
        //生产者
        for (int i = 0; i < 3; i++) {
            int index = i;
            new Thread("生产者" + i + "号") {
                @Override
                public void run() {
                    Message message = new Message(index, "消息" + index);
                    System.out.println(Thread.currentThread().getName() + ":生产消息" + index);
                    queue.putMessage(message);
                }
            }.start();
        }
        //消费者
        Thread t1 = new Thread("消费者1号") {
            @Override
            public void run() {
                while (true) {
                    //每隔一秒拿一条消息
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = queue.getMessage();
                    System.out.println(Thread.currentThread().getName() + ":拿到消息" + message.message);
                }
            }
        };
        t1.start();
        t1.join();
    }

    //验证wait和runnable状态
    @Test
    public void test16() throws InterruptedException {
        final Object lock = new Object();

        new Thread("线程1") {
            @Override
            public void run() {
                synchronized (lock) {
                    System.out.println("线程1wait");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程1run");
                }
            }
        }.start();

        new Thread("线程2") {
            @Override
            public void run() {
                synchronized (lock) {
                    System.out.println("线程2wait");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程2run");
                }
            }
        }.start();

        Thread.sleep(2000);
        synchronized (lock) {
            lock.notifyAll();
        }
        System.out.println("主线程释放锁");

    }

    //park和unpark
    @Test
    public void test17() throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                System.out.println("线程暂停");
                //暂停
                LockSupport.park();
                System.out.println("线程恢复");
            }
        };
        t1.start();
        Thread.sleep(1000);
        System.out.println("准备回复线程");
        Thread.sleep(1000);
        LockSupport.unpark(t1);

    }

    //死锁
    @Test
    public void test18() throws InterruptedException {
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread t1 = new Thread() {
            @Override
            public void run() {
                synchronized (lock1) {
                    System.out.println("线程1获得锁1");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (lock2) {
                        System.out.println("线程1获得锁2");
                    }
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                synchronized (lock2) {
                    System.out.println("线程2获得锁2");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (lock1) {
                        System.out.println("线程2获得锁1");
                    }
                }
            }
        };

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    //死锁——哲学家吃饭问题
    //筷子类
    class Chopstick extends ReentrantLock {
        int id;

        Chopstick(int id) {
            this.id = id;
        }
    }

    //哲学家类
    class Philosopher extends Thread {
        String name;
        Chopstick left;
        Chopstick right;

        Philosopher(String name, Chopstick left, Chopstick right) {
            this.name = name;
            this.left = left;
            this.right = right;
        }

        @Override
        public void run() {
            while (true) {
                if (left.tryLock()) {
                    try {
                        if (right.tryLock()) {
                            try {
                                //吃饭
                                System.out.println(name + "开始吃饭");
                            } finally {
                                right.unlock();
                            }
                        }
                    } finally {
                        left.unlock();
                    }
                }
//                      synchronized (left){
//                          synchronized (right){
//                              //吃饭
//                              System.out.println(name+"开始吃饭");
//                          }
//                      }
                //思考
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void test19() throws InterruptedException {
        Chopstick c1 = new Chopstick(1);
        Chopstick c2 = new Chopstick(2);
        Chopstick c3 = new Chopstick(3);
        Chopstick c4 = new Chopstick(4);

        Philosopher p1 = new Philosopher("阿基米德", c1, c2);
        Philosopher p2 = new Philosopher("莫泊桑", c2, c3);
        Philosopher p3 = new Philosopher("亚里士多德", c3, c4);
        Philosopher p4 = new Philosopher("恩格斯", c4, c1);

        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p1.join();
        p2.join();
        p3.join();
        p4.join();

    }

    static int count = 10;

    //活锁
    @Test
    public void test20() throws InterruptedException {

        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (count < 20) {
                    count++;
                    System.out.println("t1:" + count);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (count > 0) {
                    count--;
                    System.out.println("t2:" + count);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    //reentrantlock可重入性
    @Test
    public void test21() {
        ReentrantLock lock = new ReentrantLock();

        lock.lock();
        try {
            System.out.println("获得锁1次");
            lock.lock();
            try {
                System.out.println("获得锁2次");
            } finally {
                lock.unlock();
            }
        } finally {
            lock.unlock();
        }
    }

    //reentrantlock可打断
    @Test
    public void test22() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();

        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    lock.lockInterruptibly();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("线程1被打断");
                    //未获得锁不再执行获得锁得代码
                    return;
                }
                try {
                    System.out.println("线程1获得锁");
                } finally {
                    lock.unlock();
                }
            }
        };

        lock.lock();
        try {
            System.out.println("主线程获得锁");
            t1.start();
            Thread.sleep(1000);
            t1.interrupt();
        } finally {
            lock.unlock();
        }
    }

    //reentrantlock锁超时放弃
    @Test
    public void test23() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    if (!lock.tryLock(1, TimeUnit.SECONDS)) {
                        //规定时间内未获得锁
                        System.out.println("t1未获得锁");
                        return;
                    }
                } catch (InterruptedException e) {
                    //等待获得锁期间被打断，未获得锁
                    e.printStackTrace();
                    return;
                }
                //获得锁
                try {
                    System.out.println("t1获得锁");
                } finally {
                    lock.unlock();
                }

            }
        };
        System.out.println("主线程获得锁");
        lock.lock();
        t1.start();
        Thread.sleep(500);
        System.out.println("主线程释放锁");
        lock.unlock();
        t1.join();


    }

    boolean hasCigarette = false;
    boolean hasTakeout = false;

    //ReentrantLock多条件变量
    @Test
    public void test24() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition cigarette = lock.newCondition();
        Condition takeout = lock.newCondition();

        //等香烟线程
        Thread t1 = new Thread() {
            @Override
            public void run() {
                lock.lock();
                //拿到锁，进入临界区
                try {
                    while (!hasCigarette) {
                        //没有外卖
                        System.out.println("没有香烟，继续睡觉");
                        //条件等待，可被打断
                        try {
                            cigarette.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("起来抽烟");
                } finally {
                    lock.unlock();
                }
            }
        };

        //等外卖线程
        Thread t2 = new Thread() {
            @Override
            public void run() {
                lock.lock();
                try {
                    while (!hasTakeout) {
                        //没有外卖
                        System.out.println("没有外卖，继续睡觉");
                        try {
                            takeout.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("起来吃外卖");
                } finally {
                    lock.unlock();
                }
            }
        };
        t1.start();
        t2.start();

        //送外卖线程
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.lock();
                try {
                    System.out.println("外卖到了");
                    hasTakeout = true;
                    takeout.signal();
                } finally {
                    lock.unlock();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.lock();
                try {
                    System.out.println("香烟到了");
                    hasCigarette = true;
                    cigarette.signal();
                } finally {
                    lock.unlock();
                }
            }
        }.start();

        t1.join();
        t2.join();
    }

    //两个线程交替打印奇偶数
    static int a = 0;
    Object lock = new Object();

    @Test
    public void test25() throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (a % 2 == 0) {
                        synchronized (lock) {
                            System.out.println("线程1：" + a);
                            a++;
                        }
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    } else {
                        System.out.println("线程1没拿到锁");
                    }
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (a % 2 != 0) {
                        synchronized (lock) {
                            System.out.println("线程2：" + a);
                            a++;
                        }
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    } else {
                        System.out.println("线程2没拿到锁");
                    }
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    //synchronized实现两个线程交替打印奇偶数
    int j = 0;

    @Test
    public void test26() throws InterruptedException {
        Object lock = new Object();

        //线程1打印偶数
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    synchronized (lock) {
                        if (j % 2 == 0) {
                            System.out.println("线程1：" + j);
                            j++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            lock.notifyAll();
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        //线程2打印奇数
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    synchronized (lock) {
                        if (j % 2 != 0) {
                            System.out.println("线程2：" + j);
                            j++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            lock.notifyAll();
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    //reentrantlock实现两个线程交替打印奇偶数
    int c = 0;

    @Test
    public void test27() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition single = lock.newCondition();
        Condition dou = lock.newCondition();
        //线程1打印偶数
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    lock.lock();
                    try {
                        if (c % 2 == 0) {
                            System.out.println("线程1：" + c);
                            c++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            dou.signal();
                            try {
                                single.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                single.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };

        //线程2打印偶数
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    lock.lock();
                    try {
                        if (c % 2 != 0) {
                            System.out.println("线程2：" + c);
                            c++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            single.signal();
                            try {
                                dou.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                dou.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };
        t1.start();
        t2.start();
        t1.join();
        t1.join();
    }

    //交替输出（三个线程轮流打印abc）——wait和notify
    //类定义标记位和循环次数，打印方法
    class WaitNotify {
        int flag; //标记位1,2,3
        int loopNumber;//循环次数

        public WaitNotify(int flag, int loopNumber) {
            this.flag = flag;
            this.loopNumber = loopNumber;
        }

        public void print(int nowFlag, int nextFlag, String context) {
            //循环次数
            for (int i = 0; i < loopNumber; i++) {
                //拿到锁就进入循环判断是否当前应该打印，如果不打印就进入等待
                //该打印就跳出循环，循环是防止虚假唤醒，因为每一次都会唤醒其它两个线程
                synchronized (this) {
                    while (true) {
                        if (nowFlag != this.flag) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                    System.out.println(context);
                    this.flag = nextFlag;
                    this.notifyAll();
                }
            }
        }
    }

    @Test
    public void test28() throws InterruptedException {
        WaitNotify waitNotify = new WaitNotify(1, 5);
        Thread t1 = new Thread() {
            @Override
            public void run() {
                waitNotify.print(1, 2, "a");
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                waitNotify.print(2, 3, "b");
            }
        };
        Thread t3 = new Thread() {
            @Override
            public void run() {
                waitNotify.print(3, 1, "c");
            }
        };
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    //交替输出（三个线程轮流打印abc）——await和signal
    //类定义标记位和循环次数，打印方法
    class AwaitSignal {
        int flag;
        int loopNumber;
        ReentrantLock lock;
        Condition firstCondition;
        Condition secondCondition;
        Condition thirdCondition;

        AwaitSignal(int flag, int loopNumber, ReentrantLock lock, Condition firstCondition,
                    Condition secondCondition, Condition thirdCondition) {
            this.flag = flag;
            this.loopNumber = loopNumber;
            this.lock = lock;
            this.firstCondition = firstCondition;
            this.secondCondition = secondCondition;
            this.thirdCondition = thirdCondition;
        }

        public void print(int nowFlag, int nextFlag, Condition nowCondition,
                          Condition nextCondition, String context) {
            for (int i = 0; i < loopNumber; i++) {
                lock.lock();
                try {
                    while (true) {
                        if (flag != nowFlag) {
                            try {
                                nowCondition.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                    System.out.println(context);
                    flag = nextFlag;
                    nextCondition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
    @Test
    public void test29() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition first = lock.newCondition();
        Condition second = lock.newCondition();
        Condition third = lock.newCondition();
        AwaitSignal awaitSignal = new AwaitSignal(1, 5, lock, first,
                second, third);

        Thread t1 = new Thread() {
            @Override
            public void run() {
                awaitSignal.print(1, 2, first, second, "a");
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                awaitSignal.print(2, 3, second,third, "b");
            }
        };
        Thread t3 = new Thread() {
            @Override
            public void run() {
                awaitSignal.print(3, 1, third,first, "c");
            }
        };

        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    //交替输出（三个线程轮流打印abc）——park unpark

    class ParkUnpark{
        public void print(String context,Thread thread){
            for(int i=0;i<5;i++){
                LockSupport.park();
                System.out.println(context);
                LockSupport.unpark(thread);
            }
        }
    }
    Thread t1;
    Thread t2;
    Thread t3;
    @Test
    public void test30() throws InterruptedException {
        ParkUnpark parkUnpark = new ParkUnpark();

        t1 = new Thread() {
            @Override
            public void run() {
                parkUnpark.print("a", t2);
            }
        };
        t2 = new Thread() {
            @Override
            public void run() {
                parkUnpark.print("b", t3);
            }
        };
        t3 = new Thread() {
            @Override
            public void run() {
                parkUnpark.print("c", t1);
            }
        };

        t1.start();
        t2.start();
        t3.start();
        LockSupport.unpark(t1);
        t1.join();
        t2.join();
        t3.join();

    }

    //交替输出（三个线程轮流打印abc）——await和signal
    class AwaitSignal2 extends ReentrantLock{
        //循环次数
        int loopTime;
        public AwaitSignal2(int loopTime){
            this.loopTime=loopTime;
        }
        //打印方法
        public void print(String context,Condition nowCondition,Condition nextCondition){
            for(int i=0;i<loopTime;i++){
                this.lock();
                try{
                    try {
                        nowCondition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(context);
                    nextCondition.signal();
                }finally {
                    this.unlock();
                }
            }
        }
    }

    @Test
    public void test31() throws InterruptedException {
        AwaitSignal2 awaitSignal2 = new AwaitSignal2(5);
        Condition first = awaitSignal2.newCondition();
        Condition second = awaitSignal2.newCondition();
        Condition third = awaitSignal2.newCondition();

        Thread t1 = new Thread() {
            @Override
            public void run() {
                awaitSignal2.print("a", first, second);
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                awaitSignal2.print("b", second,third);
            }
        };
        Thread t3 = new Thread() {
            @Override
            public void run() {
                awaitSignal2.print("c", third, first);
            }
        };
        t1.start();
        t2.start();
        t3.start();

        Thread.sleep(100);
        awaitSignal2.lock();
        try{
            first.signal();
        }finally {
            awaitSignal2.unlock();
        }
        t1.join();
        t2.join();
        t3.join();


    }

    volatile static boolean flag=true;
    @Test
    public void test32() throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (flag) {

                }
            }
        };
        t.start();
        Thread.sleep(2000);
        System.out.println("停止线程");
        flag=false;
        t.join();
    }

    static Integer balance=10000;
    //取钱的线程不安全实现
    @Test
    public void test33() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        //创建1000个-10元的线程
        for(int i=0;i<1000;i++){
            Thread t = new Thread() {
                @Override
                public void run() {
                    balance -= 10;
                }
            };
            threads.add(t);
        }
        //启动1000个线程
        for(Thread t:threads){
            t.start();
        }
        //等待这1000个线程结束
        for(Thread t:threads){
            t.join();
        }
        //查看余额
        System.out.println(balance);

    }

    //取钱的线程安全实现——有锁
    @Test
    public void test34() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        Object lock = new Object();
        //创建1000个-10元的线程
        for(int i=0;i<1000;i++){
            Thread t = new Thread() {
                @Override
                public void run() {
                    synchronized (lock){
                        balance -= 10;
                    }
                }
            };
            threads.add(t);
        }
        //启动1000个线程
        for(Thread t:threads){
            t.start();
        }
        //等待这1000个线程结束
        for(Thread t:threads){
            t.join();
        }
        //查看余额
        System.out.println(balance);

    }

    //取钱的线程安全实现——无锁cas
    static AtomicInteger balance2=new AtomicInteger(10000);
    @Test
    public void test35() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        //创建1000个-10元的线程
        for(int i=0;i<1000;i++){
            Thread t = new Thread() {
                @Override
                public void run() {
                   while(true){
                       int prev=balance2.get();
                       int next=prev-10;
                       if(balance2.compareAndSet(prev,next)){
                           break;
                       }
                   }
                }
            };
            threads.add(t);
        }
        //启动1000个线程
        for(Thread t:threads){
            t.start();
        }
        //等待这1000个线程结束
        for(Thread t:threads){
            t.join();
        }
        //查看余额
        System.out.println(balance2);
    }

    //原子引用类
    @Test
    public void test36(){
        AtomicReference<BigDecimal> ref=new AtomicReference(new BigDecimal("3.14"));
        while(true){
            BigDecimal prev = ref.get();
            BigDecimal next = prev.add(new BigDecimal("1"));
            if(ref.compareAndSet(prev, next)){
                break;
            }
        }
        System.out.println(ref.get());
    }

    @Test
    public void test37() throws InterruptedException {
        AtomicStampedReference<String> ref = new AtomicStampedReference<String>("A",0);
        //线程1修改A为B
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    //获取字符串
                    String prev = ref.getReference();
                    //获取版本号
                    int stamp = ref.getStamp();
                    if (ref.compareAndSet(prev, "B", stamp, stamp + 1)) {
                        System.out.println("A-->B");
                        break;
                    }
                }
            }
        };
        //线程1修改B为A
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    //获取字符串
                    String prev = ref.getReference();
                    //获取版本号
                    int stamp = ref.getStamp();
                    if (ref.compareAndSet(prev, "A", stamp, stamp + 1)) {
                        System.out.println("B-->A");
                        break;
                    }
                }
            }
        };
        String prev = ref.getReference();
        int stamp = ref.getStamp();
        System.out.println(prev);
        System.out.println(stamp);
        t1.start();
        Thread.sleep(500);
        t2.start();
        Thread.sleep(1000);
        boolean result = ref.compareAndSet(prev, "B", stamp, stamp + 1);
        System.out.println(result);
    }

    //数组的线程不安全性
    int[] arr=new int[10];
    @Test
    public void test38() throws ParseException, InterruptedException {
        ArrayList<Thread> list = new ArrayList<>();
        //开启10个线程
        for(int i=0;i<10;i++){
            int index=i;
            Thread t = new Thread() {
                @Override
                public void run() {
                    //每个线程对arr的每个位置上执行++操作，循环1000次
                    for (int i = 0; i < 10000; i++) {
                        arr[i%arr.length]++;
                    }
                }
            };
            list.add(t);
        }
        for(Thread t:list){
            t.start();
        }
        for(Thread t:list){
            t.join();
        }
        System.out.println(Arrays.toString(arr));
    }

    //AtomicIntegerArray 原子性
    AtomicIntegerArray array=new AtomicIntegerArray(10);
    @Test
    public void test41() throws InterruptedException {
        ArrayList<Thread> list = new ArrayList<>();
        //开启10个线程
        for(int i=0;i<10;i++){
            int index=i;
            Thread t = new Thread() {
                @Override
                public void run() {
                    //每个线程对arr的每个位置上执行++操作，循环1000次
                    for (int i = 0; i < 10000; i++) {
                        array.getAndIncrement(i%array.length());
                    }
                }
            };
            list.add(t);
        }
        for(Thread t:list){
            t.start();
        }
        for(Thread t:list){
            t.join();
        }
        System.out.println(array.toString());
    }

    //AtomicInteger[]实现AtomicIntegerArray
    AtomicInteger[] array2;
    @Test
    public void test44() throws InterruptedException {
        array2=new AtomicInteger[10];
        for(int i=0;i<10;i++){
            array2[i]=new AtomicInteger(0);
        }
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i=0;i<10;i++){
            Thread t = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        array2[i % 10].getAndIncrement();
                    }
                }
            };
            threads.add(t);
        }
        for(Thread t:threads){
            t.start();
        }
        for(Thread t:threads){
            t.join();
        }
        System.out.println(Arrays.toString(array2));
    }

    //AtomicInteger的原子性
    static AtomicInteger z=new AtomicInteger(0);
    @Test
    public void test39() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i=0;i<10000;i++){
            Thread t = new Thread() {
                @Override
                public void run() {
                    z.getAndIncrement();
                }
            };
            threads.add(t);
        }
        for(Thread t:threads){
            t.start();
        }
        for(Thread t:threads){
            t.join();
        }
        System.out.println(z);
    }

    //不安全类
    class Unsafe{
        int age=0;

        public void setAge(int age){
            this.age=age;
        }
    }
    Unsafe unsafe=new Unsafe();
    @Test
    public void test40() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i=0;i<100000;i++){
            Thread t1 = new Thread() {
                @Override
                public void run() {
                    unsafe.setAge(unsafe.age+1);
                }
            };
            threads.add(t1);
        }
        for(Thread t:threads){
            t.start();
        }
        for(Thread t:threads){
            t.join();
        }

        System.out.println(unsafe.age);
    }

    class Teacher{
        int id;
        String name;
    }

    //unsafe
    @Test
    public void test42() throws NoSuchFieldException, IllegalAccessException {
      //获取unsafe对象
        //反射获得属性对象theUnsafe(unsafe的实例）
        Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        //私有属性设置可访问
        theUnsafe.setAccessible(true);
        //获取实例对象
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
        System.out.println(unsafe);
      //利用unsafe对象对teacher进行cas操作
        Teacher teacher = new Teacher();
        //获取teacher类属性对象
        Field id = teacher.getClass().getDeclaredField("id");
        Field name = teacher.getClass().getDeclaredField("name");
        //计算属性偏移量
        long idOffset = unsafe.objectFieldOffset(id);
        long nameOffset = unsafe.objectFieldOffset(name);
        unsafe.compareAndSwapInt(teacher,idOffset,0,1);
        unsafe.compareAndSwapObject(teacher,nameOffset,null,"zhang");

        System.out.println(teacher.id+","+teacher.name);
    }

    //自定义连接池
      //连接
    class MockConnection implements Connection{

        @Override
        public Statement createStatement() throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return null;
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return null;
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {

        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return false;
        }

        @Override
        public void commit() throws SQLException {

        }

        @Override
        public void rollback() throws SQLException {

        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {

        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return false;
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {

        }

        @Override
        public String getCatalog() throws SQLException {
            return null;
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {

        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return 0;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return null;
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return null;
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

        }

        @Override
        public void setHoldability(int holdability) throws SQLException {

        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return null;
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return null;
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {

        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return null;
        }

        @Override
        public Clob createClob() throws SQLException {
            return null;
        }

        @Override
        public Blob createBlob() throws SQLException {
            return null;
        }

        @Override
        public NClob createNClob() throws SQLException {
            return null;
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return null;
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return false;
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {

        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {

        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return null;
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return null;
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return null;
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return null;
        }

        @Override
        public void setSchema(String schema) throws SQLException {

        }

        @Override
        public String getSchema() throws SQLException {
            return null;
        }

        @Override
        public void abort(Executor executor) throws SQLException {

        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return 0;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }

     //连接池
    class ConnectionPool{
        //连接池容量
         int size;
         //connection数组；
         Connection[] cons;
         //connection状态,0表示空闲，1表示繁忙
         AtomicInteger[] states;

         public ConnectionPool(int size){
             this.size=size;
             cons=new MockConnection[size];
             states=new AtomicInteger[size];
             for(int i=0;i<states.length;i++){
                 states[i]=new AtomicInteger(0);
             }
             for(int i=0;i<cons.length;i++){
                 cons[i]=new MockConnection();
             }
         }

         //获取con方法
         public Connection  getConnection(){
             while(true){
                 for(int i=0;i<states.length;i++){
                     //判断连接是否空闲
                     if(states[i].get()==0){
                         //如果空闲修改状态，因为是先判断再修改，有线程安全问题
                         if(states[i].compareAndSet(0,1)){
                             //修改成功，返回连接
                             System.out.println(Thread.currentThread().getName()+":get"+cons[i]);
                             return cons[i];
                         }
                     }
                 }
                 //遍历一遍未找到空闲连接，进入等待
                 synchronized (this){
                     try {
                         System.out.println(Thread.currentThread().getName()+":wait...");
                         wait();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
         //归还con方法
         public void free(Connection con){
             for(int i=0;i<states.length;i++){
                 if(cons[i]==con){
                     //是这个连接无误，修改状态
                     //此处不用考虑线程安全问题，因为只有这个连接可能来归还
                     System.out.println(Thread.currentThread().getName()+":free"+con);
                     states[i]=new AtomicInteger(0);
                     //唤醒等待线程
                     synchronized (this){
                         notify();
                     }
                     break;
                 }
             }
         }
     }
    @Test
    public void test43() throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(2);
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i=0;i<5;i++){
            Thread t = new Thread() {
                @Override
                public void run() {
                    Connection con = pool.getConnection();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pool.free(con);
                }
            };
            threads.add(t);
        }
        for(Thread t:threads){
            t.start();
        }
        for(Thread t:threads){
            t.join();
        }

    }



}
