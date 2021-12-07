package com.avi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {

	private final int serverPort;
	private List<ServerWorker> workerList;

	public Server(int serverPort) {
		this.serverPort = serverPort;
		workerList = new ArrayList<>();
	}

	public List<ServerWorker> getWorkerList() {
		return this.workerList;
	}

	@Override
	public void run() {
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(serverPort);

			while (true) {
				System.out.println("Ready to accept client connection...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connection accepted from " + clientSocket);
				ServerWorker worker = new ServerWorker(this, clientSocket);
				workerList.add(worker);
				worker.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
