// on server emulator enter : redir add udp:6000:6000
// ++ redir add udp:6500:6500

package com.example.udpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String SERVER_IP   = "10.0.2.2"; // 10.0.2.2 224.0.0.10 Actually my host have this address as localhost!
    private static final int    CLIENT_PORT = 8000; //8000
    private static final int    SERVER_PORT = 6000; //6000

    private TextView            txt_caption;
    private EditText            txt_input;
    private Button              btn_send;
    private Button              btn_clear;
    private Handler             Handler;
    private Button              btn_exit;
    
    private Client				_client;
    
    public static final int		PACKET_SIZE = 100;
    
    static final String			LOG_TAG = "UDP_Client";
    private int Lenght;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
        multicastLock.acquire();
		
        txt_caption = (TextView) findViewById(R.id.textView1);
        txt_input = (EditText) findViewById(R.id.editText1);
        btn_send = (Button) findViewById(R.id.button1);
        btn_exit = (Button) findViewById(R.id.ext);
        btn_clear = (Button) findViewById(R.id.clr);
        btn_send.setOnClickListener(myhandler1);
        btn_exit.setOnClickListener(myhandler2);
        btn_clear.setOnClickListener(myhandler3);
        
        Handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                String text = (String) msg.obj;
                txt_caption.append(text);
            }
        };
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class Client implements Runnable
    {
		DatagramSocket			socket;			// client socket
        InetAddress				serverAddress;	// server address
        byte[]					buffer;
        int						command;

        // client constructor
        public Client() {
                try {
                		command = 1;
                        socket = new DatagramSocket(CLIENT_PORT);
                        socket.setReuseAddress(true);
                        serverAddress = InetAddress.getByName(SERVER_IP);
                        updateTrack("\ninitializing ...\n");
                }
                catch (SocketException e) {
                        // TODO Auto-generated catch block
	                    e.printStackTrace();
	                    Log.d(LOG_TAG, e.getMessage());
                }
                catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
	                    e.printStackTrace();
	                    Log.d(LOG_TAG, e.getMessage());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(LOG_TAG, e.getMessage());
            }
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(500);
                if ( txt_input.getText().toString() != "")
                {
                    buffer = txt_input.getText().toString().getBytes();
                    updateTrack("input: "+txt_input.getText()+"\n");
                }
                else
                {
                	updateTrack("Empty Text Input !");
                    return;
                }
                
                updateTrack("request is sending ...\n");
                final ByteArrayOutputStream _byteArrayOutputStream = new ByteArrayOutputStream();
                final DataOutputStream _dataOutputStream = new DataOutputStream(_byteArrayOutputStream);
                
                _dataOutputStream.writeInt(PACKET_SIZE);
                _dataOutputStream.writeInt(txt_input.length());
                _dataOutputStream.write(buffer);
                //ADD Command ! == 1
                _dataOutputStream.writeInt(command);
                command++;
                // DEL -> 2
                _dataOutputStream.close();
                
                //DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
                DatagramPacket packet = new DatagramPacket(_byteArrayOutputStream.toByteArray(), _byteArrayOutputStream.size(), serverAddress, SERVER_PORT);
                
                updateTrack("packet is sending ...\n");
                //socket.joinGroup(serverAddress);
                socket.send(packet);
                // TODO get ack from server !
                
                this.listen();
                
            }
            catch (Exception e)
            {
                updateTrack("Client: Error!\n");
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
            }
        }
        
        public void close() {
            updateTrack("socket is closing ! ...\n");
            socket.close();
        }
        
        public void listen() {
        	byte[] buffer = new byte[PACKET_SIZE];       	
        	DatagramPacket packet = new DatagramPacket(buffer, buffer.length); 
        	ByteArrayInputStream _Input = new ByteArrayInputStream(packet.getData());
        	DataInputStream _Input_data = new DataInputStream(_Input);
            ByteArrayOutputStream _byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream _dataOutputStream = new DataOutputStream(_byteArrayOutputStream);
        	int seq = 0;
        	int clientno;
            byte[] _data;
            String _data_string;
            int Port;
            int packetType;
            Random generator = new Random();
            while (true) {
            	try {
                    updateTrack("listening ...\n");
                    socket.receive(packet);
                    Port = packet.getPort();
                    _Input = new ByteArrayInputStream(packet.getData());
                    _Input_data = new DataInputStream(_Input);
                    updateTrack("response received.\n");
                    clientno = _Input_data.readInt();
                    if (seq > 0){
	                    _dataOutputStream.writeInt(clientno);
	                    _dataOutputStream.close();
	                    DatagramPacket packet2 = new DatagramPacket(_byteArrayOutputStream.toByteArray(), _byteArrayOutputStream.size(), serverAddress, Port);
	                    int r = generator.nextInt();
	                    if(r%2==0){
	                    	socket.send(packet2);
	                    }
                    }
                    seq++;
                    Lenght = _Input_data.readInt();
                    _data = new byte[Lenght];
                    _Input_data.read(_data);
	                _data_string = new String(_data);
	                packetType=_Input_data.readInt();
                    updateTrack("server : " + clientno+"\n");
                    updateTrack("server : " + _data_string+"\n");
	                updateTrack("ip: " + packet.getAddress().getHostAddress()+"\n");
	                updateTrack("port: " + packet.getPort()+"\n");
	                updateTrack("packetType: "+packetType+"\n");
            	} catch (Exception e) {
            		updateTrack("Error! Client Listening .. !\n");
                    e.printStackTrace();
                    Log.d(LOG_TAG, e.getMessage());
            	}
            }
        }
    }
	
	View.OnClickListener myhandler1 = new View.OnClickListener() {
	    @Override
	    public void onClick(View v)
	    {
    		btn_send.setEnabled(false);
	    	_client = new Client();
	        new Thread(_client).start();
	        btn_send.setEnabled(true);
	    }
	};

	View.OnClickListener myhandler2 = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
        	_client.close();
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
        Message msg2 = new Message();
        msg2.obj = msg;
        Handler.sendMessage(msg2);
    }
}