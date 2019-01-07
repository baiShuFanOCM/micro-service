from message.api import MessageService
from thrift.transport import TSocket,TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer
import smtplib
from email.mime.text import MIMEText
from email.header import Header


sender = "oracleocm@126.com"
authcode = "2019Bjj123"
class MessageServiceHandler:
    def sendMobileMessage(self, mobile, message):
        print ("sendMobileMessage,mobile:"+mobile+"message:"+message)
        return True

    def sendEmailMessage(self, email, message):
        print ("sendEmailMessage,email:"+email+"message:"+message)
        messageObj = MIMEText(message,"plain","utf-8")
        messageObj['From'] = sender
        messageObj['To']=  email
        messageObj['subject']=  Header('oralceocm Email','utf-8')
        try:
            smptObj= smtplib.SMTP('smpt.126.com')
            smptObj.login(sender,authcode)
            smptObj.sendmail(sender,[email],messageObj.as_string())
            print("send email success")
            return True
        except smtplib.SMTPException:
            print("send Email failed")
            return False



if __name__=='__main__':
    handler =  MessageServiceHandler()
    processor = MessageService.Processor(handler)
    transport = TSocket.TServerSocket("localhost","9090")
    tfactory =  TTransport.TFramedTransportFactory()
    pfactory =  TBinaryProtocol.TBinaryProtocolFactory()
    server = TServer.TSimpleServer(processor,transport,tfactory,pfactory)
    print("python thrift server start")
    server.serve()
    print("python thrift server end")
