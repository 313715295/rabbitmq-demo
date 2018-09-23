package com.zwq.rabbitmqdemo;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * created by zwq on 2018/9/22
 */
@RestController
public class TestController {
    AmqpAdmin amqpAdmin;
    RabbitTemplate rabbitTemplate;

    public TestController(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/test1")
    public String test1() {
        amqpAdmin.declareExchange(new FanoutExchange("test1.exchange"));
        amqpAdmin.declareQueue(new Queue("test1.queue", true));
        amqpAdmin.declareBinding(new Binding("test1.queue", Binding.DestinationType.QUEUE,
                "test1.exchange", "test1.routingKey", null));

        return "success";
    }

    @GetMapping("/test2")
    public String test2() {
        amqpAdmin.declareExchange(new FanoutExchange("test2.exchange"));
        amqpAdmin.declareQueue(new Queue("test2.queue", true));
        amqpAdmin.declareBinding(new Binding("test2.queue", Binding.DestinationType.QUEUE,
                "test2.exchange", "test2.routingKey", null));

        return "success";
    }
    @GetMapping("send")
    public String sendMsg() {
        rabbitTemplate.convertAndSend("task_exchange","task_routing","消费消息");
        return "success";
    }
    @GetMapping("send1")
    public String send1Msg() {
        rabbitTemplate.convertAndSend("task_exchange","","测试告警队列");
        return "success";
    }

    @GetMapping("send2")
    public String send2Msg() {
        rabbitTemplate.convertAndSend("task_exchange","task_routing","死信消息");
        return "success";
    }
    @GetMapping("send3")
    public String send3Msg() {
        String content = "10秒延迟队列消息";

        rabbitTemplate.convertAndSend("task_exchange","delay_routing",content,message -> {
            message.getMessageProperties().setExpiration("10000");
            return message;
        });
        return "success";
    }
    @GetMapping("/test3")
    public String test3() {
        amqpAdmin.declareQueue(new Queue("test3.queue", true));
        amqpAdmin.declareBinding(new Binding("test3.queue", Binding.DestinationType.QUEUE,
                "test1.exchange", "test1.routingKey", null));

        return "success";
    }
}
