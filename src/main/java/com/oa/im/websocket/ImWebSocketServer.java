package com.oa.im.websocket;

/**
 * WebSocket服务端骨架 - 即时通讯模块（预留）
 *
 * 增量开发步骤：
 * 1. 继承 org.java_websocket.server.WebSocketServer (pom已引入)
 * 2. 重写 onOpen/onClose/onMessage/onError
 * 3. onOpen 时验证用户身份（从URL参数或消息中提取token）
 * 4. onMessage 时解析JSON，调用 ImService 存储并转发给目标用户
 * 5. 维护 ConcurrentHashMap<Long, WebSocket> 管理在线连接
 * 6. 在 Application.main() 中启一个独立线程启动 WebSocketServer
 */
public class ImWebSocketServer {
    // TODO: 实现 WebSocket 消息实时推送
    // 参考代码：
    //   server = new WebSocketServer(new InetSocketAddress(8887)) {
    //       @Override public void onOpen(WebSocket conn, ClientHandshake handshake) { ... }
    //       @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) { ... }
    //       @Override public void onMessage(WebSocket conn, String message) { ... }
    //   };
    //   server.start();
}
