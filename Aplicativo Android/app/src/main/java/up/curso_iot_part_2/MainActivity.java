package up.curso_iot_part_2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button btnServer;
    private Button btnClient;
    private Button btnOk;
    private TextView ipText;
    private TextView dataText;
    private EditText editText;
    private Connection connection;

    private final static int PORTA = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnServer = (Button) findViewById(R.id.activity_main_server_btn);
        btnClient = (Button) findViewById(R.id.activity_main_client_btn);
        btnOk = (Button) findViewById(R.id.activity_main_ok_btn);
        ipText = (TextView) findViewById(R.id.activity_main_ip_txt);
        dataText = (TextView) findViewById(R.id.activity_main_data_txt);
        editText = (EditText) findViewById(R.id.activity_main_edit_txt);

        btnClient.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Config_Client();
                btnServer.setEnabled(false);
            }
        });
        btnServer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Config_Server();
                btnClient.setEnabled(false);
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                connection.Send_Data(editText.getText().toString());
                editText.setText("");
            }
        });

        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED){}
            ipText.setText("IP: " + Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress()));
    }

    private void Config_Server()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    ServerSocket serverSocket = new ServerSocket(PORTA);
                    Socket client = serverSocket.accept();
                    connection = new Connection(client,actualization_handler);
                    connection.start();
                    Toast_Message("Conectado");
                }
                catch (Exception e)
                {
                    Toast_Message("Erro de Conexão");
                }
            }
        }).start();
    }
    private void Config_Client()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Socket socket = new Socket(editText.getText().toString(),PORTA);
                    connection = new Connection(socket,actualization_handler);
                    connection.start();
                    Toast_Message("Conectado");
                }
                catch(Exception e)
                {
                    Toast_Message("Erro de Conexão");
                }
            }
        }).start();
    }
    private Handler actualization_handler = new Handler(Looper.myLooper())
    {
        @Override
        public  void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            String string = (String)msg.obj;
            dataText.setText(dataText.getText() + "\n" + string);
        }
    };
    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            String str = (String) msg.obj;
            Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
        }
    };

    private void Toast_Message(final String message)
    {
        Message _message = new Message();
        _message.obj=message;
        handler.sendMessage(_message);
    }
}
