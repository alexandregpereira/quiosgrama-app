package io.oxigen.quiosgrama.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.fiscal.Fiscal;
import io.oxigen.quiosgrama.sat.EasySAT;
import io.oxigen.quiosgrama.sat.SATException;

public class SatActivity extends Activity {

    /**
     * Código de ativação de exemplo.
     * Na prática, deve ser utilizado "00000000" antes da ativação
     * do SAT, e o código especificado em ativarSAT após a ativação.
     */
    public static final String CODIGO_DE_ATIVACAO = "99999999";
    /**
     * CNPJ do contribuinte que estiver utilizado este app.
     * Na prática, deve ser configurável.
     */
    public static final String CNPJ = "05761098000113";
    /**
     * Código IBGE do estado do contribuinte.
     * Na prática, deve ser configurável.
     */
    public static final int CODIGO_UF_SP = 35;
    /**
     * PendingIntent utilizado para pedir permissão USB ao usuário de dentro do app.
     */
    private PendingIntent usbPermissionIntent;
    
    /**
     * Nome de permissão de USB.
     */
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    /**
     * Chave da última venda, para permitir cancelamento.
     */
    private String ultimaChaveVenda = null;

    /**
     * Último número de sessão, para permitir consulta.
     */
    private long ultimoNumeroSessao = 0;
    /**
     * Gerador de números aleatórios.
     */
    private Random random = new Random();
    private EditText edtXml;
    private Button btnShowXml;
    private Button btnClear;

