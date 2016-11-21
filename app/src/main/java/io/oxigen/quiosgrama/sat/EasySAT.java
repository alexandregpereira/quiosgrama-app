/*
 * Este Software, em parte ou em seu todo, é livre para uso e modificação exclusivamente para uso
 * com o equipamento EASYS@T Kryptus.
 * Este software é provido sem nenhuma forma de garantia implícita ou explicita e a Kryptus não se
 * responsabiliza por possíveis danos diretos ou indiretos causados pelo mesmo.
 * A violação dessa licença sujeita o usuário a indenização de quaisquer perdas e danos diretos e
 * indiretos que venham a ser causados à Kryptus.
 *
 * Todos os direitos reservados - Kryptus Segurança da Informação S/A
 *
 * 10/05/2016
 */

package io.oxigen.quiosgrama.sat;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Classe que faz interface com as funções do EasySAT.
 * <p/>
 * Para ser utilizada, deve-se instanciá-la passando o UsbManager da aplicação.
 * Ao se conectar o SAT, deve-se chamar a função setDevice para registrá-lo.
 * <p/>
 * O uso desta classe está exemplificado no aplicativo de teste.
 */
public class EasySAT {
    /**
     * Vendor ID USB do EasySAT.
     * <p/>
     * Pode ser utilizado para detectar o EasySAT ao listar dispositivos USB.
     */
    public static final int EASYSAT_VENDOR_ID = 10180;

    /**
     * Product ID USB do EasySAT.
     * <p/>
     * Pode ser utilizado para detectar o EasySAT ao listar dispositivos USB.
     */
    public static final int EASYSAT_PRODUCT_ID = 2560;

    /**
     * Identifica código de ativação na função trocarCodigoDeAtivacao.
     */
    public static final int CODIGO_DE_ATIVACAO = 1;

    /**
     * Identifica código de ativação de emergência na função trocarCodigoDeAtivacao.
     */
    public static final int CODIGO_DE_ATIVACAO_DE_EMERGENCIA = 2;

    /**
     * Indica tipo de certificado AC-SAT/SEFAZ na função ativarSAT.
     */
    public static final int CERTIFICADO_SEFAZ = 1;

    /**
     * Indica tipo de certificado ICP-Brasil na função ativarSAT.
     */
    public static final int CERTIFICADO_ICP_BRASIL = 2;

    /**
     * Indica renovação de certificado ICP-Brasil na função ativarSAT.
     */
    public static final int RENOVACAO_CERTIFICADO_ICP_BRASIL = 3;

    protected static final int USB_BUFFER_SIZE = 64;
    protected static final int USB_HEADER_SIZE = 8;
    protected static final int ATIVAR_SAT = 4;
    protected static final int COMUNICAR_CERT_ICPBR = 5;
    protected static final int ENVIAR_DADOS_VENDA = 6;
    protected static final int CANCELAR_ULTIMA_VENDA = 7;
    protected static final int CONSULTAR_SAT = 8;
    protected static final int TESTE_FIM_A_FIM = 9;
    protected static final int CONSULTAR_STATUS_OP = 10;
    protected static final int CONSULTAR_NUM_SESSAO = 11;
    protected static final int CONFIG_INTERFACE_REDE = 12;
    protected static final int ASSOCIAR_ASSINATURA = 13;
    protected static final int ATUALIZAR_SW = 14;
    protected static final int EXTRAIR_LOGS = 15;
    protected static final int BLOQUEAR_SAT = 16;
    protected static final int DESBLOQUEAR_SAT = 17;
    protected static final int TROCAR_COD_DE_ATIVACAO = 18;

    protected static final int TIMEOUT = 1000 * 60 * 6; //6 minutos
    protected static final int MAX_RETRIES = 3;
    protected static final int MAX_DATA_LEN = 10 * 1024 * 1024;

    protected UsbDevice device;
    protected UsbManager manager;


    /**
     * Instancia EasySAT.
     *
     * @param manager: UsbManager que pode ser obtido com activity.getSystemService(Context.USB_SERVICE).
     */
    public EasySAT(UsbManager manager) {
        if (manager == null) {
            throw new InvalidParameterException("manager não pode ser null");
        }
        this.manager = manager;
    }

    /**
     * Dado um dispositivo USB, retorna se ele corresponde ao EasySAT.
     *
     * @param device: dispositivo USB
     * @return se corresponde ao EasySAT
     */
    public static boolean isEasySATDevice(UsbDevice device) {
        return (device != null && device.getProductId() == EasySAT.EASYSAT_PRODUCT_ID
                && device.getVendorId() == EasySAT.EASYSAT_VENDOR_ID);
    }

