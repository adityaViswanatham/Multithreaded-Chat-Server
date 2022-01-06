package com.avi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Thread class to communicate with client. New worker is spawned for every
 * incoming connection.
 */
public class ServerWorker extends Thread {

	private final Socket clientSocket;
	private Server server;
	private String userName = null;
	private OutputStream outputStream;
	private HashSet<String> channels;

	public ServerWorker(Server server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		channels = new HashSet<>();
	}

	@Override
	public void run() {
		try {
			handleClientSocket();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void handleClientSocket() throws IOException, InterruptedException {
		// input stream to read data from client
		InputStream inputStream = clientSocket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		// output stream to write data to client
		this.outputStream = clientSocket.getOutputStream();

		String line;
		while ((line = reader.readLine()) != null) {
			String[] tokens = StringUtils.split(line);
			if (tokens != null && tokens.length > 0) {
				String cmd = tokens[0];
				if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("logout")) {
					handleLogout();
					break;
				} else if (cmd.equalsIgnoreCase("login"))
					handleLogin(outputStream, tokens);
				else if (cmd.equalsIgnoreCase("msg")) {
					String[] msgTokens = StringUtils.split(line, null, 3);
					handleMessage(msgTokens);
				} else if (cmd.equalsIgnoreCase("join")) {
					String[] joinTokens = StringUtils.split(line, null, 2);
					handleJoin(joinTokens);
				} else if (cmd.equalsIgnoreCase("leave")) {
					String[] leaveTokens = StringUtils.split(line, null, 2);
					handleLeave(leaveTokens);
				} else {
					String msg = "unknown " + cmd + "\n";
					send(msg);
				}
			}
		}
	}

	// command format: "leave #topic1 ..."
	private void handleLeave(String[] tokens) throws IOException {
		List<ServerWorker> workerList = server.getWorkerList();
		if (tokens.length > 1) {
			String[] channelTokens = StringUtils.split(tokens[1]);
			for (int i = 0; i < channelTokens.length; i++) {
				String channel = channelTokens[i];
				this.channels.remove(channel);
				String msg = "Successfully left " + channel + "\n";
				send(msg);

				// notify other people in the channel
				for (ServerWorker worker : workerList) {
					if (!this.userName.equals(worker.getUserName()) && worker.isChannelMember(channel)) {
						String broadcastMsg = this.userName + " has left " + channel + "\n";
						worker.send(broadcastMsg);
					}
				}
			}
		}
	}

	// command format: "join #topic1 ..."
	private void handleJoin(String[] tokens) throws IOException {
		List<ServerWorker> workerList = server.getWorkerList();
		if (tokens.length > 1) {
			String[] channelTokens = StringUtils.split(tokens[1]);
			for (int i = 0; i < channelTokens.length; i++) {
				String channel = channelTokens[i];
				this.channels.add(channel);
				String msg = "Successfully joined " + channel + "\n";
				send(msg);

				// notify other people in the channel
				for (ServerWorker worker : workerList) {
					if (!this.userName.equals(worker.getUserName()) && worker.isChannelMember(channel)) {
						String broadcastMsg = this.userName + " has joined " + channel + "\n";
						worker.send(broadcastMsg);
					}
				}
			}
		}
	}

	// command format: "msg recipient(#topic) msgBody"
	// command format: "msg recipient(userName) msgBody"
	private void handleMessage(String[] tokens) throws IOException {
		if (tokens.length == 3) {
			List<ServerWorker> workerList = server.getWorkerList();
			String recipient = tokens[1];
			String msgBody = tokens[2];

			boolean isChannel = recipient.charAt(0) == '#';
			for (ServerWorker worker : workerList) {
				if (isChannel) {
					if (isChannelMember(recipient)) {
						if (worker.isChannelMember(recipient)) {
							String msg = "(" + recipient + ") " + this.userName + ": " + msgBody + "\n";
							worker.send(msg);
						}
					} else {
						String msg = "You are not a channel member\n";
						send(msg);
						break;
					}
				} else {
					if (recipient.equals(worker.getUserName())) {
						String msg = this.userName + ": " + msgBody + "\n";
						worker.send(msg);
					}
				}
			}
		} else {
			String msg = "Incorrect command for messaging\n";
			send(msg);
		}
	}

	private void handleLogout() throws IOException {
		this.server.removeWorker(this);
		List<ServerWorker> workerList = server.getWorkerList();
		System.out.println(userName + " has logged out");

		// notify all users about current user logout
		String broadcastMsg = userName + " has logged out\n";
		for (ServerWorker worker : workerList) {
			if (!worker.getUserName().equals(this.userName))
				worker.send(broadcastMsg);
		}
		this.clientSocket.close();
	}

	private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
		if (tokens.length == 3) {
			String userName = tokens[1];
			String password = tokens[2];

			if ((userName.equals("admin") && password.equals("admin"))
					|| (userName.equals("avi") && password.equals("avi"))) {
				String msg = "Login Ok\n";
				outputStream.write(msg.getBytes());
				this.userName = userName;
				System.out.println(userName + " logged in succesfully");

				List<ServerWorker> workerList = server.getWorkerList();

				// send current user all other online logins
				for (ServerWorker worker : workerList) {
					if (worker.getUserName() != null) {
						// not sending self's online presence to self
						if (!this.userName.equals(worker.getUserName())) {
							String onlineMsg = worker.getUserName() + " is online!!!\n";
							send(onlineMsg);
						}
					}
				}

				// send all users current online login
				String broadcastMsg = userName + " is online!!!\n";
				for (ServerWorker worker : workerList) {
					if (!this.userName.equals(worker.getUserName()))
						worker.send(broadcastMsg);
				}

			} else {
				System.err.println("Login failed for: " + userName);
				String msg = "Login Error\n";
				outputStream.write(msg.getBytes());
			}
		}
	}

	/**
	 * Helper methods for handlers
	 */
	public String getUserName() {
		return this.userName;
	}

	private void send(String msg) throws IOException {
		if (this.userName != null)
			this.outputStream.write(msg.getBytes());
	}

	public boolean isChannelMember(String topic) {
		return this.channels.contains(topic);
	}
}
