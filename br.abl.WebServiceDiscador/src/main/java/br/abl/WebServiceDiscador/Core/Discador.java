package br.abl.WebServiceDiscador.Core;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.ChannelState;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.event.CdrEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.OriginateResponseEvent;
import org.asteriskjava.manager.event.VarSetEvent;


public class Discador implements ManagerEventListener{
	
	private ManagerEventListener eventListener = null;
	private long  controlecanal = 0, controleCdr = 0, controleResponse = 0;		
	private Vector<String> x = new Vector<String>();		
	private String uniqueId,tronco,contexto,canal,numPai,callNum = "";	
	private SqlDAO num = new SqlDAO();
	private int counterID,actionId,idCampanha, vezLigacao = 0;		
	private long billSec;	
	private String hangUpCause;	
	AsteriskChannel ast;
	
	
	
	
	public Discador(int id,int counterID, String tronco, String contexto) throws IOException{		
		this.idCampanha = id;
		this.counterID = counterID;
		this.tronco = tronco;
		this.contexto = contexto;			
	}
	
	
	
	public void run() throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException, ParseException, SQLException {
		
		//Coloca o eventlistener para escutar nesta classe e adiciona o eventlistener na conexão com asterisk.
		eventListener = this;		
		CriadorCampanhas.managerConnection.addEventListener(eventListener);	
		long tt = 40000;		
		OriginateAction originateAction = new OriginateAction();
		//Cria um actionId aleatório para a ação feita pelo AMI.
	    this.actionId =	ThreadLocalRandom.current().nextInt(1,100 +1);
	    //Pega o número a ser ligado
		this.x = this.num.getProximoNumero(this.idCampanha,this.counterID);		
		this.callNum = this.x.get(0);
		//Formatando o canal com nome do tronco e tech prefix.
		String pt = "SIP/" +this.tronco +"/"+"661455" +this.callNum;
		this.vezLigacao = Integer.parseInt(this.x.get(1));
		this.numPai = this.x.get(2);
		System.out.println("Tentando ligar para :" + this.callNum);	
		//Seta os parametros do originate.
		originateAction.setCallerId("4130123027");		
		originateAction.setActionId(""+actionId);	
		originateAction.setChannel("SIP/" +this.tronco +"/"+this.callNum);
		originateAction.setContext(this.contexto);
		originateAction.setExten("9999");
		originateAction.setPriority(new Integer(1));		
		originateAction.setVariable("amivar,"+this.actionId, "" + this.actionId);
		originateAction.setTimeout(tt);
		originateAction.setAsync(true);
	
	
		System.out.println("NUMINTEIRO : " + pt);
		
	

			//Faz a ligação com os parametros especificados.
	   try {
			 CriadorCampanhas.managerConnection.sendEventGeneratingAction(originateAction,20000);			 
		
		} catch(EventTimeoutException e) {
			System.out.println("TIMEOUT MANAGER");	
		}		
		
		
		Thread.currentThread().sleep(500);		
		
	}

