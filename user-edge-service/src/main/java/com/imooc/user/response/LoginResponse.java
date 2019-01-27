package com.imooc.user.response;

/**
 * Created by oracleOCM on 2019/1/14.
 */
public class LoginResponse extends Response {
    private String token;
    public LoginResponse(String token){
        this.token= token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
