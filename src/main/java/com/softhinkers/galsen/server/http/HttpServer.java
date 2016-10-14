package com.softhinkers.galsen.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;

public class HttpServer {
	private final int port;
	private Thread serverThread;
	private final List<HttpServlet> handlers = new ArrayList();

	public HttpServer(int port) {
		this.port = port;
	}

	public void addHandler(HttpServlet handler) {
		this.handlers.add(handler);
	}

	public void start() {
		if (this.serverThread != null) {
			throw new IllegalStateException("Server is already running");
		}
		this.serverThread = new Thread() {
			public void run() {
				EventLoopGroup bossGroup = new NioEventLoopGroup(1);
				EventLoopGroup workerGroup = new NioEventLoopGroup();
				try {
					ServerBootstrap bootstrap = new ServerBootstrap();
					bootstrap.option(ChannelOption.SO_BACKLOG,
							Integer.valueOf(1024));
					((ServerBootstrap) bootstrap.group(bossGroup, workerGroup)
							.channel(NioServerSocketChannel.class))
							.childHandler(new ServerInitializer(
									HttpServer.this.handlers));

					Channel ch = bootstrap.bind(HttpServer.this.port).sync()
							.channel();
					ch.closeFuture().sync();
				} catch (InterruptedException ignored) {
				} finally {
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			}
		};
		this.serverThread.start();
	}

	public void stop() {
		if (this.serverThread == null) {
			throw new IllegalStateException("Server is not running");
		}
		this.serverThread.interrupt();
	}

	public int getPort() {
		return this.port;
	}
}