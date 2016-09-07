package services;

import com.jolbox.bonecp.BoneCP;

import java.awt.*;
import java.awt.image.*;
import models.DBConnectionPool;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by sabyasachi.upadhyay on 07/09/16.
 */
public class StoreImage {
    public static void main(String[] args){
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = null;
        String INSERT_PICTURE = "insert into images(image) values (?)";

        FileInputStream fis = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();

            URL url = new URL("http://www.mkyong.com/image/mypic.jpg");
            Image image = ImageIO.read(url);

            OutputStream out = null;

            try {
                out = new BufferedOutputStream(new FileOutputStream("/Users/sabyasachi.upadhyay/Downloads/tadianmol_trek.jpg"));
                //out.write();
            } finally {
                if (out != null) out.close();
            }

            File file = new File("/Users/sabyasachi.upadhyay/Downloads/tadianmol_trek.jpg");
            fis = new FileInputStream(file);
            ps = conn.prepareStatement(INSERT_PICTURE);
            System.out.println("file size is " + (int) file.length());
            ps.setBinaryStream(1, fis, (int) file.length());

            int a = ps.executeUpdate();
            System.out.println(a);
            System.out.println("nkjnkjnjknknkjnkjnkjnknkjnkjnkjnkjnkjnkjnkjnkjnkjnkjnkjnkjnkjnkjnkjnkjn");

        } catch(Exception e) {
            e.printStackTrace();
        }finally {

            try {
                ps.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    }

