package com.zwq.rabbitmqdemo;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * created by zwq on 2018/9/22
 */
@Configuration
public class RabbitMqConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    final String DEAD_EXCHANGE="dead_exchange";
    final String DEAD_ROUTING="dead_routing";
    final String DEAD_QUEUE="dead_queue";
    final String ALERT_QUEUE="alert_queue";
    final String ALERT_EXCHANGE="alert_exchange";
    final String TASK_QUEUE="task_queue";
    final String TASK_EXCHANGE="task_exchange";
    final String TASK_ROUTINGE="task_routing";
    final String DELAY_QUEUE = "delay_queue";
    final String DELAY_ROUTINGE ="delay_routing";


    private RabbitTemplate rabbitTemplate;

    public RabbitMqConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

//    /**
//     * 消费交换器
//     */
//    @Bean("task")
//    public FanoutExchange test1Exchange() {
//        return new FanoutExchange("task.exchange");
//    }

    /**
     * 消费交换器
     */
    @Bean("task")
    public DirectExchange taskExchange() {
//      关联告警交换器
        Map<String, Object> args = new HashMap<>();
        args.put("alternate-exchange", ALERT_EXCHANGE);
        return new DirectExchange(TASK_EXCHANGE,true,false,args);
    }

    /**
     * 告警交换器
     */
    @Bean
    public FanoutExchange alertExchange() {
        FanoutExchange alertExchange = new FanoutExchange(ALERT_EXCHANGE);
//        设置告警交换器表示内部交换器到交换器
        alertExchange.setInternal(true);
        return alertExchange;
    }
    /**
     * 死信交换器
     */
    @Bean("dead")
    public DirectExchange deadExchange() {
        return new DirectExchange(DEAD_EXCHANGE);
    }






    /**
     * 死信队列
     */
    @Bean
    public Queue deadQueue() {
        return new Queue(DEAD_QUEUE);
    }

    /**
     * 消费队列
     */
    @Bean
    public Queue taskQueue() {
//      配置死信队列关键步骤，参数键固定，routing根据类型决定是否必须
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_ROUTING);
        return new Queue(TASK_QUEUE,true,false,false,args);
    }

    /**
     * 告警队列
     */
    @Bean
    public Queue alertQueue() {
        return new Queue(ALERT_QUEUE);
    }
    /**
     * 延时队列
     */
    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_ROUTING);
        return new Queue(DELAY_QUEUE,true,false,false,args);
    }

    @Bean
    Binding bindingDeadExchange(Queue deadQueue,@Qualifier("dead")DirectExchange exchange) {
        return BindingBuilder.bind(deadQueue).to(exchange).with(DEAD_ROUTING);
    }


    @Bean
    Binding bindingTaskExchange(Queue taskQueue,@Qualifier("task") DirectExchange taskExchange) {
        return BindingBuilder.bind(taskQueue).to(taskExchange).with(TASK_ROUTINGE);
    }

    @Bean
    Binding bindingDelayExchange(Queue delayQueue,@Qualifier("task") DirectExchange taskExchange) {
        return BindingBuilder.bind(delayQueue).to(taskExchange).with(DELAY_ROUTINGE);
    }

    @Bean
    Binding bindingAlertExchange(Queue alertQueue, FanoutExchange alertExchange) {
//        FanoutExchange 忽略路由键,但是发送消息路由键参数必须有，可以随便填
        return BindingBuilder.bind(alertQueue).to(alertExchange);
    }


    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println("消息唯一标识：" + correlationData);
        System.out.println("确认结果：" + b);
        System.out.println("失败原因：" + s);
    }

    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        System.out.println("消息主体 message："+message);
        System.out.println("消息主体 message："+ Arrays.toString(message.getBody()));
        System.out.println("消息主体 message："+i);
        System.out.println("描述："+s);
        System.out.println("消息使用的交换器："+s1);
        System.out.println("消息使用的路由："+s2);
    }



}
