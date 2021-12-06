package com.avi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Thread class to communicate with client New worker is spawned for every
 * incoming connection
 */
public class ServerWorker extends Thread {

	private final Socket clientSocket;

	public ServerWorker(Socket clientSocket) {
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

		InputStream inputStream = clientSocket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		OutputStream outputStream = clientSocket.getOutputStream();

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equalsIgnoreCase("quit"))
				clientSocket.close();
			else {
				String msg = "You typed: " + line + "\n";
				outputStream.write(msg.getBytes());
			}
		}
	}
}
