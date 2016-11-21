package io.oxigen.quiosgrama.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	
	  private List<String> listaCompleta;
	  private List<String> resultados;
	  private Filter meuFiltro;

	  public AutoCompleteAdapter(Context ctx, int layout,List<String> textos) { 

	    super(ctx, layout, textos);
	    this.listaCompleta = textos;
	    this.resultados = listaCompleta;
	    this.meuFiltro = new MeuFiltro();
	  }

	  @Override
	  public int getCount() {
	    return resultados.size();
	  }

	  @Override
	  public String getItem(int position) {
		  
		  if (resultados != null && resultados.size() > 0 && position < resultados.size()){        
			  return resultados.get(position);      
		  } else {        
			  return null;     
		  }   
		  
	  }     

	  @Override   
	  public Filter getFilter() {
		  return meuFiltro;   
	  }    

	  private class MeuFiltro extends Filter {
		  private final String[] REPLACES = { "a", "e", "i", "o", "u", "c" };
		
		  private Pattern[] PATTERNS = null;
		  
		  
		  @Override
		  protected FilterResults performFiltering(CharSequence constraint) {
			
			  FilterResults filterResults = new FilterResults();
			
			  ArrayList<String> temp = new ArrayList<String>();
			      
			  if (constraint != null) {
				  
				  String term = removeAcentos(
				  constraint.toString().trim().toLowerCase());
				    
				  String placeStr;
				  for (String p : listaCompleta) {
					  placeStr = removeAcentos(p.toLowerCase());
				        
					  if ( placeStr.indexOf(term) > -1){
						  temp.add(p);
					  }
				  }
				  
			  }
			  
			  filterResults.values = temp;
			  filterResults.count = temp.size();
			  return filterResults;
		  }
		
		  private String removeAcentos(String lowerCase) {
		 
			 if (PATTERNS == null) {
			   compilePatterns();
			 }
		
			 String result = lowerCase;
			 for (int i = 0; i < PATTERNS.length; i++) {     
			   Matcher matcher = PATTERNS[i].matcher(result);     
			   result = matcher.replaceAll(REPLACES[i]);   
			 }   
			 
			 return result.toUpperCase(); 
		  }
		
		  public void compilePatterns() {
			 PATTERNS = new Pattern[REPLACES.length];
			  PATTERNS[0] = Pattern.compile("[âãáàä]", Pattern.CASE_INSENSITIVE);
			  PATTERNS[1] = Pattern.compile("[éèêë]", Pattern.CASE_INSENSITIVE);
			  PATTERNS[2] = Pattern.compile("[íìîï]", Pattern.CASE_INSENSITIVE);
			  PATTERNS[3] = Pattern.compile("[óòôõö]", Pattern.CASE_INSENSITIVE);
			  PATTERNS[4] = Pattern.compile("[úùûü]", Pattern.CASE_INSENSITIVE);
			  PATTERNS[5] = Pattern.compile("[ç]", Pattern.CASE_INSENSITIVE);
		  }
		  
		  @SuppressWarnings("unchecked")
		  @Override
		  protected void publishResults(CharSequence contraint, FilterResults filterResults) {

			resultados = (ArrayList<String>) 
					filterResults.values;

			notifyDataSetChanged();
		  }
		
	  }

}
