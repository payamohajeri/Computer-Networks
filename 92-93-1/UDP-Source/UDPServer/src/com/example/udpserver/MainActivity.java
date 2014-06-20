// on server emulator enter : redir add udp:6000:6000
// ++ redir add udp:6500:6500

package com.example.udpserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String LOG_TAG = "UDP_Server";
	
    private static final String SERVER_IP   = "10.0.2.15"; // 10.0.2.15 Actually my host have this address as localhost!
    private static final int    SERVER_PORT = 6000; //6000
    private static int packetSize = 100;
    public static final int		PACKET_SIZE = 100;

    private TextView            txt_caption;
    private EditText            txt_input;
    private Button              btn_send;
    private Button              btn_clear;
    private Button              btn_exit;
    private Handler				Handler;
    
    private int					_clientNo;
    private int					seq;
    private DatagramSocket		socket;
    private InetAddress			serverAddress;
    private static int			RTO;
    
	
    Server _server;
    Client[] clientList;
    
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        txt_caption = (TextView) findViewById(R.id.textView1);
        txt_input = (EditText) findViewById(R.id.editText1);
        btn_send = (Button) findViewById(R.id.button1);
        btn_exit = (Button) findViewById(R.id.ext);
        btn_clear = (Button) findViewById(R.id.clr);
        btn_send.setOnClickListener(myhandler1);
        btn_exit.setOnClickListener(myhandler2);
        btn_clear.setOnClickListener(myhandler3);
        
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
        multicastLock.acquire();
        
        Handler = new Handler()
        {

            @Override
            public void handleMessage(Message msg)
            {
                String text = (String) msg.obj;
                txt_caption.append(text);
            }
        };
        _server = new Server();
        new Thread(_server).start();
        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class Client {
		public int _port;
		public InetAddress _InetAddress;
		public Client(int port, InetAddress Address){
			_port=port;
			_InetAddress=Address;
		}
	}
	
	public class Server implements Runnable
    {
        private DatagramPacket packet;
        private String         msg;
        private int Lenght;
        private byte[] _data;
        private String _data_string;

        public Server()
        {
            clientList = new Client[20];
            _clientNo = 0;
            seq = 0;
            RTO=5000;
            
            try {
                serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new DatagramSocket(SERVER_PORT);
                byte[] buf = new byte[packetSize];
                packet = new DatagramPacket(buf, buf.length);
                //socket.joinGroup(serverAddress);
                socket.setReuseAddress(true);
            }
            catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
            }
            catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
            }
        }

        @Override
        public void run()
        {
        	while (true) {
	            try
	            {
	                //-------------------------------------------------------------------
	                // (1) listen
	                //-------------------------------------------------------------------
	                updateTrack("\nlistening ...\n");
	                socket.receive(packet);
	                
	                updateTrack("request received.\n");
	
	                InetAddress clientAddress = packet.getAddress();
	                int clientPort = packet.getPort();
	
	                final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(packet.getData());
	                final DataInputStream dataInputStream = new DataInputStream(byteInputStream);
	                
	                packetSize = dataInputStream.readInt();
	                Lenght = dataInputStream.readInt();
	                _data = new byte[Lenght];
	                dataInputStream.read(_data);
	                _data_string = new String(_data);
	                
                	//ByteArrayOutputStream _output = new ByteArrayOutputStream();
                	//DataOutputStream _output_data = new DataOutputStream(_output);
	                // TODO check dublicate clients !
	                
	                if(dataInputStream.readInt() == 1){
	                	if (_clientNo < 20 ){
		                	clientList[_clientNo]= new Client(clientPort,clientAddress);
		                	updateTrack("--IMP--client added with below details\n");
		                	_clientNo++;
	                	} else {
	                		updateTrack("--IMP--no more clients are allowed !\n");
	                		return;
	                	}
	                } else {
	                	updateTrack("recieve data is: " + _data_string + "\n");
	                }
	                //updateTrack("server : " + packet.getData().toString()+"\n");
	                updateTrack("ip: " + packet.getAddress().getHostAddress()+"\n");
	                updateTrack("port: " + packet.getPort()+"\n");
	                
	                byte[] buffer = new byte[PACKET_SIZE];
	                ByteArrayOutputStream _output = new ByteArrayOutputStream();
	                DataOutputStream _output_data = new DataOutputStream(_output);
	                _output_data.writeInt(seq);
	                Lenght = ("Wellcome").toString().length();
	                buffer = ("Wellcome").getBytes();
	                _output_data.writeInt(Lenght);
	                _output_data.write(buffer);
	                _output_data.writeInt(100);
	                _output_data.close();
	                packet = new DatagramPacket(_output.toByteArray(), _output.size(), clientAddress, clientPort);
	                socket.send(packet);
	                
	            }
	            catch (Exception e) {
	                //updateTrack("Server: Error!\n");
	                //e.printStackTrace();
	                Log.d(LOG_TAG, e.getMessage());
	            }
	        }
        }
    }
	
	public class Send2 implements Runnable {
		
		private DatagramPacket		packet2;
	    private DatagramSocket		socket2; //MulticastSocket
	    private	byte[]				buffer;
	    private	byte[]				buffer2;
        private int Lenght;
        private byte[] _data;
        private String _data_string;
        private boolean timeout;
        private ByteArrayOutputStream _output;
        private DataOutputStream _output_data;
        private ByteArrayInputStream _Input;
        private DataInputStream _Input_data;
        private int[] clients;
        private boolean x;
	    
	    public Send2(byte[] msg){
	    	buffer = msg;
	    	timeout=false;
	    }
	    
	    public void run() {
	    	try {
	    		socket2 = new DatagramSocket(6500);
	    		socket.setReuseAddress(true);
	    		clients = new int[_clientNo];
	    		for(int i=0; i< clients.length; i++){
	    			clients[i]=-1;
	    		}
	    		//socket2.joinGroup(serverAddress);
		    	for (int i=0; i<_clientNo; i++){
	         		// TODO insert MD5 of msg and its size
		    		_output = new ByteArrayOutputStream();
	                _output_data = new DataOutputStream(_output);
	                _output_data.writeInt(i);
	                Lenght =  new String(buffer).length();
	                _output_data.writeInt(Lenght);
	                _output_data.write(buffer);
	                _output_data.writeInt(0);
	                _output_data.close();
	                packet2 = new DatagramPacket(_output.toByteArray(), _output.size(), clientList[i]._InetAddress, clientList[i]._port);
	                socket2.send(packet2);
	    		}
		    	int count = 0;
		    	for (int i=0; i<_clientNo; i++)
		    	{
		    		buffer2 = new byte[PACKET_SIZE];
		    		packet2 = new DatagramPacket(buffer2, buffer2.length);
		    		int clientno = -1;
		    		try {
		    			socket2.setSoTimeout(RTO);
		    			socket2.receive(packet2);
	                    _Input = new ByteArrayInputStream(packet2.getData());
	                    _Input_data = new DataInputStream(_Input);
		                InetAddress clientAddress = packet2.getAddress();
		                int clientPort = packet2.getPort();
		                /*
		                for (int j=0; j <_clientNo; j++) {
		                	if( clientList[j]._InetAddress==clientAddress && clientList[j]._port==clientPort ) {
		                		clients[count]=j;
		                		count++;
		                	}
		                }
		                */
		                clientno = _Input_data.readInt();
		                if(clientno != -1){
			                clients[count]=clientno;
			                count++;
	    	                updateTrack("recieved packet to client no : " + clientno+"\n");
		                }
		    		} catch (SocketTimeoutException e) {
		    			timeout = true;
		    			updateTrack("we have timeout here !\n");
		    		}
		    	}
		    	if (timeout){
		    		for (int i=0; i < _clientNo; i++){
		    			x = false;
		    			for(int j=0; j<_clientNo; j++){
		    				if(clients[j]==i){
		    	                x=true;
		    				}
		    			}
		    			if (!x){
				    		_output = new ByteArrayOutputStream();
			                _output_data = new DataOutputStream(_output);
			                _output_data.writeInt(i);
			                _output_data.writeInt(Lenght);
			                _output_data.write(buffer);
			                _output_data.writeInt(1);
			                _output_data.close();
			                packet2 = new DatagramPacket(_output.toByteArray(), _output.size(), clientList[i]._InetAddress, clientList[i]._port);
	    	                while(this.resend(i,packet2,socket2)){
	    	                	updateTrack("Sending Again ... \n");
	    	                }
	    	                updateTrack("packet recieved.\n");
		    			}
		    		}
		    	}
		    	timeout = false;
		    	socket2.close();
        	} catch (NetworkOnMainThreadException e) {
        		updateTrack("Error: NetworkOnMainThreadException exception\n");
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
        	} catch (IOException e) {
        		updateTrack("Error: IOException exception\n");
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
        	}
	    }
	    public boolean resend(int i, DatagramPacket packet2, DatagramSocket socket2){
	    	boolean timeout = false;
	    	try {
	    		buffer = new byte[PACKET_SIZE];
	            try {
		            socket2.setSoTimeout(RTO);
		            socket2.send(packet2);
		            updateTrack("resend packet to client no : " + i +"\n");
		            packet2 = new DatagramPacket(buffer, buffer.length);
		            socket2.receive(packet2);
	            } catch (SocketTimeoutException e) {
	    			updateTrack("we have timeout here !\n");
	    			timeout=true;
	    		}
	    	} catch (NetworkOnMainThreadException e) {
        		updateTrack("Error: NetworkOnMainThreadException exception\n");
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
        	} catch (IOException e) {
        		updateTrack("Error: IOException exception\n");
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
        	}
	    	return timeout;
	    }
	}
	
	View.OnClickListener myhandler1 = new View.OnClickListener() {
	    @Override
	    public void onClick(View v)
	    {
	    	try {
	    		btn_send.setEnabled(false);
		    	byte[] buffer = new byte[PACKET_SIZE];
	            if ( txt_input.getText().toString() != "")
	            {
	                buffer = txt_input.getText().toString().getBytes();
	                updateTrack("input: "+txt_input.getText()+"\n");
	            }
	            else
	            {
	            	updateTrack("input: Default !? \n");
	            	buffer = ("Default Mesage : Hellooo !").getBytes();
	            }
	            Send2 _send = new Send2(buffer);
	            new Thread(_send).start();
	    		btn_send.setEnabled(true);
	            
	    	} catch (NullPointerException e) {
	    		Log.d(LOG_TAG, e.getMessage());
	    	}
	    }
	};

	View.OnClickListener myhandler2 = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
            finish();
            System.exit(0);
        }
    };

	View.OnClickListener myhandler3 = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
        	txt_caption.setText("History : \n");
        }
    };

    public void updateTrack(String msg)
    {
    	//Log.v(TAG, msg);
        Message msg2 = new Message();
        msg2.obj = msg;
        Handler.sendMessage(msg2);
    }
}
