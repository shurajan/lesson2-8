package com.geekbrains.client;

import com.geekbrains.CommonConstants;
import com.geekbrains.server.ServerCommandConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private final ChatController controller;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Network(ChatController chatController) {
        this.controller = chatController;
    }

    private void startReadServerMessages() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String messageFromServer = inputStream.readUTF();
                        System.out.println(messageFromServer);
                        if (messageFromServer.contains(ServerCommandConstants.ENTER)) {
                            String[] client = messageFromServer.split(" ");
                            controller.displayClient(client[1]);
                        } else if (messageFromServer.startsWith(ServerCommandConstants.EXIT)) {
                            String[] client = messageFromServer.split(" ");
                            controller.removeClient(client[1]);
                            controller.displayMessage(client[1] + " покинул чат");
                        } else if (messageFromServer.startsWith(ServerCommandConstants.CLIENTS)) {
                            String[] client = messageFromServer.split(" ");
                            for (int i = 1; i < client.length; i++) {
                                controller.displayClient(client[i]);
                            }
                        } else if (messageFromServer.startsWith(ServerCommandConstants.PRIVATE)) {
                            String[] message = messageFromServer.split(" ");
                            StringBuilder sb = new StringBuilder();
                            sb.append("<b>");
                            sb.append(message[1]);
                            sb.append(":");
                            for (int i = 2; i < message.length; i++) {
                                sb.append(" ");
                                sb.append(message[i]);

                            }
                            sb.append("<b>");
                            controller.displayPrivateMessage(sb.toString());

                        } else {
                            controller.displayMessage(messageFromServer);
                        }
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void initializeNetwork() throws IOException {
        socket = new Socket(CommonConstants.SERVER_ADDRESS, CommonConstants.SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }


    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void sendMessage(String toNickName, String message) {
        try {
            outputStream.writeUTF(ServerCommandConstants.PRIVATE + " " + toNickName + " " + message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean sendAuth(String login, String password) {
        try {
            if (socket == null || socket.isClosed()) {
                initializeNetwork();
            }
            outputStream.writeUTF(ServerCommandConstants.AUTHENTICATION + " " + login + " " + password);

            boolean authenticated = inputStream.readBoolean();
            if (authenticated) {
                startReadServerMessages();
            }
            return authenticated;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void closeConnection() {
        try {
            outputStream.writeUTF(ServerCommandConstants.EXIT);
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        System.exit(0);
    }

}
