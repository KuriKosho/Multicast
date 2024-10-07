package org.example.netreceivefx;

import java.net.*;
import java.io.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;

public class NetChatFX extends Application {
    private static final String MULTICAST_GROUP = "230.0.0.0"; // Địa chỉ multicast mặc định
    private static final int PORT = 6789; // Cổng mặc định
    private TextArea messageArea; // Khu vực hiển thị tin nhắn

    // Gửi tin nhắn tới một địa chỉ IP cụ thể
    public void sendToIP(String ipAddress, String message) throws IOException {
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
        socket.send(packet);
        socket.close();
    }

    // Gửi tin nhắn tới một nhóm (multicast group)
    public void sendToGroup(String message) throws IOException {
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
        socket.close();
    }

    // Gửi tin nhắn tới tất cả các máy trên mạng (broadcast)
    public void sendToAll(String message) throws IOException {
        InetAddress broadcast = InetAddress.getByName("255.255.255.255");
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcast, PORT);
        socket.send(packet);
        socket.close();
    }

    // Nhận tin nhắn gửi đến
    public void receiveMessages() {
        new Thread(() -> {
            try {
                MulticastSocket socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                socket.joinGroup(group);

                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    InetAddress senderAddress = packet.getAddress();
                    String displayMessage = "Received: " + message + " from: " + senderAddress.getHostAddress();

                    // Cập nhật giao diện JavaFX
                    Platform.runLater(() -> messageArea.appendText(displayMessage + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Net Chat");

        // Tạo các thành phần giao diện
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);

        Label targetLabel = new Label("Nhập IP hoặc group (* để gửi đến tất cả):");
        TextField targetField = new TextField();
        Label messageLabel = new Label("Nhập tin nhắn:");
        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Nhập tin nhắn tại đây...");
        Button sendButton = new Button("Gửi");

        // Sự kiện cho nút gửi
        sendButton.setOnAction(event -> {
            String target = targetField.getText().trim();
            String message = inputArea.getText().trim();

            try {
                if (target.equals("group")) {
                    sendToGroup(message);
                } else if (target.equals("*")) {
                    sendToAll(message);
                } else {
                    sendToIP(target, message);
                }
                // Xóa nội dung sau khi gửi
                inputArea.clear();
                targetField.clear();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể gửi tin nhắn: " + e.getMessage());
            }
        });

        // Tạo layout
        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.getChildren().addAll(targetLabel, targetField, messageLabel, inputArea, sendButton, messageArea);

        // Tạo và hiển thị cảnh
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        receiveMessages(); // Bắt đầu nhận tin nhắn
    }

    // Hiển thị thông báo lỗi
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
