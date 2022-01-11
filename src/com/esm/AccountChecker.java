package com.esm;

import javax.xml.transform.Result;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class AccountChecker {
    protected final static ArrayList<String> webServicesLinks = new ArrayList<String>(Arrays.asList("https://t.me/", "https://www.instagram.com/" ,"https://www.facebook.com/","https://twitter.com/", "https://www.reddit.com/user/", "https://www.youtube.com/c/", "https://vk.com/" ,"https://steamcommunity.com/id/"));
    protected String username;
    protected HashMap<ArrayList<String>, Boolean> existence;
    AccountChecker(String username){
        this.username = username;
        existence = new HashMap<ArrayList<String>, Boolean>();
        existence(username);
        moveToDataBase();
    }
    enum Service{
        telegram,
        instagram,
        facebook,
        twitter,
        reddit,
        youtube,
        vk,
        steam;

        @Override
        public String toString(){
          return super.toString();
        }
    }

    private void existence(String username){
        String link;
        Iterator<String> linksiterator =  webServicesLinks.iterator();
        while(linksiterator.hasNext()){
            String URLwithoutNickname =  linksiterator.next();
            link = URLwithoutNickname + username;
            if(getResponseCode(link) == HttpURLConnection.HTTP_NOT_FOUND){
                enumOptionPicker(URLwithoutNickname, link, false);
            }else{
                enumOptionPicker(URLwithoutNickname, link, true);
            }
        }
    }

    private int getResponseCode(String link){
        int responseCode = 0;
        try {
            URL url = new URL(link);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            responseCode = httpURLConnection.getResponseCode();
        }catch (MalformedURLException e){
            System.out.println(e.getMessage());
        }catch (IOException f){
            System.out.println(f.getMessage());
        }
        return responseCode;
    }

    private void enumOptionPicker(String URL, String URLWithNickname, boolean state){
        String webservice = "";
        if (URL.contains("t.me"))
            webservice = Service.telegram.toString();
        else if(URL.contains("instagram"))
            webservice = Service.instagram.toString();
        else if(URL.contains("facebook"))
            webservice = Service.facebook.toString();
        else if(URL.contains("twitter"))
            webservice = Service.twitter.toString();
        else if(URL.contains("reddit"))
            webservice = Service.reddit.toString();
        else if(URL.contains("youtube"))
            webservice = Service.youtube.toString();
        else if(URL.contains("vk"))
            webservice = Service.vk.toString();
        else if(URL.contains("steam"))
            webservice = Service.steam.toString();


        existence.put(new ArrayList<String>(Arrays.asList(webservice,URLWithNickname)), state);
        }

        protected void moveToDataBase(){
             new DatabaseConnector(this);
        }

    protected static class DatabaseConnector{
        private final static String URL = "jdbc:postgresql://localhost:5432/socialnetaccounts";
        private String username;
        private String password;
        private Connection connection;
        AccountChecker accountChecker;
        DatabaseConnector(AccountChecker accountChecker){
            this.accountChecker = accountChecker;
            setCredentials();
            try{
                Class.forName("org.postgresql.Driver");
            }catch (ClassNotFoundException e){
                System.out.println(e.getMessage());
            }
            try {
                connection = DriverManager.getConnection(URL, username, password);
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accountslinks (username) VALUES (?);");
                preparedStatement.setString(1, accountChecker.username);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }catch(SQLException sqlException){
                System.out.println(sqlException.getMessage());
            }
            migrateLinksToDB();

        }

        private void setCredentials(){
            try {
                FileReader fileReader = new FileReader("credentials.txt");
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String credentials = bufferedReader.readLine();
                username = credentials.substring(0, credentials.indexOf(':'));
                password = credentials.substring(credentials.indexOf(':') + 1);
                credentials = null;
                bufferedReader.close();
                fileReader.close();
            }catch (FileNotFoundException e){
                System.out.println(e.getMessage());
            }catch (IOException f){
                System.out.println(f.getMessage());
            }
        }
        private void migrateLinksToDB(){
            String service = "";
            String sql = "";
            String actualLink = "";
            try {
                PreparedStatement preparedStatement;
                for(Map.Entry<ArrayList<String>, Boolean> entry: accountChecker.existence.entrySet()){
                    service = entry.getKey().get(0);
                    actualLink = entry.getKey().get(1);
                    if(entry.getValue()){
                         sql = "UPDATE accountslinks"
                                + " SET " + service + " = ?"
                                + " WHERE username  = ?;";
                         preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1, actualLink);
                        preparedStatement.setString(2, accountChecker.username);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                    else{
                        sql = "UPDATE accountslinks"
                                + " SET " + service + " = ?"
                                + " WHERE username  = ?;";
                        preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1, "Not found");
                        preparedStatement.setString(2, accountChecker.username);
                    }
                }
            }catch (SQLException e){
                System.out.println(e.getMessage());
            }
            moveToFile();
        }


        private void moveToFile(){
            String toFile = "";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from accountslinks WHERE username = (?);");
                preparedStatement.setString(1, accountChecker.username);
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()){
                    toFile = resultSet.getString(1) + ":\nTelegram: " + resultSet.getString(2) + "\nInstagram: " + resultSet.getString(3) + "\nFacebook: " + resultSet.getString(4)
                    + "\nTwitter: " + resultSet.getString(5) + "\nReddit: " + resultSet.getString(6) + "\nYoutube: " + resultSet.getString(7) + "\nVK: " + resultSet.getString(8) + "\nSteam: " + resultSet.getString(9);
                }
                resultSet.close();
                try {
                    FileWriter fileWriter = new FileWriter("userInformation.txt");
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(toFile);
                    bufferedWriter.close();
                    fileWriter.close();
                }catch (IOException f){
                    System.out.println(f.getMessage());
                }
            }catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }
    }
}
