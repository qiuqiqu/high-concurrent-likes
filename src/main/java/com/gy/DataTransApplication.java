package com.gy;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.management.ManagementFactory;

/**
 * @author yun.guo
 * @date 2024/12/24
 */
@EnableScheduling
@SpringBootApplication
@Slf4j
@MapperScan("com.gy.mapper")
public class DataTransApplication {

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(DataTransApplication.class);
            application.setBannerMode(Banner.Mode.OFF);
            application.addListeners(pidFileWriter());
            application.run(args);
            String logo = "\n (♥◠‿◠)ﾉﾞ  服务启动成功   ლ(´ڡ`ლ)ﾞ  \n";
            log.info(logo);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 打印pid
     */
    public static ApplicationPidFileWriter pidFileWriter() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return new ApplicationPidFileWriter("./pids/" + jvmName.split("@")[0] + ".pid");
    }
}
