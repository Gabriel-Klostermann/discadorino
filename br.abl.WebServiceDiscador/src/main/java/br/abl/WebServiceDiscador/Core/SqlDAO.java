package br.abl.WebServiceDiscador.Core;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;



public class SqlDAO {

	private Connection connection;
	
	private String dataAgora,dataDepois;


	 
	
	public SqlDAO(){
		//this.connection = new ConnectionFactory().getConnection();
	}
	
	//Método para pegar o próximo número para ligar
	public static synchronized Vector<String>  getProximoNumero(int id, int counterID) throws ParseException, SQLException {		
		Connection connection = new ConnectionFactory().getConnection();
		Calendar cal = Calendar.getInstance();
		long vezesLigou = 0 , idCliente;
		String  numControle = "",numTel = "";
		Vector<String> numeros = new Vector<>();		
		String dataProxLigacao;		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		String agro = dateFormat.format(cal.getTime());		
		Date agora = dateFormat.parse(agro);		
		Date horarioLigar;
		String result;
		//Query para pegar numero do telefone, num de tentativas de ligar pra este número,
		//data da próxima ligação, ID do cliente, Resultado, e o Tipo.
		try {
			PreparedStatement stmt = connection.prepareStatement("Select numerotel,vezesligou,DataProxLigacao,IDCliente,Result,Tipo from campanhas.numeros where status = 0 AND IDCampanha = " + id +" AND Tipo = \'PRINC\'");
			ResultSet rs = stmt.executeQuery();
			
			//Enquanto não pegar um número, fica no loop.
			while(numTel.isEmpty()) {	
				rs.next();				
				result = rs.getString(5);
				idCliente = rs.getInt(4);
				dataProxLigacao = rs.getString(3);	
				vezesLigou = rs.getInt(2);
				numControle = rs.getString(1);
						
				horarioLigar = new Date();
				//Pega o horário agendado da próxima ligação caso exista.
				if(!dataProxLigacao.equals(" ")) {
					horarioLigar = dateFormat.parse(dataProxLigacao);
				}
				//Verifica se o número de tentativas é menor que o máximo, se não existe um horário para a próxima ligação, e se o resultado é diferente
				//de OKAY, que significa que o cliente já atendeu e não deve ser ligado novamente. Ou verifica se o horário atual é após o agendado, e se o número de tentativas
				//é menor que o máximo, e se o resultado é diferente de OKAY
				if(vezesLigou<CriadorCampanhas.campanhas.get(id-1).getNumTent() && dataProxLigacao.equals(" ") && !result.equals("OKAY") || agora.after(horarioLigar) && vezesLigou<CriadorCampanhas.campanhas.get(id-1).getNumTent() && !result.equals("OKAY")) {
					numTel = rs.getString(1);
					numeros.add(numTel);
				}
				
				//Se não der, verifica se o resultado é diferente de OKAY e se agora é depois do horário agendado.
				else if(!result.equals("OKAY") && agora.after(horarioLigar)){					
					PreparedStatement stm = connection.prepareStatement("Select numerotel,vezesligou,IDCliente,Result,DataProxLigacao from campanhas.numeros where  IDCampanha = "+id+" AND IDCliente ="+idCliente+";");					
					ResultSet rss = stm.executeQuery();
					rss.next();					
					while(numTel.isEmpty()) {						
						rss.next();
						
						if(!rss.getString(5).equals(" ")) {							
							horarioLigar = dateFormat.parse(rss.getString(5));
						}
						result = rss.getString(4);
						if(rss.getInt(2) < CriadorCampanhas.campanhas.get(id-1).getNumTent() && !result.equals("OKAY") && agora.after(horarioLigar) ) {
							numTel = rss.getString(1);
							numeros.add(numTel);							
							break;
						}
						
						if(agora.before(horarioLigar)) {
							break;
						}
						
						
												
					  //  System.out.println("VALS: " + rss.getInt(1) +" "+  rss.getInt(2) +" "+ rss.getInt(3) +" "+ rss.getString(4) +" "+ rss.getString(5));				    
						
					}
					rss.close();
					stm.close();
				}
			}
			
			
		numeros.add(String.valueOf(vezesLigou));
		numeros.add(numControle);
		System.out.println("Peguei o próximo número para ligar! " + numTel);
		stmt.close();
		rs.close();		
		connection.close();
		
		connection = new ConnectionFactory().getConnection();
		stmt =connection.prepareStatement("Update numeros set status = 1 where numerotel = " + numControle);
		stmt.execute();		
		stmt.close();
		connection.close();
		
		System.out.println("Setei seu status pra 1 !");
		
		return numeros;
		} catch (SQLException e) {	
			//throw new RuntimeException(e);
			System.out.println("Não há mais números para ligar ! ");
			CampanhaManager.ids.set(counterID, CampanhaManager.ids.get(counterID) - 1 );			
						
		    connection.close();
			return numeros; 			
		}		
	}
	
