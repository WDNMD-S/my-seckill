package com.xxxx.seckill.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.RespBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zhoubin
 * @since 1.0.0
 */
public class UserUtil {
    private static void createUser(int count) throws Exception {
        List<User> users = new ArrayList<>(count);
        //生成用户
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(13000000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Util.inputPassToDBPass("qwer123", user.getSalt()));
            users.add(user);
        }
        System.out.println("create user");
          //插入数据库
//         Connection conn = getConn();
//         String sql = "insert into t_user(login_count, nickname, salt, password, id)values(?,?,?,?,?)";
//         PreparedStatement pstmt = conn.prepareStatement(sql);
//         for (int i = 0; i < users.size(); i++) {
//         	User user = users.get(i);
//         	pstmt.setInt(1, user.getLoginCount());
//         	pstmt.setString(2, user.getNickname());
//         	pstmt.setString(3, user.getSalt());
//         	pstmt.setString(4, user.getPassword());
//         	pstmt.setLong(5, user.getId());
//         	pstmt.addBatch();
//         }
//         pstmt.executeBatch();
//         pstmt.close();
//         conn.close();
//         System.out.println("insert to db");
        //登录，生成userTicket
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("E:\\test\\config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("qwer123");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            String userTicket = ((String) respBean.getObj());
            System.out.println("create userTicket : " + user.getId());

            String row = user.getId() + "," + userTicket;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getId());
        }
        raf.close();

        System.out.println("over");
    }

    private static Connection getConn() throws Exception {
        String url = "jdbc:mysql://localhost:3306/seckill_demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) throws Exception {
        createUser(5000);
    }
}