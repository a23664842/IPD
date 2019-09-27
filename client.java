package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class client {

public static void main(String args[]) throws IOException{


	BufferedReader sc=null;//讀入IP用的
	sc= new BufferedReader(new InputStreamReader(System.in));
	System.out.println("enter ip address");
	String addr=sc.readLine();//讀入ip
    //InetAddress address=InetAddress.getLocalHost();
    Socket s1=null;//
    String line=null;
    BufferedReader br=null;
    BufferedReader is=null;
    PrintWriter os=null;

    try {
        s1=new Socket(addr, 4445); //建立SOCKET
        br= new BufferedReader(new InputStreamReader(System.in));//讀鍵盤輸入
        is=new BufferedReader(new InputStreamReader(s1.getInputStream()));//讀SERVER輸入
        os= new PrintWriter(s1.getOutputStream());//輸出到SERVER
    }
    catch (IOException e){
        e.printStackTrace();
        System.err.print("IO Exception");
    }

    System.out.println("Client Address : "+addr);
    System.out.println("Enter C to creat a room;J to join a room;L to list room ( Enter QUIT to end):");

    String response=null;
    try{
        line=br.readLine(); 
        while(line.compareTo("QUIT")!=0){//不是quit就進來做
                os.println(line);
                os.flush();
                response=is.readLine();
                response=response.replaceAll("Z", "\r\n");//因為我用Z代表換行
                if(response.equals("wait"))//代表接下來先有一個自己輸入的棋和對方輸入的棋
                {
                	System.out.println(response);
                	response=is.readLine();
                	response=response.replaceAll("Z", "\r\n");
                	System.out.println(response);
                	response=is.readLine();
                	response=response.replaceAll("Z", "\r\n");
                }
                System.out.println(response);
                line=br.readLine();

            }



    }
    catch(IOException e){
        e.printStackTrace();
    System.out.println("Socket read Error");
    }
    finally{

        is.close();os.close();br.close();s1.close();
                System.out.println("Connection Closed");

    }

}
}