    private QuiosgramaApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sat);

        app = (QuiosgramaApp) getApplicationContext();

        edtXml = (EditText) findViewById(R.id.edtXml);
        btnShowXml = (Button) findViewById(R.id.btnShowXml);
        btnClear = (Button) findViewById(R.id.btnClear);
        testeBuffer();

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtXml.setText("");
            }
        });

        btnShowXml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStream inputStream;
                try {
                    inputStream = getAssets().open("CFeVenda.xml");
                    String xmlVenda = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
                    edtXml.setText(xmlVenda);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Ao clicar em Conectar, solicita permissão do usuário ou registra dispositivo caso
        // já tenha sido autorizado.
        final Button connectButton = (Button) findViewById(R.id.connect_button);
        assert connectButton != null;
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                for (UsbDevice device : deviceList.values()) {
                    if (EasySAT.isEasySATDevice(device)) {
                        if (manager.hasPermission(device)) {
                            app.easySat.setDevice(device);
                        } else {
                            manager.requestPermission(device, usbPermissionIntent);
                        }
                        break;
                    }
                }
            }
        });

        // Funções do SAT. A maioria dos parâmetros são exemplos e devem ser alterados para uso final.

        final Button consultarButton = (Button) findViewById(R.id.consultar_button);
        assert consultarButton != null;
        consultarButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                return app.easySat.consultarSAT(genNumeroSessao());
            }
        }));

        final Button statusOperacionalButton = (Button) findViewById(R.id.status_operacional_button);
        assert statusOperacionalButton != null;
        statusOperacionalButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                return app.easySat.consultarStatusOperacional(genNumeroSessao(), CODIGO_DE_ATIVACAO);
            }
        }));

        final Button ativarButton = (Button) findViewById(R.id.ativar_button);
        assert ativarButton != null;
        ativarButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                // Ativa SAT. Na prática deve-se utilizar um código de ativação novo.
                return app.easySat.ativarSAT(genNumeroSessao(), EasySAT.CERTIFICADO_SEFAZ, CODIGO_DE_ATIVACAO, CNPJ, CODIGO_UF_SP);
            }
        }));

        final Button associarAssinaturaButton = (Button) findViewById(R.id.associar_assinatura_button);
        assert associarAssinaturaButton != null;
        associarAssinaturaButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                //Ativa na retaguarda de testes. No uso final deve-se passar a assinaturaCNPJs correta.
                return app.easySat.associarAssinatura(genNumeroSessao(), CODIGO_DE_ATIVACAO, "1671611400017205761098000113", "SGR-SAT SISTEMA DE GESTAO E RETAGUARDA DO SAT");
            }
        }));

        final Button enviarDadosVendaButton = (Button) findViewById(R.id.enviar_dados_venda_button);
        assert enviarDadosVendaButton != null;
        enviarDadosVendaButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException, IOException {
                //Realiza venda contida no arquivo
                String xmlVenda = edtXml.getText().toString();
                xmlVenda = Fiscal.handleSpecialCharacters(xmlVenda);
                edtXml.setText(xmlVenda);
                Log.d("xmlVenda2", xmlVenda);
                String result = app.easySat.enviarDadosVenda(genNumeroSessao(), CODIGO_DE_ATIVACAO, xmlVenda);
                String[] fields = result.split("\\|");
                if (fields.length >= 9) {
                    ultimaChaveVenda = fields[8];
                }
                return result;
            }
        }));

        final Button cancelarUltimaVendaButton = (Button) findViewById(R.id.cancelar_ultima_venda_button);
        assert cancelarUltimaVendaButton != null;
        cancelarUltimaVendaButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException, IOException {
                if (ultimaChaveVenda != null) {
                    //Cancela última venda, baseando-se em arquivo
                    InputStream inputStream = getAssets().open("CFeCanc.xml");
                    String xmlCanc = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
                    xmlCanc = xmlCanc.replace("XXX", ultimaChaveVenda);
                    return app.easySat.cancelarUltimaVenda(genNumeroSessao(), CODIGO_DE_ATIVACAO, ultimaChaveVenda, xmlCanc);
                } else {
                    return "Realize uma venda";
                }
            }
        }));

        final Button testeFimAFimButton = (Button) findViewById(R.id.test_fim_a_fim_button);
        assert testeFimAFimButton != null;
        testeFimAFimButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException, IOException {
                // Realiza teste fim-a-fim utilizando venda em arquivo
                InputStream inputStream = getAssets().open("CFeVenda.xml");
                String xmlVenda = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
                return app.easySat.testeFimAFim(genNumeroSessao(), CODIGO_DE_ATIVACAO, xmlVenda);
            }
        }));

        final Button consultarNumeroSessao = (Button) findViewById(R.id.consultar_numero_sessao_button);
        assert consultarNumeroSessao != null;
        consultarNumeroSessao.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                // Consulta último número de sessão
                if (ultimoNumeroSessao == 0) {
                    return "Realize uma operação";
                }
                // Necessário copiar por que é sobrescrito ao chamar genNumeroSessao()
                long numeroSessaoConsultado = ultimoNumeroSessao;
                return app.easySat.consultarNumeroSessao(genNumeroSessao(), CODIGO_DE_ATIVACAO, numeroSessaoConsultado);
            }
        }));

        final Button configurarInterfaceDeRedeButton = (Button) findViewById(R.id.configurar_interface_de_rede_button);
        assert configurarInterfaceDeRedeButton != null;
        configurarInterfaceDeRedeButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                // Configura interface de rede com ethernet / DHCP
                String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><config><tipoInter>ETHE</tipoInter><tipoLan>DHCP</tipoLan></config>";
                return app.easySat.configurarInterfaceDeRede(genNumeroSessao(), CODIGO_DE_ATIVACAO, config);
            }
        }));

        final Button trocarCodigoDeAtivacaoButton = (Button) findViewById(R.id.trocar_codigo_de_ativacao_button);
        assert trocarCodigoDeAtivacaoButton != null;
        trocarCodigoDeAtivacaoButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                // Troca o código de ativação pelo mesmo código, apenas para testar
                return app.easySat.trocarCodigoDeAtivacao(genNumeroSessao(), CODIGO_DE_ATIVACAO, EasySAT.CODIGO_DE_ATIVACAO, CODIGO_DE_ATIVACAO, CODIGO_DE_ATIVACAO);
            }
        }));

        final Button extrairLogsButton = (Button) findViewById(R.id.extrair_logs_button);
        assert extrairLogsButton != null;
        extrairLogsButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                // Extrai e decodifica logs
                String result = app.easySat.extrairLogs(genNumeroSessao(), CODIGO_DE_ATIVACAO);
                String[] fields = result.split("\\|");
                if (fields.length >= 6) {
                    try {
                        String logs = new String(Base64.decode(fields[5], Base64.DEFAULT), "UTF-8");
                        // Mostra apenas parte do final para não ficar muito grande
                        return logs.substring(Math.max(0, logs.length() - 10000));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
                return result;
            }
        }));

        final Button testeDeStressButton = (Button) findViewById(R.id.teste_de_stress_button);
        assert testeDeStressButton != null;
        testeDeStressButton.setOnClickListener(createClickListenerWithTask(new BackgroundSATTask() {
            @Override
            public String run() throws SATException {
                for (int i = 0; i < 100; i++) {
                    app.easySat.consultarSAT(genNumeroSessao());
                    app.easySat.consultarStatusOperacional(genNumeroSessao(), CODIGO_DE_ATIVACAO);
                }
                return "OK";
            }
        }));
    }

    private void testeBuffer() {
        InputStream inputStream;
        try {
            inputStream = getAssets().open("CFeVenda.xml");
            String xmlVenda = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

            ArrayList<String> params = new ArrayList<>();
            params.add("" + 100000);
            params.add("99999999");
            params.add(xmlVenda);

            StringBuilder sb = new StringBuilder();
            boolean firstTime = true;
            for (Object token: params.toArray(new String[params.size()])) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append("|");
                }
                sb.append(token);
            }

            String data = sb.toString();

            byte[] request = new byte[data.length() + 8];
            ByteBuffer buffer = ByteBuffer.wrap(request);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(data.length());
            buffer.putInt(6);
            buffer.put(data.getBytes());

            String str1 = new String(request, "UTF-8");
            String str2 = new String(buffer.array(), "UTF-8");
            System.out.println(str1);
            System.out.println(str2);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {

        } catch (BufferOverflowException e){
            e.printStackTrace();
        }
    }

    /**
     * Função chamada após usuário dar ou negar permissão USB explicitamente.
     *
     * Registra dispositivo e exibe mensagem.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null){
                            app.easySat.setDevice(device);
                            Toast toast = Toast.makeText(getApplicationContext(), "Conectado com SAT", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (EasySAT.isEasySATDevice(device)) {
                    app.easySat.setDevice(null);
                    Toast toast = Toast.makeText(getApplicationContext(), "SAT desconectado", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    /**
     * Gera número de sessão aleatoriamente e guarda para eventual consulta.
     */
    private long genNumeroSessao() {
        ultimoNumeroSessao = random.nextInt(900000) + 100000; //entre 100000 e 999999
        return ultimoNumeroSessao;
    }

    /**
     * Função utilitária para transformar exceção em String, incluindo stack tace.
     */
    private String getExceptionAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Função utilitária para exibir dialog com mensagem especificada.
     */
    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SatActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Class abstrata que define uma tarefa do SAT.
     *
     * Utilizada com o método createClickListenerWithTask para evitar repetição de código
     * ao registrar listeners para os botões da interface.
     */
    private abstract class BackgroundSATTask {
        public abstract String run() throws Exception;
    }

    /**
     * Cria um click listener para uma tarefa do SAT.
     *
     * O click listener automaticamente rodará a tarefa em background e exibirá o resultado
     * em um dialog. Se uma exceção for jogada, ela é exibida no lugar.
     */
    private View.OnClickListener createClickListenerWithTask(final BackgroundSATTask task) {
        return new View.OnClickListener() {
            public void onClick(View view) {
                final ProgressDialog dialog = new ProgressDialog(SatActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Aguarde...");
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... voids) {
                        String result;
                        try {
                            result = task.run();
                        } catch (Exception e) {
                            result = getExceptionAsString(e);
                        }
                        return result;
                    }
                    protected void onPostExecute(String result) {
                        dialog.dismiss();
                        showMessage(result);
                    }
                }.execute();
                dialog.show();
            }
        };
    }
}
