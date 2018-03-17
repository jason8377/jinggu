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

import javax.inject.Inject;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.AtmosphereResourceSession;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Universe;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketHandlerAdapter;
import org.jandjzone.buildroid.objs.WebsocketMessageBase;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildWebSocketHandler extends WebSocketHandlerAdapter{
	private static final Logger logger = LoggerFactory.getLogger(BuildWebSocketHandler.class);
	
	public static final String REQUEST_ATTRIBUTE_NAME = "websocketRequest";
	public static final String REQUEST_SESSION_NAME = "websocketSession";
	
	@Inject
	private BroadcasterFactory broadcasterFactory;
	
	@Inject
	private AtmosphereResourceFactory resourceFactory;
	@Inject
	private AtmosphereResourceSessionFactory sessionFactory;
	
    
	@Override
    public final void onOpen(WebSocket webSocket) throws IOException {
		if(logger.isInfoEnabled()){
		    logger.info("onOpen, webSocket {}", webSocket);
		}
    	AtmosphereResource atmosphereResource =  webSocket.resource();
    	if(atmosphereResource == null)return;
    	AtmosphereRequest atmosphereRequest = atmosphereResource.getRequest();
    	if(atmosphereRequest == null)return;
    	
    	//WebsocketRequest object should not be null, just for protection from crash.
    	WebsocketRequest websocketRequest = getWebsocketRequest(atmosphereRequest);
    	if(websocketRequest == null){
    		logger.warn("Unauthorized connection: request object is empty.");
    		webSocket.close();
    		return;
    	}
    	/**
    	 * Save websocket request object to Session associated with the resource.
    	 * Once the resource loses connection, all objects will be discard automatically.
    	 */
    	setSessionValue(atmosphereResource,REQUEST_SESSION_NAME,websocketRequest);
    	
    	/**
    	 * Add it to "/" broadcaster. 
    	 */
    	Broadcaster broadcaster = broadcasterFactory.lookup(Constants.BROADCASTER_ALL,true);
    	broadcaster.addAtmosphereResource(atmosphereResource);
    	
    	onOpen(websocketRequest);
    }
	
	/**
	 * This method should be implemented by concreate Websocket resource.
	 * @param websocketRequest
	 * @throws IOException
	 */
	public void onOpen(WebsocketRequest websocketRequest) throws IOException {
		
	}
	
    @Override
    public void onTextMessage(WebSocket webSocket, String data) throws IOException {
    	if(logger.isInfoEnabled()){
            logger.info("onTextMessage {}", data);
    	}
        
    	AtmosphereResource atmosphereResource =  webSocket.resource();
    	if(atmosphereResource == null)return;
    	AtmosphereRequest atmosphereRequest = atmosphereResource.getRequest();
    	if(atmosphereRequest == null)return;
    	
    	WebsocketRequest websocketRequest = getSessionValue(atmosphereResource, REQUEST_SESSION_NAME, WebsocketRequest.class);
    	if(websocketRequest == null){
    		logger.warn("The WebsocketRequest object in Session lost.");
    		return;
    	}
        
        WebsocketMessageBase messageBase = CommonUtil.fromJsonNoException(data, WebsocketMessageBase.class);
        //if(messageBase == null || !messageBase.isValidMessage()){
        	//logger.info("Not a valid JSON message.");
        	//return;
        //}
        
        onTextMessage(websocketRequest,messageBase,data);
    }
    
    
    public void onTextMessage(WebsocketRequest websocketRequest,WebsocketMessageBase messageBase, String data) throws IOException {
    	
    }

    @Override
    public final void onClose(WebSocket webSocket) {
    	if(logger.isInfoEnabled()){
            logger.info("onClose {}", webSocket);
    	}
        
        if(webSocket == null)return;
    	AtmosphereResource atmosphereResource =  webSocket.resource();
    	if(atmosphereResource == null)return;
    	
    	WebsocketRequest websocketRequest = getSessionValue(atmosphereResource, REQUEST_SESSION_NAME, WebsocketRequest.class);
    	if(websocketRequest == null){
    		logger.warn("onClose, the WebsocketRequest object in Session is lost.");
    		return;
    	}
    	
    	onClose(websocketRequest);
    }
    
    public void onClose(WebsocketRequest websocketRequest) {
    	
    }
    
    /**
	 * Get WebsocketRequest from attribute.
	 * @param atmosphereRequest
	 * @return
	 */
	private WebsocketRequest getWebsocketRequest(AtmosphereRequest atmosphereRequest){
		if(atmosphereRequest == null)return null;
		Object requestObject = atmosphereRequest.getAttribute(REQUEST_ATTRIBUTE_NAME);
		if(requestObject == null || !(requestObject instanceof WebsocketRequest))return null;
		atmosphereRequest.removeAttribute(REQUEST_ATTRIBUTE_NAME);
		return (WebsocketRequest)requestObject;
	}
	
	private void setSessionValue(AtmosphereResource resource, String name, WebsocketRequest websocketRequest) {
		if(resource == null || name == null || websocketRequest == null)return;
		logger.info("sessionFactory:{}",sessionFactory);
		
		if(sessionFactory == null)return;
		
		AtmosphereResourceSession session = sessionFactory.getSession(resource);
		session.setAttribute(name, websocketRequest);
	}

	protected <T> T getSessionValue(AtmosphereResource resource, String name, Class<T> type) {
		if(sessionFactory == null)return null;
		
		AtmosphereResourceSession session = sessionFactory.getSession(resource, false);
		T value = null;
		if (session != null) {
		      value = session.getAttribute(name, type);
		}
		return value;
	}
    

	/**
	 * Get injected broadcaster factory.
	 * @return
	 */
	protected BroadcasterFactory getBroadcasterFactory(){
		return broadcasterFactory;
	}
	/**
	 * Get injected resource factory.
	 * @return
	 */
	protected AtmosphereResourceFactory getResourceFactory(){
		return resourceFactory;
	}
	/**
	 * Get injected session factory.
	 * @return
	 */
	protected AtmosphereResourceSessionFactory getSessionFactory(){
		return sessionFactory;
	}
	
	/**
	 * Broadcast build message to all connected clients.
	 * @param buildMessage
	 */
	public synchronized static void broadCastBuildMessage(String buildMessage){
		if(buildMessage == null || buildMessage.length() ==0)return;
		//logger.info("Broadcasting message {}", buildMessage);
		BroadcasterFactory broadcasterFactory = Universe.broadcasterFactory();
		if(broadcasterFactory == null) {
			logger.info("BroadcasterFactory is empty.");
			return;
		}
		Broadcaster broadcaster = broadcasterFactory.lookup(Constants.BROADCASTER_ALL, false);
		if(broadcaster != null) {
			broadcaster.broadcast(buildMessage);
		} else {
			logger.info("Broadcaster is empty.");
		}
	}
}
