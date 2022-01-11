package com.avi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ChatClient {

	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream serverOut;
	private InputStream serverIn;
	private BufferedReader bufferedIn;
	private List<UserStatusListerner> userStatusListeners = new ArrayList<>();
	private List<MessageListener> messageListeners = new ArrayList<>();

	public ChatClient(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
	}

	public static void main(String[] args) throws IOException {
		ChatClient client = new ChatClient("localhost", 8181);
		client.addUserStatusListener(new UserStatusListerner() {
			@Override
			public void online(String userName) {
				System.out.println("ONLINE: " + userName);
			}

			@Override
			public void offline(String userName) {
				System.out.println("OFFLINE: " + userName);
			}
		});
		client.addMessageListeners(new MessageListener() {
			@Override
			public void onMessage(String sender, String msg) {
				System.out.println(sender + ": " + msg);
			}
		});

		if (!client.connect())
			System.err.println("Connection Error...");
		else {
			System.out.println("Connection Successful...");
			boolean resp = client.login("avi", "avi");
			if (resp) {
				System.out.println("Login successful");
				client.msg("admin", "Hello!!!");
			} else
				System.out.println("Login Failed");

//			client.logout();
		}
	}

	private void msg(String recipient, String msg) throws IOException {
		String cmd = "msg " + recipient + " " + msg + "\n";
		serverOut.write(cmd.getBytes());
	}

	private void logout() throws IOException {
		String cmd = "logout\n";
		this.serverOut.write(cmd.getBytes());
	}

	private boolean login(String userName, String password) throws IOException {
		String cmd = "login " + userName + " " + password + "\n";
		this.serverOut.write(cmd.getBytes());
		String response = this.bufferedIn.readLine();
		if (response.equalsIgnoreCase("login error"))
			return false;
		else {
			startMessageReader();
			return true;
		}
	}

	private void startMessageReader() {
		Thread t = new Thread() {
			@Override
			public void run() {
				readMessageLoop();
			}
		};
		t.start();
	}

	protected void readMessageLoop() {
		try {
			String line;
			while ((line = bufferedIn.readLine()) != null) {
				String[] tokens = StringUtils.split(line);
				if (tokens != null && tokens.length > 0) {
					String cmd = tokens[0];
					if (cmd.equalsIgnoreCase("online")) {
						handleOnline(tokens);
					} else if (cmd.equalsIgnoreCase("offline")) {
						handleOffline(tokens);
					} else if (cmd.equalsIgnoreCase("msg")) {
						System.out.println("INNNN");
						String[] msgTokens = StringUtils.split(line, null, 3);
						handleMessage(msgTokens);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void handleMessage(String[] tokens) {
		String userName = tokens[1];
		String msg = tokens[2];
		for (MessageListener listener : messageListeners) {
			listener.onMessage(userName, msg);
		}
	}

	private void handleOffline(String[] tokens) {
		String userName = tokens[1];
		for (UserStatusListerner listener : userStatusListeners) {
			listener.offline(userName);
		}
	}

	private void handleOnline(String[] tokens) {
		String userName = tokens[1];
		for (UserStatusListerner listener : userStatusListeners) {
			listener.online(userName);
		}
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
			e.printStackTrace();
		}
		return false;
	}

	public void addUserStatusListener(UserStatusListerner listener) {
		userStatusListeners.add(listener);
	}

	public void removeUserStatusListener(UserStatusListerner listener) {
		userStatusListeners.remove(listener);
	}

	public void addMessageListeners(MessageListener listener) {
		messageListeners.add(listener);
	}

	public void removeMessageListeners(MessageListener listener) {
		messageListeners.remove(listener);
	}
}
