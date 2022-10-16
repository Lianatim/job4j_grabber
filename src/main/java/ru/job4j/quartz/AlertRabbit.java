package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static Connection initConnection(Properties cfg) throws SQLException, ClassNotFoundException {
        Class.forName(cfg.getProperty("jdbc.driver"));
        return DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password"));
    }

    private static Properties loadConfigurations() {
        Properties cfg = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        try {
            Properties cfg = loadConfigurations();
            Connection cn = initConnection(cfg);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException | InterruptedException se) {
            se.printStackTrace();
        }
    }


    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement ps = connection.prepareStatement("insert into rabbit (created_date) values (?)")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}