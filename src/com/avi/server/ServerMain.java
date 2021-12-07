package com.avi.server;

public class ServerMain {

	final static int PORT = 8181;

	public static void main(String[] args) {
		Server server = new Server(PORT);
		server.start();
	}
}
