# 该项目是基于局域网UDP 通讯demo  ---解决你接收不到消息或者堵塞的烦人!!!!! 项目根目录的sokit.exe,是电脑端测试工具
   
##测试通过了,手机发送端和window收端通讯    
####在ServerActivity中,测试通过了UDP手机发送端和window接收端通讯,其中接收端开启固定线程池,发送端开启缓存线程池
##测试通过了,手机发送端和手机接收端通讯  
####同上
## * * 
##   * * 请看ServerActivity这个类
##   * * 使用方式一:普通的Runnable任务,更新UI需要在主线程
##   * * <p>
##   * * 1:点对点使用
##   * * a,设置inetAddress
##   * * inetAddress = InetAddress.getByName("192.168.132.102");     //目标ip
##   * * b,开启接收线程
##   * * easyFixed2Thread.execute(getReceiveRunnable());      //RECEIVE_PORT  是本地App监听端口
##   * * c,在点击事件中发送消息(多发几次可能发送数据丢失)
##   * * easyCacheThread.execute(getSendRunnable(mSendContent.getText().toString().trim()));     //BROADCAST_PORT服务器端口
##   * * <p>
##   * * <p>
##   * * 2:广播使用
##   * * a,设置inetAddress
##   * * inetAddress = InetAddress.getByName("255.255.255.255");     //广播目标ip
##   * * b,开启接收线程
##   * * easyFixed2Thread.execute(getReceiveRunnable());      //RECEIVE_PORT  是本地App监听端口
##   * * c,在点击事件中发送消息(多发几次可能发送数据丢失)
##   * * easyCacheThread.execute(getSendBroadcastRunnable(mSendContent.getText().toString().trim()));    //BROADCAST_PORT服务器端口
##   * *
##   * *
##   * * 使用方式二:异步回调任务
##   * *
##   * *  备注,这个方式有个弊端就是,接收线程接受到一次消息就会关闭,适用于一次对话的使用场景
##   * *  a,开启接收线程
##   * *  startAsyncReceive();      //RECEIVE_PORT  是本地App监听端口
##   * *  b,在点击事件中发送消息(多发几次可能发送数据丢失)
##   * *  easyCacheThread.execute(getSendBroadcastRunnable(mSendContent.getText().toString().trim()));    //BROADCAST_PORT服务器端口
##   * * 
## * * 






