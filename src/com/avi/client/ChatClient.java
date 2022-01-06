package com.avi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ChatClient {

	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream serverOut;
	private InputStream serverIn;
	private BufferedReader bufferedIn;

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
			boolean resp = client.login("admin", "admin");
			if (resp)
				System.out.println("Login successful");
			else
				System.out.println("Login Failed");
		}
	}

	private boolean login(String userName, String password) throws IOException {
		String cmd = "login " + userName + " " + password + "\n";
		this.serverOut.write(cmd.getBytes());
		String response = this.bufferedIn.readLine();
		return response.equalsIgnoreCase("login error") ? false : true;
	}

	private boolean connect() {
		try {
			this.socket = new Socket(this.serverName, this.serverPort);
			System.out.println("Client port is: " + this.socket.getLocalPort());
			this.serverOut = this.socket.getOutputStream();
			this.serverIn = this.socket.getInputStream();
			this.bufferedIn = new BufferedReader(new InputStreamReader(this.serverIn));
			return true;
		} catch (IOException e) {
			System.err.println("Inside the connect catch block");
			e.printStackTrace();
		}
		return false;
	}
}
