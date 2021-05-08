package com.e.nche.Email;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Arrays;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by Ahsan Khalil on 3/20/2020.
 */

public class GMailVerify extends Authenticator {
    static {
        Security.addProvider(new JSSEProvider());
    }

    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;
    private Multipart multipart = new MimeMultipart();
    private int inboxLength;
    public boolean sent = false;

    public GMailVerify(final String user, final String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.transport.protocol", "smtp");

        Authenticator authenticate = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        session = Session.getInstance(props, authenticate);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try {
            Message message = new MimeMessage(session);
            //DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "application/image"));
            message.setFrom(new InternetAddress(sender));
            message.setHeader("Disposition-Notification-To", sender);
            message.setSubject(subject);

            //message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

            // setup message body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            message.setContent(multipart);

            // send email
            //Transport.send(message);

            final Transport transport = session.getTransport();
            try {
                session.setDebug(true);
                transport.connect("smtp.gmail.com", user, password);

                transport.sendMessage(message, message.getAllRecipients());

                bouncedMassages(recipients);

            } catch (SendFailedException e) {
                Log.println(Log.ASSERT, "Message", e.getMessage());
                throw new RuntimeException(e);
            }

            transport.close();

        } catch (AddressException e) {
            Log.println(Log.ASSERT, "Address Log", "Error in sending: " + e.toString());
        } catch (SendFailedException e){
            Log.println(Log.ASSERT, "SendFailed Log", "Error in sending: " + e.getCause());
        } catch (MessagingException e) {
            Log.println(Log.ASSERT, "Messaging Log", "Error in sending: " + e.toString());
        }
    }

    public void allMessagesCount() {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Authenticator authenticate = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        try {
            Session session = Session.getDefaultInstance(props, authenticate);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", "<username>", "password");

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();

            inboxLength = messages.length;
            Log.println(Log.ASSERT, "inboxLength", inboxLength+"");

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void bouncedMassages(String recipients) {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Authenticator authenticate = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        try {
            Session session = Session.getDefaultInstance(props, authenticate);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", "<username>", "password");

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();

            if (messages.length > inboxLength){
                Log.println(Log.ASSERT, "messages.length", messages.length+"");
                for(int i = messages.length - 1, n = messages.length; i < n; i++) {
                    Message message = messages[i];
                    Log.println(Log.ASSERT, "Subject", message.getSubject());
                    Log.println(Log.ASSERT, "From", Arrays.toString(message.getFrom()));


                    Multipart mp = (Multipart) message.getContent();
                    int count = mp.getCount();
                    for (int j = 0; j < count; j++)
                    {
                        String mail = getText(mp.getBodyPart(j));
                        if (mail != null){
                            String checkMail = recipients.substring(0, recipients.length() - 1);
                            //String driverMail = recipients.substring(recipients.indexOf(",") + 1, recipients.length() - 1);

                            if (mail.contains(checkMail)){
                                Log.println(Log.ASSERT, "Status_Owner", checkMail);

                                sent = false;
                            }
                        }
                    }

                }
            }
            else {
                sent = true;
            }

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean textIsHtml = false;
    private String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        multipart.addBodyPart(messageBodyPart);
    }

    public class ByteArrayDataSource implements DataSource {
        public String type;
        private byte[] data;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
