import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WriteThreadPool {

    //测试代码
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(2, 1000, TimeUnit.MILLISECONDS, 10);
        for(int i=0;i<15;i++){
            int j=i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(j);

                }
            };
            //无限等待：如果队列满就等待，此时主线程进入等待
            threadPool.execute(task);
        }
    }

}

/*
* 阻塞队列
* */
class BlockingQueue<T> {

    //1.任务队列
    private Deque<T> queue = new ArrayDeque<>();
    //2.锁
    private ReentrantLock lock = new ReentrantLock();
    //3.生产者条件
    private Condition fullWaitSet = lock.newCondition();
    //4.消费者条件
    private Condition emptyWaitSet = lock.newCondition();
    //5.容量
    private int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    //消费者阻塞获取
    public T get() {
        lock.lock();
        try {
            //循环获取任务，如果队列为空则等待，不为空则跳出循环
            while (queue.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //获取第一个任务
            T t = queue.removeFirst();
            //唤醒生产者线程
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    //生产者阻塞添加
    public void add(T t) {
        lock.lock();
        try {
            //循环添加，如果队列满则等待
            while (queue.size() == capacity) {
                try {
                    System.out.println(t+":等待加入任务队列");
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //添加任务
            System.out.println(t+":加入队列");
            queue.addLast(t);
            //唤醒消费者线程
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    //消费者阻塞获取带超时
    public T tryGet(long time, TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(time);
            //循环获取任务，如果队列为空则等待，不为空则跳出循环
            while (queue.isEmpty()) {
                try {
                    if(nanos<=0){
                        return null;
                    }
                    nanos= emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //获取第一个任务
            T t = queue.removeFirst();
            //唤醒生产者线程
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    //生产者阻塞添加带超时
    public boolean tryAdd(T t, long time, TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(time);
            //循环添加，如果队列满则等待
            while (queue.size() == capacity) {
                try {
                    //被唤醒后返回的时间是剩下的时间
                    if (nanos <= 0) {
                        System.out.println(t+"任务添加失败");
                        return false;
                    }
                    nanos = fullWaitSet.awaitNanos(nanos);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //添加任务
            System.out.println(t+":加入队列");
            queue.addLast(t);
            //唤醒消费者线程
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }
}

/*
* 线程池
* */
class ThreadPool {
    //任务队列
    private BlockingQueue<Runnable> queue;
    //线程集合
    private HashSet<Worker> workerSet = new HashSet();
    //核心线程数
    private int coreNumber;
    //获取任务超时时间
    private long time;
    private TimeUnit unit;

    public ThreadPool(int coreNumber, long time, TimeUnit unit, int queueCapacity) {
        this.coreNumber = coreNumber;
        this.time = time;
        this.unit = unit;
        this.queue = new BlockingQueue<>(queueCapacity);
    }

    //执行任务的方法
    public void execute(Runnable task) {
        //如果任务数没有超过coreNumber,交给set
        //如果任务书超过coreNumber，加入任务队列
        //要加锁，queue和set不是线程安全的
        synchronized (workerSet) {
            if (workerSet.size() < coreNumber) {
                Worker worker = new Worker(task);
                workerSet.add(worker);
                System.out.println("新增worker："+worker);
                worker.start();
            } else {
                //添加任务策略：
                //1.无限等待：如果队列满就等待，此时主线程进入等待
                //queue.add(task);
                //2.超时放弃：如果队列已满等待一段时间后返回false，添加成功则是返回true
                queue.tryAdd(task, time, unit);
            }
        }
    }


    /*
    * 工作线程
    * */
    class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            //取任务策略：
            //1.无限等待：如果队列为空线程无限等待任务添加
            //while (task != null || (task = queue.get()) != null) {
            //2.超时移除：如果队列为空，线程等待一段时间后移除
            while (task != null || (task = queue.tryGet(time, unit)) != null) {
                //执行任务
                //1.第一次执行
                //2.后续执行
                try {
                    System.out.println(task+"：正在执行代码");
                    task.run();
                } catch (Exception e) {

                } finally {
                    task = null;
                }
            }
            //没有任务，退出循环,移除线程
            //要加锁，因为set集合不是线程安全
            synchronized (workerSet) {
                System.out.println("worker被移除");
                workerSet.remove(this);
            }
        }
    }
}

