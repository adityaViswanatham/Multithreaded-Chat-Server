package com.avi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChatClient {

	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream serverOut;
	private InputStream serverIn;

	public ChatClient(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient("localhost", 8181);
		if (!client.connect())
			System.err.println("Connection Error...");
		else
			System.out.println("Connection Successful...");
	}

	private boolean connect() {
		try {
			this.socket = new Socket(this.serverName, this.serverPort);
			this.serverOut = this.socket.getOutputStream();
			this.serverIn = this.socket.getInputStream();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
