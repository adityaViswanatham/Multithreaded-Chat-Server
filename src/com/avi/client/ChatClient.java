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

	public static void main(String[] args) throws IOException {
		ChatClient client = new ChatClient("localhost", 8181);
		if (!client.connect())
			System.err.println("Connection Error...");
		else {
			System.out.println("Connection Successful...");
			client.login("admin", "admin");
		}
	}

	private void login(String userName, String password) throws IOException {
		String cmd = "login " + userName + " " + password + "\n";
		this.serverOut.write(cmd.getBytes());
	}

	private boolean connect() {
		try {
			this.socket = new Socket(this.serverName, this.serverPort);
			System.out.println("Client port is: " + this.socket.getLocalPort());
			this.serverOut = this.socket.getOutputStream();
			this.serverIn = this.socket.getInputStream();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
