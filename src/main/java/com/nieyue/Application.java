package com.nieyue;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.Live;
import com.nieyue.bean.ScheduleJob;
import com.nieyue.business.LiveBusiness;
import com.nieyue.business.SendLiveBusiness;
import com.nieyue.schedule.QuartzEventService;
import com.nieyue.service.LiveService;
import com.nieyue.service.PermissionService;
import com.nieyue.service.ScheduleJobService;
import com.nieyue.util.MyDom4jUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
//@EnableRedisHttpSession
//@Import({DynamicDataSourceRegister.class})
@ServletComponentScan
@EnableSwagger2
@MapperScan("com/nieyue/dao")
public class Application implements ApplicationListener<ApplicationReadyEvent> {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(
                        new ApiInfoBuilder()
                                .title("视频平台接口文档")
                                .description("视频平台接口文档1.0版本")
                                .version("1.0")
                                .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.nieyue.controller"))
                .paths(PathSelectors.any())
                .build();
    }
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {

        return new WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>() {
            @Override
            public void customize(ConfigurableServletWebServerFactory   container) {
                ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/home/401.html");
                ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/home/404.html");
                ErrorPage error406Page = new ErrorPage(HttpStatus.NOT_ACCEPTABLE, "/home/404.html");
                ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/home/404.html");

                container.addErrorPages( error401Page);
                container.addErrorPages( error404Page);
                container.addErrorPages( error406Page);
                container.addErrorPages( error500Page);
            }
        };
    }

    @Autowired
    PermissionService permissionService;
    @Autowired
    LiveService liveService;
    @Autowired
    LiveBusiness liveBusiness;
    @Autowired
    ScheduleJobService scheduleJobService;
    @Autowired
    QuartzEventService quartzEventService;
    @Autowired
    SendLiveBusiness sendLiveBusiness;
    /**
     * 容器初始化
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //初始化权限列表
        permissionService.initPermission();
        //初始化任务计划
        List<ScheduleJob> l = scheduleJobService.simplelist(null);
        l.forEach(sj -> {
            quartzEventService.addScheduleJob(sj);
        });
        //杀掉ffmpeg-amd64进程
       /* String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){
            //System.out.println(os + " can't gunzip");
        }else{
            String cmd="ps -ef | grep ffmpeg-amd64 | awk '{print $2}' |xargs kill -9";
            try {
                Process process = ExecUtil.exec(cmd);
                ExecUtil.stop(process);
            } catch (IOException e) {
            }
        }*/
        //初始化直播
        Wrapper<Live> wrapper=new EntityWrapper<>();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("status", 1);
        wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
        List<Live> ll = liveService.simplelist(wrapper);
        ll.forEach(live->{
            try {
                /*JavaCVRecord jcv = new JavaCVRecord(live);
                jcv.stream();
                jcv.start();
                //成功就放入
                shm.put("JavaCVRecord"+live.getLiveId(),jcv);*/
                liveBusiness.startLive(live);
               // liveBusiness.igoreStartLive(live);
            } catch (Exception e) {
                live.setStatus(2);//停止
                liveService.update(live);
            }
        });
        //发送直播
        sendLiveBusiness.send();
       /* Thread thh = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        this.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                    Object lbqo = shm.get("liveRestart");
                    if (ObjectUtils.isEmpty(lbqo)) {
                        continue;
                    } else {
                        BlockingQueue<Live>  lbq = (LinkedBlockingQueue<Live>) lbqo;
                        try {
                            Live  live = lbq.take();
                            if (live != null) {
                                JavaCVRecord jcv = new JavaCVRecord(live);
                                jcv.stream();
                                jcv.start();
                                shm.put("JavaCVRecord" + live.getLiveId(), jcv);
                            }
                        } catch (InterruptedException e) {
                        }
                    }

                }
            }
        };
        thh.start();*/
    }
}
