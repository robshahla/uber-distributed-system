import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/*
 * Created by JFormDesigner on Wed Jan 20 15:34:37 IST 2021
 */


/**
 * @author unknown
 */
public class Uber {

    private Map<String, String> server_address = new HashMap<>();
    private String path_to_servers = "";

    public static void main(String[] args) {

        Uber uber_client = new Uber();

        if (args.length > 0) {
            System.out.println("Fist Arg= " + args[0]);
            uber_client.path_to_servers = args[0];
        }

        uber_client.initComponents();
        Font font = new Font("Verdana", Font.BOLD, 12);
        uber_client.logPane.setFont(font);
        uber_client.logPane.setForeground(Color.BLACK);
        uber_client.mainFrame.setTitle("Uber Client");
        uber_client.updateServersList();
        uber_client.mainFrame.show();
    }

    private void updateServersList() {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(path_to_servers));
            String line = bufferedReader.readLine();
            while (line != null) {
                var data = line.split(",");
                var key = data[0];
                var value = "http://" + data[1] + "/";
                server_address.put(key, value);
                comboServerList.addItem(key);
                line = bufferedReader.readLine();
            }
        } catch (Exception ignored) {

        }
    }

    private void addToLog(String str) {
        StringBuilder builder = new StringBuilder(logPane.getText());
        builder.append("\n").append(str).append("\n");
        logPane.setText(builder.toString());
    }


    private void btnClearLogActionPerformed(ActionEvent e) {
        logPane.setText("");
    }

    private String getServerAddress() {
        if (!checkBoxRandomServer.isSelected()) {
            String item = (String) comboServerList.getSelectedItem();
            String address = server_address.get(item);
            System.out.println("Selected server = " + item);
            System.out.println("Selected server address = " + address);
            return address;
        }
        Random rnd = new Random();
        java.util.List<String> servers = new ArrayList<>(server_address.keySet());
        int size = servers.size();
        int rand_int = rnd.nextInt();
        rand_int = rand_int < 0 ? -rand_int : rand_int;
        rand_int = rand_int % size;
        String next_server = servers.get(rand_int);
        addToLog("Chosen server to contact: " + next_server);
        return server_address.get(next_server);
    }

    private String getTextFromJTextField(JTextField field) throws IllegalArgumentException {
        if (field == null) throw new IllegalArgumentException("Null field");
        String result = field.getText().trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Empty Field " + field.getName());
        }
        return result;
    }

    private void btnReserveActionPerformed(ActionEvent event) {
        btnReserve.setEnabled(false);
        String server_address = getServerAddress();
        try {
            String first_name = getTextFromJTextField(reserveFirstName);
            String last_name = getTextFromJTextField(reserveLastName);
            String departure_time = getTextFromJTextField(reserveDepartureTime);
            String path = getTextFromJTextField(reservePath);
            Reservation reservation = new Reservation(first_name, last_name, departure_time, path);
            URI address = new URI(server_address + "reserveRide");
            String result = makePostHttpRequest(address, reservation.serialize());
            addToLog(result);
            return;
        } catch (IllegalArgumentException | URISyntaxException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException | IOException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            btnReserve.setEnabled(true);
        }
        addToLog("Error while sending request to server! server address:" + server_address);

    }

    private void btnPublishRideActionPerformed(ActionEvent event) {
        String server_address = getServerAddress();
        btnPublishRide.setEnabled(false);
        try {
            String first_name = getTextFromJTextField(publishFirstName);
            String last_name = getTextFromJTextField(publishLastName);
            String phone = getTextFromJTextField(publishPhone);
            String departure_time = getTextFromJTextField(publishDeparture);
            int vacancies = Integer.parseInt(getTextFromJTextField(publishVacncies));
            String start_position = getTextFromJTextField(publishFrom);
            String end_position = getTextFromJTextField(publishTo);
            double pd = Double.parseDouble(getTextFromJTextField(publishPD));
            Ride ride = new Ride(first_name, last_name, phone, start_position, end_position, departure_time, vacancies, pd);
            System.out.println(ride.serialize());
            URI address = URI.create(server_address + "publishRide");
            String result = makePostHttpRequest(address, ride.serialize());
            addToLog(result);
            return;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());

        } catch (InterruptedException | IOException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            btnPublishRide.setEnabled(true);
        }
        addToLog("Error while sending request to server! server address:" + server_address);
    }

    private String makeGetHttpRequest(URI address) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
                address)
                .header("accept", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response != null ? response.body() : "Empty response from server!";
    }

    private String makePostHttpRequest(URI address, String body) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(address)
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response != null ? response.body() : "Empty response from server!";
    }


    private void btnSnapshotActionPerformed(ActionEvent e) {
        btnSnapshot.setEnabled(false);
        String server_address = getServerAddress();
        try {
            URI address = new URI(server_address + "snapshot");
            String result = makeGetHttpRequest(address);
            addToLog(result);
            return;
        } catch (IOException | InterruptedException | URISyntaxException ioException) {
            addToLog("Error while getting snapeshot!");
            ioException.printStackTrace();
        } finally {
            btnSnapshot.setEnabled(true);
        }
        addToLog("Error while sending request to server! server address:" + server_address);

    }

    private void checkBoxRandomServerStateChanged(ChangeEvent e) {
        comboServerList.setEnabled(!checkBoxRandomServer.isSelected());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        mainFrame = new JFrame();
        panel2 = new JPanel();
        tabbedPane1 = new JTabbedPane();
        panel7 = new JPanel();
        label5 = new JLabel();
        publishFirstName = new JTextField();
        label6 = new JLabel();
        publishLastName = new JTextField();
        label7 = new JLabel();
        publishDeparture = new JTextField();
        label8 = new JLabel();
        publishFrom = new JTextField();
        label9 = new JLabel();
        publishTo = new JTextField();
        label12 = new JLabel();
        publishPhone = new JTextField();
        label10 = new JLabel();
        publishVacncies = new JTextField();
        label11 = new JLabel();
        publishPD = new JTextField();
        btnPublishRide = new JButton();
        panel8 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        reserveFirstName = new JTextField();
        reserveLastName = new JTextField();
        label3 = new JLabel();
        reserveDepartureTime = new JTextField();
        label4 = new JLabel();
        reservePath = new JTextField();
        btnReserve = new JButton();
        btnSnapshot = new JButton();
        checkBoxRandomServer = new JCheckBox();
        comboServerList = new JComboBox();
        panel3 = new JPanel();
        scrollPane4 = new JScrollPane();
        logPane = new JTextPane();
        btnClearLog = new JButton();

        //======== mainFrame ========
        {
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setResizable(false);
            mainFrame.setBackground(Color.black);
            var mainFrameContentPane = mainFrame.getContentPane();
            mainFrameContentPane.setLayout(new MigLayout(
                    "hidemode 3,align center center",
                    // columns
                    "[fill]" +
                            "[fill]",
                    // rows
                    "[]" +
                            "[]"));

            //======== panel2 ========
            {
                panel2.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(
                        0, 0, 0, 0), "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder
                        .BOTTOM, new java.awt.Font("Dialo\u0067", java.awt.Font.BOLD, 12), java.awt.Color.
                        red), panel2.getBorder()));
                panel2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    @Override
                    public void propertyChange(java.
                                                       beans.PropertyChangeEvent e) {
                        if ("borde\u0072".equals(e.getPropertyName())) throw new RuntimeException();
                    }
                });
                panel2.setLayout(new MigLayout(
                        "hidemode 3",
                        // columns
                        "[fill]" +
                                "[fill]" +
                                "[fill]",
                        // rows
                        "[]" +
                                "[]" +
                                "[]" +
                                "[]" +
                                "[]"));

                //======== tabbedPane1 ========
                {

                    //======== panel7 ========
                    {
                        panel7.setLayout(new MigLayout(
                                "hidemode 3",
                                // columns
                                "[fill]" +
                                        "[fill]" +
                                        "[fill]",
                                // rows
                                "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]"));

                        //---- label5 ----
                        label5.setText("First Name");
                        panel7.add(label5, "cell 0 0");
                        panel7.add(publishFirstName, "cell 2 0,wmin 150");

                        //---- label6 ----
                        label6.setText("Last Name");
                        panel7.add(label6, "cell 0 1");
                        panel7.add(publishLastName, "cell 2 1");

                        //---- label7 ----
                        label7.setText("Departure Time");
                        panel7.add(label7, "cell 0 2");
                        panel7.add(publishDeparture, "cell 2 2");

                        //---- label8 ----
                        label8.setText("From");
                        panel7.add(label8, "cell 0 3");
                        panel7.add(publishFrom, "cell 2 3");

                        //---- label9 ----
                        label9.setText("To");
                        panel7.add(label9, "cell 0 4");
                        panel7.add(publishTo, "cell 2 4");

                        //---- label12 ----
                        label12.setText("Phone");
                        panel7.add(label12, "cell 0 5");
                        panel7.add(publishPhone, "cell 2 5");

                        //---- label10 ----
                        label10.setText("Vacncies");
                        panel7.add(label10, "cell 0 7");
                        panel7.add(publishVacncies, "cell 2 7");

                        //---- label11 ----
                        label11.setText("Permitted Deviation");
                        panel7.add(label11, "cell 0 8");
                        panel7.add(publishPD, "cell 2 8");

                        //---- btnPublishRide ----
                        btnPublishRide.setText("Publish");
                        btnPublishRide.addActionListener(e -> btnPublishRideActionPerformed(e));
                        panel7.add(btnPublishRide, "cell 0 10");
                    }
                    tabbedPane1.addTab("Publish Ride", panel7);

                    //======== panel8 ========
                    {
                        panel8.setLayout(new MigLayout(
                                "hidemode 3",
                                // columns
                                "[fill]" +
                                        "[fill]",
                                // rows
                                "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]" +
                                        "[]"));

                        //---- label1 ----
                        label1.setText("First Name");
                        panel8.add(label1, "cell 0 0");

                        //---- label2 ----
                        label2.setText("Last Name");
                        panel8.add(label2, "cell 0 1");
                        panel8.add(reserveFirstName, "cell 1 0,wmin 150");
                        panel8.add(reserveLastName, "cell 1 1");

                        //---- label3 ----
                        label3.setText("Departure Time");
                        panel8.add(label3, "cell 0 2");
                        panel8.add(reserveDepartureTime, "cell 1 2");

                        //---- label4 ----
                        label4.setText("Requested Path");
                        panel8.add(label4, "cell 0 3");
                        panel8.add(reservePath, "cell 1 3");

                        //---- btnReserve ----
                        btnReserve.setText("Reserve");
                        btnReserve.addActionListener(e -> btnReserveActionPerformed(e));
                        panel8.add(btnReserve, "cell 0 4");
                    }
                    tabbedPane1.addTab("Reserve Ride", panel8);
                }
                panel2.add(tabbedPane1, "cell 0 0");

                //---- btnSnapshot ----
                btnSnapshot.setText("snapshot");
                btnSnapshot.addActionListener(e -> btnSnapshotActionPerformed(e));
                panel2.add(btnSnapshot, "cell 0 1");

                //---- checkBoxRandomServer ----
                checkBoxRandomServer.setText("Random server");
                checkBoxRandomServer.addChangeListener(e -> checkBoxRandomServerStateChanged(e));
                panel2.add(checkBoxRandomServer, "cell 0 2");
                panel2.add(comboServerList, "cell 0 3");
            }
            mainFrameContentPane.add(panel2, "tag cancel,cell 0 0");

            //======== panel3 ========
            {
                panel3.setLayout(new MigLayout(
                        "hidemode 3",
                        // columns
                        "[fill]",
                        // rows
                        "[]" +
                                "[]"));

                //======== scrollPane4 ========
                {

                    //---- logPane ----
                    logPane.setEditable(false);
                    logPane.setEnabled(false);
                    scrollPane4.setViewportView(logPane);
                }
                panel3.add(scrollPane4, "cell 0 0,wmin 300,hmin 290");

                //---- btnClearLog ----
                btnClearLog.setText("Clear log");
                btnClearLog.addActionListener(e -> btnClearLogActionPerformed(e));
                panel3.add(btnClearLog, "cell 0 1");
            }
            mainFrameContentPane.add(panel3, "cell 1 0");
            mainFrame.pack();
            mainFrame.setLocationRelativeTo(mainFrame.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JFrame mainFrame;
    private JPanel panel2;
    private JTabbedPane tabbedPane1;
    private JPanel panel7;
    private JLabel label5;
    private JTextField publishFirstName;
    private JLabel label6;
    private JTextField publishLastName;
    private JLabel label7;
    private JTextField publishDeparture;
    private JLabel label8;
    private JTextField publishFrom;
    private JLabel label9;
    private JTextField publishTo;
    private JLabel label12;
    private JTextField publishPhone;
    private JLabel label10;
    private JTextField publishVacncies;
    private JLabel label11;
    private JTextField publishPD;
    private JButton btnPublishRide;
    private JPanel panel8;
    private JLabel label1;
    private JLabel label2;
    private JTextField reserveFirstName;
    private JTextField reserveLastName;
    private JLabel label3;
    private JTextField reserveDepartureTime;
    private JLabel label4;
    private JTextField reservePath;
    private JButton btnReserve;
    private JButton btnSnapshot;
    private JCheckBox checkBoxRandomServer;
    private JComboBox comboServerList;
    private JPanel panel3;
    private JScrollPane scrollPane4;
    private JTextPane logPane;
    private JButton btnClearLog;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
