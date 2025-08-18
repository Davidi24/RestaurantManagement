// Email sender port
package pos.pos.Service.Interfecaes;


public interface EmailSender {
    void sendPasswordResetCode(String toEmail, String toName, String code, int ttlSeconds);
}
