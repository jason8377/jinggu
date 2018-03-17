/*******************************************************************************
 * Copyright (C) 2018 Jason Luo
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.jandjzone.buildroid.websocket;

import java.io.IOException;

import org.atmosphere.config.service.WebSocketHandlerService;
import org.atmosphere.util.SimpleBroadcaster;
import org.jandjzone.buildroid.objs.WebsocketMessageBase;

@WebSocketHandlerService(
		path = "/ws"
		,broadcaster = SimpleBroadcaster.class
        ,atmosphereConfig = {
		    "org.atmosphere.websocket.WebSocketProtocol=org.atmosphere.websocket.protocol.SimpleHttpProtocol"
            ,"org.atmosphere.websocket.WebSocketProcessor=org.jandjzone.buildroid.websocket.ConnectionProcessor"
		}
)
public class WebsocketResource extends BuildWebSocketHandler {
	
	@Override
    public void onOpen(WebsocketRequest websocketRequest) throws IOException {
		//Callback of a new WebSocket connection
	}
	
	@Override
    public void onTextMessage(WebsocketRequest websocketRequest,WebsocketMessageBase messageBase, String data) throws IOException {
		//Handle message from client here
    }
	@Override
    public void onClose(WebsocketRequest websocketRequest) {
		//Callback of when a WebSocket connection disconnected
    }
}
