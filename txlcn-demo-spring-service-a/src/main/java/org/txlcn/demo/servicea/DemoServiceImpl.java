package org.txlcn.demo.servicea;

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.tracing.TracingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.txlcn.demo.common.db.domain.Demo;
import org.txlcn.demo.common.spring.ServiceBClient;
import org.txlcn.demo.common.spring.ServiceCClient;

import java.util.Date;
import java.util.Objects;

/**
 * Description:
 * Date: 2018/12/25
 *
 * @author ujued
 */
@Service
@Slf4j
public class DemoServiceImpl implements DemoService {
    @Autowired
    private  DemoMapper demoMapper;
    @Autowired
    private  ServiceBClient serviceBClient;

    @Autowired
    private  ServiceCClient serviceCClient;
    @Autowired
    private  RestTemplate restTemplate;


    @Override
    public String execute(String value, String exFlag) {
        // step1. call remote ServiceD
//        String dResp = serviceBClient.rpc(value);

        String dResp = restTemplate.getForObject("http://127.0.0.1:12002/rpc?value=" + value, String.class);

        // step2. call remote ServiceE
        String eResp = serviceCClient.rpc(value);

        // step3. execute local transaction
        Demo demo = new Demo();
        demo.setGroupId(TracingContext.tracing().groupId());
        demo.setDemoField(value);
        demo.setCreateTime(new Date());
        demo.setAppName(Transactions.getApplicationId());
        demoMapper.save(demo);

        // 置异常标志，DTX 回滚
        if (Objects.nonNull(exFlag)) {
            throw new IllegalStateException("by exFlag");
        }

        return dResp + " > " + eResp + " > " + "ok-service-a";
    }
}
