package com.org.me;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * 测试1.9版本响应式流的背压功能
 * @author Lucifer
 * @date 2018／06／30 11:55
 */
public class ContractTest {

    public static void main(String[] args) throws Exception{
        //直接使用jdk9自带的 SubmissionPublisher类,该类实现了Publisher接口
        SubmissionPublisher<Integer> submissionPublisher = new SubmissionPublisher<Integer>();
        //定义订阅者
        Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<Integer>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                //保持订阅关系,需要用它给生产者响应
                this.subscription = subscription;
                //请求一个数据
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                //对接受到的数据进行处理
                System.out.println("接收到的方法: " + item);
                try {
                    Thread.sleep(3000);
                }catch (InterruptedException e){

                }
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
        //发布者与订阅者建立订阅关系
        submissionPublisher.subscribe(subscriber);
        //产生数据,并发布,这里忽略生产过程
        for (int i = 0; i < 1000; i++) {
            System.out.println("生产者产生数据: " + i);
            submissionPublisher.submit(i);
        }
        //结束后,关闭生产者，在正式的生产环境中,用try,catch和finally确保流可以正确关闭
        submissionPublisher.close();
        //主线程延迟停止,否则还没有进行消费,线程就会结束
        Thread.currentThread().join(1000);
    }

}
