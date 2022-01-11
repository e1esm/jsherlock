package com.esm;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MainActivity {
    String username;

    MainActivity(){
        run();
    }

    protected void run(){
        boolean isRunning = true;
        Scanner sc = new Scanner(System.in);
        int answer;
        while (isRunning){
            options();
            answer = sc.nextInt();
            sc.nextLine();
            switch (answer){
                case 1:
                    System.out.println("Enter the nickname you want to look for: ");
                    username = sc.nextLine();
                    new AccountChecker(username);
                    break;
                case 2:
                    fileOpener();
                    break;

                case 3:
                    System.out.println("See you soon!");
                    isRunning = false;
                    break;
                default:
                    System.out.println("You've picked wrong number, try again.");
            }

        }
    }



    protected void options(){
        System.out.println("Hey there, it's Jsherlock. Type in the username you want to search for");
        System.out.println("1.Look for accounts.");
        System.out.println("2.Open file with links");
        System.out.println("3.exit");
    }


    protected void fileOpener(){
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File("userInformation.txt"));
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

}
