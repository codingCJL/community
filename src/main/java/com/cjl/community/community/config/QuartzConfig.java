package com.cjl.community.community.config;

import com.cjl.community.community.quartz.AlphaJob;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author cjl
 * @date 2020/5/21 10:04
 */
//第一次才使用，配置->数据库->调用
@Configuration
public class QuartzConfig {
    //FactoryBean可简化bean的实例化过程
    @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return null;
    }
    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail jobDetail){
        return null;
    }
}
