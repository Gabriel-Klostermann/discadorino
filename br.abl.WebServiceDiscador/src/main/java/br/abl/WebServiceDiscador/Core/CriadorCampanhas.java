
package br.abl.WebServiceDiscador.Core;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;






public class CriadorCampanhas {
	static AsteriskServer asteriskServer;
	static ManagerConnection managerConnection;
	static int id = 1;
	static int ctrl;
	static int flagExiste = 0;
	static List<Campanha> campanhas = new ArrayList<Campanha>();
	private static List<Thread> managers = new ArrayList<Thread>();
	
	public CriadorCampanhas() {
		
	}
	public static void main(String[] args) throws Exception {		
		
		new CampanhaController(new CriadorCampanhas());
		
	} 
	

	//Método que cria campanhas, recebendo todos os atributos do objeto campanha.
	public String criaCampanha(String nomeCampanha,String maxLigacoesStr,String dataInicio,String dataFim, String horaInicio, String horaFim, String tronco, String contexto, String tmpTent, String numTent) throws IllegalStateException, IOException, AuthenticationFailedException, TimeoutException {
		
		int[] tempos;		
		int maxLigacoes = Integer.parseInt(maxLigacoesStr);
		int numeroTentativas = Integer.parseInt(numTent);
		//Controle para fazer somente um Login como manager no asterisk.
		if(ctrl<1)
		managerLogin();		
		Date dataInicia = null,dataEncerra = null;
		//Formatar a data pro formato BR.
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy",Locale.US);
		//Chama método para quebrar o horário em horas e minutos, separando em duas strings. Depois é feito a conversão para int.
		tempos = getTempo(tmpTent);			
			try {
				dataInicia = format.parse(dataInicio);
			} catch (ParseException e) {
				System.out.println("Formato de data inválido");
			}
			
			
			 try {
				 dataEncerra = format.parse(dataFim);
				 
			 } catch(ParseException e) {
				 System.out.println("Formato de data inválido");
			 }
					 
			 
			
			 if(flagExiste == 0) {
			//Adiciona a campanha nova numa lista estática de campanhas e a cria simultaneamente.
			campanhas.add(new Campanha(nomeCampanha,CriadorCampanhas.id,maxLigacoes,dataInicia,dataEncerra,horaInicio,horaFim,tronco,contexto,tempos[0],tempos[1],numeroTentativas));	
			//Cria também um manager, que é uma thread feita para controlar a campanha criada, e o adiciona numa lista de managers.
			managers.add(new Thread(new CampanhaManager(new Campanha(nomeCampanha,CriadorCampanhas.id,maxLigacoes,dataInicia,dataEncerra,horaInicio,horaFim,tronco,contexto,tempos[0],tempos[1],numeroTentativas),CriadorCampanhas.id -1)));		
			
			
		
		
		
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Inicia o manager
			managers.get(CriadorCampanhas.id - 1).start();
			CriadorCampanhas.id ++;
			CampanhaManager.ids.add(0);			
			return "Campanha criada com sucesso !  ID : " + (CriadorCampanhas.id -1);
		}
	   
	   		CriadorCampanhas.flagExiste = 0;
	   		return "Números já existem!";
		
	}
	
	//Método para retornar todas as campanhas.
	public  List<Campanha> getCampanhas(){		
		return campanhas;	
}
	//Pega campanha específica pelo ID passado.
	public Campanha getCampanha(String idC) {
		int id = Integer.parseInt(idC);		
		for (int i = 0; i< campanhas.size(); i++) {
			if(campanhas.get(i).getId() == id) {
				return campanhas.get(i);
			}
		}		
		return campanhas.get(0);
	}
	
	//Deleta campanha especificada pelo ID
	public boolean deletarCampanha(String idC) {
		int id = Integer.parseInt(idC);
		for (int i = 0; i< campanhas.size(); i++) {
			if(campanhas.get(i).getId() == id) {
				campanhas.remove(i);
				return true;
			}
		}	
		return false;
	}
	
	//Verifica se determinada campanha existe.
	public boolean verificaSeExiste(int id) {
		for (int i = 0; i< campanhas.size(); i++) {
			if(campanhas.get(i).getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	//Pausa campanha especificada pelo id.
	public String pausaCampanha(String id) {
		int idCampanha = Integer.parseInt(id);
		for (int i = 0; i< campanhas.size() ; i++) {
			if(campanhas.get(i).getId() == idCampanha) {
				campanhas.get(i).setStatus("Pausado");
			}
		}
		
		return "Parei a campanha de ID : " + id;
	}

	//Despausa campanha especificada pelo id.
	public String playCampanha(String id) {
		int idCampanha = Integer.parseInt(id);
		for (int i = 0; i< campanhas.size() ; i++) {
			if(campanhas.get(i).getId() == idCampanha) {
				campanhas.get(i).setStatus("Executando");
			}
		}
		
	
		return "Dei play na campanha de ID : " + id;
	}
	
	//Separa o tempo em dois inteiros, sendo eles hora e minuto.
	public int[] getTempo(String tmpTent)	{
		String[] partes = tmpTent.split(":");
		int[] x = new int[2];
		x[0] = Integer.parseInt(partes[0]);
		x[1] = Integer.parseInt(partes[1]);
		
		return x;
		
	}
	
	
	//Faz o login do manager no asterisk, passando ip username e password.
	public void managerLogin() {
		ManagerConnectionFactory factory = new ManagerConnectionFactory("10.1.10.45","usuario","ablsystem2010");
		asteriskServer = new DefaultAsteriskServer("10.1.10.45",5038,"usuario","ablsystem2010");
		managerConnection = factory.createManagerConnection();
		try {
			managerConnection.login();
		} catch (IllegalStateException | IOException | AuthenticationFailedException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ctrl++;
	}
}
