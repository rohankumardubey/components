package org.talend.components;

import java.util.Properties;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.util.Base64;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class TestJdbc {

    private final static String PATH = "";

    private final static String USER = "";

    private final static String ACCOUNT = "";

    private final static String DATABASE = "";

    private final static String SCHEMA = "";

    private final static String PHRASE = System.getProperty("passphrase");;

    public static void main(String[] args) throws Exception {
        File file = new File(PATH);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) file.length()];
        dis.readFully(keyBytes);
        dis.close();

        String encrypted = new String(keyBytes);
        String passphrase = PHRASE;
        encrypted = encrypted.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "");
        encrypted = encrypted.replace("-----END ENCRYPTED PRIVATE KEY-----", "");

        EncryptedPrivateKeyInfo pkInfo = new EncryptedPrivateKeyInfo(Base64.getMimeDecoder().decode(encrypted));
        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory pbeKeyFactory = SecretKeyFactory.getInstance(pkInfo.getAlgName());
        PKCS8EncodedKeySpec encodedKeySpec = pkInfo.getKeySpec(pbeKeyFactory.generateSecret(keySpec));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey encryptedPrivateKey = keyFactory.generatePrivate(encodedKeySpec);

        Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        String url = "jdbc:snowflake://" + ACCOUNT + ".snowflakecomputing.com";
        Properties prop = new Properties();
        prop.put("user", USER);
        prop.put("account", ACCOUNT);
        prop.put("database", DATABASE);
        prop.put("schema", SCHEMA);
        prop.put("privateKey", encryptedPrivateKey);

        Connection conn = DriverManager.getConnection(url, prop);
        Statement stat = conn.createStatement();
        ResultSet res = stat.executeQuery("select 1");
        while (res.next()) {
            System.out.println(res.getInt(1));
        }
        conn.close();
    }
}