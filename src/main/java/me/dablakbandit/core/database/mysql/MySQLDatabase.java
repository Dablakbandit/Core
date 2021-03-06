package me.dablakbandit.core.database.mysql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import me.dablakbandit.core.database.Database;
import me.dablakbandit.core.database.listener.SQLListener;

public class MySQLDatabase extends Database{
	
	private MySQL				mysql;
	private Connection			connection;
	
	private List<SQLListener>	listeners	= new ArrayList<SQLListener>();
	
	public MySQLDatabase(MySQLConfiguration config, boolean open){
		mysql = config.getMySQL();
		if(open){
			openConnection();
		}
	}
	
	public MySQL getInfo(){
		return mysql;
	}
	
	public Connection openConnection(){
		return connection = mysql.openConnection();
	}
	
	public Connection getConnection(){
		if(connection != null){
			if(!isConnected()){
				try{
					connection.close();
				}catch(Exception e){
				}
				openConnection();
				if(connection != null){
					setup();
				}
			}
		}
		return connection;
	}
	
	public boolean isConnected(){
		try{
			return connection.isValid(0);
		}catch(Exception e){
			return false;
		}
	}
	
	public void closeConnection(){
		if(connection != null){
			for(SQLListener listener : listeners){
				listener.close(connection);
			}
			try{
				connection.close();
				connection = null;
			}catch(Exception e){
			}
		}
	}
	
	public void setup(){
		for(SQLListener listener : listeners){
			listener.setup(connection);
		}
	}
	
	@Override
	public void addListener(SQLListener listener){
		getConnection();
		listeners.add(listener);
		listener.setDatabase(this);
		listener.setup(connection);
	}
	
}
