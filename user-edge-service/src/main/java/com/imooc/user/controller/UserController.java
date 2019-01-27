package com.imooc.user.controller;

import com.imooc.thrift.user.UserInfo;
import com.imooc.user.dto.UserDTO;
import com.imooc.user.redis.RedisClient;
import com.imooc.user.response.LoginResponse;
import com.imooc.user.response.Response;
import com.imooc.user.thrift.ServiceProvider;
import org.apache.thrift.TException;
import org.apache.tomcat.util.buf.HexUtils;
import  org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by oracleOCM on 2019/1/13.
 */
@Controller
public class UserController {
    @Autowired
    private ServiceProvider serviceProvider;
    @Autowired
    private RedisClient redisClient ;
    @RequestMapping(value ="/login",method = RequestMethod.POST)
    @ResponseBody
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
        redisClient.set(token ,toDTO(userInfo),3600);
        return new  LoginResponse(token);

    }
    @RequestMapping(value = "/sendVerifyCode",method = RequestMethod.POST)
    @ResponseBody
    public Response sendVerifyCode( @RequestParam(value = "mobile",required = false) String mobile,
                                    @RequestParam(value = "email",required = false) String email){
        String code = randomCode("012456789",6);
        String message =  "Verify code i:"+code;
        boolean   result  = false;
        try {
            if(StringUtils.isNotBlank(email)){

                result = serviceProvider.getMessageService().sendEmailMessage(email,code);
                redisClient.set("email",code);

            }else if(StringUtils.isNotBlank(mobile)){
                result = serviceProvider.getMessageService().sendMobileMessage(mobile,code);
                redisClient.set("mobile",code);
            }else{
                return Response.MOBILE_OR_EMAIL_REQUIRED;
            }
            if (!result){
                return Response.SEND_VERIFYCODE_FAILED;
            }

        } catch (TException e) {
            e.printStackTrace();
            return Response.exception(e);
        }
        return Response.SUCCESS;
    }
    @RequestMapping(value="/registry",method = RequestMethod.POST)
    @ResponseBody
    public Response register(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam(value = "mobile",required = false) String mobile,
                             @RequestParam(value = "email",required = false) String email,
                             @RequestParam(value = "verifyCode",required = false)String verifyCode){
        if(StringUtils.isBlank(mobile) && StringUtils.isBlank(email)){
            return Response.MOBILE_OR_EMAIL_REQUIRED;
        }
        if(StringUtils.isNotBlank(mobile)){
          String redisCode =  redisClient.get(mobile);
          if(!verifyCode.equals(redisCode)){
              return Response.VERIFY_CODE_INVALID;
          }

        }else{
            String redisCode  = redisClient.get(email);
            if(!verifyCode.equals(redisCode)){
                return Response.VERIFY_CODE_INVALID;
            }
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setUsername(md5(password));
        userInfo.setEmail(email);
        userInfo.setMobile(mobile);
        try {
            serviceProvider.getUserService().registerUser(userInfo);
        } catch (TException e) {
            e.printStackTrace();
            return Response.exception(e);
        }
        return Response.SUCCESS;
    }

    private UserDTO toDTO(UserInfo userInfo) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(userInfo,userDTO);
        return  userDTO;
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
