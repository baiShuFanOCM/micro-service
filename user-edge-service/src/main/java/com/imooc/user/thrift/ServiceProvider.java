package com.imooc.user.thrift;

import com.imooc.thrift.message.MessageService;
import com.imooc.thrift.user.UserService;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by oracleOCM on 2019/1/13.
 */
@Component
public class ServiceProvider {
    @Value("${thrift.user.ip}")
    private  String serverIp;
    @Value("${thrift.user.port}")
    private  int serverPort;
    @Value("${thrift.message.ip}")
    private String messageIp;
    @Value("${thrift.message.port}")
    private int messsagePort;

    private enum  ServiceType {
        USER,
        MESSAGE
    }

    public UserService.Client getUserService(){
        return getService(serverIp,serverPort,ServiceType.USER);
    }

    public MessageService.Client getMessageService(){
        return getService(serverIp,serverPort,ServiceType.MESSAGE);
    }

    public <T> T getService(String ip,int port,ServiceType serviceType){

        TSocket socket =  new TSocket(ip,port,3000);
        TTransport transport =  new TFramedTransport(socket);
        try {
            transport.open();
        } catch (TTransportException e) {
            e.printStackTrace();
            return null;
        }
        TServiceClient client = null;
        TProtocol  protocol =  new TBinaryProtocol(transport);
        switch (serviceType){
            case USER:
                client = new UserService.Client(protocol);
                break;
            case MESSAGE:
                client =  new MessageService.Client(protocol);
                break;

        }
        return (T)client;

    }


}
