package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Enumeration;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main extends JFrame {
    private static final String API_BASE_URL = "http://ajax1.lmsoft.cz/procedure.php";
    private static final String API_PEOPLE = "getPeopleList";
    private static final String API_DRINKS = "getSummaryOfDrinks";
    private static final String API_SAVE = "saveDrinks";
    private static final String USERNAME = "coffe";
    private static final String PASSWORD = "kafe";

    private JPanel drinksPanel;
    private ButtonGroup peopleGroup;

    public Main() {
        setTitle("Coffee App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLayout(new BorderLayout());

        drinksPanel = new JPanel();
        drinksPanel.setLayout(new BoxLayout(drinksPanel, BoxLayout.Y_AXIS));

        // People panel
        JPanel peoplePanel = new JPanel();
        peoplePanel.setLayout(new BoxLayout(peoplePanel, BoxLayout.Y_AXIS));

        peopleGroup = new ButtonGroup();

        // Fetch and display people
        try {
            JSONObject peopleData = fetchPeopleData(API_PEOPLE);
            for (String key : peopleData.keySet()) {
                JSONObject person = peopleData.getJSONObject(key);
                String name = person.getString("name");
                String id = person.getString("ID");

                JRadioButton radioButton = new JRadioButton(name);
                radioButton.setActionCommand(id);
                if (peopleGroup.getButtonCount() == 0) {
                    radioButton.setSelected(true);
                }
                peopleGroup.add(radioButton);
                peoplePanel.add(radioButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Drinks panel
        try {
            JSONArray drinksData = fetchDrinksData(API_DRINKS);
            for (int i = 0; i < drinksData.length(); i++) {
                JSONArray drink = drinksData.getJSONArray(i);
                String drinkName = drink.getString(0);

                JPanel drinkPanel = new JPanel();
                drinkPanel.setLayout(new FlowLayout());

                JLabel drinkLabel = new JLabel(drinkName);
                JSlider slider = new JSlider(0, 100, 0);
                JLabel valueLabel = new JLabel("0");

                slider.addChangeListener(e -> valueLabel.setText(String.valueOf(slider.getValue())));

                drinkPanel.add(drinkLabel);
                drinkPanel.add(slider);
                drinkPanel.add(valueLabel);

                drinksPanel.add(drinkPanel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Submit button
        JButton submitButton = new JButton("Odeslat");
        submitButton.addActionListener(e -> submitData());

        add(peoplePanel, BorderLayout.NORTH);
        add(new JScrollPane(drinksPanel), BorderLayout.CENTER);
        add(submitButton, BorderLayout.SOUTH);
    }

    // Fetch people data from API
    private JSONObject fetchPeopleData(String apiUrl) throws Exception {
        URL url = new URL(API_BASE_URL + "?cmd=" + apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String credentials = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
        connection.setRequestProperty("Authorization", "Basic " + credentials);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONObject(response.toString());
    }

    // Fetch drinks data from API
    private JSONArray fetchDrinksData(String apiUrl) throws Exception {
        URL url = new URL(API_BASE_URL + "?cmd=" + apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String credentials = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
        connection.setRequestProperty("Authorization", "Basic " + credentials);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONArray(response.toString());
    }

    // Submit data to the API
    private void submitData() {
        try {
            // Get selected person ID
            String selectedPersonId = peopleGroup.getSelection().getActionCommand();

            if (selectedPersonId == null) {
                throw new IllegalArgumentException("No person selected");
            }

            // Collect drink data
            StringBuilder query = new StringBuilder();
            query.append("user=").append(URLEncoder.encode(selectedPersonId, "UTF-8"));

            Component[] components = drinksPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JPanel) {
                    JPanel drinkPanel = (JPanel) component;
                    JLabel drinkLabel = (JLabel) drinkPanel.getComponent(0);
                    JSlider slider = (JSlider) drinkPanel.getComponent(1);

                    // Encode each drink's quantity as a URL parameter
                    query.append("&type[]=").append(URLEncoder.encode(String.valueOf(slider.getValue()), "UTF-8"));
                }
            }

            // Print the URL and query string for debugging
            System.out.println("URL: " + API_BASE_URL + "?cmd=" + API_SAVE);
            System.out.println("Query: " + query.toString());

            // Create URL for the POST request
            URL url = new URL(API_BASE_URL + "?cmd=" + API_SAVE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Write query data to the request body
            OutputStream os = connection.getOutputStream();
            os.write(query.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            // Get response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            System.out.println("odpoved: " + response.toString());



            System.out.println(response.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}

