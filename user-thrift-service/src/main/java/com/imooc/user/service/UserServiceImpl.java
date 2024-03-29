package com.imooc.user.service;

import com.imooc.thrift.user.UserInfo;
import com.imooc.thrift.user.UserService;
import com.imooc.user.mapper.UserMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by oracleOCM on 2019/1/10.
 */
@Service
public class UserServiceImpl implements UserService.Iface {

    @Autowired
    private UserMapper userMapper ;




    @Override
    public UserInfo getUserById(int id) throws TException {
        return userMapper.getUserById(id);
    }

    @Override
    public UserInfo getUserByName(String username) throws TException {
        return userMapper.getUserByName(username);
    }

    @Override
    public void registerUser(UserInfo userInfo) throws TException {
        userMapper.registerUser(userInfo);
    }
}
