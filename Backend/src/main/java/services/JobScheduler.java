package services;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.ConnectionFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.util.HashMap;
import java.util.Map;
import static org.quartz.TriggerBuilder.newTrigger;

public class JobScheduler {

    final static Logger logger = LoggerFactory.getLogger(Scheduler.class);
    final static ConnectionFactory factory = new ConnectionFactory();

    public static void main(String[] args) throws Exception {
    	factory.setUri(System.getenv("CLOUDAMQP_URL"));
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.start();

        JobDetail notifyDetail = newJob(NotifierJob.class).build();

        Trigger notifyTrigger = newTrigger()
                .startNow()
                .withSchedule(repeatHourlyForever(24))
                .build();

        scheduler.scheduleJob(notifyDetail, notifyTrigger);
        
        JobDetail alertDetail = newJob(AlertJob.class).build();

        Trigger alertTrigger = newTrigger()
                .startNow()
                .withSchedule(repeatHourlyForever(24))
                .build();

        scheduler.scheduleJob(alertDetail, alertTrigger);
        
        
    }
    public static class NotifierJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                String queueName = "work-queue-1";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("x-ha-policy", "all");
                channel.queueDeclare(queueName, true, false, false, params);

                String msg = "Notify";
                byte[] body = msg.getBytes("UTF-8");
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
                logger.info("Message Sent: " + msg);
                connection.close();
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

    }
    public static class AlertJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                String queueName = "work-queue-1";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("x-ha-policy", "all");
                channel.queueDeclare(queueName, true, false, false, params);               

                String msg = "Crawl";
                byte[] body = msg.getBytes("UTF-8");
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
                logger.info("Message Sent: " + msg);
                connection.close();
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

    }
}
