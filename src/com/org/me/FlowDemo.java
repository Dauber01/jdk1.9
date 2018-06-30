package com.org.me;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * 回压带中间过程的测试
 *
 * @author Lucifer
 * @date 2018／06／30 13:53
 */
public class FlowDemo {

    public static void main(String[] args) throws InterruptedException{
        //定义发布者,使用的jdk9自带的
        SubmissionPublisher submissionPublisher = new SubmissionPublisher();
        //定义一个处理器，对数据进行处理
        MyProcess myProcess = new MyProcess();
        //将发布者与处理器之间建立联系
        submissionPublisher.subscribe(myProcess);
        //定义订阅者
        Flow.Subscriber<String> subscriber = new Flow.Subscriber<String>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                //保持订阅关系,需要用它给生产者响应
                this.subscription = subscription;
                //请求一个数据
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                //对接受到的数据进行处理
                System.out.println("接收到的方法: " + item);
                //处理完数据之后,再次请求数据
                this.subscription.request(1);
                //需要的数据已经全部处理完毕,结束当次任务,告诉发布者,不再接受任何数据了
                //this.subscription.cancel();
            }

            @Override
            public void onError(Throwable throwable) {
                //出了异常
                throwable.printStackTrace();
                //告诉发布者不再接受数据
                this.subscription.cancel();
            }

            @Override
            public void onComplete() {
                //数据处理结束,既流close的时候会被出发
                System.out.println("数据处理结束");
            }
        };
        //将处理器和最终的订阅者建立关系
        myProcess.subscribe(subscriber);
        //通过发布者发布数据,同时发布者关闭
        submissionPublisher.submit(-11);
        submissionPublisher.submit(2);
        //发布者关闭
        submissionPublisher.close();
        //暂时性停止主线程,给开启的消费者提供时间
        Thread.currentThread().join(1000);
    }

}

class MyProcess extends SubmissionPublisher<String>
    implements Flow.Processor<Integer, String>{

    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        //保持订阅关系,需要该方法给发布者提供反馈
        this.subscription = subscription;
        //请求一条数据进行处理
        subscription.request(1);
    }

    @Override
    public void onNext(Integer item) {
        //接受到一个数据,开始进行处理
        System.out.println("数据处理开始: " + item);
        if (item > 0){
            this.submit("推送数据,并进行格式转换" + item);
        }
        //再次像发布者请求数据
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

}