	//Listener para eventos de manager
	public void onManagerEvent(ManagerEvent event) {	
			//Evento para resposta do originate.
		    if(event instanceof OriginateResponseEvent) {
			int reasonInt;						
			ChannelState st;
			//Verifica se a thread que entrou aqui realmente é a thread que fez a ligação, verificando o actionId.
			if(this.actionId == Integer.parseInt(((OriginateResponseEvent)event).getActionId())&& this.controleResponse ==0) {
				//Pega o motivo do desligamento da chamada.
				reasonInt = ((OriginateResponseEvent)event).getReason();					
				 System.out.println("REASON : " + reasonInt +  "NUMERO:" +this.callNum);				
				 if(reasonInt ==0 ||reasonInt ==1 || reasonInt ==3 || reasonInt ==5 ||reasonInt ==7 ||reasonInt ==8) {
					 //Chama método para pegar o motivo em forma de String.
					 this.hangUpCause = converterReasonToString(reasonInt);
					 this.billSec = 0;					
					 this.controleResponse++;
					 //Atualiza Sql com o resultado da ligação
					 try {
						this.num.atualizaLigacoes(this.uniqueId, this.callNum, this.idCampanha, this.vezLigacao, this.counterID, this.numPai, this.hangUpCause, this.billSec);
					} catch (InterruptedException e) {						
						e.printStackTrace();
					}
				 }
				 
				 else if(reasonInt == 4) {
					 //Razão número 4 significa que deu certo a chamada.
					 try {						 
						 st = CriadorCampanhas.asteriskServer.getChannelByName(this.canal).getState();
						 //Método para segurar até o cliente desligar a chamada.
						 esperaAtendeu(st,reasonInt);						
					 } catch(NullPointerException e) {
						 try {
							 this.billSec = 0;
							 this.hangUpCause = "Busy";
						//Atualiza Sql com o resultado da ligação
							this.num.atualizaLigacoes(this.uniqueId, this.callNum, this.idCampanha, this.vezLigacao, this.counterID,this.numPai, this.hangUpCause, this.billSec);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}						 
					 }					 
				 }				 
			}			
		}
		    
		   /*  if(event instanceof HangupEvent) {
				if(this.canal.equals(((HangupEvent)event).getChannel())) {
					System.out.println("ENTREI NO HANGUP DE BOA " + this.callnum);
					System.out.println(((HangupEvent)event).getCauseTxt());
					
				}
			 }*/
		     
		
		//Evento utilizado para dar o uniqueid da chamada e o canal aos atributos da thread 
		else if (event instanceof VarSetEvent){
			if(this.controlecanal == 0) {
				String vari = ((VarSetEvent)event).getVariable();				
				String comp = ""+this.actionId;
				System.out.println("VARIABLE " + vari);
			//Verifica se o actionId do evento é o mesmo da thread.
				if(vari.equals(comp)&& this.controlecanal == 0) {
					this.canal = ((VarSetEvent)event).getChannel();					
					this.uniqueId  = ((VarSetEvent)event).getUniqueId();					
					System.out.println("SÃO IGUAIS, DEU CERTO");
					this.controlecanal++;
					
			}			
			}
			
		}
		    
		//Evento gerado qndo o asterisk grava o CDR.
		else if (event instanceof CdrEvent) {			
			try {
				//Verifica se o uniqueid da thread é igual ao uniqueid do canal que gerou este CDR.
				if(this.uniqueId.equals(((CdrEvent)event).getUniqueId()) && this.controleCdr == 0) {
					System.out.println("BILLSEC: " + ((CdrEvent)event).getBillableSeconds() + " NUM " + this.callNum);
				//Pega a duração da ligação.
				this.billSec = ((CdrEvent)event).getBillableSeconds();
				this.controleCdr++;
				//Atualiza Sql com o resultado da ligação
				try {
					this.num.atualizaLigacoes(this.uniqueId, this.callNum, this.idCampanha, this.vezLigacao, this.counterID, this.numPai, this.hangUpCause, this.billSec);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
			} catch(NullPointerException e) {
				System.out.println("SAIA DAQUI");
			}
			
			
		}
		}
	
	
	
	//Método para segurar a thread até a ligação acabar.
	public void esperaAtendeu(ChannelState st,int reasonInt) {			
			 while(CriadorCampanhas.asteriskServer.getChannelByName(this.canal).getState().equals(st)) {				
			 }
						
			 this.hangUpCause = converterReasonToString(reasonInt);
			 this.controleResponse++;
			 
		}
	
	//Converte os motivos de hangup para ficarem human readable, e serem inseridos no sql.
	public String converterReasonToString(int reason) {
		if(reason ==0 ) {
			return "Numero malformado";
		}		
		if(reason ==1) {
			return "Other end has Hungup";
		}
		if(reason ==3) {
			return "Timeout, não atendeu.";
		}
		if(reason ==4) {
			return "Normal Clearing";
		}
		if(reason ==5) {
			return "Busy";
		}
		if(reason ==7) {
			return "Off Hook";
		}
		if(reason ==8) {
			return "Recusou ligação";
		}
		
		return "sei la";
	}	
	}

