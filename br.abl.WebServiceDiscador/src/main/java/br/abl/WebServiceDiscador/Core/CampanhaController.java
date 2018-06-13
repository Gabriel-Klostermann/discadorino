package br.abl.WebServiceDiscador.Core;
import static spark.Spark.*;
import static br.abl.WebServiceDiscador.Core.JsonUtil.*;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;




public class CampanhaController {

	public CampanhaController(final CriadorCampanhas man) {
		get("/campanhas", (req,res) -> man.getCampanhas(), json()); //Retorna todas as campanhas criadas em formato JSON.
			
		after((req,res) -> {
			res.type("application/json");			
	
		});
		
		get("/campanhas/:id", (req,res)  -> {    //Retorna a campanha especificada pelo ID passado na URI.
			String id = req.params(":id");
			Campanha camp = man.getCampanha(id);
			return camp;
		},json());
		after((req,res) -> {
			res.type("application/json");		
		});
		


		
		delete("/campanhas/deletar/:id", (req,res) -> {  //Deleta a campanha especificada pelo ID passado na URI.
			String id = req.params(":id");
			man.deletarCampanha(id);		
			return "{\"mensagem\":\"Campanha deletada com sucesso !\"}";
		}); 		

		
		//Rota para criação de campanhas. Payload é armazenada num Map de Strings, e seus dados são
		// passados como parâmetro para a criação de um objeto campanha.
		post("/campanhas/novaCampanha", (req,res) -> {  
			String result;
			Map<String,String> dados = JsonUtil.parse(req.body());			
			result = man.criaCampanha(dados.get("Nome"), dados.get("maxLigacoes"),dados.get("dataInicia"), dados.get("dataEncerra"), dados.get("horaInicia"), dados.get("horaFim"), dados.get("tronco"), dados.get("contexto"),dados.get("tempoTentativas"),dados.get("numTentativas"));	  
			return result;
		});
		
		//Pausa a campanha especificada pelo id na URI. Campanhas pausadas não fazem ligações.
		put("/campanhas/pausar/:id", (req,res) -> {
			String id = req.params(":id");			
			String result = man.pausaCampanha(id);
			return result;
		});
		
		//Despausa a campanha especificada pelo id na URI.
		put("/campanhas/play/:id", (req,res) -> {
			String id = req.params(":id");
			String result = man.playCampanha(id);
			return result;
		});
				
		//Adiciona números a determinada campanha especificada no corpo do payload.\
		//Gson gson = new Gson();
		post("/campanhas/addNumeros", (req,res) -> {
			SqlDAO sql = new SqlDAO();
			String jString;
			int idCampanha = 0 ,idCliente = 0,x= 0;
			String payload = req.body();
			JSONObject jsonObject = new JSONObject(payload); //Recebe o payload como um Json Object.
			Object id = jsonObject.get("IDCAMPANHA");		//Pega o id da campanha.
			idCampanha = Integer.parseInt(id.toString());		
			JSONArray arr = jsonObject.getJSONArray("Telefones");  //Recebe o array telefones como um Json Array.
			JSONObject jsonObj;
			
			while(x < arr.length()) {					//Percorre o array de telefones, inserindo os numeros no sql.
				jsonObj = arr.getJSONObject(x);											
				jString = jsonObj.getString("Numero");				
				idCliente = jsonObj.getInt("id");
				String[] splitter  = jString.split(",");				
				sql.insereNumeros(splitter, idCampanha, idCliente);
				x++;				
			}
			
			CriadorCampanhas.campanhas.get(idCampanha -1).setStatus("Executando");  //Muda o status da campanha para executando.
						
			/*
			int idCampanha = 0, idCliente = 0;
			String payload = req.body();
			NumerosCampanha numerosCampanha = gson.fromJson(payload, NumerosCampanha.class);
			idCampanha = Integer.parseInt(numerosCampanha.getIdCampanha());
			for (Numeros num : numerosCampanha.getTelefones()) {
				idCliente = Integer.parseInt(num.getNumeros().
			}
			idCliente = Integer.parseInt(numerosCampanha.getIdCliente());
			sql.insereNumeros(numerosCampanha.getTelefones(), idCampanha, idCliente);
			CriadorCampanhas.campanhas.get(idCampanha -1).setStatus("Executando");  //Muda o status da campanha para executando
			*/
			

			
			return "Deu boa";
			
		});	
		
		
		/*	post("/campanhas/addNumeros", (req,res) -> {
		SqlDAO sql = new SqlDAO();
		int idCampanha = 0 , idCliente = 0;
		Set<String> keys;
		String jString ="";			
		String payload = req.body();
		JSONObject jsonObject = new JSONObject(payload);
		
		Object id = jsonObject.get("IDCAMPANHA");
		idCampanha = Integer.parseInt(id.toString());
		JSONObject getNum = jsonObject.getJSONObject("Telefones");
		
		keys = getNum.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()) {
			idCliente = Integer.parseInt(iter.next().toString());
			System.out.println(idCliente);
			 jString = getNum.getString(""+idCliente);
			System.out.println(jString);
			String[] splitter = jString.split(",");
			sql.insereNumeros(splitter, idCampanha, idCliente);
			
		}
		return "Deu boa!";
	}); */
	
}
}