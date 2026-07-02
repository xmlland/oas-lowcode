package com.jeestudio.bpm.webSocket;

import com.alibaba.fastjson.JSONObject;

import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.webSocket.constant.WebsocketConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Description: WebSocket消息
 */
@Tag(name = "WebSocket消息")
@RestController
@RequestMapping("${adminPath}/handle/websocket")
public class WebSocketController {

	@Autowired
	private WebSocket webSocket;

	@Operation(summary = "全员广播")
	@PostMapping("/sendAll")
	public ResultJson sendAll(@RequestBody JSONObject jsonObject) {
		ResultJson resultJson = new ResultJson();
		String message = jsonObject.getString("message");
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
		obj.put(WebsocketConst.MSG_ID, "M0001");
		obj.put(WebsocketConst.MSG_TXT, message);
		webSocket.sendMessage(obj.toJSONString());
		resultJson.setMsg("群发！");
		return resultJson;
	}

	@Operation(summary = "指定用户推送")
	@PostMapping("/sendUser")
	public ResultJson sendUser(@RequestBody JSONObject jsonObject) {
		ResultJson resultJson = new ResultJson();
		String userId = jsonObject.getString("userId");
		String message = jsonObject.getString("message");
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
		obj.put(WebsocketConst.MSG_USER_ID, userId);
		obj.put(WebsocketConst.MSG_ID, "M0001");
		obj.put(WebsocketConst.MSG_TXT, message);
		webSocket.sendMessage(userId, obj.toJSONString());
		resultJson.setMsg("单发");
		return resultJson;
	}

}
