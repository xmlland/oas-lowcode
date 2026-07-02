package com.jeestudio.bpm.webSocket.listener;


import com.jeestudio.bpm.webSocket.BaseMap;

/**
 * @Description: Redis消息监听接口
 */
public interface RedisListerer {

    void onMessage(BaseMap message);

}
