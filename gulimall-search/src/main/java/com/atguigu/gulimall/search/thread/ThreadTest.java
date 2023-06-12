package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @Author:luosheng
 * @Date:2023-05-24 11:18
 * @Description:多线程练习
 */
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    //创建异步对象
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main.......start.............");
        //相当于使用CompletableFuture启动runAsync启动了一个异步任务
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("执行结果" + i);
//        }, executor);

        /**
         * 方法完成后的感知
         */
/*        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("执行结果" + i);
            return i;
        }, executor).whenComplete((res, excption) -> {//第一个是结果，第二个是异常
            //虽然能得到异常的信息，单没法修改返回数据
            System.out.println("异步任务执行成功了...结果是:" + res + "异常是:" + excption);
        }).exceptionally(throwable -> {
            //他就可以返回我们默认值，可以感知异常
            return 10;
        });

        Integer integer = future.get();
        System.out.println("main............end.........." + integer);
*/
        /**
         * 方法执行完成后的处理
         */
/*        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("执行结果" + i);
            return i;
        }, executor).handle((res, thr) -> {
            if (res != null) {
                return res * 2 ;
            }
            if (thr!=null){
                return 0;
            }
            return 0;
        });

         Integer integer = future.get();
        System.out.println("main............end.........." + integer);
        */


        /**
         * 线程串行化
         * 1) 、thenRun: 不能获取到上一步的执行结果 ，无返回值
         *
         * .thenRunAsync(() -> {
         *             System.out.println("任务2启动了...");
         *         }, executor);
         * 2. thenAcceptAsync能接受上一步结果，但是无返回值
         * 3.thenAcceptAsync：；能接受上一步结果，有返回值
         */
/*       CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("执行结果" + i);
            return i;
        }, executor);

//        Integer integer = future1.get();
        System.out.println("main............end.........." );*/
        //组合任务
/*        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1结果" + i);
            return i;
        }, executor);


        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程" + Thread.currentThread().getId());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2结束");
            return "Hello";
        }, executor);*/
        /**
         * 两个都完成,都要完成 才能执行任务三
         */
//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("任务3开始");
//        },executor);

/*        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
            System.out.println("任务3开始执行....我可以拿到之前的结果:" + f1 + "--->" + f2);
        }, executor);*/

//        CompletableFuture<String> stringCompletableFuture = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return "我是可有有返回值且可以得到前面的结果:" + f1 + ":" + f2;
//        }, executor);
//        System.out.println(stringCompletableFuture.get());

        /**
         * 两个任务，只要完成一个，我们就执行任务三
         * //runAfterEitherAsync :不感知结果，自己业务返回值
         * acceptEitherAsync :可以感知结果，自己没有返回值
         * applyToEitherAsync: 可以感知结果，自己有返回值
         */

/*        future01.runAfterEitherAsync(future02,()->{
            System.out.println("任务3开始");
        },executor);*/
//                future01.acceptEitherAsync(future02, (res) -> {
//            System.out.println("任务3开始执行....我可以拿到之前的结果:" + res);
//        }, executor);

//        CompletableFuture<String> stringCompletableFuture = future01.applyToEitherAsync(future02, (res) -> {
//            return "我是可有有返回值且可以得到前面的结果:" + res;
//        }, executor);
//        System.out.println(stringCompletableFuture.get());
        CompletableFuture<String> funtureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello.jpg";
        }, executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "黑色+256G";
        }, executor);

        CompletableFuture<String> funtureDesc = CompletableFuture.supplyAsync(() -> {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品介绍");
            return "华为";
        }, executor);

        //只有他们三个都做完
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(funtureImg, funtureDesc, futureAttr);
        //等待所有的结果完成，也就是等待上面三个线程都做完
        //  allOf.get();//如果我们没有它，就可能会出现，我们还没执行完呢 main.....end 就执行了  (我们还可以有一种写法就是，把所有的线程都.get()一下)
