package com.zwq.rabbitmqdemo;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * created by zwq on 2018/9/22
 */
@Service
public class RabbitService {
    /*
    消息接收确认机制：
    配置文件配置
    #spring.rabbitmq.listener.direct.acknowledge-mode=manual
    #spring.rabbitmq.listener.simple.acknowledge-mode=manual
    手动消息接收确认，需要消息确认代码，如果没确认，消息还会在队列里，下次启动程序还会再次收到。
    也可以选择不确认消息：放弃消息，或者重新放回队列，或者拒绝消息，
    抛异常只推送一次，消息还是在队列里。 拒绝消息或者重新放回队列会一直推送

    不启动手动确认，如果没产生异常会自动确认，如果异常或者写拒绝消息或者不确认消息重新返回队列都会一直发送消息，也可以放弃消息

    消息重试机制：
    配置消息重试功能开启，默认3次，1秒重试，可以配置。异常后重试次数满后进入死信队列
    如果是拒绝消息，不用开启消息重试可直接进入死信队列。
    关键是队列需要配置死信参数，参数键固定x-dead-letter-exchange，x-dead-letter-routing-key
     */
    @RabbitListener(queues = "task_queue")
    public void prin(String meesage, Channel channel, Message message) throws IOException {
        System.out.println(meesage);
        System.out.println(message.getMessageProperties().getDeliveryTag());
//        try {
        switch (meesage) {
            case "消费消息":
                System.out.println("消息消费成功");
                break;
            case "死信消息":
//            消息确认  如果不确认，消息还会在队列里，但是不会一直推送，下次启动的时候会再次推送，确认当次消息，如果是true是确认所有消费者
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//        } catch (Exception e) {
//            System.out.println("消息消费失败：" + e.getMessage());
//          ack返回false，并重新回到队列,会继续发送
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
//          ack返回false，并放弃消息
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
                //拒绝消息,并放回队列
//            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                System.out.println("消息消费失败，进入死信队列");
                //拒绝消息,并从队列删除 配置死信队列可以进入死信队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                break;
        }
//            throw new Exception("测试死信");
//        }
    }
    @RabbitListener(queues = "dead_queue")
    public void prinDead(String content,Message message) {
        System.out.println("死信队列发布消息");
        System.out.println(content);
        System.out.println(message.getMessageProperties().getDeliveryTag());
    }

    @RabbitListener(queues = "alert_queue")
    public void prinAlert(String content,Message message) {
        System.out.println("告警队列发布消息");
        System.out.println(content);
        System.out.println(message.getMessageProperties().getDeliveryTag());
    }
}
