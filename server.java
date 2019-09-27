package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class server {
	public static int[][] room = new int[100][2];//用來存房間內個別CLIENT的SOCKET號碼
	public static ArrayList<Socket> clients= new ArrayList<Socket>();//存每個建立連線的SOCKET
	public static int[][] game = new int[100][9];//存每個房間的棋盤
	
public static void main(String args[]){
    for(int i=0;i<100;i++)//將房間都清空-1代表空位
    {
    	room[i][0]=-1;
    	room[i][1]=-1;
    }
    System.out.println(server.room[0][0]);
    Socket s=null;
    ServerSocket ss2=null;
    System.out.println("Server Listening......");
    try{
        ss2 = new ServerSocket(4445); // can also use static final PORT_NUM , when defined

    }
    catch(IOException e){
    e.printStackTrace();
    System.out.println("Server error");

    }

    while(true){
        try{
            s= ss2.accept();
            clients.add(s);//ACCEPT後將其SOCKET加入CLIENT以便之後互傳要用
            System.out.println("connection Established");
            ServerThread st=new ServerThread(s,clients.size()-1);//將SOCKET和它的編號傳入THREAD
            st.start();

        }

    catch(Exception e){
        e.printStackTrace();
        System.out.println("Connection Error");

    }
    }

}

}

class ServerThread extends Thread{  //開始服務提供井字遊戲

    String line=null;
    BufferedReader  is = null;
    PrintWriter os=null;//自己的CLIENT輸出
    PrintWriter os1=null;//和人對戰時對方的CLIENT輸出
    Socket s=null;
    int servernum=0;
    int roomnum=0;

    public ServerThread(Socket s, int servernum){//初始化SOCKET和其編號
        this.s=s;
        this.servernum=servernum;
    }

