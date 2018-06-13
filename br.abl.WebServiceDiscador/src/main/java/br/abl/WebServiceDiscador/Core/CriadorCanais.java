package br.abl.WebServiceDiscador.Core;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;


import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;

public class CriadorCanais implements Runnable {

	
	private int id;
	private int counterID;
	private String tronco,contexto;
	
	public CriadorCanais(int id,int counterID,String tronco, String contexto) {
		this.id = id;
		this.counterID = counterID;
		this.tronco = tronco;
		this.contexto = contexto;
	}
	
	//Cria um novo discador para a thread fazer as ligações.
	public void run() {
		try {	
			Discador disc = new Discador(this.id,this.counterID,this.tronco,this.contexto);
			disc.run();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