//        System.out.println("main....end...."+funtureDesc.get()+"---->"+funtureImg.get()+"--->"+futureAttr.get());
        //有一个线程执行完就可以
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(funtureImg, funtureDesc, futureAttr);
        System.out.println("main....end...." + anyOf.get());

    }


    public void test(String[] args) throws Exception {
        //第一种线程 继承 Thread类
//        Thread1 thread1 = new Thread1();
//        thread1.run();
        //第二种线程 实现 Runnable接口
//        Runnable1 runnable1 = new Runnable1();
//        new Thread(runnable1).start();
        //第三种线程 实现Callable 接口+ futureTask  (他是可以拿到返回值的接口)
//        FutureTask<Integer> integerFutureTask = new FutureTask<Integer>(new Callable1());
//        new Thread(integerFutureTask).start();
//        integerFutureTask.get();

        //线程池[ExecutorService]

        //在我们业务代码里面，这三种启动线程的方式都不会怎么使用，【将所有的多线程异步任务都交给线程池执行】

        //当前系统中池只有一两个，每个异步任务，提交给线程池让它自己去执行就行
        // executorService.submit(new Runnable1());//使用线程池  执行Runnable1
        //1.创建:
//         1) Executors
        // 2.new ThreadPoolExecutor

        //Future，可以获取异步结果
        /**
         *
         线程池中的七大参数
         corePoolSize:[5] 核心线程数[一直存在(除非设置了allowCoreThreadTimeOut)]; 线程池，创建好以后就准备就绪的线程数量，就等待来异步任务去执行
         上面那个[5] 就相当于 Thread thread=new Thread(); thread.start() 五次
         maximumPoolSize:[200] 最大线程数量；  控制资源的
         keepAliveTime:存活时间.如果当前的线程数量大于core数量 。
         释放空闲的线程(maximumPoolSize-corePoolSize)。只要线程空闲大于指定的keepAliveTime
         unit :时间单位
         BlockingQueue<Runnable> workQueue:阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。
         只要有线程空闲，就回去队列里面取出新的任务继续执行。
         threadFactory:线程创建的工厂.
         RejectedExecutionHandler handler:如果队列满了，按照我们指定的拒绝策略拒绝执行任务
         工作顺序
         1.线程池创建，准备好core数量的核心线程，准备接受任务
         1.1如果 core我们的线程满了，就将再进来的任务放入阻塞队列中，空闲的core就会自己去阻塞队列获取任务执行
         1.2 阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         1.3 max满了就用RejectedExecutionHandler拒绝任务
         1.4 max都执行完成，有很多空闲，在指定的时间keepAliveTime以后，释放max-core这些线程
         new LinkedBlockingDeque<>() :默认是Integer的最大值，

         经典面试题: core 7; max 20, queue:50,   当100个并发进来怎么分配的；
         有7个core核心直接执行，50个进入到队列当中 ，然后再开13个进行执行，剩下的30个就会使用拒绝策略。
         如果不想抛弃还要执行。
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),//指定为10w个阻塞队列里
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
//  Executors.newCachedThreadPool() core 是0 所有都可回收
        //Executors.newFixedThreadPool()  固定大小，core=max;都不可回收
        //Executors.newScheduledThreadPool()定时任务的线程池
//Executors.newSingleThreadExecutor() 单线程的线程池，后台从队列里面获取任务，挨个执行
    }

    public static class Thread1 extends Thread {
        @Override
        public void run() {
            System.out.println("线程开始了");
            int i = 10 / 2;
            System.out.println("结果" + i);
        }
    }


    public static class Runnable1 implements Runnable {

        @Override
        public void run() {
            System.out.println("线程开始了");
            int i = 10 / 2;
            System.out.println("结果" + i);
        }
    }

    public static class Callable1 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("线程开始了");
            int i = 10 / 2;
            System.out.println("结果" + i);
            return i;
        }
    }


    //


//
//    public static void main(String[] args) throws Exception {
//        //继承Thread
//      /*  Thread1 thread = new Thread1();
//        thread.start();//启动线程
//        */
//
//        //实现Runnable接口
//     /*   Runnable1 runnable1 = new Runnable1();
//        new Thread(runnable1).start();*/
//
/////**
//// * 实现Callable接口 + FutureTask (可以拿到返回结果，可以处理异常)
//// */
////        FutureTask<Integer> integerFutureTask = new FutureTask<Integer>(new Callable1());
////        Thread thread = new Thread(integerFutureTask);
////        thread.start();
////        //阻塞等待整个线程执行完成，获取返回结果·
////        Integer integer = integerFutureTask.get();
////        System.out.println(integer);
//
//        //线程池
//              给线程池直接提交任务
    //区别
    /*
     * 第一种第二种不饿能得到返回值，三可以得到返回值
     *   1、2、3都不能控制资源
     *   线程池 可以控制资源，性能稳定,
     * */
//    }
//
//    public static class Thread1 extends Thread {
//        @Override
//        public void run() {
//            System.out.println("当前线程id" + Thread.currentThread().getId());
//            //一个内容
//            int i = 10 / 2;
//            //输出结果
//            System.out.println("运行结果" + i);
//        }
//    }
//
//
//    public static class Runnable1 implements Runnable {
//        @Override
//        public void run() {
//            //获取当前线程id
//            System.out.println("当前线程id" + Thread.currentThread().getId());
//            //一个内容
//            int i = 10 / 2;
//            //输出结果
//            System.out.println("运行结果" + i);
//        }
//    }
//
//
//    public static class Callable1 implements Callable<Integer> {
//        @Override
//        public Integer call() throws Exception {
//            //获取当前线程id
//            System.out.println("当前线程id" + Thread.currentThread().getId());
//            //一个内容
//            int i = 10 / 2;
//            //输出结果
//            System.out.println("运行结果" + i);
//            return i;
//        }
//    }


}