    public void run() {
    try{
        is= new BufferedReader(new InputStreamReader(s.getInputStream()));
        os=new PrintWriter(s.getOutputStream());

    }catch(IOException e){
        System.out.println("IO error in server thread");
    }

    try {
        line=is.readLine();//讀入指令
        while(line.compareTo("QUIT")!=0){//開始判斷指令並不斷迴圈直到退出
            if(line.compareTo("C")==0)//創建房間
            {
            	os.println("Enter C to play with Computer;P to play with player");
            	os.flush();
            	for(int i=0;i<100;i++ )//找到空房間才填入自己的編號
            	{
            		if(server.room[i][0]==-1)
            		{
            		server.room[i][0]=servernum;
            		roomnum=i;
            		break;
            		}
            	}
            	line=is.readLine();//讀對上電腦或人
            	if(line.compareTo("C")==0)//對電腦
            	{
            		server.room[roomnum][1]=servernum;//將房間都設為自己的編號以防有人以為可以進入
            		while(true)
            		{
            		Ai(roomnum);//電腦下棋
            		if(checkwin(roomnum)==1)//判斷勝負
        			{
        				os.println("you lose;back to hall");
        				os.flush();
        				server.room[roomnum][0]=-1;//清空房間和棋盤
        				server.room[roomnum][1]=-1;
        				for(int i=0;i<9;i++)
        				{
        					server.game[roomnum][i]=0;
        				}
        				break;
        			}
            		else if(checkwin(roomnum)==2)//判斷勝負
        			{
        				os.println("you win;back to hall");
        				os.flush();
        				server.room[roomnum][0]=-1;//清空房間和棋盤
        				server.room[roomnum][1]=-1;
        				for(int i=0;i<9;i++)
        				{
        					server.game[roomnum][i]=0;
        				}
        				break;
        			}
        			else if(checkwin(roomnum)==3)//判斷勝負
        			{
        				os.println("Flat;back to hall");
        				os.flush();
        				server.room[roomnum][0]=-1;//清空房間和棋盤
        				server.room[roomnum][1]=-1;
        				for(int i=0;i<9;i++)
        				{
        					server.game[roomnum][i]=0;
        				}
        				break;
        			}
            		os.println(printgame(roomnum)+"your trun enter position");//玩家下棋
            		os.flush();
            		line=is.readLine();
        			server.game[roomnum][Integer.valueOf(line)]=2;
            		}
            	}
            	else if(line.compareTo("P")==0)//對人
            	{
            		os.println("wait");//叫CLIENT要等對方的進入
            		os.flush();
            		line=is.readLine();//對方下完棋換我方下
            		server.game[roomnum][Integer.valueOf(line)]=2;
                    os1 = new PrintWriter(server.clients.get(server.room[roomnum][1]).getOutputStream());//加入對方的SOCKET輸出用來傳給對方
            		while(true)//重複下棋和判斷輸贏的動作直到平手或有人贏
            		{
            			os.println("wait");
            			os.flush();
            			os.println(printgame(roomnum));
            			os.flush();
            			os1.println(printgame(roomnum)+"your turn");
            			os1.flush();
            			line=is.readLine();
            			server.game[roomnum][Integer.valueOf(line)]=2;
            			if(checkwin(roomnum)==2)
            			{
            				os.println("you win;back to hall");
            				os.flush();
            				os1.println("you lose;back to hall");
            				os1.flush();
            				server.room[roomnum][0]=-1;
            				server.room[roomnum][1]=-1;
            				for(int i=0;i<9;i++)
            				{
            					server.game[roomnum][i]=0;
            				}
            				break;//回到大廳讀指令
            			}
            		}
            	}
            	
            }
            else if(line.compareTo("J")==0)//加入房間
            {
            	os.println("Enter number of room");//輸入房間編號
            	os.flush();
            	line=is.readLine();
            	if(server.room[Integer.valueOf(line)][0]!=-1&&server.room[Integer.valueOf(line)][1]==-1)//判斷房間是否可進入
            	{
            		server.room[Integer.valueOf(line)][1]=servernum;//將房間的第二個位置設為自己的編號
            		roomnum=Integer.valueOf(line);
            		os1=new PrintWriter(server.clients.get(server.room[Integer.valueOf(line)][0]).getOutputStream());//設置對方的SOCKET輸出
            		os.println("wait");//開始下棋我方先攻
            		os.flush();
            		os.println("enter the position you want");
            		os.flush();
            		os.println(printgame(roomnum));
            		os.flush();
            		os1.println("connect");
            		os1.flush();
            		line=is.readLine();
            		server.game[roomnum][Integer.valueOf(line)]=1;
            		while(true)//重複下棋和判斷輸贏的動作直到平手或有人贏
            		{
            			os.println("wait");
            			os.flush();
            			os.println(printgame(roomnum));
            			os.flush();
            			os1.println(printgame(roomnum)+"your turn");
            			os1.flush();
            			line=is.readLine();
            			server.game[roomnum][Integer.valueOf(line)]=1;
            			if(checkwin(roomnum)==1)
            			{
            				os.println("you win;back to hall");
            				os.flush();
            				os1.println("you lose;back to hall");
            				os1.flush();
            				server.room[roomnum][0]=-1;
            				server.room[roomnum][1]=-1;
            				for(int i=0;i<9;i++)
            				{
            					server.game[roomnum][i]=0;
            				}
            				break;
            			}
            			else if(checkwin(roomnum)==3)
                			{
                				os.println("Flat;back to hall");
                				os.flush();
                				os1.println("Flat;back to hall");
                				os1.flush();
                				server.room[roomnum][0]=-1;
                				server.room[roomnum][1]=-1;
                				for(int i=0;i<9;i++)
                				{
                					server.game[roomnum][i]=0;
                				}
                				break;//回到大廳讀指令
                			}
            		}
            		
            		
            	}
            }
            else if(line.compareTo("L")==0)//列出現在正在等待的房間
            {
            	String roomlist = "roomlist:Z";
            	for(int i=0;i<100;i++ )
            	{
            		if(server.room[i][0]!=-1&&server.room[i][1]==-1)
            		roomlist = roomlist+String.valueOf(i)+"Z";
            	}
            	os.println(roomlist+"end");
            	os.flush();
            }
            else//如果輸入的不是指令
            {
            	os.println("wrong commend ;C creat room;J join room;L list room");
            	os.flush();
            }
            line=is.readLine();
        }   
    } catch (IOException e) {

        line=this.getName(); //reused String line for getting thread name
        System.out.println("IO Error/ Client "+line+" terminated abruptly");
    }
    catch(NullPointerException e){
        line=this.getName(); //reused String line for getting thread name
        System.out.println("Client "+line+" Closed");
    }

    finally{    //最後關掉SOCKET等等
    try{
        System.out.println("Connection Closing..");
        if (is!=null){
            is.close(); 
            System.out.println(" Socket Input Stream Closed");
        }

        if(os!=null){
            os.close();
            System.out.println("Socket Out Closed");
        }
        if (s!=null){
        s.close();
        System.out.println("Socket Closed");
        }

        }
    catch(IOException ie){
        System.out.println("Socket Close Error");
    }
    }//end finally
    }//end run
    
