package com.imooc.user.controller;

import com.imooc.thrift.user.UserInfo;
import com.imooc.user.response.Response;
import com.imooc.user.thrift.ServiceProvider;
import org.apache.thrift.TException;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by oracleOCM on 2019/1/13.
 */
@Controller
public class UserController {
    @Autowired
    private ServiceProvider serviceProvider;
    @RequestMapping(value ="/login",method = RequestMethod.POST)
    public Response login(@RequestParam("username")String username,
                          @RequestParam("password")String password){
        //1.验证用户名和密码
        UserInfo userInfo = null;
        try {
            userInfo = serviceProvider.getUserService().getUserByName(username);
        } catch (TException e) {
            e.printStackTrace();
            return Response.USERNAME_PASSWORD_INVALID;
        }
        if (userInfo == null){
            return  Response.USERNAME_PASSWORD_INVALID;
        }
        if(!userInfo.getPassword().equalsIgnoreCase(md5(password))){
            return Response.USERNAME_PASSWORD_INVALID;
        }
        //2.生成token
        String token = genToken();
        //3.缓存用户
        return  null;

    }



    private String genToken() {
        String tokenStr =  randomCode("0123456789abcdefghijklmnopqrstuvwxyz",32);
        return tokenStr;
    }

    private String randomCode(String s, int size) {
        StringBuilder  result =new StringBuilder(size);
        Random random  = new Random();
        for(int i=0;i<size;i++){
            int loc =  random.nextInt(s.length());
            result.append(s.charAt(loc));

        }
        return result.toString();
    }

    private String md5(String password) {
        try {
            MessageDigest  md5 =  MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(password.getBytes("utf-8"));
            return HexUtils.toHexString(md5Bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