    /**
     * Obtém o dispositivo USB registrado para o EasySAT.
     *
     * @return o dispositivo USB.
     */
    public UsbDevice getDevice() {
        return device;
    }

    /**
     * Configura EasySAT para utilizar o dispositivo especificado.
     * <p/>
     * Deve ser chamado com um dispositivo USB correspondente ao EasySAT antes de utilizar
     * qualquer método do SAT desta classe.
     *
     * @param device dispositivo USB correspondente ao EasySAT.
     */
    public void setDevice(UsbDevice device) {
        if (device != null && !isEasySATDevice(device)) {
            throw new InvalidParameterException("Dispositivo USB não é o EasySAT");
        }
        this.device = device;
    }

    public String ativarSAT(long numeroSessao, long subComando, String codigoDeAtivacao, String cnpj, long cUF) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add("" + subComando);
        params.add(codigoDeAtivacao);
        params.add(cnpj);
        params.add("" + cUF);
        return callFunction(ATIVAR_SAT, params.toArray(new String[params.size()]));
    }

    public String comunicarCertificadoICPBRASIL(long numeroSessao, String codigoDeAtivacao, String certificado) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add(certificado);
        return callFunction(COMUNICAR_CERT_ICPBR, params.toArray(new String[params.size()]));
    }

    public String enviarDadosVenda(long numeroSessao, String codigoDeAtivacao, String dadosVenda) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add(dadosVenda);
        return callFunction(ENVIAR_DADOS_VENDA, params.toArray(new String[params.size()]));
    }

    public String cancelarUltimaVenda(long numeroSessao, String codigoDeAtivacao, String chave, String dadosCancelamento) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add(chave);
        params.add(dadosCancelamento);
        return callFunction(CANCELAR_ULTIMA_VENDA, params.toArray(new String[params.size()]));
    }

    public String consultarSAT(long numeroSessao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        return callFunction(CONSULTAR_SAT, params.toArray(new String[params.size()]));
    }

    public String testeFimAFim(long numeroSessao, String codigoDeAtivacao, String dadosVenda) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add(dadosVenda);
        return callFunction(TESTE_FIM_A_FIM, params.toArray(new String[params.size()]));
    }

    public String consultarStatusOperacional(long numeroSessao, String codigoDeAtivacao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        return callFunction(CONSULTAR_STATUS_OP, params.toArray(new String[params.size()]));
    }

    public String consultarNumeroSessao(long numeroSessao, String codigoDeAtivacao, long cNumeroDeSessao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add("" + cNumeroDeSessao);
        return callFunction(CONSULTAR_NUM_SESSAO, params.toArray(new String[params.size()]));
    }

    public String configurarInterfaceDeRede(long numeroSessao, String codigoDeAtivacao, String dadosConfiguracao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add(dadosConfiguracao);
        return callFunction(CONFIG_INTERFACE_REDE, params.toArray(new String[params.size()]));
    }

    public String associarAssinatura(long numeroSessao, String codigoDeAtivacao, String CNPJvalue, String assinaturaCNPJs) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add(CNPJvalue);
        params.add(assinaturaCNPJs + "|"); //firmware workaround
        return callFunction(ASSOCIAR_ASSINATURA, params.toArray(new String[params.size()]));
    }

    public String atualizarSoftwareSAT(long numeroSessao, String codigoDeAtivacao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        return callFunction(ATUALIZAR_SW, params.toArray(new String[params.size()]));
    }

    public String extrairLogs(long numeroSessao, String codigoDeAtivacao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        return callFunction(EXTRAIR_LOGS, params.toArray(new String[params.size()]));
    }

    public String bloquearSAT(long numeroSessao, String codigoDeAtivacao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        return callFunction(BLOQUEAR_SAT, params.toArray(new String[params.size()]));
    }

    public String desbloquearSAT(long numeroSessao, String codigoDeAtivacao) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        return callFunction(DESBLOQUEAR_SAT, params.toArray(new String[params.size()]));
    }

    public String trocarCodigoDeAtivacao(long numeroSessao, String codigoDeAtivacao, long opcao, String novoCodigo, String confNovoCodigo) throws SATCommunicationException, SATNotConnectedException {
        ArrayList<String> params = new ArrayList<>();
        params.add("" + numeroSessao);
        params.add(codigoDeAtivacao);
        params.add("" + opcao);
        params.add(novoCodigo);
        params.add(confNovoCodigo);
        return callFunction(TROCAR_COD_DE_ATIVACAO, params.toArray(new String[params.size()]));
    }

    protected synchronized String callFunction(int opcode, String[] params) throws SATCommunicationException, SATNotConnectedException {
        UsbDeviceConnection connection = null;
        if (device == null) {
            throw new SATNotConnectedException("Dispositivo não configurado");
        }
        if (device.getInterfaceCount() == 0) {
            throw new SATNotConnectedException("Interface USB não disponível");
        }
        UsbInterface intf = device.getInterface(0);

        try {
            // Open connection
            UsbEndpoint in, out;
            if (intf.getEndpointCount() != 2) {
                throw new SATNotConnectedException("Endpoints USB não disponíveis");
            }
            if (intf.getEndpoint(0).getDirection() == UsbConstants.USB_DIR_IN) {
                in = intf.getEndpoint(0);
                out = intf.getEndpoint(1);
            } else {
                in = intf.getEndpoint(1);
                out = intf.getEndpoint(0);
            }
            connection = manager.openDevice(device);
            if (connection == null) {
                throw new SATCommunicationException("Não foi possível abrir conexão com o SAT");
            }
            if (!connection.claimInterface(intf, true)) {
                throw new SATCommunicationException("Não foi possível obter interface USB com o SAT");
            }

            // Send request
            String data = TextUtils.join("|", params);
            byte[] request = new byte[data.length() + 8];
            ByteBuffer buffer = ByteBuffer.wrap(request);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(data.length());
            buffer.putInt(opcode);
            buffer.put(data.getBytes());
            int nwritten = connection.bulkTransfer(out, request, request.length, TIMEOUT);
            if (nwritten != request.length) {
                throw new SATCommunicationException("Não foi possível enviar todos os dados para o SAT");
            }

            // Receive response
            byte[] header = new byte[USB_BUFFER_SIZE];
            ByteBuffer responseBuffer = ByteBuffer.wrap(header);
            responseBuffer.order(ByteOrder.LITTLE_ENDIAN);

            int readLen = 0;
//            readAsync(connection, in, header.length, responseBuffer);
//            readLen = responseBuffer.position();
//            responseBuffer.flip();
            for (int i = 0; i < MAX_RETRIES; i++) {
                readLen = connection.bulkTransfer(in, header, header.length, TIMEOUT);
                if (readLen >= 0) {
                    break;
                }
            }

            if (readLen < 0) {
                throw new SATCommunicationException("Erro ao receber cabeçalho de dados da USB: " + readLen);
            }
            if (readLen < 8) {
                throw new SATCommunicationException("Dados recebidos do SAT são muito pequenos: " + readLen);
            }
            int receivedDataLen = readLen - USB_HEADER_SIZE;
            int dataLen = responseBuffer.getInt();
            int retOpcode = responseBuffer.getInt();
            if (dataLen > MAX_DATA_LEN) {
                throw new SATCommunicationException("Cabeçalho de resposta recebido é inválido, dataLen = " + dataLen);
            }
            byte[] response = new byte[dataLen];
            System.arraycopy(header, USB_HEADER_SIZE, response, 0, receivedDataLen);
            while (dataLen > receivedDataLen) {
                byte[] remaining = new byte[dataLen - receivedDataLen];

//                responseBuffer = ByteBuffer.wrap(remaining);
//                readAsync(connection, in, remaining.length, responseBuffer);
//                readLen = responseBuffer.position();
//                responseBuffer.flip();
                for (int i = 0; i < MAX_RETRIES; i++) {
                    readLen = connection.bulkTransfer(in, remaining, remaining.length, TIMEOUT);
                    if (readLen >= 0) {
                        break;
                    }
                }

                if (readLen < 0) {
                    throw new SATCommunicationException("Erro ao receber dados da USB: " + readLen);
                }
                System.arraycopy(remaining, 0, response, receivedDataLen, readLen);
                receivedDataLen += readLen;
            }
            try {
                return new String(response, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (connection != null) {
                connection.releaseInterface(intf);
                connection.close();
            }
        }
    }

    private void readAsync(UsbDeviceConnection connection, UsbEndpoint in, int length, ByteBuffer responseBuffer) throws SATCommunicationException, SATNotConnectedException {
        UsbRequest usbRequest = new UsbRequest();
        try {
            if (!usbRequest.initialize(connection, in)) {
                throw new SATCommunicationException("Erro ao criar requisição de leitura USB");
            }
            if (!usbRequest.queue(responseBuffer, length)) {
                throw new SATCommunicationException("Erro ao fazer requisição de leitura USB");
            }
            if (connection.requestWait() == null) {
                throw new SATCommunicationException("Erro ao esperar por leitura USB");
            }
        } finally {
            usbRequest.close();
        }
    }
}
