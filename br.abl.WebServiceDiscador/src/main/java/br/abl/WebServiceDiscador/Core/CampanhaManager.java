package br.abl.WebServiceDiscador.Core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CampanhaManager implements Runnable {

	private Campanha campanha;
	private int vezCounter = 0;
	private int idCounter;
	public static List<Integer> ids = new ArrayList<Integer>();
	static int begin = 0;

	// Recebe a campanha pelo construtor.
	public CampanhaManager(Campanha campanha, int idCounter) {
		this.campanha = campanha;
		this.idCounter = idCounter;
	}

	public CampanhaManager() {

	}

	// Thread gerada para gerenciar a campanha executa aqui.
	public void run() {
		String status = "Executando";
		Calendar cal = Calendar.getInstance();
		Date dataAgora = null;
		String dataNow;
		boolean intervalo;
		List<Long> millis = new ArrayList<Long>();
		long horas = 0;

		// Acerta o formato de data para o brasileiro.
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
		dataNow = format.format(cal.getTime());

		// Parseia a data para o formato especificado
		try {
			dataAgora = format.parse(dataNow);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Chama o método intervalo que verifica se a data de agora está dentro do
		// intervalo especificado
		// de início e fim desta campanha.
		intervalo = comparaDatas(dataAgora, this.campanha.getDataInicia(), this.campanha.getDataEncerra(),
				this.campanha.getHoraInicio(), this.campanha.getHoraFim());

		while (true) {

			// Pega o status da campanha, verificando se está pausada ou não.
			status = CriadorCampanhas.campanhas.get(this.campanha.getId() - 1).getStatus();
			intervalo = comparaDatas(dataAgora, this.campanha.getDataInicia(), this.campanha.getDataEncerra(),
					this.campanha.getHoraInicio(), this.campanha.getHoraFim());

			// Se está dentro do intervalo, podemos começar a fazer as ligações.
			if (intervalo == true) {
				// A condição do while verifica se o número de threads ativas é maior ou igual a
				// 0 e menor ou igual ao número
				// máximo de canais especificados na criação de campanha, bem como se está no
				// interval de tempo correto e se
				// seu status é executando.
				while (ids.get(this.idCounter) >= 0 && ids.get(this.idCounter) < this.campanha.getNumThreads()
						&& intervalo == true && status.equals("Executando")) {

					// Cria uma thread para fazer ligações passando seu id de campanha, tronco a ser
					// utilizado e o contexto
					// do dialplan.
					Thread thread = new Thread(new CriadorCanais(this.campanha.getId(), this.idCounter,
							this.campanha.getTronco(), this.campanha.getContexto()));
					thread.start();
					ids.set(this.idCounter, ids.get(this.idCounter) + 1);
					status = CriadorCampanhas.campanhas.get(this.campanha.getId() - 1).getStatus();
					// Se a campanha for pausada, sai do loop.
					if (status.equals("Pausado")) {
						break;
					}

					intervalo = comparaDatas(dataAgora, this.campanha.getDataInicia(), this.campanha.getDataEncerra(),
							this.campanha.getHoraInicio(), this.campanha.getHoraFim());
					this.vezCounter += 1;
					// Este if força um yield na thread que está executando para que outras
					// campanhas caso existam sejam
					// garantidas de não ficarem muito tempo sem executar.
					if (this.vezCounter == 1) {
						try {
							NoOp(this.campanha.getNome());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.vezCounter = 0;
				}
			}

			// Se a data atual estiver fora do intervalo da campanha, esta campanha irá
			// dormir até que chegue o horário inicial.
			if (intervalo == false) {
				System.out.println("Horário fora do intervalo da campanha! " + this.campanha.getNome());
				millis = getSleepTime(this.campanha.getHoraInicio());
				horas = millis.get(0) / (60 * 60 * 1000);
				System.out.println(" A campanha :" + this.campanha.getNome() + " Vai dormir por :" + horas + " horas e "
						+ millis.get(1) + "minutos !");
				try {
					Thread.currentThread().sleep(millis.get(0));
					CriadorCampanhas.campanhas.get(this.campanha.getId() - 1).setStatus("Executando");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	// Método que faz o cálculo para determinar quanto tempo a campanha deve dormir
	// caso esteja fora do intervalo.
	public static List<Long> getSleepTime(String horaInicio) {
		Calendar hoje = Calendar.getInstance();
		Calendar proxData = Calendar.getInstance();
		List<Long> valores = new ArrayList<Long>();
		int diaDeHoje = hoje.get(Calendar.DAY_OF_YEAR);
		int horaDeHoje = hoje.get(Calendar.HOUR_OF_DAY);
		String[] partes = horaInicio.split(":");
		String proxHora = partes[0];
		int proxHoraComparator = Integer.parseInt(proxHora);
		String proxMinuto = partes[1];
		int proximaHora = Integer.valueOf(proxHora);
		int proximoMinuto = Integer.valueOf(proxMinuto);
		int minutoAgora = hoje.get(Calendar.MINUTE);
		long minutoCalculado = 0;
		int proximoDia = 0;
		// Aqui são as lógicas para fazer os cálculos. Se a hora de agora for maior que
		// a próxima hora do intervalo da campanha
		// significa que a campanha só volta no dia seguinte. Portanto proximoDia é
		// incrementado. O minuto é cálculado em seguida
		// Exemplo: Se são 17:10, e o próximo horário começa as 8:00, o programa faz
		// 60-10 = 50 + 0 = 50. Portanto serão 14 horas
		// e 50 minutos dormindo para esta campanha.

		if (horaDeHoje > proxHoraComparator) {
			proximoDia = diaDeHoje + 1;
			minutoCalculado += (60 - minutoAgora) + proximoMinuto;
		}

		else if (horaDeHoje < proxHoraComparator) {
			proximoDia = diaDeHoje;
			minutoCalculado += (60 - minutoAgora) + proximoMinuto;
		} else if (horaDeHoje == proxHoraComparator) {
			proximoDia = diaDeHoje;
			minutoCalculado += proximoMinuto - minutoAgora;
		}

		// Seta a próxima data com os valores recebidos.
		proxData.set(Calendar.YEAR, 2018);
		proxData.set(Calendar.MONTH, 04);
		proxData.set(Calendar.DAY_OF_YEAR, proximoDia);
		proxData.set(Calendar.HOUR_OF_DAY, proximaHora);
		proxData.set(Calendar.MINUTE, proximoMinuto);

		// Converte as datas atual e proxima em milisegundos e faz a diferença entre
		// elas para determinar por quanto tempo
		// a campanha deve dormir e retorna estes valores.
		long millisAteProxDia = (proxData.getTimeInMillis() - hoje.getTimeInMillis());

		valores.add(millisAteProxDia);
		valores.add(minutoCalculado);
		return valores;
	}

	public boolean comparaDatas(Date dataAgora, Date dataInicia, Date dataEncerra, String horaInicio, String horaFim) {
		Calendar cal = Calendar.getInstance();
		String horaString, minutoString, tuto = "";
		int hora;
		int minuto;
		// Verifica se a data de agora está depois da data inicial da campanha e antes
		// da data de encerramento.
		// Ou se a data atual é igual à data de início
		if (dataAgora.after(dataInicia) && dataAgora.before(dataEncerra) || dataAgora.equals(dataInicia)) {
			hora = cal.get(Calendar.HOUR_OF_DAY);
			minuto = cal.get(Calendar.MINUTE);
			horaString = Integer.toString(hora);
			minutoString = Integer.toString(minuto);
			// Aqui só verificação do valor do minuto para montar a string de tempo no
			// formato hh:mm
			if (minuto > 9) {
				tuto = horaString + ":" + minutoString;
			}
			if (minuto <= 9 && hora <= 9) {
				tuto = "0" + horaString + ":0" + minutoString;
			}
			if (minuto > 9 && hora <= 9) {
				tuto = "0" + horaString + ":" + minutoString;
			}
			if (minuto <= 9 && hora > 9) {
				tuto = horaString + ":0" + minutoString;
			}

			if (minuto > 9 && hora > 9) {
				tuto = horaString + ":" + minutoString;
			}

			if (tuto.compareTo(horaInicio) >= 0 && tuto.compareTo(horaFim) <= 0 || tuto.equals(horaInicio)) {
				return true;
			}
		}
		// Se a data for fora do horário da campanha, muda seu status para dormindo.
		CriadorCampanhas.campanhas.get(this.campanha.getId() - 1).setStatus("Dormindo");
		return false;

	}

	// Método para fazer a thread liberar o processador.
	public static synchronized void NoOp(String nome) throws InterruptedException {
		Thread.currentThread();
		Thread.sleep(1000);
		Thread.currentThread().yield();
	}

}
