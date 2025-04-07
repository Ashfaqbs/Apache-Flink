package com.example.sink;


import com.example.dto.UserPlain;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class PostgresSink implements SinkFunction<UserPlain> {
    @Override
    public void invoke(UserPlain user, Context context) throws Exception {
        try (

//                Connection conn = DriverManager.getConnection(
//                "jdbc:postgresql://localhost:9991/mainschema", "postgres", "admin");
                Connection conn = DriverManager.getConnection("jdbc:postgresql://postgres:5432/mainschema", "postgres", "admin"); // Docker service name
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO flusers (id, name) VALUES (?, ?)")
        ) {

            stmt.setInt(1, user.getId());
            stmt.setString(2, user.getName());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}