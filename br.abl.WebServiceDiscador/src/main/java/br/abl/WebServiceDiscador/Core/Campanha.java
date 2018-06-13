package br.abl.WebServiceDiscador.Core;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Classe do objeto Campanha, com todos os atributos necessários do objeto.
//Possui todos seus getters e setters.

public class Campanha {
	private String nome;
	private int id;
	private int numCanais;
	private Date dataInicia;
	private Date dataEncerra;	
	private String horaInicio;
	private String horaFim;
	private String tronco;
	private String contexto;
	private int horaTent;
	private int minTent;
	private int numTent;
	private String status = "Pausado";	
	private List<Long> numeros = new ArrayList<Long>();
	
	
	public Campanha (String nome, int id,int numThreads, Date dataInicia, Date dataEncerra, 
					 String horaInicio, String horaFim, String tronco, String contexto, 
					 int horaTent, int minTent, int numTent) {
		
		this.nome = nome;
		this.id = id;
		this.numCanais = numThreads;
		this.dataInicia = dataInicia;
		this.dataEncerra = dataEncerra;
		this.horaInicio = horaInicio;
		this.horaFim = horaFim;
		this.tronco = tronco;
		this.contexto = contexto;
		this.horaTent = horaTent;
		this.minTent = minTent;
		this.numTent = numTent;		
	}
	
	public String getContexto() {
		return contexto;
	}
	
	public void addNums(long numero) {	
			this.numeros.add(numero);		
	}
	
	public int getNumCanais() {
		return numCanais;
	}

	public void setNumCanais(int numCanais) {
		this.numCanais = numCanais;
	}

	public int getHoraTent() {
		return horaTent;
	}

	public void setHoraTent(int horaTent) {
		this.horaTent = horaTent;
	}

	public int getMinTent() {
		return minTent;
	}

	public void setMinTent(int minTent) {
		this.minTent = minTent;
	}

	public int getNumTent() {
		return numTent;
	}

	public void setNumTent(int numTent) {
		this.numTent = numTent;
	}

	public List<Long> getNumeros() {
		return numeros;
	}

	public void setNumeros(List<Long> numeros) {
		this.numeros = numeros;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setTronco(String tronco) {
		this.tronco = tronco;
	}

	public String getTronco() {
		return this.tronco;
	}

	public void setContexto(String contexto) {
		this.contexto = contexto;
	}

	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setNumThreads(int numThreads) {
		this.numCanais = numThreads;
	}

	public Campanha() {
		
	}
	
	public int getId() {
		return id;
	}
	
	public String getNome() {
		return this.nome;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDataEncerra() {
		return dataEncerra;
	}

	public void setDataEncerra(Date dataEncerra) {
		this.dataEncerra = dataEncerra;
	}

	public Date getDataInicia() {
		return dataInicia;
	}

	public void setDataInicia(Date dataInicia) {
		this.dataInicia = dataInicia;
	}

	public String getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}

	public String getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(String horaFim) {
		this.horaFim = horaFim;
	}
	
	public int getNumThreads() {
		return this.numCanais;
	}
	
	
	
	
	
}

