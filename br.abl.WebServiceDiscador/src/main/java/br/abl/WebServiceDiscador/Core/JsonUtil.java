package br.abl.WebServiceDiscador.Core;
import static spark.Spark.*;

import java.awt.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;

import spark.ResponseTransformer;

public class JsonUtil {
	
	//CLASSE PARA FAZER CONVERSÕES PARA JSON UTILIZANDO A LIB GSON
	public static String toJson(Object object) {
		return new Gson().toJson(object);
	}
	
	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}
	
	public static <Campanha extends Object> Campanha fromJson(String json, Class<Campanha> classe) {
		return new Gson().fromJson(json, classe);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,String> parse(String object){
		return new Gson().fromJson(object, Map.class);
	}
	
	public static Map<String,ArrayList<String>> mapParse(String object){
		return new Gson().fromJson(object, Map.class);
	}
	
	public static ArrayList<String> parser(String object){
		return new Gson().fromJson(object, ArrayList.class);
	}
}
