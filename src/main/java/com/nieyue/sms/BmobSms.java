package com.nieyue.sms;

import net.sf.json.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Bmob验证码
 */
@Configuration
public class BmobSms {

    /**发送验证码
     * @param recNum 手机号
     * @return
     */
    public  Boolean sendSms(String recNum) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api2.bmob.cn/1/requestSmsCode");
        httpPost.addHeader("X-Bmob-Application-Id","0df5e167984a8f9cee07933a7b8c5c76");
        httpPost.addHeader("X-Bmob-REST-API-Key","5279af777d6112a63d846cdc6cb5a433");
        httpPost.addHeader("Content-Type","application/json");
        JSONObject rjson=new JSONObject();
        rjson.put("mobilePhoneNumber",recNum);
        rjson.put("template","sms001");
        httpPost.setEntity(new StringEntity(rjson.toString(),"utf-8"));
        CloseableHttpResponse response = client.execute(httpPost);
        String result ;
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            result = EntityUtils.toString(response.getEntity(),"utf-8");// 返回json格式：
            //response = JSONObject.fromObject(result);

            return true;
        }else{
            return false;

        }

    }
    /**验证验证码
     * @param recNum 手机号
     * @param code  验证码
     * @return
     */
    public  Boolean verifySms(String recNum,String code) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api2.bmob.cn/1/verifySmsCode/"+code);
        httpPost.addHeader("X-Bmob-Application-Id","0df5e167984a8f9cee07933a7b8c5c76");
        httpPost.addHeader("X-Bmob-REST-API-Key","5279af777d6112a63d846cdc6cb5a433");
        httpPost.addHeader("Content-Type","application/json");
        JSONObject rjson=new JSONObject();
        rjson.put("mobilePhoneNumber",recNum);
        httpPost.setEntity(new StringEntity(rjson.toString(),"utf-8"));
        CloseableHttpResponse response = client.execute(httpPost);
        String result ;
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            result = EntityUtils.toString(response.getEntity(),"utf-8");// 返回json格式：
            //response = JSONObject.fromObject(result);
           return true;
        }else{
            System.out.println(JSONObject.fromObject(result = EntityUtils.toString(response.getEntity(),"utf-8")));
            return false;
        }

    }

    public static void main(String[] args) throws IOException {
       // boolean b=new BmobSms().sendSms("15111336587");

        Boolean b = new BmobSms().verifySms("15111336587", "795130");
        System.out.println(b);
    }
}
