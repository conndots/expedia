package cn.edu.nju.ws.expedia.database;

import cn.edu.nju.ws.expedia.util.ConfReader;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Xiangqian on 2014/12/23.
 */
public class DBConnectionFactory {
    private static DBConnectionFactory INSTANCE = null;
    private static final Object LOCK = new Object();

    public static DBConnectionFactory getInstance(){
        if(INSTANCE == null){
            synchronized(LOCK){
                if(INSTANCE == null)
                    INSTANCE = new DBConnectionFactory();
            }
        }

        return INSTANCE;
    }

    private BoneCP defaultPool = null;
    private final Object defaultLock = new Object();
    private DBConnectionFactory(){
        try {
            this.initDefaultDatabasePool();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void initDefaultDatabasePool() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(ConfReader.getConfProperty("default_db_connection_url"));
        config.setUsername(ConfReader.getConfProperty("default_db_connection_user"));
        config.setPassword(ConfReader.getConfProperty("default_db_connection_pw"));
        config.setPartitionCount(3);
        config.setMaxConnectionsPerPartition(6);
        config.setMinConnectionsPerPartition(1);

        this.defaultPool = new BoneCP(config);
    }

    public Connection getDefaultDBConnection(){
        try {
            Connection conn = this.defaultPool.getConnection();
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