    public int checkwin(int roomn)//判斷勝負用
    {
    	int x=0;
    	for(int i=0;i<3;i=i+3)//行列判斷
    	{
    	if(server.game[roomn][i]==server.game[roomn][i+1]&&server.game[roomn][i+1]==server.game[roomn][i+2]&&server.game[roomn][i]+server.game[roomn][i+1]+server.game[roomn][i+2]!=0)
    	{
    		if(server.game[roomn][i]==1)
    		{
    			return 1;
    		}
    		else if(server.game[roomn][i]==2)
    		{
    			return 2;
    		}
    	}
    	if(server.game[roomn][x]==server.game[roomn][x+3]&&server.game[roomn][x+3]==server.game[roomn][x+6]&&server.game[roomn][x]+server.game[roomn][x+3]+server.game[roomn][x+6]!=0)
    	{
    		if(server.game[roomn][x]==1)
    		{
    			return 1;
    		}
    		else if(server.game[roomn][x]==2)
    		{
    			return 2;
    		}
    	}
    	x++;
    	}                        //行列判斷結束
    	if(server.game[roomn][0]==server.game[roomn][4]&&server.game[roomn][4]==server.game[roomn][8]&&server.game[roomn][0]+server.game[roomn][4]+server.game[roomn][8]!=0)//斜線判斷
    	{
    		if(server.game[roomn][0]==1)
    		{
    			return 1;
    		}
    		else if(server.game[roomn][0]==2)
    		{
    			return 2;
    		}
    	}//斜線判斷結束
    	if(server.game[roomn][2]==server.game[roomn][4]&&server.game[roomn][4]==server.game[roomn][6]&&server.game[roomn][2]+server.game[roomn][4]+server.game[roomn][6]!=0)//斜線判斷
    	{
    		if(server.game[roomn][2]==1)
    		{
    			return 1;
    		}
    		else if(server.game[roomn][2]==2)
    		{
    			return 2;
    		}
    	}//斜線判斷結束
    	for(int i=0;i<9;i++)//平手判斷
    	{
    		if(server.game[roomn][i]==0)
    			break;
    		else if(i==8&&server.game[roomn][1]!=0)
    			return 3;
    	}
    	return 0;
    }
    public String printgame(int roomn)//印出棋盤用
    {
    	String gamenow="";
    	for(int i=0;i<9;i++)
    	{
    		if(i!=2&&i!=5&&i!=8)
    		{
    		if(server.game[roomn][i]==1)
    		{
    			gamenow=gamenow+"X|";
    		}
    		else if(server.game[roomn][i]==2)
    		{
    			gamenow=gamenow+"O|";
    		}
    		else if(server.game[roomn][i]==0)
    		{
    			gamenow=gamenow+" |";
    		}
    		}
    		else
    		{
    			if(server.game[roomn][i]==1)//用Z代替換行
    				gamenow=gamenow+"XZ"+"------Z";
    			else if(server.game[roomn][i]==2)
    				gamenow=gamenow+"OZ"+"------Z";
    			else if(server.game[roomn][i]==0)
    				gamenow=gamenow+" Z"+"------Z";
    		}
    	}
    	
    	return gamenow;
    }
   public void Ai(int roomn)//對電腦時的ai
   {
	   ArrayList move= new ArrayList<>();
	   for(int i=0;i<9;i++)
   	{
   		if(server.game[roomn][i]==0)//可下哪一步
   			move.add(i);
   	}
	   for(int i=0;i<move.size();i++)//看下一步我方是否勝利或對方是否有下一步會勝利
	   {
		   if((int) move.get(i)==4)
		   {
			   server.game[roomn][(int) move.get(i)]=1;
			   move.clear();
			   break;
		   }
		   server.game[roomn][(int) move.get(i)]=2;
		   if(checkwin(roomn)==2)
		   {
			   server.game[roomn][(int) move.get(i)]=1;
			   move.clear();
			   break;
		   }
		   server.game[roomn][(int) move.get(i)]=1;
		   if(checkwin(roomn)==1)
		   {
			   move.clear();
			   break;
		   }
		   server.game[roomn][(int) move.get(i)]=0;
	   }
	   if(!move.isEmpty())//都沒有的話找可以形成兩個X在一起且第三個還是空的
	   {
		   for(int i=0;i<move.size();i++)
		   {
			   server.game[roomn][(int) move.get(i)]=1;
			   if(checkwin(roomn)==1)
			   {
				   move.clear();
				   break;
			   }
			   for(int x=0;x<move.size();x++)
			   {
				   server.game[roomn][(int) move.get(x)]=1;
				   if(checkwin(roomn)==1)
				   {
					   server.game[roomn][(int) move.get(i)]=0;
					   move.clear();
					   break;
				   }
				   server.game[roomn][(int) move.get(x)]=0;
			   }
			   if(!move.isEmpty())
			   server.game[roomn][(int) move.get(i)]=0;
		   }
		   if(!move.isEmpty())
			   server.game[roomn][(int) move.get(0)]=1;
	   }
	     
   }
}