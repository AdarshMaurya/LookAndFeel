package com.softhinkers.galsen.server.http;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.List;

import com.softhinkers.galsen.server.http.impl.NettyHttpRequest;
import com.softhinkers.galsen.server.http.impl.NettyHttpResponse;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	private List<HttpServlet> httpHandlers;

	public ServerHandler(List<HttpServlet> handlers) {
		this.httpHandlers = handlers;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (!(msg instanceof FullHttpRequest)) {
			return;
		}

		FullHttpRequest request = (FullHttpRequest) msg;
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

		HttpRequest httpRequest = new NettyHttpRequest(request);
		HttpResponse httpResponse = new NettyHttpResponse(response);

		for (HttpServlet handler : this.httpHandlers) {
			handler.handleHttpRequest(httpRequest, httpResponse);
			if (httpResponse.isClosed()) {
				break;
			}
		}

		if (!(httpResponse.isClosed())) {
			httpResponse.setStatus(404);
			httpResponse.end();
		}

		ctx.write(response).addListener(ChannelFutureListener.CLOSE);
	}

	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}