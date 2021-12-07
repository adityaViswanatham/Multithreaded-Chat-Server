package com.avi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
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

	public ServerWorker(Server server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
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
				if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("logout"))
					handleLogout();
				else if (cmd.equalsIgnoreCase("login"))
					handleLogin(outputStream, tokens);
				else {
					String msg = "unknown " + cmd + "\n";
					outputStream.write(msg.getBytes());
				}
			}
		}
	}

	private void handleLogout() throws IOException {
		List<ServerWorker> workerList = server.getWorkerList();
		ServerWorker toRemove = null;
		
		// notify all users about current user logout
		String broadcastMsg = userName + " has logged out\n";
		for (ServerWorker worker : workerList) {
			if (!worker.getUserName().equals(this.userName))
				worker.send(broadcastMsg);
			else
				toRemove = worker;
		}
		clientSocket.close();
		workerList.remove(toRemove);
	}

	public String getUserName() {
		return this.userName;
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
				String msg = "Login Error\n";
				outputStream.write(msg.getBytes());
			}
		}
	}

	private void send(String msg) throws IOException {
		if (this.userName != null)
			outputStream.write(msg.getBytes());
	}
}
