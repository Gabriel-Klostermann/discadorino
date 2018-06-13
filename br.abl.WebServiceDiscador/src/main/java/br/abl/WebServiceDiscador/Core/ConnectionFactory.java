package br.abl.WebServiceDiscador.Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

	//Classe existente para retornar uma conexão de sql quando necessário.
	public Connection getConnection() {
		try {
			return DriverManager.getConnection("jdbc:mysql://10.1.1.144/campanhas?useSSL=false", "root", "senha123");
												//"jdbc:mysql://127.0.0.1/campanhas?useSSL=false", "root", "ablsystem2010"
					}
		
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
