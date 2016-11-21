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

/**
 * Erro ao comunicar com SAT mas ele não está conectado.
 */
public class SATNotConnectedException extends SATException {
    public SATNotConnectedException(String detailMessage) {
        super(detailMessage);
    }
}