	public synchronized void atualizaLigacoes(String uniqueId, String numLigado, int idCampanha,int vez, int counterID, String numPai, String status, long billSec) throws InterruptedException {
		System.out.println("UNIQUE ID MEU É : " + uniqueId);		
		Calendar cal = Calendar.getInstance();
		String sql = null, sqldois = null;
		System.out.println();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		this.dataAgora = dateFormat.format(cal.getTime());
		this.connection = new ConnectionFactory().getConnection();
				
				try {				
				if(status.equals("Normal Clearing")) {			
			 atualizaDuracaoLigacao(numLigado, billSec);
			
			if (billSec >= 10) {
				 sql = "update  campanhas.numeros set Result = 'OKAY'  where numerotel =" + numLigado;
				 
				System.out.println("Setei result para OKAY, ligação para " + numLigado + " deu certo !");
			}
			else {
				sql = "update campanhas.numeros set Result = 'NOPE' where numerotel =" +numLigado;
				System.out.println("Somei 1 para o vezesligou, ligação muito curta! ");
			}
			
			sqldois = "update campanhas.numeros set vezesligou = vezesligou + 1 where numerotel =" + numLigado;
			this.connection = new ConnectionFactory().getConnection();
			
			Statement stmtObj = this.connection.createStatement();
			stmtObj.addBatch(sql);
			stmtObj.addBatch(sqldois);
			stmtObj.addBatch("update campanhas.numeros set DataUltLigacao =" + "\'" + this.dataAgora +"\'"  +  " where numerotel =" + numLigado);
			
			stmtObj.executeBatch();
			stmtObj.close();
			this.connection.close();		
			
			if(billSec < 10) {
				status = "SHORTCALL";
				this.connection = new ConnectionFactory().getConnection();
				if(CriadorCampanhas.campanhas.get(idCampanha -1).getHoraTent() > 0){
					cal.add(Calendar.HOUR_OF_DAY,CriadorCampanhas.campanhas.get(idCampanha -1).getHoraTent());
				}
				cal.add(Calendar.MINUTE, CriadorCampanhas.campanhas.get(idCampanha-1).getMinTent());
							
				this.dataDepois = dateFormat.format(cal.getTime());
				PreparedStatement stmt = connection.prepareStatement("update campanhas.numeros set DataProxLigacao =" + "\'" + this.dataDepois + "\'" + " where numerotel =" +numLigado);
				stmt.execute();
				stmt.close();
				connection.close();
			}
		}
		else {				
			
		    this.connection = new ConnectionFactory().getConnection();
		    Statement stmtObj = this.connection.createStatement();		  
		    stmtObj.addBatch("update campanhas.numeros set vezesligou = vezesligou + 1 where numerotel = " + numLigado);
		    stmtObj.addBatch("update campanhas.numeros set DataUltLigacao =" +  "\'" + this.dataAgora +"\'"  +  " where numerotel =" + numLigado);
		    stmtObj.addBatch( "update campanhas.numeros set Result = 'NOPE' where numerotel =" +numLigado);
			stmtObj.executeBatch();
			stmtObj.close();
			this.connection.close();
			System.out.println("Ligação não deu certo para " + numLigado +  ", somei 1 ao vezesligou !");			
			this.connection = new ConnectionFactory().getConnection();
			if(CriadorCampanhas.campanhas.get(idCampanha -1).getHoraTent() > 0){
				cal.add(Calendar.HOUR_OF_DAY,CriadorCampanhas.campanhas.get(idCampanha -1).getHoraTent() );
			}
			cal.add(Calendar.MINUTE, CriadorCampanhas.campanhas.get(idCampanha-1).getMinTent());
			this.dataDepois = dateFormat.format(cal.getTime());
			PreparedStatement stmt=connection.prepareStatement("update campanhas.numeros set DataProxLigacao =" +  "\'" + this.dataDepois +"\'"  +  " where numerotel =" + numLigado);
			stmt.execute();
			stmt.close();
			connection.close();
			
			System.out.println("Inseri as datas !");
		}
		} catch(SQLException e) {
			
			throw new RuntimeException(e);
			
	}
		
		try {
			this.connection = new ConnectionFactory().getConnection();
			PreparedStatement stmt = connection.prepareStatement("update campanhas.numeros set status = 0 where numerotel =" + numPai);
			
			stmt.execute();
			stmt.close();
			connection.close();
			System.out.println("Voltei o status para 0 !");				
			logger(idCampanha,numLigado,billSec,uniqueId,status,vez,this.dataAgora,numPai);
			System.out.println("Log criado com sucesso para a ligação para o numero: " + numLigado);
			CampanhaManager.ids.set(counterID, CampanhaManager.ids.get(counterID) - 1 );

		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	} 
	
	public void atualizaDuracaoLigacao(String callerNum, long billSec) {
		
		try {
			
			PreparedStatement stmt;
			
			 connection = new ConnectionFactory().getConnection();
			stmt = connection.prepareStatement("update campanhas.numeros set DuracaoUltLigacao = " + billSec + " where"
												+ " numerotel = " + callerNum);
			
			stmt.execute();
			stmt.close();
			connection.close();
			System.out.println("Duracao da ligacao : " + billSec + " segundos.");				
			
		} catch(SQLException e) {
			throw new RuntimeException(e);
		} 
		
	}
	
	public void logger(int idCampanha,String numTel,long duracao, String uniqueId,String status,int numVez,String dataAgora, String numPai ) {
		this.connection = new ConnectionFactory().getConnection();
		numVez +=1;
		
		try {
			PreparedStatement stmt = connection.prepareStatement("Insert into campanhas.logs (IDCampanha,NumeroLigado,"
												+ "Duracao,UniqueID,StatusLigacao,NumeroTentativa,DataTentativa,NumPai) values "
												+ "(?,?,?,?,?,?,?,?)");
			
			stmt.setInt(1, idCampanha);
			stmt.setString(2, numTel);
			stmt.setLong(3, duracao);
			stmt.setString(4, uniqueId);
			stmt.setString(5, status);
			stmt.setInt(6, numVez);
			stmt.setString(7, dataAgora);
			stmt.setString(8, numPai);
			
			stmt.execute();
			stmt.close();
			connection.close();
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}
	
	public void insereNumeros(String[] numeros, int idCampanha, int idCliente) {
		long nums[] = new long[numeros.length];
		for (int i = 0; i<numeros.length; i++) {
			 nums[i] = Long.parseLong(numeros[i]);			 
		}
		
		
		try {	
			this.connection = new ConnectionFactory().getConnection();
			Statement stmtObj = this.connection.createStatement();
			stmtObj.addBatch("INSERT INTO campanhas.numeros (numerotel,IDCampanha,IDCliente,Tipo) values ("+ numeros[0] + "," +idCampanha+ "," + idCliente +"," + "\'PRINC\'" +")");
			for(int i = 0; i<nums.length;i++) {
				CriadorCampanhas.campanhas.get(idCampanha - 1).addNums(nums[i]);
			}
			for(int i =1; i< nums.length;i++) {				
				stmtObj.addBatch("INSERT INTO campanhas.numeros (numerotel,IDCampanha,IDCliente,Tipo) values ("+ numeros[i] + "," +idCampanha+ "," + idCliente +"," + "\'ALT\'" +")");
			}		
				stmtObj.executeBatch();
				stmtObj.close();
				connection.close();				
				System.out.println("Numeros novos inseridos com sucesso !");				
			
		} catch(SQLException | IndexOutOfBoundsException e ) {
			throw new RuntimeException(e);
		    //System.out.println("Alguns ou todos estes números já estão na tabela");
			//CriadorCampanhas.flagExiste = 1;
		}		
	}
	
/*	public void insereNumeros(List<String> numeros, int idCampanha, int idCliente) {
		long nums[] = new long[numeros.size()];
		for (String numero : numeros) {
			nums[numeros.indexOf(numero)] = Long.parseLong(numero);
		}
		
		
		try {	
			this.connection = new ConnectionFactory().getConnection();
			Statement stmtObj = this.connection.createStatement();
			stmtObj.addBatch("INSERT INTO campanhas.numeros (numerotel,IDCampanha,IDCliente,Tipo) values ("+ numeros.get(0) + "," +idCampanha+ "," + idCliente +"," + "\'PRINC\'" +")");
			for(int i = 0; i<nums.length;i++) {
				CriadorCampanhas.campanhas.get(idCampanha - 1).addNums(nums[i]);
			}
			for(int i =1; i< nums.length;i++) {				
				stmtObj.addBatch("INSERT INTO campanhas.numeros (numerotel,IDCampanha,IDCliente,Tipo) values ("+ numeros.get(i) + "," +idCampanha+ "," + idCliente +"," + "\'ALT\'" +")");
				CriadorCampanhas.campanhas.get(idCampanha - 1).addNums(nums[i]);
			}		
				stmtObj.executeBatch();
				stmtObj.close();
				connection.close();				
				System.out.println("Numeros novos inseridos com sucesso !");				
			
		} catch(SQLException | IndexOutOfBoundsException e ) {
			throw new RuntimeException(e);
		    //System.out.println("Alguns ou todos estes números já estão na tabela");
			//CriadorCampanhas.flagExiste = 1;
		}		
	} */


	
}
