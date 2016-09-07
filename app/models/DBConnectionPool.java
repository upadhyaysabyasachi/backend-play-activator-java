package models;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class DBConnectionPool {

    static BoneCPConfig config;
    static BoneCP connectionPool;
    static Connection conn;
    static{
        config = new BoneCPConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/prod");
        config.setUsername("root");
        config.setPassword("trailgunm*123");
        config.setMinConnectionsPerPartition(10);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(5);

        try {
            connectionPool = new BoneCP(config);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static BoneCP getConnectionPool(){
            return connectionPool;
    }

}
